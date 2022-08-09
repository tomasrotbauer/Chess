package com.tomas.chess;

import com.tomas.chess.model.ChessBoard;
import com.tomas.chess.view.*;

import javax.swing.*;

public class Chess extends JFrame {

    public Chess(boolean isPlayerWhite) {
        super("Rotbitch Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChessBoard board = new ChessBoard(isPlayerWhite);
        board.initialize();
        UserInterface window = new UserInterface(board);
        window.drawBoard();
        window.drawPieces();
        add(window);
        pack();
        setVisible(true);

        if (!isPlayerWhite) {
            try {
                window.computerMove();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
   
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Chess [w | b]");
            throw new IllegalArgumentException();
        }
        final boolean isPlayerWhite = args[0].equals("w");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                new Chess(isPlayerWhite);
            }
        };

        SwingUtilities.invokeLater(r);
    }
}