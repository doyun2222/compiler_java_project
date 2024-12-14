// TypeEnv.java
// Type environment for S
import java.util.*;

// <id, type> 
class Entry {
   Identifier id;
   Type type;
   
   Entry (Identifier id, Type t) {
     this.id = id;
     this.type = t;
   }
}

class TypeEnv extends Stack<Entry> {
    public TypeEnv() { }
    
    public TypeEnv(Identifier id, Type t) {
        push(id, t);
    }
    
    public TypeEnv push(Identifier id, Type t) {
        super.push(new Entry(id, t));
	return this;
    }
    
    
    // (1) Contatins Function Implementation
    public boolean contains (Identifier v) {// state의 lookup 활용
    	int length = this.size();
        for (int i = 0; i < length; i++) {//타입환경 확인
            if (this.get(i).id.equals(v)) { //일치하는 식별자가 있으면
                return true; // return ture
            }
        }
        return false;// 없으면 return false
    }

    // (2) Get Function Implementation
    public Type get (Identifier v) {// state의 lookup 활용
    	int length = this.size();
        for (int i = length - 1; i >= 0; i--) {//스택의 맨 위부터 확인
            if (this.get(i).id.equals(v)) { //식별자가 일치하면
                return this.get(i).t; // 타입 가져오기 return
            }
        }
        throw new RuntimeException();// 없으면 오류
    }
}