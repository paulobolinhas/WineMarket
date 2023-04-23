package entities;

import java.util.List;

import enums.TransactionType;

import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BlockChain {

	private List<Block> blockchain;
	private Block currentBlock;
	private String prefixPath = "./src/blockchain/block_";
	private String sufixPath = ".blk";
	private long currentBlockID;
	private long currentTransactionID;
	private static BlockChain INSTANCE;
	//adicionar assinatura do servidor
	
	private BlockChain() {
		this.blockchain = new LinkedList<>();
		
		//posteriormente estes numeros serao inicializados com os valores obtidos na verificacao da blockchain
		this.currentBlockID = 1;
		this.currentTransactionID = 1;
	}
	
	public static BlockChain getInstance() {
		if (INSTANCE == null)
			return INSTANCE = new BlockChain();
		
		return INSTANCE;
	}
	
	public synchronized Block createBlock() throws IOException {
		Block newBlock = new Block(this.currentBlockID);
		this.currentBlock = newBlock;
		
		String content = "";
		
		if (this.currentBlockID == 1) {
			content = newBlock.getFstHeader();
		} else
			newBlock.getHeaderString();

		this.currentBlockID++;
		this.blockchain.add(newBlock);
		
		File newBlkFile = new File(this.getCurrentPath());
		if (!newBlkFile.exists()) {
			if (newBlkFile.createNewFile()) 
				System.out.println("Ficheiro "+this.getCurrentPath()+" criado com sucesso");
			else
				System.out.println("Erro ao criar ficheiro "+this.getCurrentPath());
		}
		
		FileWriter fw = new FileWriter(newBlkFile);
		fw.write(content);
		fw.close();
		
		
		return newBlock;
	}
	
	public synchronized Transaction createTransaction(TransactionType type, String wineID, int unitsNum, int unitPrice, String transactionOwner) throws IOException {
		
		if (this.currentBlock.getN_trx() == 5) {
			//adicionar metodo para assinar o bloco.
			//retornar hash com assinatura
			try {
				//De seguida criar outro bloco. Como current block é atualizado, o codigo a seguir escreve
				//automaticamente no proximo ficheiro.
				//passar o hash com a assinatura no metodo para colocar no inicio do proximo bloco
				this.createBlock();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}	
		
		Transaction t = this.currentBlock.createTransaction(currentTransactionID, type, wineID, unitsNum, unitPrice, transactionOwner);
		
		FileWriter blockFile = new FileWriter(this.getCurrentPath(), true);

		blockFile.write(t.toString());
		blockFile.close();
		
		return t;
		
	}
	
	private String getCurrentPath() {
		return this.prefixPath + this.currentBlock.getId() + this.sufixPath;
	}

	
	
}
