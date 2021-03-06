package controller;

import java.util.ArrayList;
import model.*;

public class Controller implements Runnable {
    
    private final boolean isComputerWhite;
    private static final int DEPTH = 6;
    public int row1, col1, row2, col2;
    private char[][] root;
    private int score;
    private boolean blackKingCastle, blackQueenCastle, whiteKingCastle, whiteQueenCastle;
    
    
    public void run() {
        score = -negamax(root, DEPTH, -1000000, 1000000, -1);
        System.out.println("DONE");
    }
    
    public Controller(char[][] root, int[] move, boolean isComputerWhite, boolean[] castle) {
        this.isComputerWhite = isComputerWhite;
        this.root = root;
        row1 = move[0];
        col1 = move[1];
        row2 = move[2];
        col2 = move[3];
        this.blackKingCastle = castle[0];
        this.blackQueenCastle = castle[1];
        this.whiteKingCastle = castle[2];
        this.whiteQueenCastle = castle[3];
    }
    
    public int getScore() {
        return score;
    }
    
    public void blackCannotCastle(int moveType) {
        if (moveType == -1)
            blackKingCastle = false;
        else if (moveType == -2)
            blackQueenCastle = false;
    }
    
    public boolean canBlackCastle(int moveType) {
        boolean retVal = false;
        if (moveType == -1)
            retVal = blackKingCastle;
        else if (moveType == -2)
            retVal = blackQueenCastle;
        return retVal;
    }
    
    public void whiteCannotCastle(int moveType) {
        if (moveType == -1)
            whiteKingCastle = false;
        else if (moveType == -2)
            whiteQueenCastle = false;
    }
    
    public boolean canWhiteCastle(int moveType) {
        boolean retVal = false;
        if (moveType == -1)
            retVal = whiteKingCastle;
        else if (moveType == -2)
            retVal = whiteQueenCastle;
        return retVal;
    }
    
    public int negamax(char[][] position, int depth, int alpha, int beta, int colour) {
        ArrayList<Integer> positions = new ArrayList<>();
        getAllPositions(positions, position, colour == 1);
        
        if (depth == 0 || positions.isEmpty())
            return colour * staticEvaluation(position, positions.size(), colour == 1, DEPTH - depth);
        
        orderMoves(position, positions);
        int value = -1000000, eval;
        char[][] child = new char[8][8];
        
        int max = positions.size();// > 24 ? 24 : positions.size();
        for (int i = 0; i < max; i+=4) {
            makeMove(position, child, positions, i);
            eval = -negamax(child, depth-1, -beta, -alpha, -colour);
            value = eval > value ? eval : value;
            alpha = value > alpha ? value : alpha;
            if (alpha >= beta)
                break;
        }
        return value;
    }
    
    private void orderMoves(char[][] position, ArrayList<Integer> positions) {
        int switchWith = 0;
        int temp;
        for (int i = 0; i < positions.size(); i+=4) {
            if (positions.get(i+2) < 0)
                continue;
            else if (position[positions.get(i+2)][positions.get(i+3)] != 'e') {
                if (switchWith == i)
                    switchWith += 4;
                else
                    for (int j = i; j < i+4; j++) {
                        temp = positions.get(switchWith);
                        positions.set(switchWith, positions.get(j));
                        positions.set(j, temp);
                        switchWith++;
                    }
            }
        }
    }
    
    public int minimax(char[][] position, int depth, int alpha, int beta, boolean maximizingPlayer, boolean recursiveCall) {
        ArrayList<Integer> positions = new ArrayList<>();
        getAllPositions(positions, position, maximizingPlayer);
        orderMoves(position, positions);
        
        if (depth == 0 || positions.isEmpty())
            return staticEvaluation(position, positions.size(), maximizingPlayer, DEPTH-depth);
        
        char[][] child = new char[8][8];
        
        if (maximizingPlayer) {
            int maxEval = -1000000;
            int eval;
            for (int i = 0; i < positions.size(); i+=4) {
                makeMove(position, child, positions, i);
                eval = minimax(child, depth - 1, alpha, beta, false, true);
                maxEval = eval > maxEval ? eval : maxEval;
                alpha = alpha > maxEval ? alpha : maxEval;
                if (maxEval >= beta)
                    break;
            }
            return maxEval;
        }
        
        else {
            int minEval = 1000000;
            int eval;
            for (int i = 0; i < positions.size(); i+=4) {
                makeMove(position, child, positions, i);
                eval = minimax(child, depth - 1, alpha, beta, true, true);                
                minEval = minEval < eval ? minEval : eval;
                beta = beta < minEval ? beta : minEval;
                if (minEval <= alpha)
                    break;
            }
            return minEval;
        }
    }
    
