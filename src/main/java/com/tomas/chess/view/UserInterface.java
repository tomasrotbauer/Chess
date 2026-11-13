package com.tomas.chess.view;

import com.tomas.chess.controller.ComputerMove;
import com.tomas.chess.controller.Controller;
import com.tomas.chess.model.ChessBoard;
import com.tomas.chess.model.ChessPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class UserInterface extends JLayeredPane {
    private static final int SQUARE = 112;
    private static final int BOARD_SIZE = 8 * SQUARE;
    private static final Integer BACKGROUND_LAYER = 0;
    private static final Integer HIGHLIGHT_LAYER = 1;
    private static final Integer PIECE_LAYER = 2;

    private final ChessBoard board;
    private final JLabel highlight;
    private final Controller controller;
    private final ComputerMove computerMove;
    private final List<JLabel> pieceLabels = new ArrayList<>();
    private ChessPiece selectedPiece;
    private boolean gameOver;

    public UserInterface(ChessBoard board) {
        addMouseListener(getMouseAdapter());
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));

        this.board = board;
        highlight = new JLabel(loadImage("/highlight.png"));
        highlight.setVisible(false);
        add(highlight, HIGHLIGHT_LAYER);

        controller = new Controller(!board.isPlayerWhite());
        computerMove = new ComputerMove(controller, board);
    }

    private ImageIcon loadImage(String resource) {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(resource), "Missing image " + resource));
    }

    public void drawBoard() {
        JLabel background = new JLabel(loadImage("/background.png"));
        background.setBounds(0, 0, BOARD_SIZE, BOARD_SIZE);
        add(background, BACKGROUND_LAYER);
    }

    /** Redraws every piece from the board model. */
    public void drawPieces() {
        for (JLabel label : pieceLabels)
            remove(label);
        pieceLabels.clear();

        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece == null)
                    continue;
                JLabel label = piece.getLabel();
                label.setBounds(col * SQUARE, row * SQUARE, SQUARE, SQUARE);
                add(label, PIECE_LAYER);
                pieceLabels.add(label);
            }
        repaint();
    }

    private void highlightSquare(int row, int col) {
        highlight.setBounds(col * SQUARE, row * SQUARE, SQUARE, SQUARE);
        highlight.setVisible(true);
    }

    private void makeMove(int row1, int col1, int row2, int col2, char promotion) {
        board.movePiece(row1, col1, row2, col2, promotion);
        drawPieces();
    }

    private boolean isPlayerPiece(ChessPiece piece) {
        return board.isPlayerWhite() == ChessBoard.isWhite(piece.getSymbol());
    }

    private char askPromotion() {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        char[] symbols = {'q', 'r', 'b', 'n'};
        int choice = JOptionPane.showOptionDialog(this, "Promote to:", "Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return choice < 0 ? 'q' : symbols[choice];
    }

    public MouseListener getMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver || board.isComputerTurn() || !onBoard(e))
                    return;

                int col = e.getX() / SQUARE;
                int row = e.getY() / SQUARE;
                ChessPiece piece = board.getPiece(row, col);

                if (piece != null && isPlayerPiece(piece)) {
                    selectedPiece = piece;
                    highlightSquare(row, col);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (gameOver || board.isComputerTurn() || selectedPiece == null)
                    return;
                if (!onBoard(e)) {
                    deselect();
                    return;
                }

                int col = e.getX() / SQUARE;
                int row = e.getY() / SQUARE;
                if (row == selectedPiece.getRow() && col == selectedPiece.getCol())
                    return; // a click without dragging: wait for the destination click

                int validation = controller.validatePlayerMove(board, selectedPiece, row, col);
                if (validation == Controller.ILLEGAL) {
                    deselect();
                    return;
                }

                char promotion = validation == Controller.PROMOTION ? askPromotion() : 'q';
                makeMove(selectedPiece.getRow(), selectedPiece.getCol(), row, col, promotion);
                deselect();
                board.setPlayerTurn(false);
                if (!checkGameOver())
                    computerMove();
            }
        };
    }

    private boolean onBoard(MouseEvent e) {
        return e.getX() >= 0 && e.getY() >= 0 && e.getX() < BOARD_SIZE && e.getY() < BOARD_SIZE;
    }

    private void deselect() {
        selectedPiece = null;
        highlight.setVisible(false);
    }

    /** Ends the game if the side to move has no legal moves or a draw rule applies. */
    private boolean checkGameOver() {
        boolean computerToMove = board.isWhiteToMove() != board.isPlayerWhite();
        ArrayList<Integer> moves = new ArrayList<>();
        controller.getAllPositions(moves, board.getSymbolBoard(), computerToMove, true, board.getCastlingRights());

        String message = null;
        if (moves.isEmpty()) {
            if (controller.isInCheck(board.getSymbolBoard(), computerToMove))
                message = "Checkmate. " + (computerToMove ? "You win!" : "The computer wins.");
            else
                message = "Stalemate. The game is a draw.";
        }
        else if (board.isThreefoldRepetition())
            message = "Draw by threefold repetition.";
        else if (board.isFiftyMoveRule())
            message = "Draw by the fifty-move rule.";
        else if (board.isInsufficientMaterial())
            message = "Draw by insufficient material.";

        if (message == null)
            return false;
        gameOver = true;
        JOptionPane.showMessageDialog(this, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /** Starts the computer's search in the background; the move is played on the event thread when it finishes. */
    public void computerMove() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return computerMove.makeComputerMove();
            }

            @Override
            protected void done() {
                boolean moved;
                try {
                    moved = get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    return;
                }
                if (moved) {
                    makeMove(computerMove.getRow1(), computerMove.getCol1(),
                            computerMove.getRow2(), computerMove.getCol2(), 'q');
                    highlightSquare(computerMove.getRow2(), computerMove.getCol2());
                }
                board.setPlayerTurn(true);
                checkGameOver();
            }
        }.execute();
    }
}
