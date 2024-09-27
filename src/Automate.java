import java.util.*;


public class Automate {
    protected int unique_State_id = 0;
    protected State debut_State;

    public int generer_Unique_State_id(){
        return unique_State_id++;
    }

    public Automate (RegExTree tree){
        debut_State = parse(tree);
    }

    public State parse(RegExTree tree) {
        State state = new State(generer_Unique_State_id());
        if (tree.root == RegEx.ALTERN) {
            State left = parse(tree.subTrees.get(0));
            State right = parse(tree.subTrees.get(1));
            state.ajouterEpsilonTransition(left);
            state.ajouterEpsilonTransition(right);
        }
        else if (tree.root == RegEx.CONCAT){
            State left = parse(tree.subTrees.get(0));
            int label_left = tree.subTrees.get(0).root;
            State right = parse(tree.subTrees.get(1));
            int label_right = tree.subTrees.get(1).root;

            left.ajouterTransition((char)label_right,right);
            state.ajouterTransition((char)label_left,left);
        }
        else if (tree.root == RegEx.ETOILE){
            State left = parse(tree.subTrees.get(0));
            int label = tree.subTrees.get(0).root;
            state.ajouterTransition((char)label,left);
        }
        else if (tree.root == RegEx.DOT){
            State left = parse(tree.subTrees.get(0));
            int label = tree.subTrees.get(0).root;
            state.ajouterTransition((char)label,left);
        }
        else {
            state.ajouterTransition((char)tree.root,new State(generer_Unique_State_id()));
        }
        return state;
    }


    @Override
    public String toString() {
        return "Automate{ \n" +
                "debut_State= \n" + debut_State +
                " \n}";
    }
}

//chaque State va stocker ses propres transtions a partir de lui meme
class State {
    protected int id;
    //charactere dans l'arete et les states de destination de cette arete
    protected Map<Character, Set<State>> transitions;
    //epislon transitions
    protected Set<State> epsilon_transitions;

    //Normal transitions
    public void ajouterTransition(char label,State dest){
        transitions.put(label,Set.of(dest));
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

    @Override
    public String toString() {
        return "State{" +
                "id=" + id +
                ", transitions=" + transitions +
                ", epsilon_transitions=" + epsilon_transitions +
                '}';
    }
}
