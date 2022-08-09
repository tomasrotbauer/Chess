package com.tomas.chess.model;

import java.util.*;

public class ChessBoard {
    private static final Map<String, Integer> positionHash = new HashMap<>();
    private final ChessPiece[][] board;
    private final char[][] symbolBoard;
    private boolean playerTurn;
    private final boolean playerWhite;
    
    public ChessBoard(boolean playerWhite) {
        this.playerWhite = playerWhite;
        this.playerTurn = playerWhite;
        board = new ChessPiece[8][8];
        symbolBoard = new char[8][8];
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
    
    public void initialize() {
        /*
        e = empty
        lowercase = white colour
        uppercase = black colour
        b/B = bishop
        k/K = king
        n/N = knight
        p/P = pawn
        q/Q = queen
        r/R = rook
        */
        
        for (int i = 2; i < 6; i++)
            for (int j = 0; j < 8; j++)
                board[i][j] = null;

        int row = playerWhite ? 1 : 6;
        for (int i = 0; i < 8; i++)
            board[row][i] = new ChessPiece('P',row,i);
        
        row = playerWhite ? 0 : 7;
        board[row][0] = new ChessPiece('R',row,0);
        board[row][1] = new ChessPiece('N',row,1);
        board[row][2] = new ChessPiece('B',row,2);
        board[row][5] = new ChessPiece('B',row,5);
        board[row][6] = new ChessPiece('N',row,6);
        board[row][7] = new ChessPiece('R',row,7);
        
        row = playerWhite ? 6 : 1;
        for (int i = 0; i < 8; i++)
            board[row][i] = new ChessPiece('p',row,i);
        
        row = playerWhite ? 7 : 0;
        board[row][0] = new ChessPiece('r',row,0);
        board[row][1] = new ChessPiece('n',row,1);
        board[row][2] = new ChessPiece('b',row,2);
        board[row][5] = new ChessPiece('b',row,5);
        board[row][6] = new ChessPiece('n',row,6);
        board[row][7] = new ChessPiece('r',row,7);
        
        if (!playerWhite) {
            board[0][3] = new ChessPiece('k',0,3);
            board[0][4] = new ChessPiece('q',0,4);
            board[7][3] = new ChessPiece('K',7,3);
            board[7][4] = new ChessPiece('Q',7,4);
        }
        else {
            board[0][3] = new ChessPiece('Q',0,3);
            board[0][4] = new ChessPiece('K',0,4);
            board[7][3] = new ChessPiece('q',7,3);
            board[7][4] = new ChessPiece('k',7,4);
        }

/*
        board[4][4] = new ChessPiece('p',4,4);
        board[1][3] = new ChessPiece('P', 1,3);


        board[7][0] = new ChessPiece('r',7,0);
        board[6][7] = new ChessPiece('R',6,7);
        board[6][5] = new ChessPiece('P',6,5);
        board[6][3] = new ChessPiece('Q',6,3);
        board[6][1] = new ChessPiece('P',6,1);
        board[6][0] = new ChessPiece('P',6,0);
        board[5][4] = new ChessPiece('K',5,4);
        board[4][6] = new ChessPiece('P',4,6);
        board[4][4] = new ChessPiece('N',4,4);
        board[4][2] = new ChessPiece('q',4,2);
        board[3][4] = new ChessPiece('n',3,4);
        board[1][6] = new ChessPiece('p',1,6);
        board[1][5] = new ChessPiece('p',1,5);
        board[1][2] = new ChessPiece('p',1,2);
        board[1][1] = new ChessPiece('p',1,1);
        board[1][0] = new ChessPiece('p',1,0);
        board[0][5] = new ChessPiece('b',0,5);
        board[0][1] = new ChessPiece('k',0,1);
*/
        
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == null)
                    symbolBoard[i][j] = 'e';
                else
                    symbolBoard[i][j] = board[i][j].getSymbol();
            }

        positionHash.put(hash(symbolBoard), 1);
    }
    
    public ChessPiece movePiece(int startRow, int startCol, int endRow, int endCol) {
        ChessPiece enPassant = null;
        if (startRow == endRow && startCol == endCol)
            return null;
        
        ChessPiece piece = board[startRow][startCol];
        
        if (piece.getSymbol() == 'p' && (endRow == 7 || endRow == 0))
            piece = new ChessPiece('q', endRow, endCol);
        else if (piece.getSymbol() == 'P' && (endRow == 7 || endRow == 0))
            piece = new ChessPiece('Q', endRow, endCol);
        
        piece.setRow(endRow);
        piece.setCol(endCol);
        
        if ((piece.getSymbol() == 'k' || piece.getSymbol() == 'K') && startCol == 4) {
            if (endCol == 6)
                movePiece(startRow, 7, endRow, 5);
            else if (endCol == 2)
                movePiece(startRow, 0, endRow, 3);
        }
        else if ((piece.getSymbol() == 'k' || piece.getSymbol() == 'K') && startCol == 3) {
            if (endCol == 1)
                movePiece(startRow, 0, endRow, 2);
            else if (endCol == 5)
                movePiece(startRow, 7, endRow, 4);
        }
        
        board[endRow][endCol] = piece;
        board[startRow][startCol] = null;

        if (piece.getSymbol() == 'p' && Math.abs(endRow - startRow) == 2)
            symbolBoard[endRow][endCol] = 'a';
        else if (piece.getSymbol() == 'P' && Math.abs(endRow - startRow) == 2)
            symbolBoard[endRow][endCol] = 'A';
        else
            symbolBoard[endRow][endCol] = piece.getSymbol();
        symbolBoard[startRow][startCol] = 'e';

        if (piece.getSymbol() == 'p' && Math.abs(endCol-startCol) == 1 && symbolBoard[startRow][endCol] == 'A') {
            symbolBoard[startRow][endCol] = 'e';
            enPassant = board[startRow][endCol];
            board[startRow][endCol] = null;
        }
        if (piece.getSymbol() == 'P' && Math.abs(endCol-startCol) == 1 && symbolBoard[startRow][endCol] == 'a') {
            symbolBoard[startRow][endCol] = 'e';
            enPassant = board[startRow][endCol];
            board[startRow][endCol] = null;
        }

        for (int j = 0; j < 7; j++) {
            if (!(endRow == 3 && endCol == j)) {
                if (symbolBoard[3][j] == 'a')
                    symbolBoard[3][j] = 'p';
                else if (symbolBoard[3][j] == 'A')
                    symbolBoard[3][j] = 'P';
            }
            if (!(endRow == 4 && endCol == j)) {
                if (symbolBoard[4][j] == 'a')
                    symbolBoard[4][j] = 'p';
                else if (symbolBoard[4][j] == 'A')
                    symbolBoard[4][j] = 'P';
            }
        }

        String hash = hash(symbolBoard);
        if (positionHash.containsKey(hash)) {
            if (positionHash.get(hash) == 2) {
                System.out.println("Draw by repetition.");
            }
            else
                positionHash.put(hash, positionHash.get(hash) + 1);
        }
        else
            positionHash.put(hash, 1);
        return enPassant;
    }

    public static int positionOccurrences(char[][] position) {
        String hash = hash(position);
        if (positionHash.containsKey(hash))
            return positionHash.get(hash);

        return 0;
    }

    private static String hash(char[][] position) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                sb.append(position[i][j]);
        return sb.toString();
    }
}