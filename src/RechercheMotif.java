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

    public static void chercherPlusieursMotifs(String txt, List<String> motifs, boolean useKMP, String outputFile) throws IOException{

        FileWriter writer = new FileWriter(outputFile);

        // Écrire l'en-tête du fichier CSV
        writer.append("Motif,Temps d'exécution (nanosecondes)\n");

        for (String motif : motifs) {
            System.out.println("\nRecherche du motif : " + motif);

            long execTime = duree(txt, motif, useKMP);
            List<Integer> occurrences;

            if (useKMP) {
                occurrences = KMPalgo(txt, motif);
                System.out.println("Temps d'exécution de KMP : " + execTime + " nanosecondes");
            } else {
                occurrences = naivealgo(txt, motif);
                System.out.println("Temps d'exécution de l'algorithme naïf : " + execTime + " nanosecondes");
            }

            System.out.println("Le motif '" + motif + "' a été trouvé " + occurrences.size() + " fois.");

            // Écrire les résultats dans le fichier CSV
            writer.append(motif).append(",").append(Long.toString(execTime)).append("\n");

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


    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java RechercheMotif <file_path> <motif1> <motif2> ... <motifN>");
            return;
        }

        String filePath = args[0];
        List<String> motifs = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            motifs.add(args[i]);
        }

        try {
            // Lire le contenu du fichier
            String text = readFileAsString(filePath);

            // Créer un fichier CSV pour stocker les résultats
            String csvFile = "durations.csv";

            // Chercher plusieurs motifs avec KMP et stocker les résultats dans le CSV
            chercherPlusieursMotifs(text, motifs, true, csvFile);

            // Appeler le script Python pour générer le diagramme
            String pythonCommand = "python3 generate_chart.py " + csvFile;
            Runtime.getRuntime().exec(pythonCommand);

          /*  // Chercher plusieurs motifs avec KMP
            chercherPlusieursMotifs(text, motifs, true);

            // Chercher plusieurs motifs avec la méthode naïve
            chercherPlusieursMotifs(text, motifs, false);*/


        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }
}

