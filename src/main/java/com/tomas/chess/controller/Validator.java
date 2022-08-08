package com.tomas.chess.controller;

class Validator {
    private int kingRow;
    private int kingCol;
    private final boolean isKingWhite;
    private final boolean maximizingPlayer;

    Validator(char[][] board, boolean isKingWhite, boolean maximizingPlayer) {
    this.isKingWhite = isKingWhite;
    this.maximizingPlayer = maximizingPlayer;
    char king = isKingWhite ? 'k' : 'K';
    for (int i = 0; i < 8; i++)
        for (int j = 0; j < 8; j++)
            if (board[i][j] == king) {
                kingRow = i;
                kingCol = j;
                return;
            }
    }

        public boolean isKingInCheck(char[][] board) {
        //up, down, left, right, ur, ul, dr, dl, knight
        char pawn, bishop, knight, rook, queen, king, piece;
        if (isKingWhite) {
            pawn = 'P';
            bishop = 'B';
            knight = 'N';
            rook = 'R';
            queen = 'Q';
            king = 'K';
        }
        else {
            pawn = 'p';
            bishop = 'b';
            knight = 'n';
            rook = 'r';
            queen = 'q';
            king = 'k';
        }
        //up
        for (int i = kingRow-1; i >= 0; i--) {
            piece = board[i][kingCol];
            if (i == kingRow-1 && piece == king)
                return true;
            else if (piece == rook || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //down
        for (int i = kingRow+1; i < 8; i++) {
            piece = board[i][kingCol];
            if (i == kingRow+1 && piece == king)
                return true;
            else if (piece == rook || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //left
        for (int i = kingCol-1; i >= 0; i--) {
            piece = board[kingRow][i];
            if (i == kingCol-1 && piece == king)
                return true;
            else if (piece == rook || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //right
        for (int i = kingCol+1; i < 8; i++) {
            piece = board[kingRow][i];
            if (i == kingCol+1 && piece == king)
                return true;
            else if (piece == rook || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //UR
        for (int i = 1; kingRow >= i && kingCol + i < 8; i++) {
            piece = board[kingRow-i][kingCol+i];
            if (i == 1) {
                if (piece == king)
                    return true;
                else if (!maximizingPlayer && piece == pawn)
                    return true;
            }
            if (piece == bishop || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //UL
        for (int i = 1; kingRow >= i && kingCol >= i; i++) {
            piece = board[kingRow-i][kingCol-i];
            if (i == 1) {
                if (piece == king)
                    return true;
                else if (!maximizingPlayer && piece == pawn)
                    return true;
            }
            if (piece == bishop || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //DR
        for (int i = 1; kingRow + i < 8 && kingCol + i < 8; i++) {
            piece = board[kingRow+i][kingCol+i];
            if (i == 1) {
                if (piece == king)
                    return true;
                else if (maximizingPlayer && piece == pawn)
                    return true;
            }
            if (piece == bishop || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        //DL
        for (int i = 1; kingRow + i < 8 && kingCol >= i; i++) {
            piece = board[kingRow+i][kingCol-i];
            if (i == 1) {
                if (piece == king)
                    return true;
                else if (maximizingPlayer && piece == pawn)
                    return true;
            }
            if (piece == bishop || piece == queen)
                return true;
            else if (piece != 'e')
                break;
        }
        if (kingRow > 1) {
            if (kingCol > 0 && board[kingRow-2][kingCol-1] == knight)
                return true;
            if (kingCol < 7 && board[kingRow-2][kingCol+1] == knight)
                return true;
        }
        if (kingRow > 0) {
            if (kingCol > 1 && board[kingRow-1][kingCol-2] == knight)
                return true;
            if (kingCol < 6 && board[kingRow-1][kingCol+2] == knight)
                return true;
        }
        if (kingRow < 7) {
            if (kingCol > 1 && board[kingRow+1][kingCol-2] == knight)
                return true;
            if (kingCol < 6 && board[kingRow+1][kingCol+2] == knight)
                return true;
        }
        if (kingRow < 6) {
            if (kingCol > 0 && board[kingRow+2][kingCol-1] == knight)
                return true;
            return kingCol < 7 && board[kingRow + 2][kingCol + 1] == knight;
        }

        return false;
    }

    public void moveKing(int deltaRow, int deltaCol) {
        kingRow += deltaRow;
        kingCol += deltaCol;
        if (kingRow < 0 || kingRow > 7) {
            throw new IndexOutOfBoundsException("Trying to move king out of bounds ");
        }
    }
}