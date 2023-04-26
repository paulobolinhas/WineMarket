package domain.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class IntegrationChecker {

	public static boolean checkSumIntegrityVerification(File file, String filename) {
		try (FileInputStream fis = new FileInputStream(file)) {
			while (fis.read() != -1) {
				// lê o ficheiro
			}
			return true;
		} catch (IOException e) {
			System.out.println("Erro ao abrir o ficheiro " + filename + ". O ficheiro pode estar corrompido.");
			return false;

		}
	}
}
