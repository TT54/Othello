package fr.tt54.othello.game;

import java.util.HashSet;
import java.util.Set;

public class OthelloGame {

    private final int[][] board;
    private boolean whiteToPlay = false;
    private int move = 0;
    private int whitePiecesCount = 0;
    private int blackPieces = 0;
    public boolean needUpdate = true;

    private Set<Integer> availableMoves = new HashSet<>();

    public OthelloGame(int[][] board) {
        this.board = board;
    }

    public OthelloGame() {
        this.board = new int[8][8];

        generateDefaultGame();
    }

    public void generateDefaultGame(){
        this.board[3][3] = 1;
        this.board[4][4] = 1;
        this.board[3][4] = -1;
        this.board[4][3] = -1;

        this.whitePiecesCount = 2;
        this.blackPieces = 2;

        this.generateAvailablePlacements();
    }


    /**
     *
     * @param row
     * @param column
     * @return true si à l'issue de ce coup, l'adversaire ne peut pas jouer, false sinon
     */
    public boolean playMove(int row, int column){
        int modified = this.placePiece(row, column, this.whiteToPlay);

        if(this.whiteToPlay){
            this.whitePiecesCount += modified + 1;
            this.blackPieces -= modified;
        } else {
            this.blackPieces += modified + 1;
            this.whitePiecesCount -= modified;
        }

        this.whiteToPlay = !this.whiteToPlay;
        move++;
        this.needUpdate = true;

        if(generateAvailablePlacements().isEmpty()) {
            this.whiteToPlay = !this.whiteToPlay;
            if(generateAvailablePlacements().isEmpty()){
                this.move = 100;
            }

            return true;
        }
        return false;
    }

    public boolean isGameFinished(){
        if(move >= 60)
            return true;
        return whitePiecesCount == 0 || blackPieces == 0;
    }


    private Set<Integer> generateAvailablePlacements(){
        Set<Integer> moves = new HashSet<>();

        for(int row = 0; row < 8; row++){
            for(int column = 0; column < 8; column++){
                if(getPiece(row, column) == 0){
                    if (canPlacePiece(row, column, this.whiteToPlay)) {
                        moves.add(positionToInt(row, column));
                    }
                }
            }
        }

        this.availableMoves = moves;
        return moves;
    }

    public Set<Integer> getAvailablePlacements(){
        return this.availableMoves;
    }

    public boolean canPlacePiece(int row, int column, boolean whiteMove){
        for(int i = -1; i < 2; i++){
            for(int j = -1; j < 2; j++){
                if(i == 0 && j == 0) continue;
                if(i + row < 0 || j + column < 0 || i + row >= 8 || j + column >= 8) continue;

                int piece = getPiece(row + i, column + j);
                if(piece == 0 || piece > 0 == whiteMove) continue;

                if(hasSamePieceOnPosition(row, column, i, j, whiteToPlay))
                    return true;
            }
        }
        return false;
    }

    private boolean hasSamePieceOnPosition(int startingRow, int startingColumn, int rowMove, int columnMove, boolean whiteToPlay) {
        int piece = getPiece(startingRow += rowMove, startingColumn += columnMove);
        while(piece != 0 && piece > 0 != whiteToPlay){
            startingRow += rowMove;
            startingColumn += columnMove;

            if(startingRow >= 8 || startingColumn >= 8 || startingRow < 0 || startingColumn < 0)
                return false;

            piece = getPiece(startingRow, startingColumn);
        }
        return piece != 0;
    }

    /**
     *
     * @param row
     * @param column
     * @param whiteMove
     * @return Le nombre de pièces qui ont été modifiées par ce coup
     */
    public int placePiece(int row, int column, boolean whiteMove){
        int changedPieces = 0;
        for(int i = -1; i < 2; i++){
            for(int j = -1; j < 2; j++){
                if(i == 0 && j == 0) continue;
                if(i + row < 0 || j + column < 0 || i + row >= 8 || j + column >= 8) continue;

                int piece = getPiece(row + i, column + j);
                if(piece == 0 || piece > 0 == whiteMove) continue;

                changedPieces += reversePiecesAfterPlacement(row, column, i, j, whiteToPlay);
            }
        }
        this.board[row][column] = whiteMove ? 1 : -1;

        return changedPieces;
    }

    /**
     *
     * @param startingRow
     * @param startingColumn
     * @param rowMove
     * @param columnMove
     * @param whiteMove
     * @return Le nombre de pièces qui ont été modifiées selon cette ligne
     */
    private int reversePiecesAfterPlacement(int startingRow, int startingColumn, int rowMove, int columnMove, boolean whiteMove) {
        Set<Integer> toReverse = new HashSet<>();

        int piece = getPiece(startingRow += rowMove, startingColumn += columnMove);
        while(piece != 0 && piece > 0 != whiteMove){
            toReverse.add(positionToInt(startingRow, startingColumn));

            startingRow += rowMove;
            startingColumn += columnMove;

            if(startingRow >= 8 || startingColumn >= 8 || startingRow < 0 || startingColumn < 0)
                return 0;

            piece = getPiece(startingRow, startingColumn);
        }

        if(piece != 0) {
            for (int position : toReverse) {
                int[] pos = intToPosition(position);
                this.board[pos[0]][pos[1]] = whiteMove ? 1 : -1;
            }

            return toReverse.size();
        }
        return 0;
    }

    public int getPiece(int row, int column){
        return this.board[row][column];
    }

    public int getPiece(int position){
        int[] pos = intToPosition(position);
        return getPiece(pos[0], pos[1]);
    }



    public static int positionToInt(int row, int column){
        return row * 8 + column;
    }

    public static int[] intToPosition(int pos){
        return new int[] {pos / 8, pos % 8};
    }

    public boolean isWhiteToPlay() {
        return this.whiteToPlay;
    }

    public int getMoveCount() {
        return move;
    }

    @Override
    public OthelloGame clone() {
        int[][] boardCopy = new int[8][8];
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                boardCopy[i][j] = this.board[i][j];
            }
        }
        return new OthelloGame(boardCopy);
    }

    public int getWhitePiecesCount() {
        return whitePiecesCount;
    }

    public int getBlackPiecesCount() {
        return blackPieces;
    }
}
