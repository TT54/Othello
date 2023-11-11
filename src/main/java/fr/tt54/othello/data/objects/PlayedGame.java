package fr.tt54.othello.data.objects;

public record PlayedGame(int tournament, int blackPlayer, int whitePlayer, int blackScore, int theoreticalBlackScore,
                         int[] moves) {

}
