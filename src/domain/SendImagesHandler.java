package domain;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SendImagesHandler {

	private ObjectOutputStream outStream;
	String stringPath;
	
	public SendImagesHandler(ObjectOutputStream outStream, String path) {
		this.outStream = outStream;
		this.stringPath = path;
	} 

	public Boolean sendImage(String imgPath) throws IOException {
		 
		Path imagePath = Paths.get(this.stringPath + imgPath);
		byte[] imageBytes = null;

		if(!Files.exists(imagePath)) 
			return false;

		imageBytes = Files.readAllBytes(imagePath);
		
		outStream.writeObject(imageBytes);
		outStream.flush();

		return true;
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
