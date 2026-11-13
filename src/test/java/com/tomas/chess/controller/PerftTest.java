package com.tomas.chess.controller;

import com.tomas.chess.model.ChessBoard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Perft: counts every legal move sequence to a fixed depth and compares with known totals
 * (https://www.chessprogramming.org/Perft_Results). Each position is checked from both
 * board orientations (player white at the bottom, and player black at the bottom).
 *
 * The engine only promotes to a queen, so only positions and depths without promotions are used.
 */
class PerftTest {
    private static final String START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";
    private static final String KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
    private static final String POSITION_3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";

    @Test
    void startPosition() {
        assertPerft(START, 20, 400, 8902, 197281);
    }

    @Test
    void kiwipete() {
        // castling, en passant, pins
        assertPerft(KIWIPETE, 48, 2039, 97862);
    }

    @Test
    void position3() {
        // en passant with pinned pawns and checks from pawns
        assertPerft(POSITION_3, 14, 191, 2812, 43238);
    }

    private static void assertPerft(String fen, long... expected) {
        for (boolean playerWhite : new boolean[]{true, false}) {
            Controller controller = new Controller(!playerWhite);
            char[][] position = parseFen(fen, playerWhite);
            boolean whiteToMove = fen.split(" ")[1].equals("w");
            boolean computerToMove = whiteToMove != playerWhite;
            int castling = parseCastling(fen.split(" ")[2]);
            for (int depth = 1; depth <= expected.length; depth++)
                assertEquals(expected[depth - 1], perft(controller, position, depth, computerToMove, castling),
                        "depth " + depth + ", player " + (playerWhite ? "white" : "black"));
        }
    }

    static long perft(Controller controller, char[][] position, int depth, boolean computerToMove, int castling) {
        ArrayList<Integer> moves = new ArrayList<>();
        controller.getAllPositions(moves, position, computerToMove, true, castling);
        if (depth == 1)
            return moves.size() / 4;
        long nodes = 0;
        char[][] child = new char[8][8];
        for (int i = 0; i < moves.size(); i += 4) {
            controller.makeMove(position, child, moves, i);
            int childCastling = controller.updateCastling(castling, position, moves, i);
            nodes += perft(controller, copy(child), depth - 1, !computerToMove, childCastling);
        }
        return nodes;
    }

    private static char[][] copy(char[][] position) {
        char[][] result = new char[8][];
        for (int i = 0; i < 8; i++)
            result[i] = position[i].clone();
        return result;
    }

    /**
     * Converts FEN to this program's board: lowercase/uppercase are swapped (lowercase is white
     * here), and when the player is black the board is rotated 180 degrees.
     */
    static char[][] parseFen(String fen, boolean playerWhite) {
        char[][] board = new char[8][8];
        String[] ranks = fen.split(" ")[0].split("/");
        for (int rank = 0; rank < 8; rank++) {
            int file = 0;
            for (char c : ranks[rank].toCharArray()) {
                if (Character.isDigit(c)) {
                    for (int k = 0; k < c - '0'; k++)
                        set(board, rank, file++, ChessBoard.EMPTY, playerWhite);
                }
                else {
                    char piece = Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);
                    set(board, rank, file++, piece, playerWhite);
                }
            }
        }
        return board;
    }

    private static void set(char[][] board, int rank, int file, char piece, boolean playerWhite) {
        if (playerWhite)
            board[rank][file] = piece;
        else
            board[7 - rank][7 - file] = piece;
    }

    private static int parseCastling(String castling) {
        int rights = 0;
        if (castling.contains("K")) rights |= ChessBoard.WHITE_KINGSIDE;
        if (castling.contains("Q")) rights |= ChessBoard.WHITE_QUEENSIDE;
        if (castling.contains("k")) rights |= ChessBoard.BLACK_KINGSIDE;
        if (castling.contains("q")) rights |= ChessBoard.BLACK_QUEENSIDE;
        return rights;
    }
}
