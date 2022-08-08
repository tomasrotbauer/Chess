package com.tomas.chess;

import com.tomas.chess.model.ChessBoard;
import com.tomas.chess.view.*;

public class Chess {
   
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Chess [w | b]");
            throw new IllegalArgumentException();
        }
        boolean isPlayerWhite = args[0].equals("w");
        
        ChessBoard board = new ChessBoard(isPlayerWhite);
        board.initialize();
        UserInterface window = new UserInterface(board);
        window.drawBoard();
        window.drawPieces();
        
        if (!isPlayerWhite)
            window.computerTurn();
    }
}