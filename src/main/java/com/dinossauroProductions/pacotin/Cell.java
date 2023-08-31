package com.dinossauroProductions.pacotin;

public class Cell {

    private int celula;
    private boolean wasClicked;
    private boolean isFlagged;


    public Cell() {

        celula = 0;
        wasClicked = false;
        isFlagged = false;

    }

    public static void generateMap(Cell[][] campo, int size, int mines) {

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                campo[x][y] = new Cell();

                //if(x == 0 || y == 0 || x == size-1 || y == size-1)
                    //System.out.println(campo[x][y].getCelula());
            }
        }

        int counter = mines;

        do {

            int x = Game.generateRandom(0, size - 1);
            int y = Game.generateRandom(0, size - 1);

            if (campo[x][y].getCelula() != -1) {

                campo[x][y].setCelula(-1);
                incrementNearbyCells(campo, x, y);
                counter--;

            }

        } while (counter > 0);

    }

    private static void incrementNearbyCells(Cell[][] field, int x, int y) {


        if(x-1 >= 0 && y - 1 >= 0)
            field[x - 1][y - 1].changeCell(1);
        if(y - 1 >= 0)
            field[x    ][y - 1].changeCell(1);
        if(x+1 < field.length && y - 1 >= 0)
            field[x + 1][y - 1].changeCell(1);
        if(x-1 >= 0)
            field[x - 1][y    ].changeCell(1);
        if(x+1 < field.length)
            field[x + 1][y    ].changeCell(1);
        if(x-1 >= 0 && y+1 < field.length)
            field[x - 1][y + 1].changeCell(1);
        if(y+1 < field.length)
            field[x    ][y + 1].changeCell(1);
        if(x+1 < field.length && y+1 < field.length)
            field[x + 1][y + 1].changeCell(1);

    }

    public void changeCell(int amount) {
        if (celula != -1) {
            celula += amount;
        }

    }

    public int getCelula() {
        return celula;
    }

    public void setCelula(int celula) {
        this.celula = celula;
    }

    public boolean getClicked() {
        return wasClicked;
    }

    public void setClicked(boolean wasClicked) {
        this.wasClicked = wasClicked;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public void revealCell(Cell[][] field, int x, int y) {

        if (wasClicked || celula == -2)
            return;

        wasClicked = true;

        if (celula == 0) {



                //System.out.println("Revelando vizinhos do tile "+x+", "+y);
            if(x-1 >= 0 && y - 1 >= 0)
                field[x - 1][y - 1].revealCell(field, x - 1, y - 1);
            if(y - 1 >= 0)
                field[x    ][y - 1].revealCell(field,    x,     y - 1);
            if(x+1 <= field.length-1 && y - 1 >= 0)
                field[x + 1][y - 1].revealCell(field, x + 1, y - 1);
            if(x-1 >= 0)
                field[x - 1][y    ].revealCell(field, x - 1,    y    );
            if(x+1 <= field.length-1)
                field[x + 1][y    ].revealCell(field, x + 1,    y    );
            if(x-1 >= 0 && y+1 < field.length-1)
                field[x - 1][y + 1].revealCell(field, x - 1, y + 1);
            if(y+1 <= field.length-1)
                field[x    ][y + 1].revealCell(field,    x,     y + 1);
            if(x+1 <= field.length-1 && y+1 <= field.length-1)
                field[x + 1][y + 1].revealCell(field, x + 1, y + 1);



            //System.out.println("EXCEPTION: "+x+", "+y);


        }


    }


}

