package fr.tt54.othello.data.patterns;

import fr.tt54.othello.OthelloGame;
import fr.tt54.othello.data.PlayedGame;
import fr.tt54.othello.data.WTHORReader;
import fr.tt54.othello.data.openings.OpeningLoader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Map;

@SuppressWarnings("all")
public class PatternLoader {

    public static final String PATTERNS_WINS_ONLY = "/patterns/patterns_wins_only.json";
    public static final String PATTERNS_WINS_OR_DRAWS = "/patterns/patterns_wins_or_draws.json";
    public static final String PATTERNS_V2 = "/patterns/patterns_v2.json";
    public static final String PATTERNS_REWORK = "/patterns/patterns_rework.json";
    public static final String PATTERNS_REWORK_V2= "/patterns/patterns_rework_v2.json";

    public static void generatePatterns(String resourceFolder) throws FileNotFoundException {
        File folder = new File(OpeningLoader.class.getResource(resourceFolder).getFile());
        if(!folder.isDirectory())
            return;

        // On parcourt tous les fichiers de parties au format WTHOR dans le dossier
        for (File file : folder.listFiles()){
            // Récupération des parties dans le fichier
            PlayedGame[] games = WTHORReader.readWTHORFile(new FileInputStream(file));

            for(PlayedGame playedGame : games){
                int[] moves = playedGame.moves(); // Coups joués dans la partie

                int score; // Résultat théorique de la partie
                if(playedGame.theoreticalBlackScore() >= 33){
                    score = -1; // Victoire noire
                } else if(playedGame.theoreticalBlackScore() == 32){
                    score = 0; // Nulle
                } else {
                    score = 1; // Victoire blanche
                }

                OthelloGame game = new OthelloGame();
                for(int i = 0; i < 60; i++){
                    if(moves[i] < 0) // On est arrivé à la fin de la partie
                        break;
                    game.playMove(moves[i]);

                    // Enregistrement des patterns reconnus dans la position courante
                    if(game.getMoveCount() > 60 - 24){
                        // Fin de partie
                        for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                            for(int[][] patternLocation : patternType.getPatternsLocations()){
                                Pattern.addPatternOccurrence(
                                        patternType,
                                        Pattern.GameStage.ENDGAME,
                                        game.getPattern(patternLocation),
                                        game.isWhiteToPlay(),
                                        (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            }
                        }
                    } else if(game.getMoveCount() >= 16){
                        // Milieu de jeu
                        for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                            for(int[][] patternLocation : patternType.getPatternsLocations()){
                                Pattern.addPatternOccurrence(
                                        patternType,
                                        Pattern.GameStage.MID_GAME,
                                        game.getPattern(patternLocation),
                                        game.isWhiteToPlay(),
                                        (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            }
                        }
                    } else {
                        // Ouverture
                        for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                            for(int[][] patternLocation : patternType.getPatternsLocations()){
                                Pattern.addPatternOccurrence(
                                        patternType,
                                        Pattern.GameStage.OPENING,
                                        game.getPattern(patternLocation),
                                        game.isWhiteToPlay(),
                                        (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void savePatterns() {
        JSONObject json = new JSONObject();

        for(Pattern.PatternType patternType : Pattern.PatternType.values()){
            JSONObject typeObject = new JSONObject();

            for(Pattern.GameStage gameStage : Pattern.GameStage.values()){
                JSONObject stageObject = new JSONObject();

                for(Map.Entry<Integer, Pattern> entry : Pattern.getPatterns(patternType, gameStage).entrySet()){
                    // En entrée on a : un identifiant unique pour chaque pattern et le pattern associé
                    stageObject.put(entry.getKey(), getPatternJSON(entry.getValue()));
                }

                typeObject.put(gameStage.name().toLowerCase(), stageObject);
            }

            json.put(patternType.name().toLowerCase(), typeObject);
        }

        try (FileWriter file = new FileWriter("patterns.json")) {
            file.write(json.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getPatternJSON(Pattern pattern){
        JSONObject object = new JSONObject();
        object.put("amount_played", pattern.getPlayedGamesAmount());
        object.put("amount_wins", pattern.getGameWonAmount());
        return object;
    }

    public static void loadPatterns(String file){
        JSONParser jsonParser = new JSONParser();
        // Récupération du fichier contenant les patterns
        File realFile = new File(OpeningLoader.class.getResource(file).getFile());

        try (FileReader reader = new FileReader(realFile)) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            // On parcourt les différents types de patterns possibles
            for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                JSONObject patternTypeJSON = (JSONObject) jsonObject.get(patternType.name().toLowerCase());

                if(patternTypeJSON != null) {
                    for (Pattern.GameStage gameStage : Pattern.GameStage.values()) {
                        JSONObject gameStageJSON = (JSONObject) patternTypeJSON.get(gameStage.name().toLowerCase());

                        for (Object patternObject : gameStageJSON.keySet()) {
                            if (patternObject instanceof String key) {
                                // Récupération des données du pattern
                                int patternValue = Integer.parseInt(key); // Identifiant unique associé au pattern
                                JSONObject patternJSON = (JSONObject) gameStageJSON.get(patternObject);
                                int amountWins = (int) ((long) patternJSON.get("amount_wins"));
                                int amountPlayed = (int) ((long) patternJSON.get("amount_played"));

                                // On charge les données du pattern
                                Pattern.loadPattern(patternType, gameStage, patternValue, amountWins, amountPlayed);
                            }
                        }
                    }
                }
            }
        } catch (ParseException | IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
