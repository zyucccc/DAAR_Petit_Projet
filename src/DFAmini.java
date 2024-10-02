import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//---------------------Logique de Minimisation de DFA---------------------//
//Reference: Page 555 Acho et Ullman
//1.Basic:  si l'un state est state final,l'autre n'est pas,ces 2 states peuvent etre distinguer,on les indique comme distinguable
//2.Pour les pairs_State qui ne sont pas encore indique comme distinguable,si leurs states suivantes sont indiques comme distinguable,alors ils sont distinguable
//3.Finalement,les paris qui ne sont pas encore indique comme distinguable sont les states qui sont equivalent,ils peuvent etre fusionner
//------------------------------------------------------------------------//
public class DFAmini {

    private static Map<Pair_DFA_State, Boolean> table_association;

    public DFAmini(){}

    public DFA minimize(DFA dfa) {
        DFA dfa_with_dead_state = addDeadState(dfa);
        table_association = new HashMap<>();
        try {
            init_table_association(dfa_with_dead_state, table_association);
        } catch (Exception e) {
            System.err.println("Error init table d'association");
        }
        return minimize(dfa_with_dead_state,table_association);
    }

    public DFA minimize(DFA dfa,Map<Pair_DFA_State, Boolean> table_association) {
        //---------------------completer le tableau d'association---------------------//
        //--Lois d'induction de minimisation de DFA à la page 555 Aho et Ullman--//
        boolean changed = true;
        while (changed) {
            changed = false;
            int debug = 0;
            for (Pair_DFA_State pair : table_association.keySet()) {
                debug++;
                //dans le cas où le pair n'est pas encore indique comme distinguable
                if (!table_association.get(pair)) {
                    DFA_State s1 = pair.getState1();
                    DFA_State s2 = pair.getState2();
                    boolean distinguable = false;
                    //on va voir si les states suivants sont distinguable
                    for (Character symbol : dfa.getAllInputSymbols()) {
                        DFA_State next1 = s1.getTransitions().get(symbol);
                        DFA_State next2 = s2.getTransitions().get(symbol);
                        Pair_DFA_State nextPair = new Pair_DFA_State(next1, next2);
                        //dès que on trouve un cas où les states suivants sont distinguable,alors les states actuels sont distinguable,on break le boucle
                        if (next1 == null || next2 == null) {
                            continue; // skip null
                        }
                        if ((next1 == null && next2 != null) || (next1 != null && next2 == null)) {
                            table_association.put(pair, true);
                            changed = true;
                            break;
                        }

                        // Boolean.TRUE.equals => traiter null
                        if (Boolean.TRUE.equals(table_association.get(nextPair))) {
                            distinguable = true;
                            break;
                        }
                    }

                    if (distinguable) {
                        table_association.put(pair, true);
                        changed = true;
                    }
                }
            }
        }
        System.out.println("table_association: " + table_association);

        //---------------------Fusionner les states equivalent,Build new DFA mini---------------------//
        return build_DFAmini(dfa, table_association);
    }

