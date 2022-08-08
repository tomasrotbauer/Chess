package controller;

import java.util.ArrayList;

public class MoveCalculator {
    private final char[][] board;
    private final boolean isWhite;
    private final ArrayList<Integer> moves;
    private final Validator validator;
    
    public MoveCalculator(char[][] board, boolean isWhite, boolean maximizingPlayer, ArrayList<Integer> moves) {
        this.board = board;        
        this.isWhite = isWhite;
        this.moves = moves;
        this.validator = new Validator(board, isWhite, maximizingPlayer);
    }
    
    private void testMove(int row1, int col1, int row2, int col2) {
        char temp = board[row2][col2];
        board[row2][col2] = board[row1][col1];
        board[row1][col1] = 'e';
        
        if (!validator.isKingInCheck(board)) {
            moves.add(row1); moves.add(col1); moves.add(row2); moves.add(col2);
        }
        
        board[row1][col1] = board[row2][col2];
        board[row2][col2] = temp;
    }
    
    
    private boolean moveHelper(int row1, int col1, int row2, int col2) {
        if (isWhite) {
            if (board[row2][col2] == 'e') {
                testMove(row1, col1, row2, col2);
                return false;
            }
            else if (board[row2][col2] < 97) {
                testMove(row1, col1, row2, col2);
                return true;
            }
            else
                return true;
        }
        else {
            if (board[row2][col2] == 'e') {
                testMove(row1, col1, row2, col2);
                return false;
            }
            else if (board[row2][col2] > 90) {
                testMove(row1, col1, row2, col2);
                return true;
            }
            else
                return true;
        }
    }
    
    public void pawn(int row, int col, boolean maximizingPlayer) {
        int startRow, opening, move, promotion;
        if (maximizingPlayer) {
            startRow = 1;
            opening = 3;
            move = 1;
            promotion = 7;
        }
        else {
            startRow = 6;
            opening = 4;
            move = -1;
            promotion = 0;
        }
        
        if (row == startRow && board[opening][col] == 'e' && board[startRow + move][col] == 'e')
            testMove(row, col, opening, col);
        
        if (row+move == promotion)
            board[row][col] = isWhite ? 'q' : 'Q';
        
        if (board[row+move][col] == 'e')
            testMove(row, col, row+move, col);

        if (isWhite) {
            if (col > 0) {
                if (board[row + move][col - 1] < 97)
                    testMove(row, col, row + move, col - 1);
                if (board[row][col - 1] == 'A') {
                    board[row][col-1]='e';
                    testMove(row, col, row + move, col - 1);
                    board[row][col-1]='A';
                }
            }
            if (col < 7) {
                if (board[row+move][col+1] < 97)
                    testMove(row, col, row + move, col + 1);
                if (board[row][col+1] == 'A') {
                    board[row][col+1]='e';
                    testMove(row, col, row + move, col + 1);
                    board[row][col+1]='A';
                }
            }
        }
        else {
            if (col > 0) {
                if (board[row + move][col - 1] > 90 && board[row + move][col - 1] != 'e')
                    testMove(row, col, row + move, col - 1);
                if (board[row][col - 1] == 'a') {
                    board[row][col-1]='e';
                    testMove(row, col, row + move, col - 1);
                    board[row][col-1]='a';
                }
            }
            if (col < 7) {
                if(board[row+move][col+1] > 90 && board[row+move][col+1] != 'e')
                    testMove(row, col, row+move, col+1);
                if (board[row][col+1] == 'a') {
                    board[row][col+1]='e';
                    testMove(row, col, row + move, col + 1);
                    board[row][col+1]='a';
                }
            }
        }
        
        board[row][col] = isWhite ? 'p' : 'P';
    }
    
    public void bishop(int row, int col) {
        //UR
        for (int i = 1; row >= i && col + i < 8; i++)
            if (moveHelper(row, col, row-i, col+i))
                break;
        //UL
        for (int i = 1; row >= i && col >= i; i++)
            if (moveHelper(row, col, row-i, col-i))
                break;
        //DR
        for (int i = 1; row + i < 8 && col + i < 8; i++)
            if (moveHelper(row, col, row+i, col+i))
                break;
        //DL
        for (int i = 1; row + i < 8 && col >= i; i++)
            if (moveHelper(row, col, row+i, col-i))
                break;
    }
    
    public void knight(int row, int col) {
        if (row > 1) {
            if (col > 0)
                moveHelper(row, col, row-2, col-1);
            if (col < 7)
                moveHelper(row, col, row-2, col+1);
        }
        if (row > 0) {
            if (col > 1)
                moveHelper(row, col, row-1, col-2);
            if (col < 6)
                moveHelper(row, col, row-1, col+2);
        }
        if (row < 7) {
            if (col > 1)
                moveHelper(row, col, row+1, col-2);
            if (col < 6)
                moveHelper(row, col, row+1, col+2);
        }
        if (row < 6) {
            if (col > 0)
                moveHelper(row, col, row+2, col-1);
            if (col < 7)
                moveHelper(row, col, row+2, col+1);
        }
    }
    
