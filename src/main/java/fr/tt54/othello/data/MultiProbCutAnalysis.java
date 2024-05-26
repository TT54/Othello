package fr.tt54.othello.data;

import fr.tt54.othello.OthelloGame;
import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.data.genetic.GeneticAlgorithm;

import java.io.*;

public class MultiProbCutAnalysis {


    static int currentAmountOfMoves = 0;        // Nombre de coups pour la position de départ actuelle
    static int currentPositionsSearched = 0;    // Nombre de positions cherchées par tous les threads
    static String savedDatas = "";              // Contenu du fichier de données
    static boolean toSave = false;              // Vaudra true s'il faut sauvegarder des données dans le fichier csv


    /**
     *
     * @param depthsToCheck     Profondeurs auxquelles les évaluations vont être faites
     * @param positionsToSearch Nombre de positions à rechercher pour chaque moment de la partie
     * @param minAvancement    Avancement de la partie minimal pour l'évaluation
     * @param maxAvancement    Avancement de la partie maximal pour l'évaluation
     * @param csvFolder         Dossier de sauvegarde des données
     * @param csvFileName       Nom du fichier
     * @param threadAmount      Nombre de threads
     */
    public static void getEvaluationComparisonAsync(int[] depthsToCheck, int positionsToSearch, int minAvancement, int maxAvancement, String csvFolder, String csvFileName, int threadAmount){
        int[][][] evaluations = new int[60][positionsToSearch][depthsToCheck.length];
        // evaluations[nombre de coups joués][indice de la position cherchée][indice de la profondeur]
        currentAmountOfMoves = minAvancement;


        File result = new File(csvFolder, csvFileName + ".csv");
        try {
            FileWriter writer = new FileWriter(result);

            savedDatas = "Avancement;Indice Position";

            for(int depth : depthsToCheck){
                savedDatas += ";Profondeur " + depth;
            }

            writer.write(savedDatas);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        final boolean[] threadStatus = new boolean[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            final int threadI = i;
            Thread thread = new Thread(){

                private final int threadIndex = threadI; // Indice du thread
                private int threadAmountOfMoves = minAvancement; // Nombre de coups joués avant d'évaluer la position à différentes profondeurs

                @Override
                public void run() {
                    while (!this.isInterrupted()){
                        if(threadAmountOfMoves != currentAmountOfMoves){
                            threadStatus[threadIndex] = true;

                            partialSave();
                        }

                        // Sauvegarde des données
                        if(shouldSave()){
                            for(int i = 0; i < threadAmount; i++){
                                threadStatus[i] = false;
                            }

                            try {
                                FileWriter writer = new FileWriter(result);

                                for(int j = 0; j < positionsToSearch; j++){
                                    savedDatas += "\n" + (threadAmountOfMoves + 1) + ";" + j;

                                    for(int k = 0; k < depthsToCheck.length; k++){
                                        savedDatas += ";" + evaluations[threadAmountOfMoves][j][k];
                                    }
                                }

                                writer.write(savedDatas);
                                writer.flush();
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        threadAmountOfMoves = currentAmountOfMoves;
                        int threadPositionsSearched = currentPositionsSearched; // Indice de la position courante

                        currentPositionsSearched++;

                        // Si on a fini d'évaluer le nombre de positions requis pour un nombre de coups donné, on passe au nombre de coups suivant
                        if(currentPositionsSearched >= positionsToSearch){
                            currentAmountOfMoves++;
                            currentPositionsSearched = 0;

                            System.out.println("Passage au coup numéro : " + (currentAmountOfMoves+1));

                            toSave = true;
                        }

                        // Fin
                        if(threadAmountOfMoves >= maxAvancement){
                            this.interrupt();
                            return;
                        }

                        // Affichage de l'avancement
                        if(threadPositionsSearched % (positionsToSearch/10) == 0){
                            System.out.println(threadPositionsSearched + "/" + positionsToSearch + " positions cherchées");
                        }

                        // Génération de la partie et évaluation
                        OthelloGame game = OthelloGame.generateRandomPos(threadAmountOfMoves);
                        for(int k = 0; k < depthsToCheck.length; k++){
                            int depth = depthsToCheck[k];
                            evaluations[threadAmountOfMoves][threadPositionsSearched][k] = Bot.alphaBeta(game, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, GeneticAlgorithm.FIRST_ATTEMPT_BOT::advancedEvaluation).getEvaluation();
                        }
                    }
                }

                private boolean shouldSave(){
                    for(int i = 0; i < threadAmount; i++){
                        if(!threadStatus[i])
                            return false;
                    }
                    return true;
                }

                private void partialSave(){
                    try {
                        File file = new File(csvFolder, csvFileName + "_" + this.threadAmountOfMoves + ".csv");
                        FileWriter writer = new FileWriter(file);
                        String datas = "Avancement;Indice Position";

                        for(int depth : depthsToCheck){
                            datas += ";Profondeur " + depth;
                        }

                        for(int j = 0; j < positionsToSearch; j++){
                            datas += "\n" + (threadAmountOfMoves + 1) + ";" + j;

                            for(int k = 0; k < depthsToCheck.length; k++){
                                datas += ";" + evaluations[threadAmountOfMoves][j][k];
                            }
                        }

                        writer.write(datas);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }


    public static void formatDatas(int[] depthsToCheck, int positionsToSearch, int minAvancement, int maxAvancement, String folder, String currentDataFile, String newDataFile){
        int[][][] evaluations = new int[maxAvancement - minAvancement][positionsToSearch][depthsToCheck.length];

        File currentFile = new File(folder, currentDataFile + ".csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(currentFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] datas = line.split(";");
                try {
                    int avancement = Integer.parseInt(datas[0]) - 1 - minAvancement;
                    int posIndex = Integer.parseInt(datas[1]);
                    int depth = Integer.parseInt(datas[2]) - 1;

                    evaluations[avancement][posIndex][depth] = Integer.parseInt(datas[3]);
                } catch (NumberFormatException e){}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        File newFile = new File(folder, newDataFile + ".csv");
        try {
            FileWriter writer = new FileWriter(newFile);
            savedDatas = "Avancement;Indice Position";

            for(int depth : depthsToCheck){
                savedDatas += ";Profondeur " + depth;
            }

            for(int i = 0; i < evaluations.length; i++){
                for(int j = 0; j < positionsToSearch; j++){
                    savedDatas += "\n" + (i + 1 + minAvancement) + ";" + positionsToSearch;

                    for(int k = 0; k < depthsToCheck.length; k++){
                        savedDatas += ";" + evaluations[i][j][k];
                    }
                }
            }

            writer.write(savedDatas);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mergeCSV(String folder, String csvBaseName, String csvFinalName, int minAvancement, int maxAvancement) {
        File newFile = new File(folder, csvFinalName + ".csv");
        try {
            FileWriter writer = new FileWriter(newFile);

            for(int i = minAvancement; i < maxAvancement + 1; i++){
                File currentFile = new File(folder, csvBaseName + "_" + i + ".csv");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(currentFile)))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] datas = line.split(";");
                        try {
                            int avancement = Integer.parseInt(datas[0]);
                            writer.write("\n" + line);
                        } catch (NumberFormatException e){
                            if(i == minAvancement){
                                writer.write(line);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            writer.write(savedDatas);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
