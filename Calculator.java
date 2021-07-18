import java.util.*;
import java.nio.file.*;

public class Calculator {
	
	private static void about() {
		System.out.println("EvalCalc 1.0\nType 'exit' or 'quit' to quit the program/debug mode.\nType 'dbg' or 'debug' to enter debug mode.\nType 'help' to learn this program's features.\n");
	}
	
	private static void help() {
		try {
			String help = new String(Files.readAllBytes(Paths.get("README.md")));
			System.out.println(help.replaceAll("\\\\", "").replaceAll("\\*\\*", ""));
		}
		catch (Exception e) {
			System.out.println("Error loading help!");
		}
	}
	
	public static void main(String[] args) {
		Expression c = new Expression();
		Scanner sc = new Scanner(System.in);
		
		boolean exit = false;
		boolean debug = false;
		
		boolean bin = false;
		boolean hex = false;
		boolean oct = false;
	
		String input;
		String oldinput = "0.0";
				
		about();
		while (!exit) {
			System.out.print((debug) ? "(*) " : ">>> ");
			if (sc.hasNextLine()) {
				input = sc.nextLine().toLowerCase();
				
				if (input.equals("exit") || input.equals("quit")) {
					if (debug) debug = false;
					else exit = true;
				}
				else if (input.equals("help")) {
					help();
				}
				else if (input.equals("dbg") || input.equals("debug")) {
					debug = true;
				}
				else if (input.equals("bin")) {
					bin = true; 
					hex = oct = false;
				}
				else if (input.equals("hex")) {
					hex = true; 
					oct = bin = false;
				}
				else if (input.equals("oct")) {
					oct = true; 
					hex = bin = false;
				}
				else if (input.equals("dec")) {
					oct = bin = hex = false;
				}
				else {
					try {
						if (input.equals("")) 
							input = oldinput;
						
						for (String expr : input.split(";")) {
							Object[] soln = c.solve(expr);
							System.out.print(soln[0] + " = ");
							Double sol = (Double) soln[1];
							System.out.println((hex) ? ("0x"+Long.toHexString(sol.longValue())) 
											: ((bin) ? ("0b"+Long.toBinaryString(sol.longValue())) 
											: ((oct) ? ("0o"+Long.toOctalString(sol.longValue())) 
											: String.valueOf(sol)))); 
						}
						
						oldinput = input;
					}
					catch (Exception e) { 
						System.out.println("Invalid expression"); 
					}
					
					if (debug) 
						c.print();
				}
				System.out.println();
			}
		}
	}
}
