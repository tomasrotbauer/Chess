package com.tomas.chess.controller;

import com.tomas.chess.model.ChessBoard;
import com.tomas.chess.model.ChessPiece;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Move generation and search. The computer is the "maximizing" side and always plays from
 * the top of the board (rows 0-1).
 *
 * Moves are stored four ints at a time in an ArrayList: row1, col1, row2, col2. A castling
 * move has row2 == col2 == CASTLE_KINGSIDE or CASTLE_QUEENSIDE.
 */
public class Controller implements Runnable {
    public static final int ILLEGAL = 0;
    public static final int NORMAL = 1;
    public static final int PROMOTION = 2;
    public static final int CASTLE_KINGSIDE = -1;
    public static final int CASTLE_QUEENSIDE = -2;

    static final int INFINITY = 1000000;
    static final int MATE = 100000;

    private final boolean isComputerWhite;

    // Only used when this Controller is a search task for one root move.
    private final char[][] root;
    private final int[] rootMove;
    private final int rootCastling;
    private final int depth;
    private final long deadline;
    private final AtomicInteger bestRootScore;
    private volatile boolean aborted;
    private int score;

    /** A controller for move generation and validation only. */
    public Controller(boolean isComputerWhite) {
        this(null, null, isComputerWhite, 0, 0, Long.MAX_VALUE, null);
    }

    /**
     * A search task: scores the position {@code root} reached by the computer playing
     * {@code move}, searching {@code depth} more plies after it. {@code bestRootScore} holds the
     * best score found so far among the sibling root moves and is used as alpha.
     */
    Controller(char[][] root, int[] move, boolean isComputerWhite, int castling, int depth,
               long deadline, AtomicInteger bestRootScore) {
        this.isComputerWhite = isComputerWhite;
        this.root = root;
        this.rootMove = move;
        this.rootCastling = castling;
        this.depth = depth;
        this.deadline = deadline;
        this.bestRootScore = bestRootScore;
    }

    @Override
    public void run() {
        // Alpha one below the best so far: moves equal to the best still get an exact score,
        // so ties can be broken randomly.
        int alpha = bestRootScore.get() - 1;
        score = -negamax(root, depth, 1, -INFINITY, -alpha, -1, rootCastling);
        if (!aborted)
            bestRootScore.accumulateAndGet(score, Math::max);
    }

    int[] getRootMove() {
        return rootMove;
    }

    int getScore() {
        return score;
    }

    void setScore(int score) {
        this.score = score;
    }

    boolean wasAborted() {
        return aborted;
    }

    /**
     * Score of {@code position} from the side to move's point of view
     * ({@code colour} 1 = computer, -1 = player).
     */
    private int negamax(char[][] position, int depth, int ply, int alpha, int beta, int colour, int castling) {
        if (System.nanoTime() > deadline) {
            aborted = true;
            return 0;
        }

        boolean computerToMove = colour == 1;
        ArrayList<Integer> moves = new ArrayList<>();
        int mobility = getAllPositions(moves, position, computerToMove, true, castling);

        if (moves.isEmpty()) {
            if (!isInCheck(position, computerToMove))
                return 0; // stalemate
            // Prefer the fastest mate, and the slowest when being mated.
            return -(MATE - ply);
        }
        if (depth == 0)
            return colour * evaluate(position, mobility, computerToMove, castling);

        orderMoves(position, moves);
        int value = -INFINITY;
        char[][] child = new char[8][8];

        for (int i = 0; i < moves.size(); i += 4) {
            makeMove(position, child, moves, i);
            int eval = -negamax(child, depth - 1, ply + 1, -beta, -alpha, -colour,
                    updateCastling(castling, position, moves, i));
            if (aborted)
                return 0;
            value = Math.max(eval, value);
            alpha = Math.max(value, alpha);
            if (alpha >= beta)
                break;
        }
        return value;
    }

    /** Captures first, so alpha-beta cuts off sooner. */
    private void orderMoves(char[][] position, ArrayList<Integer> moves) {
        int switchWith = 0;
        for (int i = 0; i < moves.size(); i += 4) {
            if (moves.get(i + 2) >= 0 && position[moves.get(i + 2)][moves.get(i + 3)] != ChessBoard.EMPTY) {
                if (switchWith != i)
                    for (int j = i; j < i + 4; j++) {
                        int temp = moves.get(switchWith + j - i);
                        moves.set(switchWith + j - i, moves.get(j));
                        moves.set(j, temp);
                    }
                switchWith += 4;
            }
        }
    }

    /** Static evaluation from the computer's point of view: material plus weighted mobility of both sides. */
    private int evaluate(char[][] position, int sideMobility, boolean computerToMove, int castling) {
        int opponentMobility = getAllPositions(new ArrayList<Integer>(), position, !computerToMove, false, castling);

        int material = 0; // positive = good for black
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++) {
                char piece = position[row][col];
                int value;
                switch (Character.toLowerCase(piece)) {
                    case 'p': case 'a': value = 100; break;
                    case 'b': case 'n': value = 300; break;
                    case 'r': value = 500; break;
                    case 'q': value = 900; break;
                    default: value = 0;
                }
                material += ChessBoard.isBlack(piece) ? value : -value;
            }
        if (isComputerWhite)
            material = -material;

