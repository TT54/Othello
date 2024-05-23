package fr.tt54.othello.data.patterns;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class Pattern {

    private static final Map<PatternType, Map<GameStage, Map<Integer, Pattern>>> patternsMap = new HashMap<>();

    private int[] pawns;
    private int played;
    private int won;

    public Pattern(int[] pawns, int played, int won) {
        this.pawns = pawns;
        this.played = played;
        this.won = won;
    }

    /**
     *
     * @param pawns La liste des pions avec noirs = -1 ; rien = 0 ; blancs = 1
     */
    public Pattern(int[] pawns, boolean whiteToPlay) {
        for (int i = 0; i < pawns.length; i++) {
            if(!whiteToPlay){
                pawns[i] *= -1;
            }
            pawns[i]++;
        }
        this.pawns = pawns;
    }

    public void addPositionPlayed(boolean won){
        this.played++;
        if(won)
            this.won++;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public void setWon(int won) {
        this.won = won;
    }

    public void addPlayed(int played){
        this.played += played;
    }

    public void addWon(int won){
        this.won += won;
    }

    /**
     *
     * @return La liste des pions sous la forme [0, -1, 1] avec 0 si la case est vide, -1 si elle est occupée par les noirs, 1 sinon. Attention
     * le pattern renvoie les pions de telle sorte que c'est toujours aux blancs de jouer.
     */
    public int[] getPawns() {
        int[] pawns = new int[this.pawns.length];
        for(int i = 0; i < pawns.length; i++){
            pawns[i] = this.pawns[i]-1;
        }
        return pawns;
    }

    public int getPlayedGamesAmount() {
        return played;
    }

    public int getGameWonAmount() {
        return won;
    }

    public float getPatternValue(){
        return (won + 0.5f) / (played + 1f) - 0.5f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pattern pattern = (Pattern) o;
        return Arrays.equals(pawns, pattern.pawns);
    }

    @Override
    public int hashCode() {
        int code = 0;
        int k = 1;
        for (int pawn : pawns) {
            code += k * pawn;
            k *= 3;
        }
        return code;
    }

    public static void loadPattern(PatternType patternType, GameStage stage, int patternValue, int amountWins, int amountPlayed) {
        int initialValue = patternValue;
        int[] pawns = new int[9];
        for(int i = 0; i < 9; i++){
            pawns[i] = patternValue % 3;
            patternValue -= pawns[i];
            patternValue /= 3;
        }

        Pattern p = new Pattern(pawns, amountPlayed, amountWins);

        if(initialValue != p.hashCode()) {
            System.out.println(p.hashCode() + " - " + initialValue);
        }

        Map<GameStage, Map<Integer, Pattern>> stageMap = patternsMap.getOrDefault(patternType, new HashMap<>());
        Map<Integer, Pattern> patternMap = stageMap.getOrDefault(stage, new HashMap<>());
        Pattern p2 = patternMap.getOrDefault(p.hashCode(), p);

        p2.setPlayed(amountPlayed);
        p2.setWon(amountWins);

        patternMap.put(p2.hashCode(), p2);
        stageMap.put(stage, patternMap);
        patternsMap.put(patternType, stageMap);
    }

    /**
     *
     * @param stage Moment de la partie
     * @param pawnsPosition Pattern sous forme de liste (Noirs = -1 ; Rien = 0 ; Blancs = 1)
     * @param whiteToPlay
     * @return Le pattern stocké
     */
    public static Pattern getPatternFromPosition(PatternType type, GameStage stage, int[] pawnsPosition, boolean whiteToPlay){
        Pattern p = new Pattern(pawnsPosition, whiteToPlay);

        return patternsMap.getOrDefault(type, new HashMap<>()).getOrDefault(stage, new HashMap<>()).getOrDefault(p.hashCode(), p);
    }

    public static Map<Integer, Pattern> getPatterns(PatternType patternType, GameStage stage) {
        return patternsMap.getOrDefault(patternType, new HashMap<>()).getOrDefault(stage, new HashMap<>());
    }

    public static Pattern addPatternOccurrence(PatternType patternType, GameStage stage, int[] pattern, boolean whiteToPlay, boolean winningPosition){
        Pattern p = new Pattern(pattern, whiteToPlay);

        Map<GameStage, Map<Integer, Pattern>> stageMap = patternsMap.getOrDefault(patternType, new HashMap<>());
        Map<Integer, Pattern> patternMap = stageMap.getOrDefault(stage, new HashMap<>());
        Pattern p2 = patternMap.getOrDefault(p.hashCode(), p);

        p2.addPositionPlayed(winningPosition);

        patternMap.put(p2.hashCode(), p2);
        stageMap.put(stage, patternMap);
        patternsMap.put(patternType, stageMap);

        return p2;
    }

    public enum GameStage{
        OPENING,
        MID_GAME,
        ENDGAME;
    }

    public enum PatternType{
        MAIN_DIAGONAL(true, new int[][]{{7, 0}, {6, 1}, {5, 2}, {4, 3}, {3, 4}, {2, 5}, {1, 6}, {0, 7}},
                new int[][]{{0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}}),

        DIAGONAL_7(true, new int[][]{{6, 0}, {5, 1}, {4, 2}, {3, 3}, {2, 4}, {1, 5}, {0, 6}},
                new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}},
                new int[][]{{1, 7}, {2, 6}, {3, 5}, {4, 4}, {5, 3}, {6, 2}, {7, 1}},
                new int[][]{{7, 6}, {6, 5}, {5, 4}, {4, 3}, {3, 2}, {2, 1}, {1, 0}}),

        DIAGONAL_6(true, new int[][]{{5, 0}, {4, 1}, {3, 2}, {2, 3}, {1, 4}, {0, 5}},
                new int[][]{{0, 2}, {1, 3}, {2, 4}, {3, 5}, {4, 6}, {5, 7}},
                new int[][]{{2, 7}, {3, 6}, {4, 5}, {5, 4}, {6, 3}, {7, 2}},
                new int[][]{{7, 5}, {6, 4}, {5, 3}, {4, 2}, {3, 1}, {2, 0}}),

        DIAGONAL_5(true, new int[][]{{4, 0}, {3, 1}, {2, 2}, {1, 3}, {0, 4}},
                new int[][]{{0, 3}, {1, 4}, {2, 5}, {3, 6}, {4, 7}},
                new int[][]{{3, 7}, {4, 6}, {5, 5}, {6, 4}, {7, 3}},
                new int[][]{{7, 4}, {6, 3}, {5, 2}, {4, 1}, {3, 0}}),

        BORDER(true, new int[][]{{0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}},
                new int[][]{{0, 7}, {1, 7}, {2, 7}, {3, 7}, {4, 7}, {5, 7}, {6, 7}, {7, 7}},
                new int[][]{{7, 7}, {7, 6}, {7, 5}, {7, 4}, {7, 3}, {7, 2}, {7, 1}, {7, 0}},
                new int[][]{{7, 0}, {6, 0}, {5, 0}, {4, 0}, {3, 0}, {2, 0}, {1, 0}, {0, 0}}),

        HORIZONTAL_2(true, new int[][]{{1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {1, 7}},
                new int[][]{{0, 6}, {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6}, {6, 6}, {7, 6}},
                new int[][]{{6, 7}, {6, 6}, {6, 5}, {6, 4}, {6, 3}, {6, 2}, {6, 1}, {6, 0}},
                new int[][]{{7, 1}, {6, 1}, {5, 1}, {4, 1}, {3, 1}, {2, 1}, {1, 1}, {0, 1}}),

        HORIZONTAL_3(true, new int[][]{{2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}, {2, 7}},
                new int[][]{{0, 5}, {1, 5}, {2, 5}, {3, 5}, {4, 5}, {5, 5}, {6, 5}, {7, 5}},
                new int[][]{{5, 7}, {5, 6}, {5, 5}, {5, 4}, {5, 3}, {5, 2}, {5, 1}, {5, 0}},
                new int[][]{{7, 2}, {6, 2}, {5, 2}, {4, 2}, {3, 2}, {2, 2}, {1, 2}, {0, 2}}),

        HORIZONTAL_4(true, new int[][]{{3, 0}, {3, 1}, {3, 2}, {3, 3}, {3, 4}, {3, 5}, {3, 6}, {3, 7}},
                new int[][]{{0, 4}, {1, 4}, {2, 4}, {3, 4}, {4, 4}, {5, 4}, {6, 4}, {7, 4}},
                new int[][]{{4, 7}, {4, 6}, {4, 5}, {4, 4}, {4, 3}, {4, 2}, {4, 1}, {4, 0}},
                new int[][]{{7, 3}, {6, 3}, {5, 3}, {4, 3}, {3, 3}, {2, 3}, {1, 3}, {0, 3}}),

        CORNER_3_3(false, new int[][]{{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}, {2, 0}, {2, 1}, {2, 2}},
                new int[][]{{0, 7}, {1, 7}, {2, 7}, {0, 6}, {1, 6}, {2, 6}, {0, 5}, {1, 5}, {2, 5}},
                new int[][]{{7, 7}, {7, 6}, {7, 5}, {6, 7}, {6, 6}, {6, 5}, {5, 7}, {5, 6}, {5, 5}},
                new int[][]{{7, 0}, {6, 0}, {5, 0}, {7, 1}, {6, 1}, {5, 1}, {7, 2}, {6, 2}, {5, 2}});

        private final int[][][] patternsLocations; // Stocke les positions sur le plateau pour lire les patterns
        private final boolean reverseReading; // Indique si les positions stockées peuvent être lues à l'envers ou non (utile dans le cas de certaines symétries

        PatternType(boolean reverseReading, int[][]... patternsLocations){
            this.reverseReading = reverseReading;
            if(!reverseReading){
                this.patternsLocations = patternsLocations;
            } else {
                this.patternsLocations = new int[2 * patternsLocations.length][][];
                for(int i = 0; i < 2 * patternsLocations.length; i++){
                    int indice = i%patternsLocations.length;
                    int[][] pattern = new int[patternsLocations[indice].length][];

                    for (int j = 0; j < patternsLocations[indice].length; j++) {
                        if(i == indice) {
                            pattern[j] = patternsLocations[indice][j];
                        } else {
                            pattern[patternsLocations[indice].length - 1 - j] = patternsLocations[indice][j];
                        }
                    }

                    this.patternsLocations[i] = pattern;
                }
            }
        }

        public int[][][] getPatternsLocations() {
            return patternsLocations;
        }
    }
}
