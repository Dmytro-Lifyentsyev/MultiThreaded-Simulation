package pl.edu.pwr.lifyentsyev.core;

import pl.edu.pwr.lifyentsyev.figures.Bulldozer;
import pl.edu.pwr.lifyentsyev.figures.Figure;
import pl.edu.pwr.lifyentsyev.figures.Scavenger;
import pl.edu.pwr.lifyentsyev.figures.Shooter;

import java.util.Random;

public class Creator implements Runnable{
    private final int maxTreasures;
    private final int maxBulldozers;
    private final int maxShooters;
    private final int maxScavengers;

    private final Random random = new Random();
    private final Board board;
    private volatile boolean running = true;

    public Creator(Board board, int maxTreasures, int maxScavengers, int maxShooters, int maxBulldozers) {
        this.board = board;
        this.maxTreasures = maxTreasures;
        this.maxScavengers = maxScavengers;
        this.maxShooters = maxShooters;
        this.maxBulldozers = maxBulldozers;
    }

    @Override
    public void run(){
        while(running){
            try{
                while (board.isPaused()) {
                    Thread.sleep(100);
                }

                Thread.sleep(random.nextInt(200, 400));

                if(board.treasureCount.get() < maxTreasures){
                    tryToSpawn('T');
                }else if(board.bulldozerCount.get() < maxBulldozers){
                    tryToSpawn('B');
                }else if(board.scavengerCount.get() < maxScavengers){
                    tryToSpawn('S');
                }else if(board.shooterCount.get() < maxShooters){
                    tryToSpawn('H');
                }
            }catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void tryToSpawn(char type) throws  InterruptedException{
        int x = random.nextInt(board.getWidth());
        int y = random.nextInt(board.getHeight());
        Field field = board.getField(x,y);

        if(field.lock.tryLock())
        {
            try {
                Thread.sleep(500);
                if (!field.isOccupied()) {
                    if (type == 'T') {
                        if(!field.hasTreasure()){
                        field.addTreasure();
                        board.treasureCount.incrementAndGet();
                        }
                    }else{
                        Figure figure = null;

                        switch (type) {
                            case 'S' -> {
                                figure = new Scavenger(board);
                                board.scavengerCount.incrementAndGet();
                            }
                            case 'H' -> {
                                figure = new Shooter(board);
                                board.shooterCount.incrementAndGet();
                            }
                            case 'B' -> {
                                figure = new Bulldozer(board);
                                board.bulldozerCount.incrementAndGet();
                            }
                        }

                        if(figure != null){
                            figure.setPosition(x, y);
                            field.setOccupant(figure);
                            new Thread(figure).start();
                        }
                    }
                }
            }finally {
                field.lock.unlock();
            }
        }
    }
}