    private void printFuncCallInfo(int depth, boolean maximizingPlayer, int num) {
        System.out.println("DEPTH = " + depth);
        System.out.println(maximizingPlayer ? "COMPUTER" : "PLAYER");
        System.out.println("NUMBER OF POSITIONS = " + num);
    }
    
    private void printChild(char[][] child) {
        for (int i = 0; i < 8; i++) {
            System.out.println("");
            for (int j = 0; j < 8; j++)
                System.out.print(child[i][j]);
        }
        System.out.println("");
    }
    
    private int staticEvaluation(char[][] position, int options, boolean maximizingPlayer, int depth) {
        if (options == 0) {
            Validator validator = new Validator(position, maximizingPlayer == isComputerWhite, maximizingPlayer);
            if (validator.isKingInCheck(position))
                return maximizingPlayer ? -100000 : (100000-depth);
            else
                return 0;
        }
        char piece;
        int score = 0;
        //First, assume computer is black
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++) {
                piece = position[row][col];
                if (piece == 'e')
                    continue;
                else if (piece == 'P')
                    score += 100;
                else if (piece == 'p')
                    score -= 100;
                else if (piece == 'B')
                    score += 300;
                else if (piece == 'b')
                    score -= 300;
                else if (piece == 'N')
                    score += 300;
                else if (piece == 'n')
                    score -= 300;
                else if (piece == 'R')
                    score += 500;
                else if (piece == 'r')
                    score -= 500;
                else if (piece == 'Q')
                    score += 900;
                else if (piece == 'q')
                    score -= 900;
            }
            
