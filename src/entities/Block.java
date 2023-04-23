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
	
	public Transaction createTransaction(TransactionType type, int wineID, int unitsNum, int unitPrice, int transactionOwner) {
		
		if (this.transactions.size() == 5)
			return null;
		
		Transaction res = new Transaction(type, wineID, unitsNum, unitPrice, transactionOwner);
		this.transactions.add(res);
		return res;
	}

	public long getId() {
		return this.block_id;
	}
	
	public String getHeaderString() {
		return "Hash: " + this.previousHash +
				"\n block_id: " + this.block_id +
				"\n n_trx: " + this.n_trx;
	}

}
