package com.tomas.chess.controller;

import com.tomas.chess.model.ChessBoard;

import java.util.ArrayList;

class MoveCalculator {
    private final char[][] board;
    private final boolean isWhite;
    private final boolean maximizingPlayer;
    private final ArrayList<Integer> moves;
    private final Validator validator;
    private final boolean getMoves;
    // Sum over this side's legal moves of a weight per moving piece (pawn captures 9 ... queen/king 1),
    // signed so that it is positive for the computer.
    private int weightedMobility;

    public MoveCalculator(char[][] board, boolean isWhite, boolean maximizingPlayer, ArrayList<Integer> moves, boolean getMoves) {
        this.board = board;        
        this.isWhite = isWhite;
        this.maximizingPlayer = maximizingPlayer;
        this.moves = moves;
        this.validator = new Validator(board, isWhite, maximizingPlayer);
        weightedMobility = 0;
        this.getMoves = getMoves;
    }

    int getWeightedMobility() {
        return weightedMobility;
    }

    private int mobilityWeight(char piece) {
        if (piece == 'k' || piece == 'K' || piece == 'q' || piece == 'Q')
            return 1;
        else if (piece == 'r' || piece == 'R')
            return 2;
        else if (piece == 'b' || piece == 'B')
            return 5;
        else if (piece == 'n' || piece == 'N')
            return 6;
        else
            return 9;
    }
    
    private void testMove(int row1, int col1, int row2, int col2) {
        char temp = board[row2][col2], piece = board[row1][col1];
        board[row2][col2] = piece;
        board[row1][col1] = ChessBoard.EMPTY;
        
        if (!validator.isKingInCheck(board)) {
            if (getMoves) {
                moves.add(row1);
                moves.add(col1);
                moves.add(row2);
                moves.add(col2);
            }
            if (maximizingPlayer)
                weightedMobility += mobilityWeight(piece);
            else
                weightedMobility -= mobilityWeight(piece);
        }
        
        board[row1][col1] = piece;
        board[row2][col2] = temp;
    }
    
    
    private boolean isOpponent(char piece) {
        return isWhite ? ChessBoard.isBlack(piece) : ChessBoard.isWhite(piece);
    }

    /** Tries a move to an empty or enemy square. Returns true if a sliding piece is blocked there. */
    private boolean moveHelper(int row1, int col1, int row2, int col2) {
        char target = board[row2][col2];
        if (target == ChessBoard.EMPTY) {
            testMove(row1, col1, row2, col2);
            return false;
        }
        if (isOpponent(target))
            testMove(row1, col1, row2, col2);
        return true;
    }
    
    public void pawn(int row, int col, boolean maximizingPlayer) {
        char original = board[row][col];
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
        
        // pushes don't count towards mobility, only pawn captures do
        if (row == startRow && board[opening][col] == ChessBoard.EMPTY && board[startRow + move][col] == ChessBoard.EMPTY) {
            int saved = weightedMobility;
            testMove(row, col, opening, col);
            weightedMobility = saved;
        }
        
        if (row+move == promotion)
            board[row][col] = isWhite ? 'q' : 'Q';
        
        if (board[row+move][col] == ChessBoard.EMPTY) {
            int saved = weightedMobility;
            testMove(row, col, row + move, col);
            weightedMobility = saved;
        }

        char enPassantVictim = isWhite ? 'A' : 'a';
        for (int side = -1; side <= 1; side += 2) {
            int target = col + side;
            if (target < 0 || target > 7)
                continue;
            if (isOpponent(board[row + move][target]))
                testMove(row, col, row + move, target);
            if (board[row][target] == enPassantVictim) {
                board[row][target] = ChessBoard.EMPTY;
                testMove(row, col, row + move, target);
                board[row][target] = enPassantVictim;
            }
        }

        board[row][col] = original;
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
                if (kingCastle && board[startRow][7] == rook && board[startRow][5] == ChessBoard.EMPTY && board[startRow][6] == ChessBoard.EMPTY)
                    castle(true, false, startRow, king);
                //queen side
                if (queenCastle && board[startRow][0] == rook && board[startRow][1] == ChessBoard.EMPTY && board[startRow][2] == ChessBoard.EMPTY && board[startRow][3] == ChessBoard.EMPTY)
                    castle(false, false, startRow, king);
            }
        }
        else if (row == startRow && col == 3 && !validator.isKingInCheck(board)) {
            //king side
            if (kingCastle && board[startRow][0] == rook && board[startRow][1] == ChessBoard.EMPTY && board[startRow][2] == ChessBoard.EMPTY)
                castle(true, true, startRow, king);
            //queen side
            if (queenCastle && board[startRow][7] == rook && board[startRow][6] == ChessBoard.EMPTY && board[startRow][5] == ChessBoard.EMPTY && board[startRow][4] == ChessBoard.EMPTY)
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
        board[startRow][kingStart] = ChessBoard.EMPTY;
        board[startRow][empty1] = king;
        validator.moveKing(0,move);
        if (!validator.isKingInCheck(board)) {
            board[startRow][empty1] = ChessBoard.EMPTY;
            board[startRow][empty2] = king;
            validator.moveKing(0,move);
            if (!validator.isKingInCheck(board) && getMoves) {
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
        board[startRow][empty2] = ChessBoard.EMPTY;
        board[startRow][empty1] = ChessBoard.EMPTY;
        board[startRow][kingStart] = king;
    }
}