import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

public class Diff {
	public static void main(String[] args) {

		if (args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			return;
		}

		System.out.println("arg0 : " + args[0]); // 4242
		System.out.println("arg1 : " + args[1]); // 5555

		Thread.currentThread().setName("DIFF -APPL");
		String input = "";
		Scanner scan = new Scanner(System.in);

		try {
			RingoSocket diffSocket = new RingoSocket("DIFF####", Integer.parseInt(args[0]), Integer.parseInt(args[1]),true);
			while (true) {
				input = scan.nextLine();
				input = input.length() + " " + input;

				try {
					diffSocket.send(input.getBytes());
				} catch (SizeMessageException e) {
					System.out.println("SizeMessageException !! the limit is : "+Ringo.maxSizeMsg);
				}
			}
		} catch (BindException e) {
			System.out.println("DOWNmessageException , the port are already in use");
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		} catch (DOWNmessageException e) {
			System.out.println("DOWNmessageException , the socket is CLOSE");
		}
	}
}