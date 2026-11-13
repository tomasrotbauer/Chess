package com.tomas.chess.model;

import javax.swing.*;
import java.net.URL;
import java.util.Objects;

public class ChessPiece {
    private final char symbol;
    private final JLabel label;
    private int row;
    private int col;

    public ChessPiece(char symbol, int row, int col) {
        this.symbol = symbol;
        this.row = row;
        this.col = col;

        String filename = (ChessBoard.isWhite(symbol) ? "w" : "b") + symbol + ".png";
        URL image = Objects.requireNonNull(getClass().getResource("/pieces/" + filename),
                "Missing piece image " + filename);
        this.label = new JLabel(new ImageIcon(image));
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
