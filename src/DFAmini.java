import java.util.*;

//---------------------Logique de Minimisation de DFA---------------------//
//Reference: Page 555 Acho et Ullman
//1.Basic:  si l'un state est state final,l'autre n'est pas,ces 2 states peuvent etre distinguer,on les indique comme distingable
//2.Pour les pairs_State qui ne sont pas encore indique comme distingable,si leurs states suivantes sont indiques comme distingable,alors ils sont distingable
//3.Finalement,les paris qui ne sont pas encore indique comme distingable sont les states qui sont equivalent,ils peuvent etre fusionner
//------------------------------------------------------------------------//
public class DFAmini {

    private static Map<Pair_DFA_State, Boolean> table_association;

    public DFAmini(){}

    public DFAmini(DFA dfa) {
        table_association = new HashMap<>();
        try {
            init_table_association(dfa, table_association);
        } catch (Exception e) {
            System.err.println("Error init table d'association");
        }
        DFA dfa_with_dead_state = addDeadState(dfa);
        DFA dfaMini = minimize(dfa_with_dead_state, table_association);
    }

    public DFA minimize(DFA dfa, Map<Pair_DFA_State, Boolean> table_association) {

        return null;
    }

    public void init_table_association(DFA dfa, Map<Pair_DFA_State, Boolean> table_association) {
        Set<DFA_State> states = dfa.getAllStates();
        List<DFA_State> stateList = new ArrayList<>(states);

        for (int i = 0; i < stateList.size(); i++) {
            for (int j = i + 1; j < stateList.size(); j++) {
                DFA_State s1 = stateList.get(i);
                DFA_State s2 = stateList.get(j);

                Pair_DFA_State pair = new Pair_DFA_State(s1, s2);
                // si l'un est state final et l'autre n'est pas,donc ca veut dire que on peut les distinguer,on met（true）
                if (s1.isFinal != s2.isFinal) {
                    table_association.put(pair, true);
                } else {
                    table_association.put(pair, false);
                }
            }
        }
    }

    public DFA addDeadState(DFA dfa) {
        Set<Character> inputSymbols = dfa.getAllInputSymbols();
        System.err.println("inputSymbols: " + inputSymbols);

        Set<State> DEAD_Satate_Mark = Collections.emptySet();
        DFA_State deadState = new DFA_State(-1,DEAD_Satate_Mark); // id for dead state: -1

        // dead state has transition to itself on every input
        for (Character symbol : inputSymbols) {
            deadState.getTransitions().put(symbol, deadState);
        }

        // check every state's transition, add transition to dead state for the missing input
        for (DFA_State state : dfa.getAllStates()) {
            for (Character symbol : inputSymbols) {
                if (!state.getTransitions().containsKey(symbol)) {
                    state.getTransitions().put(symbol, deadState);
                }
            }
        }
        dfa.add_registre(deadState);

        return dfa;
    }



}

class Pair_DFA_State {
    private DFA_State state1;
    private DFA_State state2;

    public Pair_DFA_State(DFA_State state1, DFA_State state2) {
        this.state1 = state1;
        this.state2 = state2;
    }

    public DFA_State getState1() {
        return state1;
    }

    public DFA_State getState2() {
        return state2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Pair_DFA_State other = (Pair_DFA_State) obj;

        //n'importe quel ordre de state1 et state2
        //le meme contenu => pair1 == pairs
        return (objectsEqual(this.state1, other.state1) && objectsEqual(this.state2, other.state2)) ||
                (objectsEqual(this.state1, other.state2) && objectsEqual(this.state2, other.state1));
    }

    private boolean objectsEqual(Object o1, Object o2) {
        if (o1 == o2) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        return state1.hashCode() + state2.hashCode();
    }
}
