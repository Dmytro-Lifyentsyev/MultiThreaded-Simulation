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
    public final AtomicInteger shotsFired = new AtomicInteger(0);
    public final AtomicInteger transformations = new AtomicInteger(0);
    public final AtomicInteger kills = new AtomicInteger(0);

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

    private volatile boolean paused = false;

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
