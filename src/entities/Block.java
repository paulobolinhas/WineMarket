package entities;

import java.util.List;

import enums.TransactionType;

import java.util.ArrayList;

public class Block {

	private byte[] previousHash;
	private long block_id;
	private long n_trx;
	private List<Transaction> transactions;
	//assinatura aqui;
	
	public Block(long block_id) {
		this.block_id = block_id;
		this.n_trx = 0;
		this.transactions = new ArrayList<>();
	}
	
	public Transaction createTransaction(long transactionID, TransactionType type, String wineID, int unitsNum, int unitPrice, String transactionOwner) {
		Transaction res = new Transaction(transactionID, type, wineID, unitsNum, unitPrice, transactionOwner);
		this.transactions.add(res);
		this.n_trx++;
		return res;
	}

	public long getId() {
		return this.block_id;
	}
	
	public long getN_trx() {
		return this.n_trx;
	}
	
	public String getHeaderString() {
		return "hash: " + this.previousHash +
				"\nblock_id: " + this.block_id +
				"\nn_trx: " + this.n_trx;
	}
	
	public String getFstHeader() {
		return "hash: 00000000" +
				"\nblock_id: " + this.block_id +
				"\nn_trx: " + this.n_trx;
	}

}
