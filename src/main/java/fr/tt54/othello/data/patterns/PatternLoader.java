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
                            Pattern.addPatternOccurrence(Pattern.GameStage.ENDGAME, game.getTopLeftPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.ENDGAME, game.getTopRightPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.ENDGAME, game.getBottomLeftPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.ENDGAME, game.getBottomRightPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                        } else if(game.getMoveCount() > 20){
                            // Mid Game
                            Pattern.addPatternOccurrence(Pattern.GameStage.MID_GAME, game.getTopLeftPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.MID_GAME, game.getTopRightPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.MID_GAME, game.getBottomRightPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.MID_GAME, game.getBottomLeftPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                        } else {
                            // Opening
                            Pattern.addPatternOccurrence(Pattern.GameStage.OPENING, game.getTopLeftPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.OPENING, game.getBottomLeftPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.OPENING, game.getTopRightPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                            Pattern.addPatternOccurrence(Pattern.GameStage.OPENING, game.getBottomRightPattern(), game.isWhiteToPlay(), (game.isWhiteToPlay()) ? score >= 0 : score <= 0);
                        }
                    }
                }
            }
        }
    }

    public static void savePatterns() {
        JSONObject json = new JSONObject();

        JSONObject openingPatterns = new JSONObject();
        for(Map.Entry<Integer, Pattern> entry : Pattern.getOpeningPatterns().entrySet()){
            openingPatterns.put(entry.getKey(), getPatternObject(entry.getValue()));
        }
        json.put("opening", openingPatterns);

        JSONObject midGamePatterns = new JSONObject();
        for(Map.Entry<Integer, Pattern> entry : Pattern.getMidGamePatterns().entrySet()){
            midGamePatterns.put(entry.getKey(), getPatternObject(entry.getValue()));
        }
        json.put("mid_game", midGamePatterns);

        JSONObject endGamePatterns = new JSONObject();
        for(Map.Entry<Integer, Pattern> entry : Pattern.getEndGamePatterns().entrySet()){
            endGamePatterns.put(entry.getKey(), getPatternObject(entry.getValue()));
        }
        json.put("end_game", endGamePatterns);

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

            JSONObject jsonEndGame = (JSONObject) jsonObject.get("end_game");
            for(Object keyObject : jsonEndGame.keySet()){
                if(keyObject instanceof String key){
                    int patternValue = Integer.parseInt(key);
                    JSONObject patternObject = (JSONObject) jsonEndGame.get(keyObject);
                    int amountWins = (int) ((long) patternObject.get("amount_wins"));
                    int amountPlayed = (int) ((long) patternObject.get("amount_played"));

                    Pattern.loadPattern(Pattern.GameStage.ENDGAME, patternValue, amountWins, amountPlayed);
                }
            }

            JSONObject jsonMidGame = (JSONObject) jsonObject.get("mid_game");
            for(Object keyObject : jsonMidGame.keySet()){
                if(keyObject instanceof String key){
                    int patternValue = Integer.parseInt(key);
                    JSONObject patternObject = (JSONObject) jsonMidGame.get(keyObject);
                    int amountWins = (int) ((long) patternObject.get("amount_wins"));
                    int amountPlayed = (int) ((long) patternObject.get("amount_played"));

                    Pattern.loadPattern(Pattern.GameStage.MID_GAME, patternValue, amountWins, amountPlayed);
                }
            }

            JSONObject jsonOpening = (JSONObject) jsonObject.get("opening");
            for(Object keyObject : jsonOpening.keySet()){
                if(keyObject instanceof String key){
                    int patternValue = Integer.parseInt(key);
                    JSONObject patternObject = (JSONObject) jsonOpening.get(keyObject);
                    int amountWins = (int) ((long) patternObject.get("amount_wins"));
                    int amountPlayed = (int) ((long) patternObject.get("amount_played"));

                    Pattern.loadPattern(Pattern.GameStage.OPENING, patternValue, amountWins, amountPlayed);
                }
            }

        } catch (ParseException | IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
