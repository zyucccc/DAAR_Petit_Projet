import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        // le chemin du fichier texte
        String filePath = "/Users/ghitamikou/Desktop/test.txt";

        // les motifs à chercher
        List<String> motifs = new ArrayList<>();
        motifs.add("from");
        motifs.add("crucial");
        motifs.add("crucial");

        try {
            // Lire le contenu du fichier
            String text = RechercheMotif.readFileAsString(filePath);

            // Créer un fichier CSV pour stocker les résultats
            String csvFile = "durations.csv";

            // Chercher plusieurs motifs avec KMP et stocker les résultats dans le CSV
            RechercheMotif.chercherPlusieursMotifs(text, motifs, csvFile);

            // Appeler le script Python pour générer le diagramme
            String pythonCommand = "python3 generate_chart.py " + csvFile;
            Runtime.getRuntime().exec(pythonCommand);

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }
}
