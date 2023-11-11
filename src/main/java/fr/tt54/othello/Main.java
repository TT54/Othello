package fr.tt54.othello;

import fr.tt54.othello.bots.AlphaBetaBot;
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


        //playRandomGames();
    }

    public static void playRandomGames(){
        int wins = 0;
        int draws = 0;
        int looses = 0;
        for(int i = 0; i < 100; i++) {
            System.out.println("partie " + i + " / " + 100);
            int score = AlphaBetaBot.playAgainstRandom(true, 6);
            if(score > 0){
                wins++;
            } else if(score == 0){
                draws++;
            } else {
                looses++;
            }
        }
        System.out.println(wins + " wins | " + draws + " draws | " + looses + " looses");
    }


    @Override
    public void onKeyPressed(KeyEvent e) {

    }
}
