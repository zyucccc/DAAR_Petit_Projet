import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    //MAIN
    public static void main(String arg[]) {
        System.out.println("Welcome to the RegEx parser.");
        String regEx;
        if (arg.length!=0) {
            regEx = arg[0];
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("  >> Please enter a regEx: ");
            regEx = scanner.next();
        }
        System.out.println("  >> Parsing regEx \""+regEx+"\".");
        System.out.println("  >> ...");

        if (regEx.length()<1) {
            System.err.println("  >> ERROR: empty regEx.");
        } else {
            System.out.print("  >> ASCII codes: ["+(int)regEx.charAt(0));
            for (int i=1;i<regEx.length();i++) System.out.print(","+(int)regEx.charAt(i));
            System.out.println("].");
            try {
                RegEx reg = new RegEx(regEx);
                RegExTree ret = reg.parse();
                System.out.println("  >> Tree result: "+ret.toString()+".");
                System.out.println("  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println("  Automate start: ");
                Automate automate = new Automate(ret);
                automate.toDot();
                System.out.println("  Automate result: \n"+automate.toString());

                System.out.println("  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println("  DFA start: ");
                DFA dfa = new DFA(automate);
                dfa.toDot();
                System.out.println("  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println("  DFA Minimisation start: ");
                DFAmini mini = new DFAmini();
                DFA DFA_minimisation = mini.minimize(dfa);
                mini.toDot(DFA_minimisation);

                System.out.println("Bravo! Success de construire l'automate");
                System.out.println("Maintenant veuillez entre le path de texte à traiter avec l'automate");
                Scanner scanner2 = new Scanner(System.in);
                String path = scanner2.nextLine();
                System.out.println("Le path est: " + path);

                //pour valider que le resultat soit correcte,on peut faire egrep simultanement
                //egrep -c 'pattern' test.txt
                try {
                    // record start time
                    long startTime = System.currentTimeMillis();

                    // open file et lire ligne par ligne
                    List<String> lines = Files.readAllLines(Paths.get(path));
                    int matchedLines = 0;

                    for (String line : lines) {
                        if (DFA_minimisation.traite_texte(line)) {
                            System.out.println(line);
                            matchedLines++;
                        }
                    }

                    // record end time
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    // synthese
                    System.out.println("Total matching lines: " + matchedLines);
                    System.out.println("Total time taken: " + duration + " milliseconds");

                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier: " + e.getMessage());
                }


            } catch (Exception e) {
                System.err.println(">> ERROR:  "+e.toString());
            }
        }

        System.out.println("  >> ...");
        System.out.println("  >> Parsing completed.");
    }

}
