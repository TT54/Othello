package fr.tt54.othello.graphic;

import fr.tt54.othello.Main;
import fr.tt54.othello.OthelloGame;
import fr.tt54.othello.bots.Bot;
import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.nodes.GraphicNode;
import fr.ttgraphiclib.graphics.nodes.RectangleNode;
import fr.ttgraphiclib.thread.Frame;
import fr.ttgraphiclib.thread.MainClass;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OthelloGraphicManager extends MainClass {

    private OthelloGame game;
    private java.util.List<GraphicNode> allowedMovesNodes = new ArrayList<>();
    private Set<Integer> allowedMoves = new HashSet<>();
    private Bot bot;
    private int timePerGame = 2 * 60 * 1000; // Temps par partie pour le programme (en ms)


    public OthelloGraphicManager() {
        this.game = new OthelloGame("");
        this.drawBoard();
    }

    public void playAgainstBot(Bot bot){
        this.bot = bot;
    }

    private int tick = 0;


    /**
     * Méthode appelée à chaque tick (tous les 1/30e de seconde) pour gérer l'affichage à l'écran
     * @param frame La fenêtre
     */
    @Override
    public void doTickContent(Frame frame) {
        this.tick++;
        if(this.game.needUpdate){
            this.updatePanel();
            this.game.needUpdate = false;
        } else if(tick == 1000){
            this.tick = 0;
            this.updatePanel();
        }

        if(this.bot != null && this.game.isWhiteToPlay() == this.bot.isWhite()){
            for(GraphicNode node : new ArrayList<>(this.allowedMovesNodes)){
                Main.panel.removeNode(node);
            }
            this.allowedMovesNodes.clear();

            long time = System.currentTimeMillis();
            if(this.game.getAvailablePlacements().isEmpty() || !bot.playMove(this.game, this.timePerGame)){
                bot = null;
            }
            this.timePerGame -= System.currentTimeMillis() - time;
            System.out.println(this.timePerGame);
        }
    }

    /**
     * Dessine le plateau
     */
    private void drawBoard() {
        GraphicPanel panel = Main.panel;
        panel.addNode(new RectangleNode(panel, -405, -405, 810, 810, Color.BLACK));

        boolean white = false;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                RectangleNode rect = new RectangleNode(panel, -400 + j * 100, -400 + i * 100, 100, 100, new Color(25, 15, 25));
                RectangleNode rect2 = new RectangleNode(panel, -400 + j * 100 + 1, -400 + i * 100 + 1, 98, 98, new Color(75, 125, 75));
                final int row = i;
                final int column = j;
                rect.setClickAction(event -> {
                    int position = OthelloGame.positionToInt(row, column);
                    moveSelectedPiece(position);
                });
                white = !white;
            }
            white = !white;
        }
    }

    private void updatePanel() {
        GraphicPanel panel = Main.panel;
        List<GraphicNode> nodes = panel.getNodes().stream().filter(graphicNode -> graphicNode instanceof GraphicPiece).toList();

        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int value = this.game.getPiece(i, j);
                if (value != 0) {
                    new GraphicPiece(panel, -400 + j * 100 + 5, -400 + i * 100 + 5, 90, 90, value == 1);
                }
            }
        }

        nodes.forEach(panel::removeNode);
        showAvailableMoves();
    }

    private void moveSelectedPiece(int targetPosition){
        int candidate = -1;
        for(int move : this.game.getAvailablePlacements()){
            if(move == targetPosition){
                candidate = move;
                break;
            }
        }

        if(candidate == -1)
            return;

        int[] move = OthelloGame.intToPosition(candidate);

        this.game.playMove(move[0], move[1]);

        if(this.game.isGameFinished()){
            System.out.println("Blancs : " + this.game.getWhitePiecesCount());
            System.out.println("Noirs : " + this.game.getBlackPiecesCount());
        }
    }

    public void showAvailableMoves() {
        for(GraphicNode node : new ArrayList<>(this.allowedMovesNodes)){
            Main.panel.removeNode(node);
        }
        this.allowedMovesNodes.clear();
        allowedMoves.clear();

        allowedMoves.addAll(this.game.getAvailablePlacements());

        for (int allowed : allowedMoves) {
            int[] allowedPos = OthelloGame.intToPosition(allowed);
            allowedMovesNodes.add(new RectangleNode(Main.panel, -400 + 25 + allowedPos[1] * 100, -400 + 25 + allowedPos[0] * 100, 50, 50, game.isWhiteToPlay() ? new Color(255, 220, 255, 150) : new Color(25, 10, 25, 150)));
        }
    }

    public void setGame(OthelloGame game) {
        this.game = game;
    }

    public OthelloGame getGame() {
        return this.game;
    }
}