    public void init_table_association(DFA dfa, Map<Pair_DFA_State, Boolean> table_association) {
        //--------------------------Initialisation du tableau d'association--------------------------//
        //--Lois de basic de minimisation de DFA à la page 555 Aho et Ullman--//
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

    private Map<DFA_State,Set<DFA_State>> create_equivalent_classes(DFA dfa,Map<Pair_DFA_State, Boolean> table_association){
        Map<DFA_State,Set<DFA_State>> equivalent_classes = new HashMap<>();
        //initialiser les classes equivalentes (creer une classe pour chaque state,ajouter le state-meme dans sa classe)
        for(DFA_State state : dfa.getAllStates()){
            Set<DFA_State> propre_equivalent_class = new HashSet<>();
            propre_equivalent_class.add(state);
            equivalent_classes.put(state,propre_equivalent_class);
        }

        //traiter les pairs qui est equivalent dans le tableau d'association
        for(Map.Entry<Pair_DFA_State,Boolean> entry : table_association.entrySet()){
            Pair_DFA_State pair = entry.getKey();
            boolean isDistinguable = entry.getValue();
            //dès que on trouve un pair qui est equivalent,alors on fusionne les classes equivalentes
            if(!isDistinguable){
                DFA_State state1 = pair.getState1();
                DFA_State state2 = pair.getState2();
                Set<DFA_State> equivalent_class1 = equivalent_classes.get(state1);
                Set<DFA_State> equivalent_class2 = equivalent_classes.get(state2);
                //fusionner les classes equivalentes
                equivalent_class1.addAll(equivalent_class2);
                for(DFA_State state : equivalent_class2){
                    equivalent_classes.put(state,equivalent_class1);
                }
            }

        }
        return equivalent_classes;
    }

    //creer un mapping entre les classes equivalentes et les states representatifs
    private Map<Set<DFA_State>,DFA_State> create_state_fusionee(DFA dfa,Map<DFA_State,Set<DFA_State>> equivalent_classes){
        Map<Set<DFA_State>,DFA_State> map_state_fusionee = new HashMap<>();
        Set<Set<DFA_State>> processed_Classes = new HashSet<>();

        for(Set<DFA_State> equivalent_class : equivalent_classes.values()){
            if (processed_Classes.contains(equivalent_class)) {
                continue; // si ce set est deja traite,skip
            }
            processed_Classes.add(equivalent_class);
            //choisir un state representatif pour chaque classe equivalent
            DFA_State representative = equivalent_class.iterator().next();
            map_state_fusionee.put(equivalent_class,representative);
        }
        return map_state_fusionee;
    }

    private DFA build_DFAmini(DFA dfa_withDeadState,Map<Pair_DFA_State, Boolean> table_association){
        //creer les classes equivalentes
        Map<DFA_State,Set<DFA_State>> equivalent_classes = create_equivalent_classes(dfa_withDeadState,table_association);
        //fusionner les classes equivalentes
        Map<Set<DFA_State>,DFA_State> map_state_fusionee = create_state_fusionee(dfa_withDeadState,equivalent_classes);

        //construire le map/mapping entre old state et new state (old -> new)
        Map<DFA_State,DFA_State> map_old_new = new HashMap<>();
        for(DFA_State state : dfa_withDeadState.getAllStates()){
            if(state.getId() == -1)
                continue; // skip dead state

            //recuperer le state representatif (ou new state)
            Set<DFA_State> propre_equivalent_class = equivalent_classes.get(state);
            DFA_State representative = map_state_fusionee.get(propre_equivalent_class);
            map_old_new.put(state,representative);
        }
//        System.err.println("Ca passe!");
        //debut/final state
        Set<DFA_State> newStates = new HashSet<>(map_old_new.values());
        Set<DFA_State> new_FinalStates = new HashSet<>();
        DFA_State new_StartState = map_old_new.get(dfa_withDeadState.debut_State);
        delete_transition_toDeadState(new_StartState);

        //mise a jour des final states
        for (DFA_State state : newStates) {
            if (state.isFinal) {
                new_FinalStates.add(state);
            }
        }
        //build new DFAmini
        for(DFA_State old_state : dfa_withDeadState.getAllStates()){
            if(old_state.getId() == -1)
                continue; // skip dead state

            delete_transition_toDeadState(old_state);

            DFA_State new_state = map_old_new.get(old_state);
            for(Map.Entry<Character,DFA_State> entry : old_state.getTransitions().entrySet()){
                Character symbol = entry.getKey();
                DFA_State next_state = entry.getValue();
                if (next_state.getId() == -1) {
                    continue;//skip dead state
                }
                DFA_State new_next_state = map_old_new.get(next_state);
                new_state.getTransitions().put(symbol,new_next_state);
            }
        }

        return new DFA(new_StartState,new_FinalStates);
    }

    private void delete_transition_toDeadState(DFA_State state){
        Set<Character> symbolsToRemove = new HashSet<>();

        for (Map.Entry<Character, DFA_State> entry : state.getTransitions().entrySet()) {
            Character symbol = entry.getKey();
            DFA_State next_state = entry.getValue();
            if (next_state.getId() == -1) {
                symbolsToRemove.add(symbol);
            }
        }

        for (Character symbol : symbolsToRemove) {
            state.getTransitions().remove(symbol);
        }
    }

    public DFA addDeadState(DFA dfa) {
        Set<Character> inputSymbols = dfa.getAllInputSymbols();
//        System.err.println("inputSymbols: " + inputSymbols);

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

    // ----------------------convertir à Dot file--------------------- //
    // Bash commande:::  dot -Tpng automateDFA.dot -o automateDFA.png
    public void toDot(DFA dfa_mini) {
        DFA_State debut_State = dfa_mini.debut_State;
        Set<DFA_State> final_States = dfa_mini.final_States;

        StringBuilder sb = new StringBuilder();
        sb.append("digraph Automate {\n");
        sb.append("  rankdir=LR;\n"); // Left to right
        sb.append("  node [shape=circle];\n");

        Set<Integer> visited = new HashSet<>();
        Queue<DFA_State> queue = new LinkedList<>();
        queue.add(debut_State);

        // start state
        sb.append("  start [shape=point];\n");
        sb.append("  start -> ").append(debut_State.getId()).append(";\n");

        // final states
        for (DFA_State acceptState : final_States) {
            sb.append("  ").append(acceptState.getId()).append(" [shape=doublecircle];\n");
        }

        while (!queue.isEmpty()) {
            DFA_State state = queue.poll();
            if (visited.contains(state.getId())) {
                continue;
            }
            visited.add(state.getId());

            // transitions
            for (Map.Entry<Character, DFA_State> entry : state.getTransitions().entrySet()) {
                char label = entry.getKey();
                DFA_State dest = entry.getValue();
                sb.append("  ").append(state.getId()).append(" -> ").append(dest.getId())
                        .append(" [label=\"").append(label).append("\"];\n");
                queue.add(dest);

            }

        }

        sb.append("}\n");

        //save dot file
        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get("./automate_out/"));
            // dot file output path
            java.io.FileWriter fw = new java.io.FileWriter("./automate_out/automateDFA_MINI.dot");
            fw.write(sb.toString());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//--------------------------------------------Pair_DFA_State--------------------------------------------//
//////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        int hash1 = (state1 != null) ? state1.hashCode() : 0;
        int hash2 = (state2 != null) ? state2.hashCode() : 0;
        return hash1 + hash2;
    }
}
