import java.util.*;
// State as stack 

// <id, val> 
class Pair {
   Identifier id;
   Value val;
   
   Pair (Identifier id, Value v) {
     this.id = id;
     this.val = v;
   }
}

class State extends Stack<Pair> {
    public State() { }
    
    public State(Identifier id, Value val) {
        push(id, val);
    }

    // (1) Push function Implementation
    public State push(Identifier id, Value val) {
    	// Push Implementation
    	Pair pair = new Pair(id, val);
        this.push(pair); // 식별자, 값 pair 넣기
        return this;
    }

    // (2) Pop function Implementation (Optional)
    public Pair pop() {
    	// Pop Implementation (Optional)
        if (this.isEmpty()) { // stack이 비어있으면
            throw new RuntimeException(); // 오류
        }
        return this.remove(this.size()-1); // 아니면 그 값 return 하고 삭제
    	
    }
    
    // (3) Lookup function Implementation
    public int lookup (Identifier v) {
       // Lookup Implementation
    	int length = this.size();
        for (int i = length - 1; i >= 0; i--) {//스택의 맨 위부터 확인
            if (this.get(i).id.equals(v)) { //식별자가 일치하면
                return i; // 인덱스 return
            }
        }
        throw new RuntimeException();// 없으면 오류
    }

    // (4) Set Function Implementation
    public State set(Identifier id, Value val) {
    	// Set Implementation
        int index = lookup(id);
        Pair pair = new Pair(id, val);
        this.set(index, pair); // 해당 식별자 위치의 pair 변경
        return this;
    }
    
    // (5) Get Function Implementation
    public Value get (Identifier id) {
    	// Get Implementation
        int index = lookup(id);
        return this.get(index).val; // 해당 식별자의 변수의 값 return
    }

}