package entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import enums.TransactionType;

public class Block {

	private byte[] previousHash;
	private long block_id;
	private long n_trx;
	private List<Transaction> transactions;
	private byte[] serverSignature;

	// assinatura aqui;

	public Block(long block_id, byte[] previousHash) {
		this.block_id = block_id;
		this.n_trx = 0;
		this.transactions = new ArrayList<>();
		this.previousHash = previousHash;
	}

	public Block(long block_id) {
		this.block_id = block_id;
		this.n_trx = 0;
		this.transactions = new ArrayList<>();
		this.previousHash = new byte[8];
	}

	public Block(long blockID, byte[] previousHash2, int nTrx, List<Transaction> transactions2) {
		this.block_id = blockID;
		this.previousHash = previousHash2;
		this.n_trx = nTrx;
		this.transactions = transactions2;
	}

	public Transaction createTransaction(long transactionID, TransactionType type, String wineID, int unitsNum,
			int unitPrice, String transactionOwner) {
		return new Transaction(transactionID, type, wineID, unitsNum, unitPrice, transactionOwner);
	}
	
	public byte[] getPreviousHash() {
		return this.previousHash;
	}

	public long getId() {
		return this.block_id;
	}

	public long getN_trx() {
		return this.n_trx;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public String getHeaderString() {
		return "hash: " + this.previousHash + "\nblock_id: " + this.block_id + "\nn_trx: " + this.n_trx;
	}

	public String getFstHeader() {
		return "hash: " + String.format("%032X", new BigInteger(1, this.previousHash)) + "\nblock_id: " + this.block_id + "\nn_trx: " + this.n_trx;
	}

	public void addTransaction(Transaction t) {
		this.transactions.add(t);
		this.n_trx++;
	}

	public byte[] getServerSignature() {
		return this.serverSignature;
	}

	public void setServerSignature(byte[] serverSignature) {
		this.serverSignature = serverSignature;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("hash: " + this.previousHash + "\n" + "block_id: " + this.block_id + "\n" + "n_trx: " + this.n_trx
				+ "\n--------");

		for (Transaction t : transactions) {
			sb.append(t.toString());
			sb.append("\n--------");
		}

		return sb.toString();
	}

}
