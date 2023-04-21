package domain;

public class Sell {

	private String wineID;
	private String seller;
	private String image;
	private int value;
	private int quantity;

	public Sell(String wineID, String image, int value, int quantity, String seller) {
		this.wineID = wineID;
		this.image = image;
		this.seller = seller;
		this.quantity = quantity;
		this.value = value;
	}

	public String getSeller() {
		return seller;
	}

	public String getImage() {
		return image;
	}

	public String getWineId() {
		return wineID;
	}

	public int getValue() {
		return value;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int newQuantity) {
		this.quantity = newQuantity;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String toString() {
//		vinho1;vinho1.png;10;1;222
		return wineID + ";" + image + ";" + value + ";" + quantity + ";" + seller;
	}

}
