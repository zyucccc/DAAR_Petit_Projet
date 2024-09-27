import java.util.ArrayList;
import java.util.HashMap;

public class Automate {

    private int istate;
    private int astate;
    HashMap<Integer, ArrayList<Transition2>> transitions;

    public Automate(int istate, int fstate) {
        this.istate = istate;
        this.astate = fstate;
        this.transitions = new HashMap<>();
    }

    public int getIstate() { return istate; }

    public void setIstate(int istate) { this.istate = istate; }

    public int getFstate() { return astate; }

    public void setFstate(int fstate) { this.astate = fstate; }

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
        System.out.println("Final state is:"+astate);

        for(int state : transitions.keySet()){
            for(Transition2 t : transitions.get(state)){
                System.out.println("State"+state+"-----"+t.getSymbol()+"-----"+t.getTostate());
            }
        }
    }

    public Automate parse(RegExTree tree) {

        int stateCounter = 0;

        if(tree.root == RegEx.CONCAT){
            Automate leftConcat = parse(tree.subTrees.get(0));
            Automate rightConcat = parse(tree.subTrees.get(1));

            //Ajouter une transition 'ε' entre les 2 automates
            leftConcat.addepsilontransition(leftConcat.astate , rightConcat.istate);

            return new Automate(leftConcat.istate , rightConcat.astate);

        }else if(tree.root == RegEx.ETOILE) {

            Automate middleAut = parse(tree.subTrees.get(0));
            int initialState = stateCounter++;
            int acceptState = stateCounter++;
            Automate etoileAut = new Automate(initialState , acceptState);

            //L'ajout des 'ε' transitions
            etoileAut.addepsilontransition(initialState , acceptState);
            etoileAut.addepsilontransition(initialState , middleAut.istate);
            etoileAut.addepsilontransition(middleAut.astate , middleAut.istate);
            etoileAut.addepsilontransition(middleAut.astate, acceptState);

            return etoileAut;
        } else if(tree.root == RegEx.ALTERN){

            Automate autR1 = parse(tree.subTrees.get(0));
            Automate autR2 = parse(tree.subTrees.get(1));
            int initialState = stateCounter++;
            int acceptState = stateCounter++;
            Automate alternAut = new Automate(initialState , acceptState);

            //L'ajout des 'ε' transitions
            alternAut.addepsilontransition(initialState , autR1.istate);
            alternAut.addepsilontransition(initialState , autR2.istate);
            alternAut.addepsilontransition(autR1.astate , acceptState);
            alternAut.addepsilontransition(autR2.astate, acceptState);

            return alternAut;
        }

        return null;
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



