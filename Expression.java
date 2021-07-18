import java.util.*;
import java.util.regex.Pattern;
import java.math.BigInteger;

class Operator {
	public static final int LEFT = 0;
	public static final int BINARY = 0;
	public static final int RIGHT = 1;
	public static final int UNARY = 1;
	
	public char op;
	public int precedence;
	public int associativity;
	public int type;
	
	public Operator(char op, int precedence, int associativity, int type) {
		this.op = op;
		this.precedence = precedence;
		this.associativity = associativity;
		this.type = type;
	}
}

public class Expression {
	private String input;
	
	private HashMap<Character, Operator> operators;
	private HashSet<String> oneINFunctions;
	private HashSet<String> twoINFunctions;
	
	private HashMap<String, String> defaults;
	private HashMap<String, String> variables;

	private ArrayList<String> tokens;
	private Queue<String> outqueue;
	
	private String outstr;
	private double output;

	public Expression() {		
		operators = new HashMap<>();
		operators.put('^', new Operator('^', 4, Operator.RIGHT, Operator.BINARY));
		operators.put('*', new Operator('*', 3,  Operator.LEFT, Operator.BINARY));
		operators.put('/', new Operator('/', 3,  Operator.LEFT, Operator.BINARY));
		operators.put('+', new Operator('+', 2,  Operator.LEFT, Operator.BINARY));
		operators.put('-', new Operator('-', 2,  Operator.LEFT, Operator.BINARY));
		operators.put('%', new Operator('%', 3,  Operator.LEFT, Operator.BINARY));
		operators.put('!', new Operator('!', 5,  Operator.LEFT,  Operator.UNARY));
		
		oneINFunctions = new HashSet<>(
			Arrays.asList("sin", "cos", "tan", 
				"asin", "acos", "atan", 
				"cosh", "sinh", "tanh", 
				"sqrt", "cbrt", "exp", 
				"abs", "ceil", "floor", "round", 
				"ln", "log", 
				"deg", "rad", "sign")
			);
			
		twoINFunctions = new HashSet<>(
			Arrays.asList("max", "min", "mod", "randr", "randir")
			);
		
		defaults = new HashMap<>();
		defaults.put("pi", String.valueOf(Math.PI));
		defaults.put("e", String.valueOf(Math.E));
		defaults.put("ans", "0.0");
		defaults.put("rand", String.valueOf(Math.random()));		
		
		variables = new HashMap<>();
	}

