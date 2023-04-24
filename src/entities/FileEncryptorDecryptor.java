package entities;

import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

public class FileEncryptorDecryptor {

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

	public static String decryptUsersCat(String inputFile, String pass) {

		String decrypted = null; 

		try {
			decrypted = decryptUsers("./src/userCatalogEncrypted.txt", pass);
			System.out.println("Ficheiro decifrado com sucesso.");

		} catch (Exception ex) {
			System.out.println("Ocorreu um erro: " + ex.getMessage());
			ex.printStackTrace();
		}

		return decrypted;
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

		// salt aleatorio - garante que, mesmo que duas pessoas usem a mesma pass, as chaves criadas serão diferentes
		salt = generateSalt();

		// cria uma key secreta para se usar na cifra
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);  //dps aqui defini os 128 
		SecretKey tmp = keyFactory.generateSecret(keySpec);
		SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

		// iv aleatorio (vetor inicializacao) - permite que blocos parecidos de texto não sejam criptografados da mesma forma
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

	public static String decryptUsers(String inputFile, String password)
			throws GeneralSecurityException, IOException {

		FileInputStream fis = new FileInputStream(inputFile);

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

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Decrypt the input file
		byte[] in = new byte[64]; 
		int read;
		while ((read = fis.read(in)) != -1) {
			byte[] output = cipher.update(in, 0, read);
			if (output != null) {
				baos.write(output);
			}
		}
		byte[] output = cipher.doFinal();
		if (output != null) {
			baos.write(output);
		}

		fis.close();
		baos.close();

		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

}
