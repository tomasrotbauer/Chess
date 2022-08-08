package controller;

import model.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.ArrayList;

public class ComputerMove {
    private final Controller controller;
    private final ChessBoard board;
    public int row1, col1, row2, col2, moveType;
    
    public ComputerMove(Controller controller, ChessBoard board) {
        this.controller = controller;
        this.board = board;
    }

    public void makeComputerMove() {
        Random rand = new Random();
        ArrayList<Integer> positions = new ArrayList<>();
        ArrayList<Controller> controllers = new ArrayList<>();
        
        controller.getAllPositions(positions, board.getSymbolBoard(), true);
        if (positions.isEmpty()) {
            System.out.println("Game over");
            moveType = -5;
            return;
        }
        
        ExecutorService exec = Executors.newFixedThreadPool(8);
        
        for (int i = 0; i < positions.size(); i+=4) {
            char[][] child = new char[8][8];
            controller.makeMove(board.getSymbolBoard(), child, positions, i);
            int[] move = {positions.get(i), positions.get(i+1), positions.get(i+2), positions.get(i+3)};
            boolean[] castle = {controller.canBlackCastle(-1), controller.canBlackCastle(-2), 
                                controller.canWhiteCastle(-1), controller.canWhiteCastle(-2)};
            Controller ctrlr = new Controller(child, move, !board.isPlayerWhite(), castle);
            controllers.add(ctrlr);
            Thread t = new Thread(ctrlr);
            exec.submit(t);
        }
        
        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        positions.clear();
        int max = -1000000;
        for (int i = 0; i < controllers.size(); i++) {
            if (max < controllers.get(i).getScore()) {
                max = controllers.get(i).getScore();
                positions.clear();
                positions.add(i);
            }
            else if (max == controllers.get(i).getScore()) {
                positions.add(i);
            }
        }
        System.out.println("Best score : " + max + "\n");
        
        int index = rand.nextInt(positions.size());
        index = positions.get(index);
        row1 = controllers.get(index).row1;
        col1 = controllers.get(index).col1;
        row2 = controllers.get(index).row2;
        col2 = controllers.get(index).col2;
        
        char piece = board.getPiece(row1, col1).getSymbol();
        if (board.isPlayerWhite()) {
            if (piece == 'K') {
                controller.blackCannotCastle(-1);
                controller.blackCannotCastle(-2);
            }
            else if (piece == 'R') {
                if (col1 == 0)
                    controller.blackCannotCastle(-2);
                else if (col1 == 7)
                    controller.blackCannotCastle(-1);
            }
        }
        else if (piece == 'k') {
            controller.whiteCannotCastle(-1);
            controller.whiteCannotCastle(-2);
        }
        else if (piece == 'r') {
            if (col1 == 0)
                controller.whiteCannotCastle(-1);
            else if (col1 == 7)
                controller.whiteCannotCastle(-2);
        }
        
        if (row2 == -1) {
            moveType = -1;
            row2 = row1;
            col2 = board.isPlayerWhite() ? 6 : 1;
        }
        else if (row2 == -2) {
            moveType = -2;
            row2 = row1;
            col2 = board.isPlayerWhite() ? 2 : 5;
        }
        else {
            piece = board.getPiece(row1, col1).getSymbol();
            if ((piece == 'p' || piece == 'P') && row2 == 7)
                moveType = 2;
            else
                moveType = 1;
        }
    }
}