	private void tokenize() {
		tokens = new ArrayList<>();
		
		int i = 0;
		while (i < input.length()) {
			// Check if +, - is either a unary operator (sign) or binary (add/sub)
			if (input.charAt(i) == '+' || input.charAt(i) == '-') {
				// Look at previous character to tell sign
				// If the operator is at the beginning, e.g. -5
				// Or if the previous character is another operator, e.g 5*-5
				// Or if the previous character is the left parenthese, e.g tan(-5)
				// Then it's a unary operator
				if (i == 0 || operators.containsKey(input.charAt(i-1)) || input.charAt(i-1) == '(') {
					tokens.add(input.charAt(i) + "1");
					tokens.add("*");
				}
				// Otherwise, it's binary
				else tokens.add(String.valueOf(input.charAt(i)));
			}
			// Add other operators
			else if (operators.containsKey(input.charAt(i))) {
				// If operator not preceded by a number, set the preceding number to the answer of the previous operation
				if (i == 0) tokens.add(defaults.get("ans"));
				tokens.add(String.valueOf(input.charAt(i)));
			}
			// Add parentheses
			else if (input.charAt(i) == ')') {
				tokens.add(String.valueOf(input.charAt(i)));
				// Check for input like (5)(5), (5)5 or (5).5 and add the implicit multiplication
				if (i < (input.length() - 1) && 
					(input.charAt(i+1) == '(' ||Character.isDigit(input.charAt(i+1)) || input.charAt(i+1) == '.' || Character.isLetter(input.charAt(i+1))))
				tokens.add("*");
			}				
			else if (input.charAt(i) == '(') {
				// Check for input like 5(5) or 5.(5) and add the implicit multiplication
				if (i != 0 && (Character.isDigit(input.charAt(i-1)) || input.charAt(i-1) == '.'))
					tokens.add("*");
				tokens.add(String.valueOf(input.charAt(i)));
			}
			else if (input.charAt(i) == ',') {
				tokens.add(String.valueOf(input.charAt(i)));
			}
			// Parse hexadecimal/octal/bianry numbers
			else if (i < (input.length() - 2) && input.charAt(i) == '0' && 
					(input.charAt(i+1) == 'x' || input.charAt(i+1) == 'b' || input.charAt(i+1) == 'o' || input.charAt(i+1) == 'd')) {
				char type = input.charAt(i+1);
				int radix;
				switch(type) {
					case 'x': radix = 16; break;
					case 'b': radix =  2; break;
					case 'o': radix =  8; break;
					default:  radix = 10; break;
				}
				i += 2;
				StringBuilder radsb = new StringBuilder();
				while (i < input.length() && Character.digit(input.charAt(i), radix) >= 0) {
					radsb.append(input.charAt(i++));
				}
				i--;
				tokens.add(String.valueOf(Long.parseLong(radsb.toString(), radix)));
			}
			// Add numbers
			else if (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.') {
				StringBuilder numsb = new StringBuilder();
				while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.' || input.charAt(i) == 'e')) {
					numsb.append(input.charAt(i++));
					// Check for scientific notation
					if (i < (input.length() - 1) && input.charAt(i-1) == 'e' && (input.charAt(i) == '+' || input.charAt(i) == '-'))
						numsb.append(input.charAt(i++));
				}
				i--;
				
				// If number ends in e, roll back the counter and chop the last character of the number string
				String numstr = numsb.toString();
				if (numstr.endsWith("e")) {
					numstr = numstr.substring(0, numstr.length() - 1);
					i--;
				}
				
				tokens.add(numstr);

				// If number followed by function, add the implicit multiplication, e.g. 5tan(pi/2) => 5*tan(pi/2)
				if (i < (input.length() - 1) && Character.isLetter(input.charAt(i+1)))
					tokens.add("*");
			}
			// Add functions
			else if (Character.isLetter(input.charAt(i))) {
				StringBuilder funcsb = new StringBuilder();
				while (i < input.length() && Character.isLetter(input.charAt(i))) {
					funcsb.append(input.charAt(i++));
				}
				i--;
				
				// Check for constants/variables and replace them accordingly
				String funcstr = funcsb.toString();
				if (defaults.containsKey(funcstr))
					tokens.add(defaults.get(funcstr));
				else if (variables.containsKey(funcstr))
					tokens.add(variables.get(funcstr));
				else
					tokens.add(funcstr);
			}
			
			i++;
		}
	}

	// Helper methods to check if a string is a number or a function
	public static boolean isNumber(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isAlpha(String str) {
		if (str == null) return false;
		return Pattern.compile("[a-zA-Z]+").matcher(str).matches();
	}

	// Use the Shunting-Yard Algorithm to convert infix to postfix
	private void infixToPostfix() {
		outqueue = new LinkedList<>();
		Stack<String> opstack = new Stack<>();
		
		for (String t : this.tokens) {
			// Number so add to output queue
			if (isNumber(t)) outqueue.add(t);
			// Function so add to operator stack
			else if (isAlpha(t)) opstack.push(t);
			// Operator
			else if (operators.containsKey(t.charAt(0))) {
				// Let o1 be the operation associated with the current char
				Operator o1 = operators.get(t.charAt(0));
				// While the operator stack is not empty and there isn't a parenthetical expression coming up,
				// If o1 is left associative and its precedence is leq the precedence of the op at the top of the operator stack
				// Or o1 is right associative and its precedence is lt the precedence of the op at the top of the operator stack
				// Push from the operator stack to the output queue  
				while (!opstack.empty() && !opstack.peek().equals("(")
					&& ((o1.associativity == Operator.LEFT && o1.precedence <= operators.get(opstack.peek().charAt(0)).precedence) 
					|| (o1.associativity == Operator.RIGHT && o1.precedence < operators.get(opstack.peek().charAt(0)).precedence))) {
						outqueue.add(opstack.pop());
					}
				// Push o1 to the operator stack
				opstack.push(t);
			}
			// Left parenthese so add to operator stack
			else if (t.equals("(")) opstack.push(t);
			// Right parenthese
			else if (t.equals(")")) {
				// Add everything within parentheses to the output queue as they have the highest priority
				while (!opstack.peek().equals("("))
					outqueue.add(opstack.pop());
				// Discard the left parenthese
				opstack.pop();
				// If the parenthese is for a function, add the subsequent function
				if (!opstack.empty() && isAlpha(opstack.peek()))
					outqueue.add(opstack.pop());
			}
			else if (t.equals(",")) {
				while (!opstack.peek().equals("("))
					outqueue.add(opstack.pop());
			}
		}
		
		// Add everything else in the operator stack to the output queue
		while (!opstack.empty())
			outqueue.add(opstack.pop());
	}

	public static String factorial (long n) {
		BigInteger result = BigInteger.valueOf(1);
		for (; n > 1; n--)
			result = result.multiply(BigInteger.valueOf(n));
		return String.valueOf(result);
	}

	private String performBinaryOperation(String a, String b, char op) {
		double u = Double.parseDouble(b);
		double v = Double.parseDouble(a);
		
		switch (op) {
			case '^': return String.valueOf(Math.pow(u, v));
			case '*': return String.valueOf(u * v);
			case '/': return String.valueOf(u / v);
			case '+': return String.valueOf(u + v);
			case '-': return String.valueOf(u - v);
			case '%': return String.valueOf(u / 100.0 * v);
		}
		
		return "0";
	}
	
	private String performUnaryOperation(String a, char op) {
		double u = Double.parseDouble(a);
		
		switch (op) {
			case '!': return factorial((long) u);
		}
		
		return "0";
	}

	private String performOneINFunctions(String a, String func) {
		double u = Double.parseDouble(a);
		
		if (func.equals("sin")) 		return String.valueOf(Math.sin(u));
		else if (func.equals("cos")) 	return String.valueOf(Math.cos(u));
		else if (func.equals("tan")) 	return String.valueOf(Math.tan(u));
		
		else if (func.equals("asin")) 	return String.valueOf(Math.asin(u));
		else if (func.equals("acos")) 	return String.valueOf(Math.acos(u));
		else if (func.equals("atan")) 	return String.valueOf(Math.atan(u));
		
		else if (func.equals("sinh")) 	return String.valueOf(Math.sinh(u));
		else if (func.equals("cosh")) 	return String.valueOf(Math.cosh(u));
		else if (func.equals("tanh")) 	return String.valueOf(Math.tanh(u));
		
		else if (func.equals("sqrt")) 	return String.valueOf(Math.sqrt(u));
		else if (func.equals("cbrt")) 	return String.valueOf(Math.cbrt(u));
		else if (func.equals("exp")) 	return String.valueOf(Math.exp(u));
		
		else if (func.equals("abs")) 	return String.valueOf(Math.abs(u));
		else if (func.equals("ceil")) 	return String.valueOf(Math.ceil(u));
		else if (func.equals("floor")) 	return String.valueOf(Math.floor(u));
		else if (func.equals("round")) 	return String.valueOf(Math.round(u));
		
		else if (func.equals("ln")) 	return String.valueOf(Math.log(u));
		else if (func.equals("log")) 	return String.valueOf(Math.log10(u));

		else if (func.equals("deg")) 	return String.valueOf(Math.toDegrees(u));
		else if (func.equals("rad")) 	return String.valueOf(Math.toRadians(u));
		else if (func.equals("sign")) 	return String.valueOf(Math.signum(u));
		
		else 							return "0";
	}
	
	private String performTwoINFunctions(String a, String b, String func) {
		double u = Double.parseDouble(b);
		double v = Double.parseDouble(a);
		
		if (func.equals("max")) 		return String.valueOf(Math.max(u, v));
		else if (func.equals("min")) 	return String.valueOf(Math.min(u, v));
		else if (func.equals("mod")) 	return String.valueOf(u % v);
		else if (func.equals("randr")) 	return String.valueOf(((Math.random() * (v - u)) + u));
		else if (func.equals("randir")) return String.valueOf((int) ((Math.random() * (v - u)) + u));
		else 							return "0";
	}

	private void evaluate() {
		Stack<String> stack = new Stack<>();
		
		while (!outqueue.isEmpty()) {
			String t = outqueue.remove();
			char tc = t.charAt(0);
			
			if (isNumber(t)) 
				stack.push(t);
			else if (operators.containsKey(tc)) {
				if (operators.get(tc).type == Operator.BINARY)
					stack.push(performBinaryOperation(stack.pop(), stack.pop(), tc));
				else if (operators.get(tc).type == Operator.UNARY)
					stack.push(performUnaryOperation(stack.pop(), tc));
			}
			else if (oneINFunctions.contains(t))
				stack.push(performOneINFunctions(stack.pop(), t));
			else if (twoINFunctions.contains(t))
				stack.push(performTwoINFunctions(stack.pop(), stack.pop(), t));
		}
		
		outstr = stack.pop();
		defaults.put("ans", outstr);
		defaults.put("rand", String.valueOf(Math.random()));
		
		output = Double.parseDouble(outstr);
	}
	
	public Object[] solve(String input) {
		// Sanitize input by removing all whitespace and making input lower case
		this.input = input.replaceAll("\\s+", "").toLowerCase();
		
		int eqidx = this.input.indexOf("=");
		
		String varName = null;
		
		// Set variable name to everything befor '=' and expression to everything after
		if (eqidx > 0) {
			varName = this.input.substring(0, eqidx);
			if (oneINFunctions.contains(varName) || twoINFunctions.contains(varName))
				varName = "x";
			this.input = this.input.substring(eqidx + 1);
		}
		
		// Solve
		tokenize();
		infixToPostfix();
		evaluate();
		
		// Add the variable and its value to the variable HashMap
		if (varName != null) {
			variables.put(varName, this.outstr);
		}
		
		return new Object[]{(varName != null) ? varName : "Ans", this.output};
	}
	
	public void print() {
		System.out.println("Input: " + input);		
		System.out.print("Tokens: "); tokens.forEach(i -> System.out.print(i + " ")); System.out.println();
		// Have to redo infix to postfix conversion because the queue was emptied but its debug mode so ¯\_(ツ)_/¯
		infixToPostfix();
		System.out.print("Postfix: "); outqueue.forEach(i -> System.out.print(i + " ")); System.out.println();
		System.out.println("Output: " + output);
	}
}
