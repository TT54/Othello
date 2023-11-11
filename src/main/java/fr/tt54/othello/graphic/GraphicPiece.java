package fr.tt54.othello.graphic;

import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.nodes.ImageNode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GraphicPiece extends ImageNode {

    public static final BufferedImage whitePiece;
    public static final BufferedImage blackPiece;

    static {
        try {
            whitePiece = ImageIO.read(GraphicPiece.class.getResource("/images/white_piece.png"));
            blackPiece = ImageIO.read(GraphicPiece.class.getResource("/images/black_piece.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GraphicPiece(GraphicPanel panel, double x, double y, double baseWidth, double baseHeight, boolean white) {
        super(panel, x, y, baseWidth, baseHeight, white ? whitePiece : blackPiece);
    }


}
