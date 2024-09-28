import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFA {
}

class DFA_State {
    private int id;
    //charactere dans l'arete et les states de destination de cette arete
    private Map<Character, Set<State>> transitions;

    //Normal transitions
    public void ajouterTransition(char label,State dest){
        if (transitions.containsKey(label)){
            transitions.get(label).add(dest);
        }
        else {
            transitions.put(label,new HashSet<>(Set.of(dest)));
        }
    }

}