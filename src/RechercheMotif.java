import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;


public class RechercheMotif {

    // Algo de KMP pour chercher toutes les occurrences d'un motif dans un texte
    public static List<Integer> KMPalgo(String txt , String motif){

        List<Integer> occurrences = new ArrayList<>();
        int t = txt.length();
        int m = motif.length();

        //création de lps[] == le plus long prefixe-suffixe
        int[] lps = new int[m];
        int j = 0;

        calculerLPS(motif, m, lps);

        int i = 0;
        while (i < t) {
            if (motif.charAt(j) == txt.charAt(i)) {
                j++;
                i++;
            }
            if (j == m) {
                occurrences.add(i - j); // Store occurrence index
                j = lps[j - 1];
            } else if (i < t && motif.charAt(j) != txt.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i = i + 1;
                }
            }
        }
        return occurrences;

    }

    public static void calculerLPS(String motif, int m, int[] lps) {
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < m) {
            if (motif.charAt(i) == motif.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = len;
                    i++;
                }
            }
        }
    }


    // Algo naif pour chercher toutes les occurrences d'un motif dans un texte
    public static List<Integer> naivealgo(String txt, String motif) {
        List<Integer> occurrences = new ArrayList<>();
        int t = txt.length();
        int m = motif.length();

        for (int i = 0; i <= t - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (txt.charAt(i + j) != motif.charAt(j)) {
                    break;
                }
            }
            if (j == m) {
                occurrences.add(i);  // Store occurrence index
            }
        }
        return occurrences;  // Return all occurrences
    }

    // focntion qui calcule la durée d'execution pour chaque algo
    public static long duree(String txt, String motif, boolean useKMP) {
        long startTime = System.nanoTime();

        if (useKMP) {
            KMPalgo(txt, motif);  // Use KMP algorithm
        } else {
            naivealgo(txt, motif);  // Use naive algorithm
        }

        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // fonction pour lire le contenu d'un fichier comme un string
    public static String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java PatternSearchComparison <file_path> <pattern>");
            return;
        }

        String filePath = args[0];
        String pattern = args[1];

        try {
            // lire le contenu du fichier
            String text = readFileAsString(filePath);

            // Mesurer la duree d'execution pour KMP
            long kmpTime = duree(text, pattern, true);
            List<Integer> kmpOccurrences = KMPalgo(text, pattern);
            System.out.println("Temps d'exécution de KMP : " + kmpTime + " nanosecondes");
            System.out.println("Le motif a été trouvé " + kmpOccurrences.size() + " fois avec KMP.");

            // Mesurer la duree d'execution pour la methode naive
            long naiveTime = duree(text, pattern, false);
            List<Integer> naiveOccurrences = naivealgo(text, pattern);
            System.out.println("Temps d'exécution de la méthode naïve : " + naiveTime + " nanosecondes");
            System.out.println("Le motif a été trouvé " + naiveOccurrences.size() + " fois avec la méthode naïve.");

            // Comparer les resultats
            if (kmpOccurrences.equals(naiveOccurrences)) {
                System.out.println("Les deux méthodes donnent le même résultat.");
            } else {
                System.out.println("Les résultats sont différents !");
            }

            // afficher les lignes ou le motif apparait
            String[] lines = text.split("\n");
            System.out.println("\nLes lignes où le motif est trouvé :");
            for (int occurrence : kmpOccurrences) {
                // chercher la ligne qui contient les occurrences
                int lineIndex = 0;
                int charCount = 0;
                for (String line : lines) {
                    charCount += line.length() + 1;
                    if (charCount > occurrence) {
                        System.out.println("Motif trouvé à la ligne " + (lineIndex + 1) + " : " + line);
                        break;
                    }
                    lineIndex++;
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }
}
