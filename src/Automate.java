import java.util.ArrayList;
import java.util.HashMap;

public class Automate {

    private int istate;
    private int fstate;
    HashMap<Integer, ArrayList<Transition2>> transitions;

    public Automate(int istate, int fstate) {
        this.istate = istate;
        this.fstate = fstate;
        this.transitions = new HashMap<>();
    }

    public int getIstate() { return istate; }

    public void setIstate(int istate) { this.istate = istate; }

    public int getFstate() { return fstate; }

    public void setFstate(int fstate) { this.fstate = fstate; }

    public HashMap<Integer, ArrayList<Transition2>> getTransitions() { return transitions; }

    public void setTransitions(HashMap<Integer, ArrayList<Transition2>> transitions) { this.transitions = transitions; }

    public void addtransition(int from, char s , int to){

        if(!transitions.containsKey(from)){
            transitions.put(from, new ArrayList<>());
        }
        transitions.get(from).add(new Transition2(s , to));
    }

    public void addepsilontransition(int from , int to ){

        addtransition(from , 'ε' , to);
    }

    public void print() {

        System.out.println("Initial state is:"+istate);
        System.out.println("Final state is:"+fstate);

        for(int state : transitions.keySet()){
            for(Transition2 t : transitions.get(state)){
                System.out.println("State"+state+"-----"+t.getSymbol()+"-----"+t.getTostate());
            }
        }
    }
}

class Transition2 {

    private char symbol;
    private int tostate;

    public Transition2( char symbol , int tostate){

        this.symbol= symbol;
        this.tostate= tostate;
    }

    public char getSymbol() { return symbol; }

    public void setSymbol(char symbol) { this.symbol = symbol; }

    public int getTostate() { return tostate; }

    public void setTostate(int tostate) { this.tostate = tostate; }

}

class RegEx3 {
    static final int CONCAT = 0xC04CA7;
    static final int ETOILE = 0xE7011E;
    static final int ALTERN = 0xA17E54;
    static final int PROTECTION = 0xBADDAD;
}

class RegExTree3 {
    protected int root;
    protected ArrayList<RegExTree3> subTrees;

    public RegExTree3(int root, ArrayList<RegExTree3> subTrees) {
        this.root = root;
        this.subTrees = subTrees;
    }
}


