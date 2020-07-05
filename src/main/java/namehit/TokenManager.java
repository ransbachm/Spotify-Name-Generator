package namehit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.util.Scanner;

public class TokenManager {
	
	private static String client_id;
	private static String client_secret;
	
	static {
		loadToken();
	}
	
	private static void loadToken() {
		try {
			Scanner scan = new Scanner(new FileInputStream("apiSecret.key"));
			client_id = scan.nextLine();
			client_secret = scan.nextLine();
		} catch (Exception e) {
			throw new RuntimeException(new IOException("Secret / id was not found or loaded! Please create a file called 'apiSecret.key' in the current directory. "
					+ "Then place, first ID then in a NEW LINE the SECRET."));
		}
	}
	
	static String getID() {
		return client_id;
	}

	
	static String getSecret() {
		return client_secret;
	}
	
}
