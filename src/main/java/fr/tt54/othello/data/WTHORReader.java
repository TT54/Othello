package fr.tt54.othello.data;

import fr.tt54.othello.data.objects.PlayedGame;

import java.io.IOException;
import java.io.InputStream;

public class WTHORReader {

    /**
     *
     * @param inputStream Fichier à lire
     * @return Un tableau contenant les parties jouées
     */
    public static PlayedGame[] readWTHORFile(InputStream inputStream){
        try {
            int[] bytes = convertBytes(inputStream.readAllBytes());

            int creationCentury = bytes[0];
            int creationYear = bytes[1];
            int creationMonth = bytes[2];
            int creationDay = bytes[3];

            int[] N1Array = new int[] {bytes[4], bytes[5], bytes[6], bytes[7]};
            int N1 = readLongInt(N1Array);

            int[] N2Array = new int[] {bytes[8], bytes[9]};
            int N2 = readWord(N2Array);
            int[] gamesYearArray = new int[] {bytes[10], bytes[11]};
            int gamesYear = readWord(gamesYearArray);

            int P1 = bytes[12];
            int P2 = bytes[13];
            int P3 = bytes[14];

            if(P1 == 10 || P2 == 1){
                System.out.println("Mauvais type de fichier");
                return new PlayedGame[0];
            }

            PlayedGame[] games = new PlayedGame[N1];

            int i = 16;
            for(int game = 0; game < N1; game++){
                int tournament = readWord(new int[]{bytes[i], bytes[i + 1]});
                int blackPlayer = readWord(new int[]{bytes[i + 2], bytes[i + 3]});
                int whitePlayer = readWord(new int[]{bytes[i + 4], bytes[i + 5]});
                int blackScore = bytes[i + 6];
                int theoreticalBlackScore = bytes[i + 7];

                int[] moves = new int[60];
                for(int j = 0; j < 60; j++){
                    // La base WTHOR a ses positions sous la forme
                    // b3 ==> 32 (colonne * 10 + ligne)
                    // alors que le programme les a sous la forme
                    // b3 ==> 17 = (2 - 1) + (3 - 1) * 8 = 17 ((ligne - 1) + (colonne - 1) * 8)
                    moves[j] = (bytes[i + 8 + j] % 10) - 1 + ((bytes[i + 8 + j] / 10) - 1) * 8;
                    if(moves[j] < 0){
                        moves[j] = -1;
                    }
                }

                games[game] = new PlayedGame(tournament, blackPlayer, whitePlayer, blackScore, theoreticalBlackScore, moves);

                i += 68;
            }

            return games;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new PlayedGame[0];
    }

    /**
     * Permet de convertir un tableau de bytes en un tableau d'entiers positifs (les bytes étant compris entre -128 et 127)
     * @param bytes un tableau de bytes
     * @return le tableau d'entiers positifs
     */
    private static int[] convertBytes(byte[] bytes){
        int[] converted = new int[bytes.length];
        for(int i = 0; i < bytes.length; i++){
            if(bytes[i] < 0){
                converted[i] = 256 + bytes[i];
            } else {
                converted[i] = bytes[i];
            }
        }
        return converted;
    }

    /**
     *
     * @param bytes tableau des 4 bytes composant un LongInt au format [1, 256, 256 * 256, 256 * 256 * 256]
     * @return le LongInt
     */
    private static int readLongInt(int[] bytes){
        if(bytes.length != 4) throw new IllegalArgumentException("Un LongInt doit être composé de 4 bytes");

        int k = 0;

        int pow = 1;
        for(int i = 0; i < 4; i++){
            k += pow * bytes[i];
            pow *= 256;
        }

        return k;
    }

    /**
     *
     * @param bytes tableau des 2 bytes composant le Word au format : [1, 256]
     * @return le Word
     */
    private static int readWord(int[] bytes){
        if(bytes.length != 2) throw new IllegalArgumentException("Un Word doit être composé de 4 bytes");

        return bytes[0] + 256 * bytes[1];
    }

}

