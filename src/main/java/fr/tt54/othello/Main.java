package fr.tt54.othello;

import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.genetic.GeneticAlgorithm;
import fr.tt54.othello.graphic.OthelloGraphicManager;
import fr.ttgraphiclib.GraphicManager;
import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.events.listener.UserListener;
import fr.ttgraphiclib.thread.Frame;

import java.awt.event.KeyEvent;

public class Main extends UserListener {

    public static final GraphicPanel panel = new GraphicPanel();
    public static OthelloGraphicManager othelloGraphicManager;


    public static void main(String[] args) {
        DataManager.enable();
        GraphicManager.setMaxFPS(30);
        GraphicManager.setMaxMovePerSecond(30);
        Frame frame = new Frame("Othello", 900, 900);
        GraphicManager.enable(frame, panel);
        frame.setMainClass(othelloGraphicManager = new OthelloGraphicManager());

        GeneticAlgorithm.FIRST_ATTEMPT_BOT.setWhite(true);
        othelloGraphicManager.playAgainstBot(GeneticAlgorithm.FIRST_ATTEMPT_BOT);
    }

    @Override
    public void onKeyPressed(KeyEvent e) {

    }
}
