package entities;

import enums.TransactionType;

public class Transaction {
	
	private TransactionType type;
	private long transactionID;
	private String wineID;
	private int unitsNum;
	private int unitPrice;
	private String transactionOwner; //se é um venda é o id do vendedor. se é uma compra é o id do comprador
	//adicionar assinatura. se for venda o vendedor assina, se for compra, o comprador assina
	
	public Transaction(long transactionID, TransactionType type, String wineID, int unitsNum, int unitPrice, String transactionOwner) {
		this.transactionID = transactionID;
		this.type = type;
		this.wineID = wineID;
		this.unitsNum = unitsNum;
		this.unitPrice = unitPrice;
		this.transactionOwner = transactionOwner;
	}

	public int getUnitsNum() {
		return this.unitsNum;
	}

	public int getUnitPrice() {
		return this.unitPrice;
	}

	public String getWineID() {
		return this.wineID;
	}

	public String getTransactionOwner() {
		return this.transactionOwner;
	}
	
	public String toString() {
		return "\ntransaction_id: " + this.transactionID +
				"\ntype: " + this.type.toString() +
				"\nwine: " + this.wineID +
				"\nnumber of units: " + this.unitsNum +
				"\nprice per unit: " + this.unitPrice +
				"\nowner: " + this.transactionOwner;
	}
	
}
