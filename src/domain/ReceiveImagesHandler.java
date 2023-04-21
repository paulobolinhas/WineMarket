package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReceiveImagesHandler {

	private ObjectInputStream inStream;
	String stringPath;
 
	public ReceiveImagesHandler(ObjectInputStream inStream, String path) {
		this.inStream = inStream;
		this.stringPath = path;
		
	}

	public Boolean receiveImage(String imgPath) throws IOException {

		byte[] receivedBytes = null;

		try {
			receivedBytes = (byte[]) inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		Path imagePath = Paths.get(this.stringPath + imgPath);
		Files.write(imagePath, receivedBytes);

		return true;
	}

	public void consumeInput() {
 
		try {
			@SuppressWarnings("unused")
			byte[] receivedBytes = (byte[]) inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteImage(String imgPath) {
		Path imagePath = Paths.get(this.stringPath + imgPath);
		try {
			Files.deleteIfExists(imagePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
