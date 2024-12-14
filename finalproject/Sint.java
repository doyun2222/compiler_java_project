// Sint.java
// Interpreter for S
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) { 
        if (!state.empty()) {
            Pair p = state.peek();
            if (p.val.type() == Type.RAISEDEXC) {
                state.pop();
            }
        }

	    if (c instanceof Decl) {
	        Decls decls = new Decls();
	        decls.add((Decl)c);
	        return allocate(decls, state);
	    }

	    if (c instanceof Function) {
	        Function f = (Function) c; 
	        state.push(f.id, new Value(f)); 
	        return state;
	    }

	    if (c instanceof Stmt)
	        return Eval((Stmt)c, state); 

	throw new IllegalArgumentException("no command");
    }
  
    State Eval(Stmt s, State state) {
        if (!state.empty()) {
            Pair p = state.peek();
            if (p.val.type() == Type.RAISEDEXC) 
                return state;
        }

        if (s instanceof Empty) 
	        return Eval((Empty)s, state);
        if (s instanceof Assignment)  
	        return Eval((Assignment)s, state);
        if (s instanceof If)  
	        return Eval((If)s, state);
        if (s instanceof While)  
	        return Eval((While)s, state);
        if (s instanceof Stmts)  
	        return Eval((Stmts)s, state);
	    if (s instanceof Let)  
	       return Eval((Let)s, state);
	    if (s instanceof Read)  
	       return Eval((Read)s, state);
	    if (s instanceof Print)  
	       return Eval((Print)s, state);
        if (s instanceof Call) 
	       return Eval((Call)s, state);
	    if (s instanceof Return) 
	       return Eval((Return)s, state);
	    if (s instanceof Raise) 
	       return Eval((Raise)s, state);
	    if (s instanceof Try) 
	       return Eval((Try)s, state);
        throw new IllegalArgumentException("no statement");
    }

    // call without return value
    State Eval(Call c, State state) {
	    Value v = state.get(c.fid);      // find the function
        Function f = v.funValue();
        State s = newFrame(state, c, f); // new the frame on the stack
        System.out.println("Calling " + c.fid);
        s = Eval(f.stmt, s); 		// interpret the call
        System.out.println("Returning " + c.fid); 
        s = deleteFrame(s, c, f);	// delete the frame
    	return s;
    }

    // value-returning call 
    Value V (Call c, State state) { 
	    Value v = state.get(c.fid);		// find function
        Function f = v.funValue();
        State s = newFrame(state, c, f); // new the frame on the stack
        System.out.println("Calling " + c.fid); 
        s = Eval(f.stmt, s); 			// interpret the call
        System.out.println("Returning " + c.fid); 
	    v = s.pop().val;   				// remove the return value
        s = deleteFrame(s, c, f); 		// delete the frame from the stack
    	return v;
    }

    State Eval(Return r, State state) {
        Value v = V(r.expr, state);
        return state.push(r.fid, v);
    }

    State newFrame (State state, Call c, Function f) {
        if (c.args.size() == 0) 
            return state;
	    Value val[] = new Value[f.params.size()];
        int i = 0;
        for (Expr e : c.args) 
            val[i++] = V(e,state);

        state.push(new Identifier("barrier"), null); // barrier 
	    // activate a new stack frame in the stack 
        state = allocate(f.params, state);     
        i = 0; 
        for (Decl d : f.params) { // pass by value
            Identifier v = d.id;
            state.set(v, val[i++]);
        }
        return state;
    }

    State deleteFrame (State state, Call c, Function f) {
	// free a stack frame from the stack
        if (f.params != null) 
	        state  = free(f.params, state);
        state.pop(); // pop barrier
        return state;            
    }

    State Eval(Empty s, State state) {
        return state;
    }
  
    
    // (1) Assignment Eval Implementation
    State Eval(Assignment a, State state) {
    	// Assignment Implementation
    	Value v = V(a.expr, state);
    	return state.set(a.id, v); //state에 해당 식별자 값 수정
    }

    // (2) If Eval Implementation
    State Eval(If c, State state) {
        // If Implementation
    	if (V(c.expr, state).boolValue()) //조건 참이면
    		return Eval (c.stmt1, state); //s1실행 후 상태 return
    	else
    		return Eval (c.stmt2, state);
    }
 
    // (3) While Eval Implementation
    State Eval(While l, State state) {
        // While Implementation
    	if (V(l.expr, state).boolValue())// 조건 참이면
    		return Eval(l, Eval (l.stmt, state)); // 재귀적으로 호출 반복해서 상태 업데이트
    	else
    		return state;
    }

    // (4) Let Eval Implementation
    State Eval(Let l, State state) {
        // Let Implementation
    	State s = allocate(l.decls, state); // decls에 포함된 변수 추가
    	s = Eval(l.stmts, s); // 연산 진행
    	return free(l.decls, s); // 선언된 변수 제거하고 원래 상태로 되돌림
    }
    
    // (5) Read Eval Implementation
    State Eval(Read r, State state) {
        // Read Implementation
    	// 타입에 따라 상태 업데이트
    	if (r.id.type== Type.INT) {
    		int i= sc.nextInt();
    		state.set(r.id, new Value(i));
    	}
    	if (r.id.type== Type.BOOL) {
    		boolean b = sc.nextBoolean();
    		state.set(r.id, new Value(b));
    	}
    	if (r.id.type== Type.STRING) {
    		String s = sc.nextLine();
    		state.set(r.id, new Value(s));
    	}
    	return state;
    }

    // (6) Print Eval Implementation
    State Eval(Print p, State state) {
	    // Print Implementation
    	System.out.println(V(p.expr, state)); // 수식 값 계산 후 출력
    	return state;
    }
  
    
    State Eval(Stmts ss, State state) {
        for (Stmt stmt : ss.stmts) {
            state = Eval(stmt, state);
        }
        return state;
    }

    
    // (7) Allocate Function Implementation
    State allocate (Decls ds, State state) {
        // Allocate Implementation
        for (Decl d : ds) {
        	Value v;
            Identifier id = d.id;
            if (d.expr != null) { // 초기화 수식이 있을 경우
                v = V(d.expr, state); // 계산해서 값에 넣음
            } else {
                switch (d.type) { // 초기화 수식 없는 경우
                    case Type.INT: // int면 0으로 자동 초기화
                        v = new Value(0);
                        break;
                    case Type.STRING: // string이면 ""으로 자동 초기화
                        v = new Value("");
                        break;
                    case Type.BOOL: // boolean이면 false로 자동 초기화
                        v = new Value(false);
                        break;
                    default: // 위 3가지 타입이 아니면 에러
                        throw new RuntimeException();
                }
            }
            state.push(id, v); //스택에 넣어주고
        }
        return state; //상태 return
    }

    // (8) Free Function Implementation
    State free (Decls ds, State state) {
	    // Free Implementation
    	int numDs = ds.size();
        for (int i = numDs - 1; i >= 0; i--) {// 마지막 선언부터 거꾸로 확인
            Identifier id = ds.get(i).id;
            if (!state.isEmpty() && state.peek().id.euqals(id)) {
                state.pop(); // 스택이 비어있지도 않고 맨위에 pop해야할거랑 식별자 같으면 pop
            } else {
                throw new RuntimeException(); // 아니면 오류
            }
        }
        return state; //상태 return
    }
    
    // @@추가 기능 구현 1 for Eval구현@@
    State Eval (For f, State state) {
    	 State s = allocate (f.decl, state);  // decls에 포함된 변수 추가
    	 while (V(f.expr, s).boolValue()){ // 비교 수식이 참일 동안
    	 s = Eval(f.stmt, s); // for 안의 문장 실행 후 상태 update
    	 s = Eval(f.assignment, s); // 할당문 실행 후 상태 update
    	 }
    	 return free (f.decl, s); // 선언된 변수 제거하고 원래 상태로 되돌림
    } 

    
    // Allocate for Function Implementation (Optional)
    State allocate (Decls ds, Functions fs, State state) {
        // Allocate Implementation
    }
    // Free for Function Implementation (Optional)
    State free (Decls ds, Functions fs, State state) {
        // Free Implementation
    }

    
    
    State Eval(Raise r, State state) {
        Value v = V(r.eid, state);
        return state.push(r.eid, new Value(Type.RAISEDEXC));
    }

    State Eval(Try t, State state) {
        state = Eval(t.stmt1, state); 
        Pair p = state.peek();
        if (p.val.type() == Type.RAISEDEXC) 
           if (p.id.equals(t.eid)) {  	// caught
               state.pop();
               state = Eval(t.stmt2, state);  	
           }
        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef, "reference to undef value");
	    switch (op.val) {
	    case "+":
            return new Value(v1.intValue() + v2.intValue());
        case "-": 
            return new Value(v1.intValue() - v2.intValue());
        case "*": 
            return new Value(v1.intValue() * v2.intValue());
        case "/": 
            return new Value(v1.intValue() / v2.intValue());
        case "==": 
            return new Value(v1.intValue() == v2.intValue());
        case "!=": 
            return new Value(v1.intValue() != v2.intValue());
        case "<": 
            return new Value(v1.intValue() < v2.intValue());
        case "<=": 
            return new Value(v1.intValue() <= v2.intValue());
        case ">": 
            return new Value(v1.intValue() > v2.intValue());
        case ">=": 
            return new Value(v1.intValue() >= v2.intValue());
        case "&": 
            return new Value(v1.boolValue() && v2.boolValue());
        case "|": 
            return new Value(v1.boolValue() || v2.boolValue());
	    default:
	        throw new IllegalArgumentException("no operation");
	    }
    } 
    
    Value unaryOperation(Operator op, Value v) {
        check(!v.undef, "reference to undef value");
	    switch (op.val) {
        case "!": 
            return new Value(!v.boolValue( ));
	    case "-": 
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val); 
        }
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) 
            return (Value) e;
        if (e instanceof Identifier) { 
            Identifier v = (Identifier) e; 
            return (Value)(state.get(v));
	    }
        if (e instanceof Array) {
	        Array ar = (Array) e;
            Value i = V(ar.expr, state);
            Value v = (Value) state.get(ar.id);
            Value[] vs = v.arrValue(); 
            return (vs[i.intValue()]); 
	    }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2); 
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v); 
        }
        if (e instanceof Call) 
    	    return V((Call)e, state);  
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint(); Lexer.interactive = true;
            System.out.println("Language S Interpreter 1.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		        parser.token = parser.lexer.getToken();
	       
	            Command command=null;
                try {
	                command = parser.command();
                    command.type = TypeChecker.Check(command); 
                } catch (Exception e) {
                    System.out.println(e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println(e);  
                    }
                }
		    System.out.print(">> ");
	        } while (true);
	    }
        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;
	         
                try {
		            command = parser.command();
                    command.type = TypeChecker.Check(command);    
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);  
                    }
                }
	        } while (command != null);
        }        
    }
}