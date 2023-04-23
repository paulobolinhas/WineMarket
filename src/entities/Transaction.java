package entities;

import enums.TransactionType;

public class Transaction {
	
	private TransactionType type;
	private int wineID;
	private int unitsNum;
	private int unitPrice;
	private int transactionOwner; //se é um venda é o id do vendedor. se é uma compra é o id do comprador
	//adicionar assinatura. se for venda o vendedor assina, se for compra, o comprador assina
	
	public Transaction(TransactionType type, int wineID, int unitsNum, int unitPrice, int transactionOwner) {
		this.type = type;
		this.wineID = wineID;
		this.unitsNum = unitsNum;
		this.unitPrice = unitPrice;
		this.transactionOwner = transactionOwner;
	}

	public int getUnitsNum() {
		return unitsNum;
	}

	public int getUnitPrice() {
		return unitPrice;
	}

	public int getWineID() {
		return wineID;
	}

	public int getTransactionOwner() {
		return transactionOwner;
	}
	
	public String toString() {
		return "Type";
	}
	
}
