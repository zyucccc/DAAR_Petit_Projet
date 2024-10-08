import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Automate {
    //unique id pour chaque instance de automate
    protected static final AtomicInteger unique_State_id = new AtomicInteger(0);
    private State debut_State;
    //final_States ou "accpeting states" sont les etats où l'automate peut s'arreter
    protected Set<State> final_States;

    public int generer_Unique_State_id(){
        return unique_State_id.getAndIncrement();
    }

    public Automate (RegExTree tree){
        Automate automate = parse(tree);
        this.debut_State = automate.debut_State;
        this.final_States = automate.final_States;
    }

    public Automate (){
    }

    public Automate parse(RegExTree tree) {
// -----------------------------ALTERN--------------------------------------
        if (tree.root == RegEx.ALTERN) {
            Automate left = parse(tree.subTrees.get(0));
            Automate right = parse(tree.subTrees.get(1));

            //creer un nouveau etat debut pour les 2 etats de altern
            State newDebut = new State(generer_Unique_State_id());
            newDebut.ajouterEpsilonTransition(left.debut_State);
            newDebut.ajouterEpsilonTransition(right.debut_State);

            State newFinal = new State(generer_Unique_State_id());
            newFinal.isFinal = true;
            //mise a jour des etats finaux pour les 2 sous_automates
            for(State final_State : left.final_States){
                final_State.isFinal = false;
                final_State.ajouterEpsilonTransition(newFinal);
            }

            for(State final_State : right.final_States){
                final_State.isFinal = false;
                final_State.ajouterEpsilonTransition(newFinal);
            }

            Automate result = new Automate();
            result.debut_State = newDebut;
            result.final_States = Set.of(newFinal);
            return result;
        }
// -----------------------------CONCAT--------------------------------------
        else if (tree.root == RegEx.CONCAT){
            Automate left = parse(tree.subTrees.get(0));
            Automate right = parse(tree.subTrees.get(1));
            //mise a jour les final_States
            for(State final_State : left.final_States){
                final_State.isFinal = false;
                //lier left et right
                final_State.ajouterEpsilonTransition(right.debut_State);
            }

            State newFinal_right = new State(generer_Unique_State_id());
            newFinal_right.isFinal = true;
            for(State final_State : right.final_States){
                final_State.isFinal = false;
                final_State.ajouterEpsilonTransition(newFinal_right);
            }

            Automate result = new Automate();
            result.debut_State = left.debut_State;
            result.final_States = Set.of(newFinal_right);
            return result;
        }
// ----------------------------ETOILE---------------------------------------
        else if (tree.root == RegEx.ETOILE){
            Automate left = parse(tree.subTrees.get(0));
            //mise a jour les debut_States
            State newDebut_left = new State(generer_Unique_State_id());
            newDebut_left.ajouterEpsilonTransition(left.debut_State);

            //mise a jour les final_States
            State newFinal_left = new State(generer_Unique_State_id());
            newFinal_left.isFinal = true;


            //newFinal_left.ajouterEpsilonTransition(left.debut_State);
            for(State final_State : left.final_States){
                final_State.isFinal = false;
                //dans le cas où on revient au debut de l'etoile pour repeter
                final_State.ajouterEpsilonTransition(left.debut_State);
                final_State.ajouterEpsilonTransition(newFinal_left);
            }


            //dans le cas où on saute directement a la fin de l'etoile
            newDebut_left.ajouterEpsilonTransition(newFinal_left);

            Automate result = new Automate();
            result.debut_State = newDebut_left;
            result.final_States = Set.of(newFinal_left);
            return result;
        }
// ----------------------------DOT---------------------------------------
        else if (tree.root == RegEx.DOT){
            State newDebut = new State(generer_Unique_State_id());
            State newFinal = new State(generer_Unique_State_id());
            newFinal.isFinal = true;
            //label of tansition
            newDebut.ajouterTransition((char)tree.root,newFinal);

            Automate result = new Automate();
            result.debut_State = newDebut;
            result.final_States = Set.of(newFinal);
            return result;
        }
        else {
            State newDebut = new State(generer_Unique_State_id());
            State newFinal = new State(generer_Unique_State_id());
            newFinal.isFinal = true;
            //label of tansition
            newDebut.ajouterTransition((char)tree.root,newFinal);

            Automate result = new Automate();
            result.debut_State = newDebut;
            result.final_States = Set.of(newFinal);
            return result;
        }
    }

    //----------------------------Getters-----------------------------//
    public State getDebut_State() {
        return this.debut_State;
    }


    // --------------------------toString----------------------------- //

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<State> visited = new HashSet<>();
        sb.append("Automate:\n");
        sb.append("Start State: ").append(debut_State.getId()).append("\n");
        sb.append("Final States: ").append(final_States.stream().map(State::getId).toList()).append("\n");
        sb.append("States:\n");
        printState(debut_State, sb, visited, 0);
        return sb.toString();
    }

    private void printState(State state, StringBuilder sb, Set<State> visited, int indent) {
        if (visited.contains(state)) {
            sb.append("  ".repeat(indent)).append("State ").append(state.getId()).append(" (already visited)\n");
            return;
        }
        visited.add(state);

        sb.append("  ".repeat(indent)).append("State ").append(state.getId()).append(":\n");
        for (Map.Entry<Character, Set<State>> entry : state.getTransitions().entrySet()) {
            sb.append("  ".repeat(indent + 1)).append("Via label '").append(entry.getKey()).append("' aller à: ");
            sb.append(entry.getValue().stream().map(State::getId).toList()).append("\n");
        }
        if (!state.getEpsilonTransitions().isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Epsilon transitions to: ");
            sb.append(state.getEpsilonTransitions().stream().map(State::getId).toList()).append("\n");
        }

        for (State nextState : state.getEpsilonTransitions()) {
            printState(nextState, sb, visited, indent + 2);
        }
        for (Set<State> nextStates : state.getTransitions().values()) {
            for (State nextState : nextStates) {
                printState(nextState, sb, visited, indent + 2);
            }
        }
    }

    // ----------------------convertir à Dot file--------------------- //
    // Bash commande:::  dot -Tpng automate.dot -o automate.png
    public void toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Automate {\n");
        sb.append("  rankdir=LR;\n"); // Left to right
        sb.append("  node [shape=circle];\n");

        Set<Integer> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        queue.add(debut_State);

        // start state
        sb.append("  start [shape=point];\n");
        sb.append("  start -> ").append(debut_State.getId()).append(";\n");

        // final states
        for (State acceptState : final_States) {
            sb.append("  ").append(acceptState.getId()).append(" [shape=doublecircle];\n");
        }

        while (!queue.isEmpty()) {
            State state = queue.poll();
            if (visited.contains(state.getId())) {
                continue;
            }
            visited.add(state.getId());

            // transitions
            for (Map.Entry<Character, Set<State>> entry : state.getTransitions().entrySet()) {
                char label = entry.getKey();
                for (State dest : entry.getValue()) {
                    sb.append("  ").append(state.getId()).append(" -> ").append(dest.getId())
                            .append(" [label=\"").append(label).append("\"];\n");
                    queue.add(dest);
                }
            }

            // epsilon tansitions
            for (State dest : state.getEpsilonTransitions()) {
                sb.append("  ").append(state.getId()).append(" -> ").append(dest.getId())
                        .append(" [label=\"ε\"];\n");
                queue.add(dest);
            }
        }

        sb.append("}\n");

        //save dot file
        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get("./automate_out/"));
            // dot file output path
            java.io.FileWriter fw = new java.io.FileWriter("./automate_out/automate.dot");
            fw.write(sb.toString());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

