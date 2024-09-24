import java.util.Map;
import java.util.Set;


public class Automate {
    protected int unique_State_id = 0;
    protected State debut;
    protected Set<State> finaux;

    public int generer_Unique_State_id(){
        return unique_State_id++;
    }

//    public Automate

    public State parse(RegExTree tree){
        State state = new State(generer_Unique_State_id());

        return state;
    }


    @Override
    public String toString() {
        return super.toString();
    }
}

//chaque State va stocker ses propres transtions a partir de lui meme
class State {
    protected int id;
    //charactere dans l'arete et les states de destination de cette arete
    protected Map<Character, Set<State>> transitions;


    public void ajouterTransition(char label,State dest){
        transitions.put(label,Set.of(dest));
    }
    public void ajouterTransition(char label,Set<State> dest){
        transitions.put(label,dest);
    }

    public State(int id) {
        this.id = id;
    }
}
