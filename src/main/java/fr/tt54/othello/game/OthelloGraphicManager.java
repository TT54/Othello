package fr.tt54.othello.game;

import fr.tt54.othello.Main;
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

    private OthelloGame game = new OthelloGame();
    private java.util.List<GraphicNode> allowedMovesNodes = new ArrayList<>();
    private Set<Integer> allowedMoves = new HashSet<>();


    public OthelloGraphicManager() {
        this.drawBoard();
    }

    @Override
    public void doTickContent(Frame frame) {
        if(this.game.needUpdate){
            this.updatePanel();
            this.game.needUpdate = false;
        }
    }

    private void drawBoard() {
        GraphicPanel panel = Main.panel;
        panel.addNode(new RectangleNode(panel, -405, -405, 810, 810, Color.BLACK));

        boolean white = false;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                RectangleNode rect = new RectangleNode(panel, -400 + j * 100, -400 + (7 - i) * 100, 100, 100, white ? new Color(245, 230, 230) : new Color(100, 75, 75));
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
                    new GraphicPiece(panel, -400 + j * 100, -400 + (7 - i) * 100, 90, 90, value == 1);
                }
            }
        }

        nodes.forEach(panel::removeNode);

        showAvailableMoves();
    }

    private void moveSelectedPiece(int targetPosition){
        List<Integer> candidates = new ArrayList<>();
        for(int move : allowedMoves){
            if(move == targetPosition){
                candidates.add(move);
            }
        }

        if(candidates.size() == 0)
            return;

        int[] move = OthelloGame.intToPosition(candidates.get(0));

        this.game.playMove(move[0], move[1]);
    }

    public void showAvailableMoves() {
        allowedMoves.clear();
        for(GraphicNode node : new ArrayList<>(this.allowedMovesNodes)){
            Main.panel.removeNode(node);
        }

        allowedMoves.addAll(this.game.getAvailablePlacements());

        for (int allowed : allowedMoves) {
            int[] allowedPos = OthelloGame.intToPosition(allowed);
            allowedMovesNodes.add(new RectangleNode(Main.panel, -400 + 25 + allowedPos[1] * 100, -400 + 25 + (7 - allowedPos[0]) * 100, 50, 50, new Color(100, 100, 255, 150)));
        }
    }

}
