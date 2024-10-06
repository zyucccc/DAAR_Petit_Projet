# DAAR_Petit_Projet

## Pour lancer le projet:
il y a un seul "main" dans le code,la premiere partie concerne strategie1 (Automate),la deuxieme concerne l'algo KMP.

1. Strategie Automate:
lancer le main,et taper le pattern on veut traiter dans le console. 
ex: a|bc* , S(a|r|l)*on.
La path de texte à traiter est fixe dans le code(Main): ./test.txt

### Pour la sortie,3 moyens d'analyse du resultat sont fournis:
- (Le plus recommandé):
  Les text fichiers .dot seront genere apres l'excution de code dans le dossier ./automate_out/
  Et vous pouvez generer les graphes depuis ces ficher .dot par commande:
  ~ automate_out % dot -Tpng automate.dot -o automate_NFA.png
  ~ automate_out % dot -Tpng automateDFA.dot -o automate_DFA.png 
  ~ automate_out % dot -Tpng automateDFA_MINI.dot -o automate_DFA_MINI.png

  Certains exemples (graphes) sont deja fournis dans ./automate_out/.  (a,ab,abc,a|bc*)

- Soit par le output de console (Systeme.out.println),mais attention,y aura beaucoup de output dans le console des que l'execution du code,y compris nfa,dfa,dfa_mini,kmp
  
- Soit par les fichers .csv.Les syntheses de resultat seront stockés dans "./performance_results_automate.csv".

2. Strategie KMP:
le traitement de KMP est suivi de traitement de automate.
La path de texte à traiter est fixe dans le code(Main): ./test.txt
Les motifs sont egalement fixes dans le code(Main):
// les motifs à chercher
        List<String> motifs = new ArrayList<>();
        motifs.add("from");
        motifs.add("Another");
        motifs.add("Paris");
        motifs.add("Egypt");
        motifs.add("Sargon");

Si vous voulez,vous pouvez les modifier manuellement.

### Pour la sortie,2 moyens d'analyse du resultat est fourni:
- Soit par le output de console (Systeme.out.println),mais attention,y aura beaucoup de output dans le console des que l'execution du code,y compris nfa,dfa,dfa_mini,kmp
  
- Soit par les fichers .csv.Les syntheses de resultat seront stockés dans "./performance_results_KMP.csv".


### script py
D'ailleurs,nous avons ecris les script py qui aide à generer les graphes.Certains graphes sont deja fournis dans la repertoire racine.