package com.tomas.chess.controller;

import com.tomas.chess.model.ChessBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Picks the computer's move with iterative deepening: it searches 1 ply, then 2, 3, ...
 * until the time limit, and plays the best move from the deepest search that finished.
 * Depths up to MIN_DEPTH are always finished, so a move can take longer than the time limit.
 */
public class ComputerMove {
    private static final long TIME_LIMIT_NANOS = 3_000_000_000L;
    /** Plies searched after the root move that are always completed, whatever the time limit. */
    private static final int MIN_DEPTH = 6;
    private static final int MAX_DEPTH = 12;

    private final Controller controller;
    private final ChessBoard board;
    private final Random rand = new Random();
    private int row1, col1, row2, col2, moveType;

    public ComputerMove(Controller controller, ChessBoard board) {
        this.controller = controller;
        this.board = board;
    }

    public int getRow1() { return row1; }
    public int getCol1() { return col1; }
    public int getRow2() { return row2; }
    public int getCol2() { return col2; }

    /** Controller.NORMAL, PROMOTION, CASTLE_KINGSIDE or CASTLE_QUEENSIDE. */
    public int getMoveType() { return moveType; }

    /**
     * Chooses a move and stores it in the getters. Does not play it.
     *
     * @return false if the computer has no legal move
     */
    public boolean makeComputerMove() {
        char[][] position = new char[8][8];
        for (int i = 0; i < 8; i++)
            position[i] = board.getSymbolBoard()[i].clone();
        int castling = board.getCastlingRights();
        boolean computerWhite = !board.isPlayerWhite();

        ArrayList<Integer> moves = new ArrayList<>();
        controller.getAllPositions(moves, position, true, true, castling);
        if (moves.isEmpty())
            return false;

        long deadline = System.nanoTime() + TIME_LIMIT_NANOS;
        List<Controller> best = null;
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        try {
            for (int depth = 0; depth <= MAX_DEPTH; depth++) {
                // Iterations up to MIN_DEPTH always run to completion, even past the time limit.
                long iterationDeadline = depth <= MIN_DEPTH ? Long.MAX_VALUE : deadline;
                AtomicInteger bestScore = new AtomicInteger(-Controller.INFINITY);
                List<Controller> tasks = new ArrayList<>();
                List<Future<?>> futures = new ArrayList<>();

                for (int i = 0; i < moves.size(); i += 4) {
                    char[][] child = new char[8][8];
                    controller.makeMove(position, child, moves, i);
                    int childCastling = controller.updateCastling(castling, position, moves, i);
                    int[] move = {moves.get(i), moves.get(i + 1), moves.get(i + 2), moves.get(i + 3)};
                    Controller task = new Controller(child, move, computerWhite, childCastling, depth,
                            iterationDeadline, bestScore);
                    tasks.add(task);
                    // Playing into a position for the third time ends the game in a draw.
                    if (board.getPositionCount(child, board.isPlayerWhite(), childCastling) >= 2)
                        task.setScore(0);
                    else
                        futures.add(exec.submit(task));
                }

                for (Future<?> f : futures)
                    f.get();

                boolean finished = true;
                for (Controller task : tasks)
                    if (task.wasAborted())
                        finished = false;
                if (!finished)
                    break;

                best = bestMoves(tasks);
                int score = best.get(0).getScore();
                if (Math.abs(score) > Controller.MATE - 1000 || (depth >= MIN_DEPTH && System.nanoTime() > deadline))
                    break; // forced mate found either way, or out of time
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Search interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Search failed", e.getCause());
        } finally {
            exec.shutdownNow();
        }

        int[] move = best.get(rand.nextInt(best.size())).getRootMove();
        row1 = move[0];
        col1 = move[1];
        row2 = move[2];
        col2 = move[3];

        if (row2 == Controller.CASTLE_KINGSIDE || row2 == Controller.CASTLE_QUEENSIDE) {
            moveType = row2;
            boolean kingside = row2 == Controller.CASTLE_KINGSIDE;
            row2 = row1;
            if (board.isPlayerWhite())
                col2 = kingside ? 6 : 2;
            else
                col2 = kingside ? 1 : 5;
        }
        else {
            char piece = position[row1][col1];
            moveType = (piece == 'p' || piece == 'P') && row2 == 7 ? Controller.PROMOTION : Controller.NORMAL;
        }
        return true;
    }

    private static List<Controller> bestMoves(List<Controller> tasks) {
        List<Controller> best = new ArrayList<>();
        int max = Integer.MIN_VALUE;
        for (Controller task : tasks) {
            if (task.getScore() > max) {
                max = task.getScore();
                best.clear();
            }
            if (task.getScore() == max)
                best.add(task);
        }
        return best;
    }
}
