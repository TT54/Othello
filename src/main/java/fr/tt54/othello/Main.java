package fr.tt54.othello;

import com.sun.nio.sctp.NotificationHandler;
import fr.tt54.othello.bots.AlphaBetaBot;
import fr.tt54.othello.bots.MinMaxBot;
import fr.tt54.othello.game.OthelloGame;
import fr.tt54.othello.game.OthelloGraphicManager;
import fr.ttgraphiclib.GraphicManager;
import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.events.listener.UserListener;
import fr.ttgraphiclib.thread.Frame;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
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


        int wins = 0;
        for(int i = 0; i < 100; i++) {
            System.out.println("partie " + i + " / " + 100);
            if(AlphaBetaBot.playAgainstRandom(true, 6))
                wins++;
        }
        System.out.println(wins);
    }


    @Override
    public void onKeyPressed(KeyEvent e) {
        /*if(e.getKeyCode() == KeyEvent.VK_LEFT){
            System.out.println("show previous");
            othelloGraphicManager.showPreviousPosition();
        } else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            othelloGraphicManager.showNextPosition();
        }*/
    }
}