    public void rook(int row, int col) {
        for (int i = 1; row >= i; i++)
            if (moveHelper(row, col, row-i, col))
                break;
        
        for (int i = 1; row + i < 8; i++)
            if (moveHelper(row, col, row+i, col))
                break;
        
        for (int i = 1; col >= i; i++)
            if (moveHelper(row, col, row, col-i))
                break;
        
        for (int i = 1; col + i < 8; i++)
            if (moveHelper(row, col, row, col+i))
                break;
    }
    
    public void queen(int row, int col) {
        bishop(row, col);
        rook(row, col);
    }
    
    public void king (int row, int col, boolean maximizingPlayer, boolean kingCastle, boolean queenCastle) {
        if (row > 0) {
            validator.moveKing(-1,0);
            moveHelper(row, col, row-1, col);
            if (col > 0) {
                validator.moveKing(0,-1);
                moveHelper(row, col, row-1, col-1);
                validator.moveKing(0,1);
            }
            if (col < 7) {
                validator.moveKing(0,1);
                moveHelper(row, col, row-1, col+1);
                validator.moveKing(0,-1);
            }
            validator.moveKing(1,0);
        }
        if (row < 7) {
            validator.moveKing(1,0);
            moveHelper(row, col, row+1, col);
            if (col > 0) {
                validator.moveKing(0,-1);
                moveHelper(row, col, row+1, col-1);
                validator.moveKing(0,1);
            }
            if (col < 7) {
                validator.moveKing(0,1);
                moveHelper(row, col, row+1, col+1);
                validator.moveKing(0,-1);
            }
            validator.moveKing(-1,0);
        }
        if (col > 0) {
            validator.moveKing(0,-1);
            moveHelper(row, col, row, col-1);
            validator.moveKing(0,1);
        }
        if (col < 7) {
            validator.moveKing(0,1);
            moveHelper(row, col, row, col+1);
            validator.moveKing(0,-1);
        }
        
        int startRow = maximizingPlayer ? 0 : 7;
        char king = isWhite ? 'k' : 'K';
        char rook = isWhite ? 'r' : 'R';
        if (maximizingPlayer ^ isWhite) {
            if (row == startRow && col == 4 && !validator.isKingInCheck(board)) {
                //king side
                if (kingCastle && board[startRow][7] == rook && board[startRow][5] == 'e' && board[startRow][6] == 'e')
                    castle(true, false, startRow, king);
                //queen side
                if (queenCastle && board[startRow][0] == rook && board[startRow][1] == 'e' && board[startRow][2] == 'e' && board[startRow][3] == 'e')
                    castle(false, false, startRow, king);
            }
        }
        else if (row == startRow && col == 3 && !validator.isKingInCheck(board)) {
            //king side
            if (kingCastle && board[startRow][0] == rook && board[startRow][1] == 'e' && board[startRow][2] == 'e')
                castle(true, true, startRow, king);
            //queen side
            if (queenCastle && board[startRow][7] == rook && board[startRow][6] == 'e' && board[startRow][5] == 'e' && board[startRow][4] == 'e')
                castle(false, true, startRow, king);
        }
    }
    
    private void castle(boolean kingSide, boolean maxWhite, int startRow, char king) {
        int empty1, empty2, kingStart = maxWhite ? 3 : 4, move = kingSide ^ maxWhite ? 1 : -1;
        if (maxWhite) {
            if (kingSide) {
                empty1 = 2;
                empty2 = 1;
            }
            else {
                empty1 = 4;
                empty2 = 5;
            }
        }
        else {
            if (kingSide) {
                empty1 = 5;
                empty2 = 6;
            }
            else {
                empty1 = 3;
                empty2 = 2;
            }
        }
        board[startRow][kingStart] = 'e';
        board[startRow][empty1] = king;
        validator.moveKing(0,move);
        if (!validator.isKingInCheck(board)) {
            board[startRow][empty1] = 'e';
            board[startRow][empty2] = king;
            validator.moveKing(0,move);
            if (!validator.isKingInCheck(board)) {
                moves.add(startRow); moves.add(kingStart); 
                if (kingSide) {
                    moves.add(-1); moves.add(-1); //king side castle
                }
                else {
                    moves.add(-2); moves.add(-2); //queen side castle
                }
            }
            validator.moveKing(0,-1*move);
        }
        validator.moveKing(0,-1*move);
        board[startRow][empty2] = 'e';
        board[startRow][empty1] = 'e';
        board[startRow][kingStart] = king;
    }
}

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