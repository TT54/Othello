package fr.tt54.othello.graphic;

import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.nodes.ImageNode;

public class GraphicPiece extends ImageNode {
    public GraphicPiece(GraphicPanel panel, double x, double y, double baseWidth, double baseHeight, boolean white) {
        super(panel, x, y, baseWidth, baseHeight, GraphicPiece.class.getResource("/" + (white ? "white" : "black") + "_piece.png"));
    }


}
