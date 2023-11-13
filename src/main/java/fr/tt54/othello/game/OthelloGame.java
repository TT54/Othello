package fr.tt54.othello.game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OthelloGame {

    private final byte[][] board;           // Le plateau
    private boolean whiteToPlay = false;    // true si c'est aux blancs de jouer, false si c'est aux noirs
    private int move = 0;                   // Nombre de coups joués
    private int whitePiecesCount = 0;       // Nombre de pièces blanches
    private int blackPiecesCount = 0;       // Nombre de pièces noires
    public boolean needUpdate = true;       // true si l'affichage du plateau doit être rafraichi, false sinon

    private final byte[] playedMoves = new byte[60];        // Tableau contenant tous les coups joués depuis le début de la partie
    private Set<Integer> availableMoves = new HashSet<>();  // Set contenant tous les coups valides que l'on peut jouer dans cette position

    public OthelloGame(byte[][] board) {
        this.board = board;
    }

    public OthelloGame(String game){
        this.board = new byte[8][8];
        this.generateDefaultGame();

        loadGame(game);
    }

    public OthelloGame() {
        this.board = new byte[8][8];
        generateDefaultGame();
    }

    public void generateDefaultGame(){
        this.board[3][3] = 1;
        this.board[4][4] = 1;
        this.board[3][4] = -1;
        this.board[4][3] = -1;

        this.whitePiecesCount = 2;
        this.blackPiecesCount = 2;

        Arrays.fill(playedMoves, (byte) -1);

        this.generateAvailablePlacements();
    }

    /**
     * Charge une partie à partir des coups joués
     * @param game Chaine de caractères de la forme "f5f4e3f6" contenant les coups joués
     */
    public void loadGame(String game) {
        for(int i = 0; i < game.length(); i += 2){
            int column = game.charAt(i) - 97;
            int row = game.charAt(i + 1) - 49;

            this.playMove(row, column);
        }
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
            this.blackPiecesCount -= modified;
        } else {
            this.blackPiecesCount += modified + 1;
            this.whitePiecesCount -= modified;
        }

        this.playedMoves[move] = (byte) positionToInt(row, column);

        this.whiteToPlay = !this.whiteToPlay;
        move++;

        // On regarde si l'adversaire peut jouer ou non
        if(generateAvailablePlacements().isEmpty()) {
            this.whiteToPlay = !this.whiteToPlay;
            if(generateAvailablePlacements().isEmpty()){
                this.move = 100;
            }

            this.needUpdate = true;
            return true;
        }

        this.needUpdate = true;
        return false;
    }

    public boolean isGameFinished(){
        if(move >= 60)
            return true;
        return whitePiecesCount == 0 || blackPiecesCount == 0;
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

        // Pour chaque pièce adverse autour du pion placé, on appelle reversePiecesAfterPlacement
        for(int i = -1; i < 2; i++){
            for(int j = -1; j < 2; j++){
                if(i == 0 && j == 0) continue;
                if(i + row < 0 || j + column < 0 || i + row >= 8 || j + column >= 8) continue;

                int piece = getPiece(row + i, column + j);
                if(piece == 0 || piece > 0 == whiteMove) continue;

                changedPieces += reversePiecesAfterPlacement(row, column, i, j, whiteToPlay);
            }
        }
        this.board[row][column] = whiteMove ? (byte) 1 : -1;

        return changedPieces;
    }

    /**
     * Permet de retourner, si nécessaire, les pièces averses selon une certaine direction après que l'on ait posé une pièce
     * @param startingRow
     * @param startingColumn
     * @param rowMove Direction de la ligne selon laquelle on se déplace
     * @param columnMove Direction de la colonne selon laquelle on se déplace
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
                this.board[pos[0]][pos[1]] = whiteMove ? (byte) 1 : -1;
            }

            return toReverse.size();
        }
        return 0;
    }

    public int getPiece(int row, int column){
        return this.board[row][column];
    }

    public boolean isWhiteToPlay() {
        return this.whiteToPlay;
    }

    public int getMoveCount() {
        return move;
    }

    public byte[] getPlayedMoves() {
        return playedMoves;
    }

    @Override
    public OthelloGame clone() {
        byte[][] boardCopy = new byte[8][8];
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                boardCopy[i][j] = this.board[i][j];
            }
        }
        OthelloGame copy =  new OthelloGame(boardCopy);
        copy.availableMoves = new HashSet<>(this.availableMoves);
        copy.whiteToPlay = this.whiteToPlay;
        copy.move = move;
        copy.whitePiecesCount = whitePiecesCount;
        copy.blackPiecesCount = blackPiecesCount;

        return copy;
    }

    public int getWhitePiecesCount() {
        return whitePiecesCount;
    }

    public int getBlackPiecesCount() {
        return blackPiecesCount;
    }




    public static int positionToInt(int row, int column){
        return row * 8 + column;
    }

    public static int[] intToPosition(int pos){
        return new int[] {pos / 8, pos % 8};
    }

    public static String positionToStringPosition(int row, int column) {
        return ((char) (column + 1 + 96)) + "" + (row+1);
    }

    public static String intToStringPosition(int position) {
        int[] pos = intToPosition(position);
        return positionToStringPosition(pos[0], pos[1]);
    }
}
