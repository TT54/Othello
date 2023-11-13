package fr.tt54.othello;

import fr.tt54.othello.bots.AlphaBetaBot;
import fr.tt54.othello.bots.OpeningTestBot;
import fr.tt54.othello.game.OthelloGraphicManager;
import fr.ttgraphiclib.GraphicManager;
import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.events.listener.UserListener;
import fr.ttgraphiclib.thread.Frame;

import java.awt.event.KeyEvent;
import java.util.Random;

public class Main extends UserListener {

    public static final Random random = new Random();
    public static final GraphicPanel panel = new GraphicPanel();
    public static OthelloGraphicManager othelloGraphicManager;


    public static void main(String[] args) {
        GraphicManager.setMaxFPS(30);
        GraphicManager.setMaxMovePerSecond(30);
        Frame frame = new Frame("Othello", 900, 900);
        GraphicManager.enable(frame, panel);
        frame.setMainClass(othelloGraphicManager = new OthelloGraphicManager());

        othelloGraphicManager.playAgainstBot(new OpeningTestBot(true));
        //playRandomGames();
    }

    @Override
    public void onKeyPressed(KeyEvent e) {

    }
}
