package catalogs;

import domain.Wine;
import java.util.ArrayList;

public class WineCatalog {
	
	private static WineCatalog instance;
	private ArrayList<Wine> winesCatalog;
	
	private WineCatalog() {
		winesCatalog = new ArrayList<Wine>();
	}
	
	public static WineCatalog getWineCatalog() {
		if (instance == null)
            instance = new WineCatalog();
		
        return instance;
	}
	
	public synchronized Wine getWineByID(String id) {
		
		for (Wine w: winesCatalog)
			if (w.getID().equals(id))
				return w;
		
		return null;	
	}
	
	public synchronized void add(Wine wine) {
		winesCatalog.add(wine);
	}

	public synchronized int getSize() {
		return winesCatalog.size();
	}
	
	public synchronized Boolean exists(String wineID) {
		return instance.getWineByID(wineID) != null;
	}
}
