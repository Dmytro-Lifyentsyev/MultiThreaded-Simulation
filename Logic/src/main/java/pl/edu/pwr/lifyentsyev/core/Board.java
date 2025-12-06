package pl.edu.pwr.lifyentsyev.core;

import java.util.concurrent.atomic.AtomicInteger;

public class Board {
    private final int width;
    private final int height;
    private final Field[][] grid;

    public final AtomicInteger treasureCount = new AtomicInteger(0);
    public final AtomicInteger scavengerCount = new AtomicInteger(0);
    public final AtomicInteger shooterCount = new AtomicInteger(0);
    public final AtomicInteger bulldozerCount = new AtomicInteger(0);

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Field[width][height];

        for(int i=0; i<width; i++){
            for(int j=0; j<height; j++){
                grid[i][j]= new Field();
            }
        }
    }

    public Field getField(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        return grid[x][y];
    }

    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public void printBoard(){
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                if(getField(x,y).isOccupied()){
                    System.out.print(getField(x,y).getOccupant().getSymbol());
                } else if (getField(x,y).hasTreasure()) {
                    System.out.print("$");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
    }
}
