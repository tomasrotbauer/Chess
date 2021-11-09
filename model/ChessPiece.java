package model;

import javax.swing.*;

public class ChessPiece {
    private char symbol;
    private JLabel label;
    private int row;
    private int col;

    public ChessPiece(char symbol, int row, int col) {
        this.symbol = symbol;
        this.row = row;
        this.col = col;

        String filename;
        if (symbol > 90) //white piece
            filename = "view/pieces/w" + symbol + ".png";
        else
            filename = "view/pieces/b" + symbol + ".png";

        ImageIcon img = new ImageIcon(filename);
        this.label = new JLabel(img);
    }

    public JLabel getLabel() {
        return label;
    }

    public char getSymbol() {
        return symbol;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    void setRow(int row) {
        this.row = row;
    }

    void setCol(int col) {
        this.col = col;
    }
}