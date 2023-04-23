package entities;

import javax.crypto.*;
import javax.crypto.spec.*;

import catalogs.UserCatalog;

import java.io.*;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

public class FileEncryptorDecryptor {
	
	public static void decryptUsersCat(String inputFile, String pass) {

		try {
			decryptUsers("./src/userCatalogEncrypted.txt", inputFile, pass);
			System.out.println("Ficheiro decifrado com sucesso.");
			File usersCatalog = new File("./src/userCatalogEncrypted.txt");
			usersCatalog.delete();

		} catch (Exception ex) {
			System.out.println("Ocorreu um erro: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void encryptUsersCat(String inputFile, String pass) {

		try {
			encryptUsers(inputFile, "./src/userCatalogEncrypted.txt", pass);
			System.out.println("Ficheiro cifrado com sucesso.");
			File usersCatalogC = new File(inputFile);
			usersCatalogC.delete();

		} catch (Exception ex) {
			System.out.println("Ocorreu um erro: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

//-------------------------------------
// Cifrar
	
	public static void encryptUsers(String inputFile, String outputFile, String password)
			throws GeneralSecurityException, IOException {

		byte[] salt = new byte[8];
		FileInputStream inFile = new FileInputStream(inputFile);
		FileOutputStream outFile = new FileOutputStream(outputFile);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
																	// neste o numero de bits e variavel (unico que deu para 
																	// corresponder dos dois lados sem dar erro)
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		// salt aleatorio
		salt = generateSalt();

		// cria uma key secreta para se usar na cifra
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);  //dps aqui defini os 128 
		SecretKey tmp = keyFactory.generateSecret(keySpec);
		SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

		// iv aleatorio (vetor inicializacao)
		byte[] iv = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);

		// inicializa
		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

		// escreve o salt e o iv no cifrado para depois vermos no decifrado
		outFile.write(salt);
		outFile.write(iv);

		// Cifra o usersCatalog
		byte[] input = new byte[64]; // tive de por 64 porque senao depois no decifrar cortava a mensagem
		int bytesRead;
		while ((bytesRead = inFile.read(input)) != -1) {
			byte[] output = cipher.update(input, 0, bytesRead);
			if (output != null) {
				outFile.write(output);
			}
		}
		byte[] output = cipher.doFinal(); // escreve o resultado final 
		if (output != null) {
			outFile.write(output);
		}

		inFile.close();
		outFile.flush();
		outFile.close();
	}

	private static byte[] generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return salt;
	}
	
//-------------------------------------
// Decifrar
	
	public static void decryptUsers(String inputFile, String outputFile, String password)
	        throws GeneralSecurityException, IOException {

	    FileInputStream fis = new FileInputStream(inputFile);
	    FileOutputStream fos = new FileOutputStream(outputFile);

	    byte[] salt = new byte[8];
	    fis.read(salt);

	    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); 
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

	    KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
	    SecretKey key = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");

	    byte[] iv = new byte[16];
	    fis.read(iv);

	    AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
	    params.init(new IvParameterSpec(iv));
	    cipher.init(Cipher.DECRYPT_MODE, key, params);

	    // Decifra o usersCatalog
	    byte[] in = new byte[64]; 
	    int read;
	    while ((read = fis.read(in)) != -1) {
	        byte[] output = cipher.update(in, 0, read);
	        if (output != null) {
	            fos.write(output);
	        }
	    }
	    byte[] output = cipher.doFinal();
	    if (output != null) {
	        fos.write(output);
	    }

	    fis.close();
	    fos.flush();
	    fos.close();
	}

}
