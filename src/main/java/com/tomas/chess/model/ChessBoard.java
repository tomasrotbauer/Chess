package com.tomas.chess.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Board state for the game being played.
 *
 * Symbols: 'e' = empty, lowercase = white, uppercase = black.
 * b = bishop, k = king, n = knight, p = pawn, q = queen, r = rook.
 * 'a'/'A' is a pawn that has just moved two squares (it can be captured en passant).
 *
 * The player's pieces always start on rows 6-7 (bottom), the computer's on rows 0-1 (top).
 * When the player is black the board is rotated, so the kings start on column 3 instead of 4.
 */
public class ChessBoard {
    public static final char EMPTY = 'e';

    public static final int WHITE_KINGSIDE = 1;
    public static final int WHITE_QUEENSIDE = 2;
    public static final int BLACK_KINGSIDE = 4;
    public static final int BLACK_QUEENSIDE = 8;
    public static final int ALL_CASTLING = 15;

    private final Map<String, Integer> positionCounts = new HashMap<>();
    private final ChessPiece[][] board = new ChessPiece[8][8];
    private final char[][] symbolBoard = new char[8][8];
    private final boolean playerWhite;
    private volatile boolean playerTurn;
    private boolean whiteToMove = true;
    private int castlingRights = ALL_CASTLING;
    private int halfmoveClock = 0;

    public ChessBoard(boolean playerWhite) {
        this.playerWhite = playerWhite;
        this.playerTurn = playerWhite;
    }

    public static boolean isWhite(char piece) {
        return piece >= 'a' && piece <= 'z' && piece != EMPTY;
    }

    public static boolean isBlack(char piece) {
        return piece >= 'A' && piece <= 'Z';
    }

    public char[][] getSymbolBoard() {
        return symbolBoard;
    }

    public ChessPiece getPiece(int row, int col) {
        return board[row][col];
    }