        return isComputerWhite ? -score : score;
    }
    
    public void getAllPositions(ArrayList<Integer> positions, char[][] position, boolean maximizingPlayer) {
        MoveCalculator calc = new MoveCalculator(position, maximizingPlayer == isComputerWhite, maximizingPlayer, positions);
        char piece;
        if (maximizingPlayer ^ isComputerWhite) {
            for (int row = 0; row < 8; row++)
                for (int col = 0; col < 8; col++) {
                    piece = position[row][col];
                    if (piece > 90)
                        continue;
                    else if (piece == 'P')
                        calc.pawn(row, col, maximizingPlayer);
                    else if (piece == 'B')
                        calc.bishop(row, col);
                    else if (piece == 'N')
                        calc.knight(row, col);
                    else if (piece == 'R')
                        calc.rook(row, col);
                    else if (piece == 'Q')
                        calc.queen(row, col);
                    else
                        calc.king(row, col, maximizingPlayer, blackKingCastle, blackQueenCastle);
                }
        }
        else {
            for (int row = 0; row < 8; row++)
                for (int col = 0; col < 8; col++) {
                    piece = position[row][col];
                    if (piece == 'e' || piece < 97)
                        continue;
                    else if (piece == 'p')
                        calc.pawn(row, col, maximizingPlayer);
                    else if (piece == 'b')
                        calc.bishop(row, col);
                    else if (piece == 'n')
                        calc.knight(row, col);
                    else if (piece == 'r')
                        calc.rook(row, col);
                    else if (piece == 'q')
                        calc.queen(row, col);
                    else
                        calc.king(row, col, maximizingPlayer, whiteKingCastle, whiteQueenCastle);
                }
        }
    }
    
    public void makeMove(char[][] position, char[][] child, ArrayList<Integer> positions, int i) {
        for (int j = 0; j < 8; j++)
            for (int k = 0; k < 8; k++)
                child[j][k] = position[j][k];

        int row1, col1, row2, col2;
        row1 = positions.get(i);
        col1 = positions.get(i+1);
        row2 = positions.get(i+2);
        col2 = positions.get(i+3);
        
        child[row1][col1] = 'e';
        
        // king side castle
        if (row2 == -1) {
            if (isComputerWhite) {
                child[row1][0] = 'e';
                child[row1][1] = position[row1][col1];
                child[row1][2] = position[row1][0];
            }
            else {
            child[row1][7] = 'e';
            child[row1][6] = position[row1][col1];
            child[row1][5] = position[row1][7];
            }
        }
        //queen side castle
        else if (row2 == -2) {
            if (isComputerWhite) {
                child[row1][7] = 'e';
                child[row1][5] = position[row1][col1];
                child[row1][4] = position[row1][7];
            }
            else {
                child[row1][0] = 'e';
                child[row1][2] = position[row1][col1];
                child[row1][3] = position[row1][0];
            }
        }
        //promotion
        else if (position[row1][col1] == 'p' && (row2 == 0 || row2 == 7))
            child[row2][col2] = 'q';
        else if (position[row1][col1] == 'P' && (row2 == 0 || row2 == 7))
            child[row2][col2] = 'Q';
        else
            child[row2][col2] = position[row1][col1];
    }
    
    public int validatePlayerMove(ChessBoard board, ChessPiece selectedPiece, int row, int col) {
        //validate
        ArrayList<Integer> legalMoves = new ArrayList<>();
        MoveCalculator calc = new MoveCalculator(board.getSymbolBoard(), 
                                board.isPlayerWhite(),
                                false, legalMoves);
        
        char symbol = selectedPiece.getSymbol();
        int selectedRow = selectedPiece.getRow();
        int selectedCol = selectedPiece.getCol();
        int castling = 0;
        
        if (symbol == 'p' || symbol == 'P')
            calc.pawn(selectedRow, selectedCol, false);
        else if (symbol == 'b' || symbol == 'B')
            calc.bishop(selectedRow, selectedCol);
        else if (symbol == 'n' || symbol == 'N')
            calc.knight(selectedRow, selectedCol);
        else if (symbol == 'r' || symbol == 'R')
            calc.rook(selectedRow, selectedCol);
        else if (symbol == 'q' || symbol == 'Q')
            calc.queen(selectedRow, selectedCol);
        else {
            if (selectedRow == row){
                if ((selectedCol == 3 && col == 1) || (selectedCol == 4 && col == 6))
                    castling = -1;
                else if ((selectedCol == 3 && col == 5) || (selectedCol == 4 && col == 2))
                    castling = -2;
            }
            if (board.isPlayerWhite())
                calc.king(selectedRow, selectedCol, false, whiteKingCastle, whiteQueenCastle);
            else
                calc.king(selectedRow, selectedCol, false, blackKingCastle, blackQueenCastle);
        }
        
        int valid = 0;
        for (int i = 2; i < legalMoves.size(); i+=4) {
            if (legalMoves.get(i) == row && legalMoves.get(i+1) == col) {
                valid = 1;
                break;
            }
            else if (castling < 0 && castling == legalMoves.get(i)) {
                valid = castling;
                break;
            }
        }
        if (valid == 1 && (symbol == 'p' || symbol == 'P') && row == 0)
            valid = 2;
        
        if (valid == 0) {
            System.out.print(selectedRow);
            System.out.print(selectedCol);
            System.out.print(row);
            System.out.println(col);
            
            for (int i = 0; i < legalMoves.size(); i+=4) {
                System.out.print(legalMoves.get(i));
                System.out.print(legalMoves.get(i+1));
                System.out.print(legalMoves.get(i+2));
                System.out.println(legalMoves.get(i+3));
            }
        }
        
        if (valid != 0) {
            if (board.isPlayerWhite()) {
                if (symbol == 'k') {
                    whiteCannotCastle(-1);
                    whiteCannotCastle(-2);
                }
                else if (symbol == 'r') {
                    if (col1 == 0)
                        whiteCannotCastle(-2);
                    else if (col1 == 7)
                        whiteCannotCastle(-1);
                }
            }
            else if (symbol == 'K') {
                blackCannotCastle(-1);
                blackCannotCastle(-2);
            }
            else if (symbol == 'r') {
                if (col1 == 0)
                    blackCannotCastle(-1);
                else if (col1 == 7)
                    blackCannotCastle(-2);
            }
        }
        
        return valid;
    }
}