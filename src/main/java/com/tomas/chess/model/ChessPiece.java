package com.tomas.chess.model;

import javax.swing.*;
import java.util.Objects;

public class ChessPiece {
    public static String sep = System.getProperty("file.separator");
    private final char symbol;
    private final JLabel label;
    private int row;
    private int col;

    public ChessPiece(char symbol, int row, int col) {
        this.symbol = symbol;
        this.row = row;
        this.col = col;

        String filename;
        if (symbol > 90) //white piece
            filename = "w" + symbol + ".png";
        else
            filename = "b" + symbol + ".png";

        ImageIcon img = new ImageIcon(Objects.requireNonNull(getClass().getResource(sep + "pieces" + sep + filename)));
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