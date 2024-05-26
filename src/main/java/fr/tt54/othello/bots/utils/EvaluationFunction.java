package fr.tt54.othello.bots.utils;

import fr.tt54.othello.OthelloGame;

public interface EvaluationFunction {

    double evaluate(OthelloGame position);

}
