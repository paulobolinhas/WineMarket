package entities;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

public class FileEncryptor {

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
}