    public boolean isComputerTurn() {
        return !playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public boolean isPlayerWhite() {
        return playerWhite;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public int getCastlingRights() {
        return castlingRights;
    }

    public void initialize() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                place(null, i, j);

        char[] back = playerWhite
                ? new char[]{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}
                : new char[]{'r', 'n', 'b', 'k', 'q', 'b', 'n', 'r'};
        for (int col = 0; col < 8; col++) {
            char white = back[col];
            char black = Character.toUpperCase(white);
            int whiteBack = playerWhite ? 7 : 0, whitePawns = playerWhite ? 6 : 1;
            int blackBack = playerWhite ? 0 : 7, blackPawns = playerWhite ? 1 : 6;
            place(new ChessPiece(white, whiteBack, col), whiteBack, col);
            place(new ChessPiece('p', whitePawns, col), whitePawns, col);
            place(new ChessPiece(black, blackBack, col), blackBack, col);
            place(new ChessPiece('P', blackPawns, col), blackPawns, col);
        }

        whiteToMove = true;
        castlingRights = ALL_CASTLING;
        halfmoveClock = 0;
        positionCounts.clear();
        recordPosition();
    }

    /**
     * Plays a move that has already been validated. Handles castling (a king moving two
     * squares), en passant and promotion. {@code promotion} is the piece letter to promote
     * to (any case); it is ignored for moves that are not promotions.
     *
     * @return the pawn captured en passant, or null
     */
    public ChessPiece movePiece(int startRow, int startCol, int endRow, int endCol, char promotion) {
        ChessPiece piece = board[startRow][startCol];
        char symbol = piece.getSymbol();
        boolean white = isWhite(symbol);
        boolean pawn = symbol == 'p' || symbol == 'P';
        boolean capture = board[endRow][endCol] != null;
        ChessPiece enPassant = null;

        castlingRights = updateCastlingRights(castlingRights, playerWhite, symbol,
                startRow, startCol, endRow, endCol);

        if (pawn && startCol != endCol && board[endRow][endCol] == null) {
            enPassant = board[startRow][endCol];
            place(null, startRow, endCol);
            capture = true;
        }

        if ((symbol == 'k' || symbol == 'K') && Math.abs(endCol - startCol) == 2) {
            boolean towardsHigherCol = endCol > startCol;
            int rookFrom = towardsHigherCol ? 7 : 0;
            int rookTo = towardsHigherCol ? endCol - 1 : endCol + 1;
            ChessPiece rook = board[startRow][rookFrom];
            place(null, startRow, rookFrom);
            place(rook, startRow, rookTo);
        }

        if (pawn && (endRow == 0 || endRow == 7)) {
            char promoted = white ? Character.toLowerCase(promotion) : Character.toUpperCase(promotion);
            piece = new ChessPiece(promoted, endRow, endCol);
        }

        place(null, startRow, startCol);
        place(piece, endRow, endCol);

        clearEnPassantMarks(symbolBoard);
        if (pawn && Math.abs(endRow - startRow) == 2)
            symbolBoard[endRow][endCol] = white ? 'a' : 'A';

        halfmoveClock = (pawn || capture) ? 0 : halfmoveClock + 1;
        whiteToMove = !white;
        recordPosition();
        return enPassant;
    }

    private void place(ChessPiece piece, int row, int col) {
        board[row][col] = piece;
        symbolBoard[row][col] = piece == null ? EMPTY : piece.getSymbol();
        if (piece != null) {
            piece.setRow(row);
            piece.setCol(col);
        }
    }

    /** Turns every 'a'/'A' (just moved two squares) back into an ordinary pawn. */
    public static void clearEnPassantMarks(char[][] position) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                if (position[i][j] == 'a')
                    position[i][j] = 'p';
                else if (position[i][j] == 'A')
                    position[i][j] = 'P';
            }
    }

    /**
     * Returns the castling rights left after a move. Moving the king removes both of its
     * rights; anything moving from or to a corner square removes the right for that rook
     * (it either moved or was captured). {@code endRow} may be negative for an encoded
     * castling move; the king rule covers that case.
     */
    public static int updateCastlingRights(int rights, boolean playerWhite, char piece,
                                           int startRow, int startCol, int endRow, int endCol) {
        if (piece == 'k')
            rights &= ~(WHITE_KINGSIDE | WHITE_QUEENSIDE);
        else if (piece == 'K')
            rights &= ~(BLACK_KINGSIDE | BLACK_QUEENSIDE);
        rights &= ~cornerRight(playerWhite, startRow, startCol);
        if (endRow >= 0)
            rights &= ~cornerRight(playerWhite, endRow, endCol);
        return rights;
    }

    private static int cornerRight(boolean playerWhite, int row, int col) {
        if (row != 0 && row != 7)
            return 0;
        boolean whiteHome = row == (playerWhite ? 7 : 0);
        int kingsideCol = playerWhite ? 7 : 0;
        if (col == kingsideCol)
            return whiteHome ? WHITE_KINGSIDE : BLACK_KINGSIDE;
        if (col == 7 - kingsideCol)
            return whiteHome ? WHITE_QUEENSIDE : BLACK_QUEENSIDE;
        return 0;
    }

    private void recordPosition() {
        String key = positionKey(symbolBoard, whiteToMove, castlingRights);
        Integer count = positionCounts.get(key);
        positionCounts.put(key, count == null ? 1 : count + 1);
    }

    /** How many times this position (with this side to move and these castling rights) has occurred. */
    public int getPositionCount(char[][] position, boolean whiteToMove, int castling) {
        Integer count = positionCounts.get(positionKey(position, whiteToMove, castling));
        return count == null ? 0 : count;
    }

    private static String positionKey(char[][] position, boolean whiteToMove, int castling) {
        StringBuilder sb = new StringBuilder(67);
        for (int i = 0; i < 8; i++)
            sb.append(position[i]);
        return sb.append(whiteToMove ? 'w' : 'b').append(castling).toString();
    }

    public boolean isThreefoldRepetition() {
        return getPositionCount(symbolBoard, whiteToMove, castlingRights) >= 3;
    }

    public boolean isFiftyMoveRule() {
        return halfmoveClock >= 100;
    }

    /** King against king, or king and one bishop or knight against a lone king. */
    public boolean isInsufficientMaterial() {
        int others = 0;
        boolean minorOnly = true;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                char p = Character.toLowerCase(symbolBoard[i][j]);
                if (p == EMPTY || p == 'k')
                    continue;
                others++;
                if (p != 'b' && p != 'n')
                    minorOnly = false;
            }
        return others == 0 || (others == 1 && minorOnly);
    }
}
