package fr.tt54.othello.data.objects;

import fr.tt54.othello.game.OthelloGame;

public record PlayedPosition (byte[] position, boolean whiteToPlay){

    public byte getPiece(int row, int column){
        return this.position[row * 8 + column];
    }


    public static PlayedPosition convertPosition(OthelloGame game){
        byte[] position = new byte[64];
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                position[i * 8 + j] = game.getPiece(i, j);
            }
        }
        return new PlayedPosition(position, game.isWhiteToPlay());
    }


}
