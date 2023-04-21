package catalogs;

import java.util.ArrayList;

import domain.Sell;

public class SellsCatalog {

	private static SellsCatalog instance;
	private ArrayList<Sell> sellsCatalog;

	private SellsCatalog() {
		sellsCatalog = new ArrayList<Sell>();
	}

	public static SellsCatalog getSellsCatalog() {
		if (instance == null)
			instance = new SellsCatalog();

		return instance;
	}

	public synchronized void add(Sell sell) {
		sellsCatalog.add(sell);
	}

	public synchronized int size() {
		return sellsCatalog.size();
	}

	public synchronized Sell getSale(String wineID, String sellerID) {
		for (Sell s : sellsCatalog) {
			if (s.getWineId().equals(wineID) && s.getSeller().equals(sellerID))
				return s;
		}

		return null;
	}

	public synchronized ArrayList<Sell> getSalesByWineID(String wineID) {
		ArrayList<Sell> sales = new ArrayList<Sell>();
		for (Sell s : sellsCatalog) {
			if (s.getWineId().equals(wineID))
				sales.add(s);
		}

		return sales;
	}

	public synchronized int getSize() {
		return sellsCatalog.size();
	}

	public synchronized Boolean existsSale(String wineID, String sellerID) {
		return getSale(wineID, sellerID) != null;
	}
}
