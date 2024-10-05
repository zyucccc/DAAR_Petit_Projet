import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DFA {
    //unique id pour chaque instance de automate
    //pour distinguer avec celui de Automate,nous commencons par 100
    protected static final AtomicInteger unique_State_id = new AtomicInteger(100);
    protected DFA_State debut_State;
    //final_States ou "accpeting states" sont les etats où l'automate peut s'arreter
    protected Set<DFA_State> final_States;
    private Map<Set<State>,DFA_State> dfa_registre = new HashMap<>();


    //------------------------------Constructeur-----------------------------------//
    public DFA(DFA_State debut_State, Set<DFA_State> final_States){
        this.debut_State = debut_State;
        this.final_States = final_States;
    }

    public DFA(Automate nfa){
        DFA dfa = transformer_fromNFA(nfa);
        this.debut_State = dfa.debut_State;
        this.final_States = dfa.final_States;
        this.dfa_registre = dfa.dfa_registre;
    }

    public DFA(){
        this.debut_State = null;
        this.final_States = new HashSet<>();
    }

    //unique id pour chaque instance de automate
    public int generer_Unique_State_id(){
        return unique_State_id.getAndIncrement();
    }

    //-------------------------------Trainter texte-----------------------------------//
    //Traiter le texte (une ligne) avec l'automate
    public boolean traite_texte(String texte) {
        for (int startIndex = 0; startIndex < texte.length(); startIndex++) {
            DFA_State current_state = debut_State;
            for (int i = startIndex; i < texte.length(); i++) {
                char c = texte.charAt(i);
                if (!current_state.getTransitions().containsKey(c)) {
                    break;
                }
                current_state = current_state.getTransitions().get(c);
                if (current_state.isFinal) {
                    return true;  // des que on trouve une occurence de pattern, on retourne true
                }
            }
        }
        return false;
    }

    //-------------------------------Getter-----------------------------------//
    public Map<Set<State>,DFA_State> getDfa_registre() {
        return this.dfa_registre;
    }

    public Set<DFA_State> getAllStates(){
        Set<DFA_State> allStates = new HashSet<>();
        for(DFA_State state : dfa_registre.values()){
//            System.err.println("ajouteId:"+state.getId());
            allStates.add(state);
        }
        return allStates;
    }

    public Set<Character> getAllInputSymbols() {
        Set<Character> symbols = new HashSet<>();
        for (DFA_State state : getAllStates()) {
            symbols.addAll(state.getTransitions().keySet());
        }
        return symbols;
    }

    //-------------------------------Updater-----------------------------------//
    public void add_registre(DFA_State state){
        this.dfa_registre.put(state.getSubStates(),state);
    }


    //-------------------------------Methode de transformer l'automate NFA à DFA---------------------------------//
    protected DFA transformer_fromNFA(Automate nfa){
        //registre globale pour stocker les "etat" de DFA_State
        //ex: Pour chaque subset de states de NFA, on a un DFA_State unique
        Map<Set<State>,DFA_State> dfa_registre = new HashMap<>();
        //worklist pour les states de DFA à traiter
        Set<DFA_State> worklist = new HashSet<>();

        //calculer le premier state de DFA à partir de l'ensemble de l'episilon-closure de state de debut de NFA
        DFA_State dfa_debut_state = reduire_Episilon_Closutre(nfa.getDebut_State(),dfa_registre);
        //stocker l'info de cette state_DFA dans le registre
        dfa_registre.put(dfa_debut_state.getSubStates(), dfa_debut_state);

        Set<DFA_State> dfa_final_states = new HashSet<>();
        if(dfa_debut_state.isFinal){
            dfa_final_states.add(dfa_debut_state);
        }

        Map<Character, Set<State>> transitions_subState = parse(dfa_debut_state);

        //calculer les states suivants et mettre à jour le registre
        for (Character label : transitions_subState.keySet()){
            Set<State> next_subSet = transitions_subState.get(label);
            DFA_State next_dfa_state = reduire_Episilon_Closutre(next_subSet,dfa_registre);
            if(!dfa_registre.containsKey(next_dfa_state.getSubStates())) {
                dfa_registre.put(next_dfa_state.getSubStates(), next_dfa_state);

                if (next_dfa_state.isFinal) {
                    dfa_final_states.add(next_dfa_state);
                }
                dfa_debut_state.getTransitions().put(label, next_dfa_state);
                worklist.add(next_dfa_state);
            }else {
                DFA_State existing_dfa_state = dfa_registre.get(next_dfa_state.getSubStates());
                dfa_debut_state.getTransitions().put(label, existing_dfa_state);
            }
        }

        //traiter le workliste
        while (!worklist.isEmpty()){
            //pop un state_DFA de worklist et le traiter
            DFA_State current_dfa_state = worklist.iterator().next();
            worklist.remove(current_dfa_state);

            transitions_subState = parse(current_dfa_state);
            for (Character label : transitions_subState.keySet()){
                Set<State> next_subSet = transitions_subState.get(label);
                DFA_State next_dfa_state = reduire_Episilon_Closutre(next_subSet,dfa_registre);

                //si la state generée n'est pas dans le registre, on le traite et ajoute
                if(!dfa_registre.containsKey(next_dfa_state.getSubStates())) {
                    dfa_registre.put(next_dfa_state.getSubStates(), next_dfa_state);

                    //check si cet nouveau state est final,si oui on ajoute dans dfa_final_states
                    if (next_dfa_state.isFinal) {
                        dfa_final_states.add(next_dfa_state);
                    }
                    //mise a jour les transitions de current_dfa_state
                    current_dfa_state.getTransitions().put(label, next_dfa_state);
                    //ajouter les nouveaux states à la worklist
                    if (!worklist.contains(next_dfa_state)){
                        worklist.add(next_dfa_state);
                    }
                    //sinon,si une state_dfa est deja registre dans le registre pour cette ensemble de states nfa
                    //on utilise celui existant dans le registre
                }else {
                    DFA_State existing_dfa_state = dfa_registre.get(next_dfa_state.getSubStates());
                    current_dfa_state.getTransitions().put(label, existing_dfa_state);
                }
            }
        }

        DFA dfa = new DFA(dfa_debut_state, dfa_final_states);
        int size = dfa_registre.size();
        dfa.dfa_registre = dfa_registre;
        return dfa;
    }

    //calculer l'ensemble de l'episilon-closure etant donnee une seule state NFA
    protected DFA_State reduire_Episilon_Closutre(State state,Map<Set<State>,DFA_State> dfa_registre){
        DFA_State dfa_state = new DFA_State(generer_Unique_State_id());

        Set<State> closure = new HashSet<>();
        Stack<State> stack = new Stack<>();
        closure.add(state);
        stack.push(state);

        //ajouter tous les states atteignables par epsilon transitions recursivement
        while(!stack.isEmpty()){
            State s = stack.pop();
//            System.err.println("state:"+s.getId()+"for :"+s.isFinal);
            if(s.isFinal && !dfa_state.isFinal) {
//                System.err.println("final state:" + s.getId()+" for "+dfa_state.getId());
                dfa_state.isFinal = true;
            }

            for(State e : s.getEpsilonTransitions()){
                if(!closure.contains(e)){
                    closure.add(e);
                    stack.push(e);
                }
            }
        }

        dfa_state.setSubStates(closure);
        if(dfa_registre.containsKey(closure)){
            return dfa_registre.get(closure);
        }
        return dfa_state;
    }

    //calculer l'ensemble de l'episilon-closure etant donnee une ensemble de states NFA
    protected DFA_State reduire_Episilon_Closutre(Set<State> states,Map<Set<State>,DFA_State> dfa_registre){
        DFA_State dfa_state = new DFA_State(generer_Unique_State_id());

        Set<State> closure = new HashSet<>(states);
        Stack<State> stack = new Stack<>();
        for(State state : states)
            stack.push(state);

        //ajouter tous les states atteignables par epsilon transitions recursivement
        while(!stack.isEmpty()){
            State s = stack.pop();
//            System.err.println("state:"+s.getId()+"for :"+s.isFinal);
            if(s.isFinal && !dfa_state.isFinal) {
//                System.err.println("final state:" + s.getId() + " for " + dfa_state.getId());
                dfa_state.isFinal = true;
            }

            for(State e : s.getEpsilonTransitions()){
                if(!closure.contains(e)){
                    closure.add(e);
                    stack.push(e);
                }
            }
        }

        dfa_state.setSubStates(closure);
        if(dfa_registre.containsKey(closure)){
            return dfa_registre.get(closure);
        }
        return dfa_state;
    }

    //parser une state NFA,calculer les transitions possibles
    protected Map<Character,Set<State>> parse(DFA_State dfa_state){
        Set<Character> labels = dfa_state.get_labels();
        Map<Character, Set<State>> transitions_subState = new HashMap<>();
        for (Character label : labels) {
            Set<State> next_subSet = new HashSet<>();
            for (State state : dfa_state.getSubStates()) {
                if (state.getTransitions().containsKey(label)){
                    for (State dest : state.getTransitions().get(label)){
                        next_subSet.add(dest);
                    }
                }

            }
            if(next_subSet.isEmpty()){ continue; }
            transitions_subState.put(label, next_subSet);
        }

        return transitions_subState;
    }

    // ----------------------convertir à Dot file--------------------- //
    // Bash commande:::  dot -Tpng automateDFA.dot -o automateDFA.png
    public void toDot() {
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
            java.io.FileWriter fw = new java.io.FileWriter("./automate_out/automateDFA.dot");
            fw.write(sb.toString());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class DFA_State {
    private int id;
    //charactere dans l'arete et les states de destination de cette arete
    private Map<Character, DFA_State> transitions;

    private Set<State> subStates;
    //boolean qui indique si l'etat est final
    protected boolean isFinal = false;

    //------------------------------Constructeur-----------------------------------//

    public DFA_State(int id){
        this.id = id;
        this.transitions = new HashMap<>();
        this.subStates = new HashSet<>();
    }

    public DFA_State(int id, Set<State> subStates){
        this.id = id;
        this.transitions = new HashMap<>();
        this.subStates = subStates;
    }


    //get all possible labels from the substates
    public Set<Character> get_labels(){
        Set<Character> labels = new HashSet<>();
        for (State state : subStates){
            for (Character label : state.getTransitions().keySet()){
                labels.add(label);
            }
        }
        return labels;
    }



    //----------------------------Updaters-----------------------------//
    //Normal transitions
    public void ajouterTransition(char label,DFA_State dest){
        transitions.put(label,dest);
    }

    public void setTransitions(Map<Character, DFA_State> transitions) {
        this.transitions = transitions;
    }

    public void setSubStates(Set<State> subStates) {
        this.subStates = subStates;
    }

    //----------------------------Getters-----------------------------//
    public int getId() {
        return this.id;
    }

    public Set<State> getSubStates() {
        return this.subStates;
    }

    public Map<Character, DFA_State> getTransitions() {
        return this.transitions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DFA_State other = (DFA_State) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}