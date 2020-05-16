import java.io.*;
import java.util.*;
/**
 * The following defines a simple language, in which a program consists of 
 * assignments and each variable is assumed to be of the integer type. 
 * For the sake of simplicity, only operators that give integer values are included. 
 * @author yingdong
 *
 */
public class Evaluator {
	//hashtable: keys - variable names, values - assigned values
	public static Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
	
	//List of variable names
	public static ArrayList<String> variables = new ArrayList<String>();
	public static void main(String[] args) throws Exception{
		try {
			//Scanner input = new Scanner(new File(args[0]));
			Scanner input = new Scanner(new File("./"+args[0]));
			
			//read input line til the end of file
			while(input.hasNext()){
				String line = input.nextLine();
				//not closing with ; is invalid
				if(line.charAt(line.length()-1) != ';'){
					throw new EvaluationException();
				}
				line = line.substring(0, line.length()-1);
				//separate variable names and values
				String[] assign = line.split("=");
				
				//evaluate expression
				int result = parser(assign[1].trim());
				String name = assign[0].trim();
				
				//if variable name already exists, reassign new values
				if(variables.contains(name)){
					ht.replace(name, result);
				}else{
					variables.add(name);
					ht.put(name, result);
				}
			}
			//result
			for(String s: variables){
				System.out.println(s + " = " + ht.get(s));
			}
		} catch (FileNotFoundException e) {
			System.out.print("File not found");
		}
	}
	/**
	 * evaluate expression
	 * @param expression
	 * @return int result
	 */
	public static int parser(String expression){
		//if expression contains non digit values, throw error
		//otherwise return numeric values
		String digitOnly = "\\d+";
		if(expression.matches(digitOnly)){
			if(expression.charAt(0) == '0' && expression.length() > 1){
				throw new EvaluationException();
			}
			return Integer.parseInt(expression);
		}
		//if expression is a variable, return values in hashtable
		//otherwise, perform replace function on expression and then return its evaluated values
		if(ht.get(expression)!=null){
			return ht.get(expression);
		} else{
			expression = replace(expression);
			return evaluate(expression);
		}
	}
	/**
	 * replace identifier with its numeric values
	 * @param expression
	 * @return String new expression
	 */
	public static String replace(String expression){
		char[] tokens = expression.toCharArray();
		
		Deque<String> ex = new ArrayDeque<String>();
		String name = "";
		int len = tokens.length;
		int i = 0;
		//replace identifier with real values
		while(i < len){
			if(tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/' 
					|| tokens[i] == '(' || tokens[i] == ')'){
				
				if(!name.equals("")){
					//if it is valid variable, push to stack
					//otherwise throw exception
					if(ht.get(name) != null){
						int re = ht.get(name);
						ex.push(re+"");
					} else {
						throw new EvaluationException();
					}
					name = "";
				}
				//push current opereator to stack
				ex.push(tokens[i]+"");
			} else if(tokens[i] >= '0' && tokens[i] <= '9'){
					//push integer to stack
					ex.push(tokens[i]+"");
			} else {
				//concatenate variable name
				name += tokens[i];	

			}
			i++;
		}
		String result  = "";
		//stack to string expression
		Iterator<String> re = ex.iterator();
		while(re.hasNext()){
			result = re.next()+result;
		}
		return result;
	}
	public static int evaluate(String expression) 
    { 
	
        char[] tokens = expression.toCharArray(); 
        
        Deque<Integer> values = new ArrayDeque<Integer>(); 
       
        Deque<Character> ops = new ArrayDeque<Character>(); 
        
        for (int i = 0; i < tokens.length; i++){ 
        	// current token is a number, push it to values stack
            if (tokens[i] >= '0' && tokens[i] <= '9'){ 
                StringBuffer sbuf = new StringBuffer(); 
                sbuf.append(tokens[i]);

                // there may be more than one digits in number 
                while (i < tokens.length-1 && tokens[i+1] >= '0' && tokens[i+1] <= '9'){  	
                    sbuf.append(tokens[i+1]); 
                    i++;     
                }
                values.push(Integer.parseInt(sbuf.toString()));    
            } 
            //current token is an opening brace, push it to ops stack
            else if (tokens[i] == '('){
                ops.push(tokens[i]);
              
            }
            // Closing brace encountered, solve entire brace 
            else if (tokens[i] == ')'){ 
            
                while (ops.peek() != '(') 
                  values.push(applyOperators(ops.pop(), values.pop(), values.pop())); 
                ops.pop();
                
            } 
            //curren token and next token are + or -
            else if((tokens[i] == '+' && tokens[i+1] == '-') || (tokens[i] == '+' && tokens[i+1] == '-')
            		||(tokens[i] == '-' && tokens[i+1] == '+') || (tokens[i] == '-' && tokens[i+1] == '-')){
            	char newOp = applyOperators(tokens[i], tokens[i+1]);
            	tokens[i+1] = newOp;
            }
            // current token is an operator. 
            else if (tokens[i] == '+' || tokens[i] == '-' || 
                     tokens[i] == '*' || tokens[i] == '/') { 
                // while top of ops stack has same or greater precedence to current token, 
            	//apply operator on top of ops stack to top two elements in values stack 
                while (!ops.isEmpty() && hasPrecedence(tokens[i], ops.peek())){ 
                  values.push(applyOperators(ops.pop(), values.pop(), values.pop()));
                 
                }
  
                // push current token to 'ops'. 
                ops.push(tokens[i]); 
            } else {
            	throw new EvaluationException();
            }
        } 
  
        // apply remaining operators to remaining values
        while (!ops.isEmpty()) {
        	if(values.size()>1)
        		values.push(applyOperators(ops.pop(), values.pop(), values.pop()));
        	//remaining operators are unary
        	else
        		values.push(applyOperators(ops.pop(), values.pop()));
        }
        
        int result = values.pop();
        return result;
    }
	/**
	 * evalute precedence of operators
	 * @param op1 char operator
	 * @param op2 char operator
	 * @return boolean true op1 has lower precedence than op2, other wise false
	 */
	public static boolean hasPrecedence(char op1, char op2){
		if (op2 == '(' || op2 == ')') 
            return false; 
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) 
            return false; 
        else
            return true;
	}
	/**
	 * apply operators with 2 numbers
	 * @param op char operator
	 * @param b int number1
	 * @param a int number2
	 * @return int result
	 */
	public static int applyOperators(char op, int b, int a) 
    { 
        switch (op) 
        { 
        case '+': 
            return a + b;
		case '-': 
            return a - b; 
        case '*': 
            return a * b; 
        case '/': 
            if (b == 0) 
                throw new EvaluationException(); 
            return a / b; 
        } 
        return 0; 
    }
	/**
	 * unary expression
	 * @param op char operator
	 * @param a int number
	 * @return int result
	 */
	public static int applyOperators(char op,int a){
		if(op == '-')
			return a * -1;
		return a;
	}
	/**
	 * evaluate operators '+' or '-'
	 * @param op1 char '+' or '-'
	 * @param op2 char '+' or '-'
	 * @return char result '+' or '-'
	 */
	public static char applyOperators(char op1, char op2){
		if(op1 == op2){
			return '+';
		} else {
			return '-';
		}
			
	}
  
}

