package fr.tt54.othello.data.patterns;

import fr.tt54.othello.data.WTHORReader;
import fr.tt54.othello.data.objects.PlayedGame;
import fr.tt54.othello.data.openings.OpeningLoader;
import fr.tt54.othello.game.OthelloGame;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Map;

public class PatternLoader {

    public static final String PATTERNS_WINS_ONLY = "/patterns/patterns_wins_only.json";
    public static final String PATTERNS_WINS_OR_DRAWS = "/patterns/patterns_wins_or_draws.json";
    public static final String PATTERNS_V2 = "/patterns/patterns_v2.json";
    public static final String PATTERNS_REWORK = "/patterns/patterns_rework.json";

    public static void generatePatterns(String resourceFolder) throws FileNotFoundException {
        File folder = new File(OpeningLoader.class.getResource(resourceFolder).getFile());
        if(!folder.isDirectory())
            return;

        for (File file : folder.listFiles()){
            PlayedGame[] games = WTHORReader.readWTHORFile(new FileInputStream(file));

            for(PlayedGame playedGame : games){
                int[] moves = playedGame.moves();

                int score;
                if(playedGame.theoreticalBlackScore() >= 33){
                    score = -1;
                } else if(playedGame.theoreticalBlackScore() == 32){
                    score = 0;
                } else {
                    score = 1;
                }

                OthelloGame game = new OthelloGame();
                for(int i = 0; i < 60; i++){
                    if(moves[i] < 0)
                        break;
                    game.playMove(moves[i]);
                    if(i >= 16 && i <= (60 - 23)){
                        if(game.getMoveCount() > 60 - 24){
                            // End Game
                            for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                                for(int[][] patternLocation : patternType.getPatternsLocations()){
                                    Pattern.addPatternOccurrence(patternType, Pattern.GameStage.ENDGAME, game.getPattern(patternLocation), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                                }
                            }
                        } else if(game.getMoveCount() > 10){
                            // Mid Game
                            for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                                for(int[][] patternLocation : patternType.getPatternsLocations()){
                                    Pattern.addPatternOccurrence(patternType, Pattern.GameStage.MID_GAME, game.getPattern(patternLocation), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                                }
                            }
                        } else {
                            // Opening
                            for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                                for(int[][] patternLocation : patternType.getPatternsLocations()){
                                    Pattern.addPatternOccurrence(patternType, Pattern.GameStage.OPENING, game.getPattern(patternLocation), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                                }
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

                for(Map.Entry<Integer, Pattern> entry : Pattern.getPattern(patternType, gameStage).entrySet()){
                    stageObject.put(entry.getKey(), getPatternObject(entry.getValue()));
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

    public static JSONObject getPatternObject(Pattern pattern){
        JSONObject object = new JSONObject();
        object.put("amount_played", pattern.getPlayedGamesAmount());
        object.put("amount_wins", pattern.getGameWonAmount());
        return object;
    }

    public static void loadPatterns(String file){
        JSONParser jsonParser = new JSONParser();
        File realFile = new File(OpeningLoader.class.getResource(file).getFile());

        try (FileReader reader = new FileReader(realFile)) {
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            for(Pattern.PatternType patternType : Pattern.PatternType.values()){
                JSONObject typeObject = (JSONObject) jsonObject.get(patternType.name().toLowerCase());

                if(typeObject != null) {
                    for (Pattern.GameStage gameStage : Pattern.GameStage.values()) {
                        JSONObject stageObject = (JSONObject) typeObject.get(gameStage.name().toLowerCase());

                        for (Object keyObject : stageObject.keySet()) {
                            if (keyObject instanceof String key) {
                                int patternValue = Integer.parseInt(key);
                                JSONObject patternObject = (JSONObject) stageObject.get(keyObject);
                                int amountWins = (int) ((long) patternObject.get("amount_wins"));
                                int amountPlayed = (int) ((long) patternObject.get("amount_played"));

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
