package namehit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TokenManager {

	private static String token;
	
	static {
		loadToken();
	}
	
	private static void loadToken() {
		try {
			Scanner scan = new Scanner(new FileInputStream("apiSecret.key"));
			token = scan.nextLine();
		} catch (FileNotFoundException e) {
			System.out.println("Token was not found!\nPlease create a file called 'apiSecret.key' in the current directory and place the token in there.");
		}
	}
	
	static String getToken() {
		return token;
	}

}
