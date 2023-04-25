package entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import enums.TransactionType;

public class BlockChain {

	private List<Block> blockchain;
	private Block currentBlock;
	private String prefixPath = "./src/blockchain/block_";
	private String sufixPath = ".blk";
	private long nextBlockID;
	private long nextTransactionID;
	private static BlockChain INSTANCE;
	private PrivateKey serverPK;
	// adicionar assinatura do servidor

	private BlockChain(PrivateKey serverPK) {
		this.blockchain = new LinkedList<>();

		// posteriormente estes numeros serao inicializados com os valores obtidos na
		// verificacao da blockchain
		this.nextBlockID = 1;
		this.nextTransactionID = 1;
		this.serverPK = serverPK;
	}

	public static BlockChain getInstance(PrivateKey serverPK) {
		if (INSTANCE == null)
			return INSTANCE = new BlockChain(serverPK);

		return INSTANCE;
	}

	public synchronized Block createBlock(byte[] previousHash) throws IOException {
		Block newBlock = null;
		String content = "";

		if (this.nextBlockID == 1) {
			newBlock = new Block(this.nextBlockID);
			content = newBlock.getFstHeader();
		} else {
			newBlock = new Block(this.nextBlockID, previousHash);
			content = newBlock.getHeaderString();
		}

		this.currentBlock = newBlock;
		this.blockchain.add(this.currentBlock);
		this.nextBlockID++;

		File newBlkFile = new File(this.getCurrentPath());
		newBlkFile.createNewFile();

		FileWriter fw = new FileWriter(newBlkFile);
		fw.write(content);
		fw.flush();
		fw.close();

		return newBlock;
	}

	public synchronized Transaction createTransaction(TransactionType type, String wineID, int unitsNum, int unitPrice,
			String transactionOwner) throws IOException {
		return this.currentBlock.createTransaction(nextTransactionID, type, wineID, unitsNum, unitPrice,
				transactionOwner);
	}

	public synchronized void addTransaction(Transaction t)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {

		int transactionsPerBlock = 2;

		if (this.currentBlock.getN_trx() == transactionsPerBlock) {

			Signature s = Signature.getInstance("SHA256withRSA");
			s.initSign(this.serverPK);

			String content = new String(Files.readAllBytes(Paths.get(this.getCurrentPath())));

			s.update(content.getBytes());
			byte[] signedContent = s.sign();
			
			this.currentBlock.setServerSignature(signedContent);
			
			FileWriter blockFile = new FileWriter(this.getCurrentPath(), true);
			blockFile.write("\n--------\nServer Signature: " + signedContent);
			blockFile.flush();

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] previousHash = digest.digest(signedContent);
				this.createBlock(previousHash);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String newContent = this.getNewContent(t, transactionsPerBlock);
		FileWriter blockFile = new FileWriter(this.getCurrentPath());
		blockFile.write(newContent);
		blockFile.close();

		this.currentBlock.addTransaction(t);
		this.nextTransactionID++;
	}

	private String getNewContent(Transaction t, int transactionsPerBlock) throws IOException {

		String oldContent = "";
		String lineToReplace = "n_trx: " + this.currentBlock.getN_trx();
		String newLine = "n_trx: " + (this.currentBlock.getN_trx() + 1L);

		BufferedReader reader = new BufferedReader(new FileReader(this.getCurrentPath()));

		String line = reader.readLine();

		while (line != null) {

			oldContent += line + System.lineSeparator();
			line = reader.readLine();
		}

		String newContent = oldContent.replace(lineToReplace, newLine);

		return newContent += "--------" + t.toString();

	}

	private String getCurrentPath() {
		return this.prefixPath + this.currentBlock.getId() + this.sufixPath;
	}

