package entities;

import java.util.List;

import enums.TransactionType;

import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public class BlockChain {

	private List<Block> blockchain;
	private Block currentBlock;
	private String prefixPath = "./src/blockchain/block_";
	private String sufixPath = ".blk";
	private long nextBlockID;
	private long currentTransactionID;
	private static BlockChain INSTANCE;
	private PrivateKey serverPK;
	//adicionar assinatura do servidor

	private BlockChain(PrivateKey serverPK) {
		this.blockchain = new LinkedList<>();

		//posteriormente estes numeros serao inicializados com os valores obtidos na verificacao da blockchain
		this.nextBlockID = 1;
		this.currentTransactionID = 1;
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

	public synchronized Transaction createTransaction(TransactionType type, String wineID, int unitsNum, int unitPrice, String transactionOwner) throws IOException {
		return this.currentBlock.createTransaction(currentTransactionID, type, wineID, unitsNum, unitPrice, transactionOwner);
	}

	public synchronized void addTransaction(Transaction t) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {

		int transactionsPerBlock = 1;

		if (this.currentBlock.getN_trx() == transactionsPerBlock) {
			//adicionar metodo para assinar o bloco.
			//retornar hash com assinatura

			Signature s = Signature.getInstance("SHA256withRSA");
			s.initSign(this.serverPK);
			
			String content = new String(Files.readAllBytes(Paths.get(this.getCurrentPath())));
			System.out.println("BLOCK CONTENT:\n"+content);
			
			s.update(content.getBytes());
			byte[] signedContent = s.sign();

			FileWriter blockFile = new FileWriter(this.getCurrentPath(), true);
			blockFile.write("\n--------\nServer Signature: " + signedContent);
			blockFile.flush();
			try {
				//De seguida criar outro bloco. Como current block é atualizado, o codigo a seguir escreve
				//automaticamente no proximo ficheiro.
				//passar o hash com a assinatura no metodo para colocar no inicio do proximo bloco
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] previousHash = digest.digest(signedContent);
				this.createBlock(previousHash);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}	

		//Aqui nao basta escrever, tem q se alterar o numero de transacoes entao tem q se substituir

		String newContent = this.getNewContent(t, transactionsPerBlock);
		FileWriter blockFile = new FileWriter(this.getCurrentPath());
		blockFile.write(newContent);
		blockFile.close();

		this.currentBlock.addTransaction(t);
		this.currentTransactionID++;
	}
	
	private String getNewContent(Transaction t, int transactionsPerBlock) throws IOException {
		
		String oldContent = "";
		String lineToReplace = "n_trx: "+this.currentBlock.getN_trx();
		String newLine = "n_trx: "+(this.currentBlock.getN_trx()+1L);
		
		BufferedReader reader = new BufferedReader(new FileReader(this.getCurrentPath()));

		String line = reader.readLine();

		while (line != null) {

			oldContent += line + System.lineSeparator();
			line = reader.readLine();
		}
		
		String newContent = oldContent.replace(lineToReplace, newLine);

		return newContent += "--------"+t.toString();

	}



	private String getCurrentPath() {
		return this.prefixPath + this.currentBlock.getId() + this.sufixPath;
	}
	

	/*
	 * percorrer os ficheiros usando os numeros, até nao encontrar mais ficheiros
	 * 
	 * em cada ficheiro extrair a informacao e as transacoes e o bloco.
	 * 
	 * depois da blockchain ter sido carregada para a memoria, verificar as assinaturas com o hash do bloco
	 * seguinte. se nao bater certo, fechar o servidor
	 * 
	 * */
	public void initializeBlockChain() {



	}



}
