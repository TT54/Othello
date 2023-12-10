package fr.tt54.othello.game;

import fr.tt54.othello.Main;
import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.graphic.GraphicPiece;
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


    public OthelloGraphicManager() {
        //this.game = new OthelloGame("c4e3f6e6f5c5f4g6f7e8f8g8h6c3b4g5b6d3d2e7e2f3g3b5d7d6d8c8g4h4a6h3h5h7c6f2h2h1g1c1d1a5g7a3b3a7b7c7b8e1a4a8f1a2c2b1g2b2");
        //this.game = new OthelloGame("f5f4e3d2e2f6d3c3c5c4f3d6e6c6b5b4a4a5a6c2b3a3a2b6d7e7a7d8g4c7c1f1f8d1b8e8c8g6f7h4");
        this.game = new OthelloGame("");
        //this.game = new OthelloGame("f5f4e3f6d3e2f3f2e6d2g5h6g4h5h3g3c1e7d6g6f8d8e8d7f7c3c2c4c8e1c5b3d1b4b5a6f1b6c6c7h4h2g2g8h8h1g1b1a2a3b8a1b2b7a8g7");
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

            if(!bot.playMove(this.game, 30 * 1000L)){
                bot = null;
            }
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

/*        panel.addPainting(ttGraphics -> {
            ttGraphics.getGraphics().drawString("SalutSalutSalutSalutSalutSalutSalutSalutSalutSalutSalutSalutSalutSalutSalutSalut", 10, 10);
            return true;
        }, 150);*/

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