	/*
	 * percorrer os ficheiros usando os numeros, at� nao encontrar mais ficheiros
	 * 
	 * em cada ficheiro extrair a informacao e as transacoes e o bloco.
	 * 
	 * depois da blockchain ter sido carregada para a memoria, verificar as
	 * assinaturas com o hash do bloco seguinte. se nao bater certo, fechar o
	 * servidor
	 * 
	 */
	public void initializeBlockChain() throws IOException {
		Boolean firstTime = true;
		// Loop through all blockchain files
		while (true) {
			String filePath = this.prefixPath + this.nextBlockID + this.sufixPath;
			File file = new File(filePath);

			if (!file.exists() && firstTime) {
				createBlock(null);
				break;
			}

			if (!file.exists()) {
				break;
			}

			String content = new String(Files.readAllBytes(Paths.get(filePath)));
			String[] lines = content.split("\n");

			firstTime = false;
			// Pars
			byte[] previousHash = parseHash(lines[0]); //verificar este hash com a ultima assinatura
			long blockID = Long.parseLong(lines[1].split(": ")[1].replaceAll("\\r", ""));
			int nTrx = Integer.parseInt(lines[2].split(": ")[1].replaceAll("\\r", ""));

			// Parse transactions
			List<Transaction> transactions = new ArrayList<>();
			for (int i = 4; i < lines.length - 2; i += 8) {
				long trxID = Long.parseLong(lines[i].split(": ")[1].replaceAll("\\r", ""));

				TransactionType trxType = TransactionType
						.valueOf(lines[i + 1].split(": ")[1].replaceAll("\\r", "").toUpperCase());

				String wineID = lines[i + 2].split(": ")[1].replaceAll("\\r", "");

				int unitsNum = Integer.parseInt(lines[i + 3].split(": ")[1].replaceAll("\\r", ""));

				int unitPrice = Integer.parseInt(lines[i + 4].split(": ")[1].replaceAll("\\r", ""));

				String owner = lines[i + 5].split(": ")[1].replaceAll("\\r", "");

				byte[] signedContent = parseSignature(lines[i + 6].split(": ")[1]);

				Transaction transactionAux = new Transaction(trxID, trxType, wineID, unitsNum, unitPrice, owner);
				transactionAux.setSignature(signedContent);
				transactions.add(transactionAux);
				this.nextTransactionID++;
			}


			// Create and add the block to the blockchain
			Block block = new Block(blockID, previousHash, nTrx, transactions);

			// Parse server signature
			if (lines[lines.length - 1].contains("Server Signature")) {
				byte[] serverSignature = parseSignature(lines[lines.length - 1].split(": ")[1]);
				block.setServerSignature(serverSignature);

			}

			this.currentBlock = block;
			this.blockchain.add(this.currentBlock);
			this.nextBlockID++;

		}
	
	}

	private static byte[] parseHash(String hashLine) {
		String hashValue = hashLine.split(": ")[1];
		return hashValue.equals("null") ? null : hashValue.getBytes();
	}

	private static byte[] parseSignature(String signatureString) {
		return signatureString.getBytes();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Blockchain representation: \n\n");

		for (Block b : blockchain) {
			sb.append(b.toString());

			if (b.getServerSignature() != null)
				sb.append("\n--------------------------------\nServer Signature: ")
				.append(b.getServerSignature() + "\n--------------------------------\n\n");
		}

		return sb.toString();
	}

	public boolean verify() throws NoSuchAlgorithmException {
	
		for (int i = 1; i <= this.nextBlockID - 2; i++) {
			Block currentBlock = this.getBlockById((long) i);
			byte[] serverSignature = currentBlock.getServerSignature();
			
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			
			Block nextBlock = this.getBlockById((long) (i+1));
			byte[] previousHash = nextBlock.getPreviousHash();
			
			if (!MessageDigest.isEqual(digest.digest(serverSignature), previousHash))
				return false;
		}
		
		return true;
		
	}
	
	private Block getBlockById(long id) {
		for (Block b: this.blockchain) {
			if (b.getId() == id)
				return b;
		}
		
		return null;
	}
}