        // both mobility terms are already signed from the computer's point of view
        return material + sideMobility + opponentMobility;
    }

    public boolean isInCheck(char[][] position, boolean computer) {
        return new Validator(position, computer == isComputerWhite, computer).isKingInCheck(position);
    }

    /**
     * Appends every legal move for one side to {@code moves} (if {@code getMoves}) and returns
     * that side's weighted mobility. {@code position} is modified temporarily and restored.
     */
    public int getAllPositions(ArrayList<Integer> moves, char[][] position, boolean computer,
                               boolean getMoves, int castling) {
        boolean white = computer == isComputerWhite;
        MoveCalculator calc = new MoveCalculator(position, white, computer, moves, getMoves);
        boolean kingside = (castling & (white ? ChessBoard.WHITE_KINGSIDE : ChessBoard.BLACK_KINGSIDE)) != 0;
        boolean queenside = (castling & (white ? ChessBoard.WHITE_QUEENSIDE : ChessBoard.BLACK_QUEENSIDE)) != 0;

        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++) {
                char piece = position[row][col];
                if (white ? !ChessBoard.isWhite(piece) : !ChessBoard.isBlack(piece))
                    continue;
                switch (Character.toLowerCase(piece)) {
                    case 'p': case 'a': calc.pawn(row, col, computer); break;
                    case 'b': calc.bishop(row, col); break;
                    case 'n': calc.knight(row, col); break;
                    case 'r': calc.rook(row, col); break;
                    case 'q': calc.queen(row, col); break;
                    case 'k': calc.king(row, col, computer, kingside, queenside); break;
                }
            }
        return calc.getWeightedMobility();
    }

    /** Castling rights after playing move {@code i} of {@code moves} in {@code position}. */
    int updateCastling(int castling, char[][] position, ArrayList<Integer> moves, int i) {
        int row1 = moves.get(i), col1 = moves.get(i + 1);
        return ChessBoard.updateCastlingRights(castling, !isComputerWhite, position[row1][col1],
                row1, col1, moves.get(i + 2), moves.get(i + 3));
    }

    /** Copies {@code position} into {@code child} and plays move {@code i} of {@code moves} on it. Promotes to a queen. */
    public void makeMove(char[][] position, char[][] child, ArrayList<Integer> moves, int i) {
        for (int j = 0; j < 8; j++)
            System.arraycopy(position[j], 0, child[j], 0, 8);
        ChessBoard.clearEnPassantMarks(child);

        int row1 = moves.get(i), col1 = moves.get(i + 1);
        int row2 = moves.get(i + 2), col2 = moves.get(i + 3);
        char piece = position[row1][col1];
        child[row1][col1] = ChessBoard.EMPTY;

        if (row2 == CASTLE_KINGSIDE || row2 == CASTLE_QUEENSIDE) {
            // Kings start on column 3 when the computer is white (rotated board), else column 4.
            boolean towardsLowerCol = (row2 == CASTLE_KINGSIDE) == isComputerWhite;
            int rookCol = towardsLowerCol ? 0 : 7;
            int kingCol = towardsLowerCol ? col1 - 2 : col1 + 2;
            int newRookCol = towardsLowerCol ? kingCol + 1 : kingCol - 1;
            child[row1][rookCol] = ChessBoard.EMPTY;
            child[row1][newRookCol] = position[row1][rookCol];
            child[row1][kingCol] = piece;
            return;
        }

        boolean white = ChessBoard.isWhite(piece);
        boolean pawn = Character.toLowerCase(piece) == 'p' || Character.toLowerCase(piece) == 'a';
        if (pawn) {
            if (col1 != col2 && position[row2][col2] == ChessBoard.EMPTY)
                child[row1][col2] = ChessBoard.EMPTY; // en passant
            if (row2 == 0 || row2 == 7)
                piece = white ? 'q' : 'Q';
            else if (Math.abs(row1 - row2) == 2)
                piece = white ? 'a' : 'A';
            else
                piece = white ? 'p' : 'P';
        }
        child[row2][col2] = piece;
    }

    /**
     * Checks a move by the player.
     *
     * @return ILLEGAL, NORMAL, PROMOTION, CASTLE_KINGSIDE or CASTLE_QUEENSIDE
     */
    public int validatePlayerMove(ChessBoard board, ChessPiece selectedPiece, int row, int col) {
        ArrayList<Integer> legalMoves = new ArrayList<>();
        getAllPositions(legalMoves, board.getSymbolBoard(), false, true, board.getCastlingRights());

        char symbol = selectedPiece.getSymbol();
        int fromRow = selectedPiece.getRow(), fromCol = selectedPiece.getCol();
        boolean king = symbol == 'k' || symbol == 'K';
        int castling = 0;
        if (king && row == fromRow && Math.abs(col - fromCol) == 2)
            castling = col == (board.isPlayerWhite() ? 6 : 1) ? CASTLE_KINGSIDE : CASTLE_QUEENSIDE;

        for (int i = 0; i < legalMoves.size(); i += 4) {
            if (legalMoves.get(i) != fromRow || legalMoves.get(i + 1) != fromCol)
                continue;
            int toRow = legalMoves.get(i + 2), toCol = legalMoves.get(i + 3);
            if (castling != 0 && toRow == castling)
                return castling;
            if (castling == 0 && toRow == row && toCol == col) {
                boolean pawn = symbol == 'p' || symbol == 'P';
                return pawn && row == 0 ? PROMOTION : NORMAL;
            }
        }
        return ILLEGAL;
    }
}
