package view;

import controller.*;
import model.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class UserInterface implements MouseListener {
    private JFrame frame;
    private JPanel panel;
    private JLayeredPane layeredPane;
    private final ChessBoard board;
    private JLabel highlight;
    private ChessPiece selectedPiece;
    private Controller controller;
    private ComputerMove computerMove;
    
    public UserInterface(ChessBoard board) {
        frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new JPanel();
        panel.setPreferredSize(new Dimension(896,896));
        panel.addMouseListener(this);
        
        layeredPane = frame.getLayeredPane();
        layeredPane.setPreferredSize(new Dimension(896,896));
        
        frame.add(panel); 
        frame.setResizable(false);
        frame.setVisible(true);
        frame.pack();
        
        this.board = board;
        selectedPiece = null;
        
        ImageIcon cyanSquare = new ImageIcon("view/highlight.png");
        highlight = new JLabel(cyanSquare);
        layeredPane.add(highlight, Integer.valueOf(-1));
        
        int[] move = {-1,-1,-1,-1};
        boolean[] castle = {true, true, true, true};
        controller = new Controller(board.getSymbolBoard(), move, !board.isPlayerWhite(), castle);
        
        computerMove = new ComputerMove(controller, board);
    }
    
    public void drawBoard(boolean isPlayerWhite) {
        ImageIcon bgnd = new ImageIcon("view/background.png");
        JLabel bgndLabel = new JLabel(bgnd);
        bgndLabel.setBounds(0,0,896,896);
        layeredPane.add(bgndLabel, Integer.valueOf(0));
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
                layeredPane.add(label, Integer.valueOf(2));
            }
    }
    
    public void removeLayeredComponent(JLabel label) {
        layeredPane.setLayer(label, Integer.valueOf(-1));
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    
    public void computerTurn() {
        computerMove.makeComputerMove();
        if (computerMove.moveType == -5)
            return;
        makeMove(computerMove.row1, computerMove.col1, computerMove.row2, computerMove.col2, computerMove.moveType);
        highlight.setBounds(computerMove.col2*112, computerMove.row2*112,112,112);
        layeredPane.setLayer(highlight, Integer.valueOf(1));
        board.setPlayerTurn(true);
    }
    
    public void makeMove(int row1, int col1, int row2, int col2, int moveType) {
        ChessPiece from = board.getPiece(row1, col1);
        ChessPiece to = board.getPiece(row2, col2);
        board.movePiece(row1, col1, row2, col2);
        
        if (to != null)
            removeLayeredComponent(to.getLabel());
        
        JLabel label = from.getLabel();
        label.setBounds(col2*112,row2*112,112,112);
        layeredPane.setLayer(label, Integer.valueOf(2));
        
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
            layeredPane.add(label, Integer.valueOf(2));
        }
        layeredPane.setLayer(label, Integer.valueOf(2));
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
        if (!board.isPlayerTurn())
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
            layeredPane.setLayer(highlight, Integer.valueOf(1));
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (!board.isPlayerTurn())
            return;

        int col = e.getX() / 112;
        int row = e.getY() / 112;
        
        if (selectedPiece != null) {
            
            if (row == selectedPiece.getRow() && col == selectedPiece.getCol())
                return;
            
            ArrayList<Integer> positions = new ArrayList<>();
        
            controller.getAllPositions(positions, board.getSymbolBoard(), false);
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
            computerTurn();
        }
    }
}