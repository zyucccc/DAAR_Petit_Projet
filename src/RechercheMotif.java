import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;

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
                occurrences.add(i - j); // Stocker l'indice de l'occurrence
                j = lps[j - 1];
            } else if (i < t && motif.charAt(j) != txt.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
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

    // fonction qui calcule la durée d'exécution pour KMP
    public static long duree(String txt, String motif) {
        long startTime = System.nanoTime();

        KMPalgo(txt, motif);  // Utiliser l'algorithme KMP

        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // fonction pour lire le contenu d'un fichier comme une chaîne de caractères
    public static String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    // Rechercher plusieurs motifs et écrire les résultats dans un fichier
    public static void chercherPlusieursMotifs(String txt, List<String> motifs, String outputFile) throws IOException {

        FileWriter writer = new FileWriter(outputFile);

        // Écrire l'en-tête du fichier CSV
        writer.append("Motif,Temps d'exécution (nanosecondes),Nombre d'occurrences\n");

        for (String motif : motifs) {
            System.out.println("\nRecherche du motif : " + motif);

            long execTime = duree(txt, motif);
            List<Integer> occurrences = KMPalgo(txt, motif);

            System.out.println("Temps d'exécution de KMP : " + execTime + " nanosecondes");
            System.out.println("Le motif '" + motif + "' a été trouvé " + occurrences.size() + " fois.");

            // Écrire les résultats dans le fichier CSV
            writer.append(motif).append(",").append(Long.toString(execTime)).append(",").append(Integer.toString(occurrences.size())).append("\n");

            // Afficher les lignes où le motif est trouvé
            String[] lines = txt.split("\n");
            System.out.println("\nLes lignes où le motif '" + motif + "' est trouvé :");
            for (int occurrence : occurrences) {
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
        }
        writer.flush();
        writer.close();
    }


}
