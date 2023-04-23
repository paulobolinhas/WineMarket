package entities;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class BlockChain {

	private List<Block> blockchain;
	private String prefix = "src/blockchain/block_";
	private long currentID;
	private static BlockChain INSTANCE;
	
	private BlockChain() {
		this.blockchain = new ArrayList<>();
	}
	
	public static BlockChain getInstance() {
		if (INSTANCE == null)
			return INSTANCE = new BlockChain();
		
		return INSTANCE;
	}
	
	public Block createBlock() throws IOException {
		Block newBlock = new Block(this.currentID);
		this.blockchain.add(newBlock);
		
		String path = "src/blockchain/block_"+newBlock.getId()+".blk";
		File newBlkFile = new File(path);
		boolean sucess = newBlkFile.createNewFile();
		if (sucess) 
			System.out.println("Ficheiro "+path+" criado com sucesso");
		else
			System.out.println("Erro ao criar ficheiro "+path);
		
		String content = newBlock.getHeaderString();
		
		
		
		
		
		return newBlock;
	}
	
	
}
