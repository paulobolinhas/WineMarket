package catalogs;

import java.util.ArrayList;

import domain.Mensagem;

public class MessageCatalog {
	private static MessageCatalog instance;
	private ArrayList<Mensagem> messageCatalog;

	private MessageCatalog() {
		messageCatalog = new ArrayList<Mensagem>();
	}

	public static MessageCatalog getMessageCatalog() {
		if (instance == null)
			instance = new MessageCatalog();

		return instance;
	}

	public synchronized void add(Mensagem mensagem) {
		messageCatalog.add(mensagem);

	}

	public synchronized boolean existsMessagesFor(String clientID) {
		for (Mensagem m : messageCatalog) {
			if (m.getRecipient().equals(clientID))
				return true;
		}
		return false;
	}

	public synchronized int getSize() {
		return messageCatalog.size();
	}

	public synchronized ArrayList<Mensagem> getMessagesForClient(String clientID) {

		ArrayList<Mensagem> messagesForClient = new ArrayList<Mensagem>();
		for (Mensagem m : messageCatalog) {
			if (m.getRecipient().equals(clientID))
				messagesForClient.add(m);
		}

		return messagesForClient;
	}

	public synchronized  void remove(Mensagem m) {
		int index = messageCatalog.indexOf(m);
		messageCatalog.remove(index);
	}

}
