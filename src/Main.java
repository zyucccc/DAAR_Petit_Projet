import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    //MAIN
    public static void main(String arg[]) {
        ///////////////////////////////////////////////////////////////////////////////////
        //-----------------------------------Automate------------------------------------//
        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("Methode 1: Automate >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
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
//                String path = scanner2.nextLine();
                String path = "./test.txt";
                System.out.println("Le path est: " + path);

                //ex test:S(a|g|r)*on
                //pour valider que le resultat soit correcte,on peut faire egrep simultanement
                //egrep -c 'pattern' test.txt
                List<String> lines = null;
                try {
                    // open file et lire ligne par ligne
                    lines = Files.readAllLines(Paths.get(path));
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier: " + e.getMessage());
                }
                //---------------------------------Traitement de texte avec DFA-------------------------------//
                int matchedLines_DFA = 0;
                long total_duration_DFA = 0;
                for(int i=0;i<5;i++) {
                    // record start time
                    long startTime_DFA = System.currentTimeMillis();

                    for (String line : lines) {
                        if (dfa.traite_texte(line)) {
                            System.out.println(line);
                            matchedLines_DFA++;
                        }
                    }

                    // record end time
                    long endTime_DFA = System.currentTimeMillis();
                    long duration_DFA = endTime_DFA - startTime_DFA;
                    total_duration_DFA += duration_DFA;
                }
                matchedLines_DFA = matchedLines_DFA / 5;
                long average_duration_DFA = total_duration_DFA / 5;

                //---------------------------------Traitement de texte avec DFA Minimum-------------------------------//
                int matchedLines_DFA_mini = 0;
                long total_duration_DFA_mini = 0;
                for(int i=0;i<5;i++) {
                    // record start time
                    long startTime_DFA_mini = System.currentTimeMillis();

                    for (String line : lines) {
                        if (DFA_minimisation.traite_texte(line)) {
                            System.out.println(line);
                            matchedLines_DFA_mini++;
                        }
                    }

                    // record end time
                    long endTime_DFA_mini = System.currentTimeMillis();
                    long duration_DFA_mini = endTime_DFA_mini - startTime_DFA_mini;
                    total_duration_DFA_mini += duration_DFA_mini;
                }
                matchedLines_DFA_mini = matchedLines_DFA_mini / 5;
                long average_duration_DFA_mini = total_duration_DFA_mini / 5;


                //-----------------------------------Traitement de texte avec egrep---------------------------------//
                int matchedLines_egrep = 0;
                long total_duration_egrep = 0;
                for(int i=0;i<5;i++) {
                    // record start time
                    long startTime_egrep = System.currentTimeMillis();

                    // execute egrep command
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder("egrep", "-c", regEx, path);
                        Process process = processBuilder.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String output = reader.readLine();
                        if (output != null) {
                            matchedLines_egrep = Integer.parseInt(output);
                        }

                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            System.err.println("egrep command failed with exit code: " + exitCode);
                        }
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Error executing egrep: " + e.getMessage());
                    }

                    // record end time
                    long endTime_egrep = System.currentTimeMillis();
                    long duration_egrep = endTime_egrep - startTime_egrep;
                    total_duration_egrep += duration_egrep;
                }
                long average_duration_egrep = total_duration_egrep / 5;

                // synthese DFA
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>Synthese of DFA<<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("Total matching lines with DFA: " + matchedLines_DFA);
                System.out.println("Average time taken with DFA: " + average_duration_DFA + " milliseconds");

                // synthese DFA mini
                System.out.println(">>>>>>>>>>>>>>>>>>>>>Synthese of DFA mini<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("Total matching lines with DFA mini: " + matchedLines_DFA_mini);
                System.out.println("Average time taken with DFA mini: " + average_duration_DFA_mini + " milliseconds");

                // synthese egrep pour valider le resultat
                System.out.println(">>>>>>>>>>>>>>>>>>>>>Synthese of egrep<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("Total matching lines with egrep: " + matchedLines_egrep);
                System.out.println("Average time taken with egrep: " + average_duration_egrep + " milliseconds");

                if(matchedLines_DFA_mini == matchedLines_DFA && matchedLines_DFA == matchedLines_egrep){
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>Resultat<<<<<<<<<<<<<<<<<<<<<<<<<");
                    System.out.println(">>>>>>>>>Bravo! Les resultats sont correctes");
                }

                if(matchedLines_DFA_mini != 0) {
                    // output .csv
                    String csvFilePath = "performance_results_automate.csv";
                    boolean fileExists = new File(csvFilePath).exists();

                    try (FileWriter writer = new FileWriter(csvFilePath, true)) {
                        // s'il n'existe pas, écrire l'en-tête les noms des colonnes
                        if (!fileExists) {
                            writer.append("Pattern,DFA Avg Time (ms),DFA Mini Avg Time (ms),Egrep Avg Time (ms)\n");
                        }

                        // append les résultats a la fin du fichier
                        writer.append(String.format("%s,%d,%d,%d\n",
                                regEx,
                                average_duration_DFA,
                                average_duration_DFA_mini,
                                average_duration_egrep));

                        System.out.println("CSV file has been updated successfully.");
                    } catch (IOException e) {
                        System.err.println("Error writing to CSV file: " + e.getMessage());
                    }
                }


            } catch (Exception e) {
                System.err.println(">> ERROR:  "+e.toString());
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////
        //--------------------------------------KMP--------------------------------------//
        ///////////////////////////////////////////////////////////////////////////////////
        System.out.println("Methode 2: L'algo KMP >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Welcome to the algo KMP.");
        // le chemin du fichier texte
        String filePath = "./test.txt";

        // les motifs à chercher
        List<String> motifs = new ArrayList<>();
        motifs.add("from");
        motifs.add("Another");
        motifs.add("Paris");
        motifs.add("Egypt");
        motifs.add("Sargon");

        try {
            // Lire le contenu du fichier
            String text = RechercheMotif.readFileAsString(filePath);

            // Créer un fichier CSV pour stocker les résultats
            String csvFile = "performance_results_KMP.csv";

            // Chercher plusieurs motifs avec KMP et stocker les résultats dans le CSV
            RechercheMotif.chercherPlusieursMotifs(text, motifs, csvFile);

            // Appeler le script Python pour générer le diagramme
            String pythonCommand = "python3 generate_chart.py " + csvFile;
            Runtime.getRuntime().exec(pythonCommand);

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }

        System.out.println("  >> ...");
        System.out.println("  >> Parsing completed.");
    }

}