//chaque State va stocker ses propres transtions a partir de lui meme
class State {
    private int id;
    //charactere dans l'arete et les states de destination de cette arete
    private Map<Character, Set<State>> transitions;
    //epislon transitions
    private Set<State> epsilon_transitions;
    //boolean qui indique si l'etat est final
    protected boolean isFinal = false;

    //Normal transitions
    public void ajouterTransition(char label,State dest){
        if (transitions.containsKey(label)){
            transitions.get(label).add(dest);
        }
        else {
            transitions.put(label,new HashSet<>(Set.of(dest)));
        }
    }
    public void ajouterTransition(char label,Set<State> dest){
        transitions.put(label,dest);
    }
    //Epsilon transitions
    public void ajouterEpsilonTransition(State dest){
        epsilon_transitions.add(dest);
    }

    public State(int id) {
        this.id = id;
        this.transitions = new HashMap<>();
        this.epsilon_transitions = new HashSet<>();
    }

    // ----------------------toString--------------------- //
    @Override
    public String toString() {
        return "State{" +
                "id=" + id +
                ", transitions=" + transitionsToString() +
                ", epsilon_transitions=" + epsilon_transitions +
                '}';
    }

    private String transitionsToString() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<Character, Set<State>> entry : transitions.entrySet()) {
            sb.append("label: ");
            sb.append(entry.getKey()).append(" States :[");
            for (State state : entry.getValue()) {
              sb.append(state.toString()).append(", ");
            }
            if (!entry.getValue().isEmpty()) {
                sb.setLength(sb.length() - 2);  // Remove last ","et espace
            }
            sb.append("], ");
        }
        if (!transitions.isEmpty()) {
            sb.setLength(sb.length() - 2);  // Remove last ", "
        }
        return sb.append("}").toString();
    }

    public int getId() {
        return this.id;
    }

    public Map<Character, Set<State>> getTransitions() {
        return this.transitions;
    }

    public Set<State> getEpsilonTransitions() {
        return this.epsilon_transitions;
    }

    // ----------------------equals & hashCode--------------------- //
    // pour realiser les comparaison correctement entre Set<State>
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;
        return id == state.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

