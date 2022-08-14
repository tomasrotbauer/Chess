package com.tomas.chess.view;

import com.tomas.chess.controller.*;
import com.tomas.chess.model.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Objects;

public class UserInterface extends JLayeredPane {
    private final ChessBoard board;
    private final JLabel highlight;
    private ChessPiece selectedPiece;
    private final Controller controller;
    private final ComputerMove computerMove;
    
    public UserInterface(ChessBoard board) {
        addMouseListener(getMouseAdapter());
        setPreferredSize(new Dimension(896,896));
        setVisible(true);
        
        this.board = board;
        selectedPiece = null;
        
        ImageIcon cyanSquare = new ImageIcon(Objects.requireNonNull(getClass().getResource(ChessPiece.sep + "highlight.png")));
        highlight = new JLabel(cyanSquare);
        add(highlight, Integer.valueOf(-1));
        
        int[] move = {-1,-1,-1,-1};
        boolean[] castle = {true, true, true, true};
        controller = new Controller(board.getSymbolBoard(), move, !board.isPlayerWhite(), castle);
        
        computerMove = new ComputerMove(controller, board);
    }
    
    public void drawBoard() {
        ImageIcon bgnd = new ImageIcon(Objects.requireNonNull(getClass().getResource(ChessPiece.sep + "background.png")));
        JLabel bgndLabel = new JLabel(bgnd);
        bgndLabel.setBounds(0,0,896,896);
        add(bgndLabel, Integer.valueOf(0));
    }
    
    public void drawPieces() {
        JLabel label;
        ChessPiece piece;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                piece = board.getPiece(i, j);
                if (piece == null)
                    continue;
                label = piece.getLabel();
                label.setBounds(j*112,i*112,112,112);
                add(label, Integer.valueOf(2));
            }
    }
    
    public void removeLayeredComponent(JLabel label) {
        setLayer(label, -1);
    }

    public void makeMove(int row1, int col1, int row2, int col2, int moveType) {
        ChessPiece from = board.getPiece(row1, col1);
        ChessPiece to = board.getPiece(row2, col2);
        ChessPiece enPassant = board.movePiece(row1, col1, row2, col2);

        if (to != null)
            removeLayeredComponent(to.getLabel());

        if (enPassant != null)
            removeLayeredComponent(enPassant.getLabel());

        JLabel label = from.getLabel();
        label.setBounds(col2*112,row2*112,112,112);
        setLayer(label, 2);

        if (moveType == -1 && board.isPlayerWhite()) {
            label = board.getPiece(row1, 5).getLabel();
            label.setBounds(560,row1*112,112,112);
        }
        else if (moveType == -2 && board.isPlayerWhite()) {
            label = board.getPiece(row1, 3).getLabel();
            label.setBounds(336,row1*112,112,112);
        }
        else if (moveType == -1 && !board.isPlayerWhite()) {
            label = board.getPiece(row1, 2).getLabel();
            label.setBounds(224,row1*112,112,112);
        }
        else if (moveType == -2 && !board.isPlayerWhite()) {
            label = board.getPiece(row1, 4).getLabel();
            label.setBounds(448,row1*112,112,112);
        }
        else if (moveType == 2) {
            removeLayeredComponent(label);
            label = board.getPiece(row2, col2).getLabel();
            label.setBounds(col2*112,row2*112,112,112);
            add(label, Integer.valueOf(2));
        }
        setLayer(label, 2);
    }

    public MouseListener getMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (board.isComputerTurn())
                    return;

                int col = e.getX() / 112;
                int row = e.getY() / 112;

                ChessPiece piece = board.getPiece(row, col);

                if (selectedPiece == null && piece != null) {

                    if (board.isPlayerWhite() && piece.getSymbol() < 97)
                        return;
                    else if (!board.isPlayerWhite() && piece.getSymbol() > 90)
                        return;

                    selectedPiece = piece;
                    highlight.setBounds(col*112,row*112,112,112);
                    setLayer(highlight, 1);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (board.isComputerTurn())
                    return;

                int col = e.getX() / 112;
                int row = e.getY() / 112;

                if (selectedPiece != null) {

                    if (row == selectedPiece.getRow() && col == selectedPiece.getCol())
                        return;

                    ArrayList<Integer> positions = new ArrayList<>();

                    controller.getAllPositions(positions, board.getSymbolBoard(), false, true);
                    if (positions.isEmpty()) {
                        System.out.println("Game over");
                        return;
                    }

                    int validation = controller.validatePlayerMove(board, selectedPiece, row, col);
                    removeLayeredComponent(highlight);

                    if (validation == 0) {
                        selectedPiece = null;
                        return;
                    }

                    makeMove(selectedPiece.getRow(), selectedPiece.getCol(), row, col, validation);
                    selectedPiece = null;
                    board.setPlayerTurn(false);
                    computerMove();
                }
            }
        };
    }

    public void computerMove() {
        ComputerTurn ct = new ComputerTurn();
        try {
            ct.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class ComputerTurn extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            computerTurn();
            return null;
        }
        public void computerTurn() {
            computerMove.makeComputerMove();
            if (computerMove.moveType == -5)
                return;
            makeMove(computerMove.row1, computerMove.col1, computerMove.row2, computerMove.col2, computerMove.moveType);
            highlight.setBounds(computerMove.col2*112, computerMove.row2*112,112,112);
            setLayer(highlight, 1);
            board.setPlayerTurn(true);
        }
    }
}