package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

import java.util.Random;

public abstract class Figure implements Runnable{
    protected int x;
    protected int y;
    protected final Board board;
    protected volatile boolean running = true;
    protected final Random random = new Random();
    private Direction direction;

    public Figure(Board board) {
        this.board = board;
        Direction[] values = Direction.values();
        this.direction = values[random.nextInt(values.length)];
    }

    public Direction getDirection() {return direction;}
    public void setDirection(Direction direction) {this.direction = direction;}

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void run(){
        while(running){
            try{
                while (board.isPaused()) {
                    Thread.sleep(100);
                }

                Thread.sleep(random.nextInt(500)+500);

                if (!running) {
                    break;
                }

                action();
            }catch (InterruptedException e) {
                running = false;
            }
        }
    }

    protected abstract void action();
    public abstract char getSymbol();
    protected abstract void onRemoveFromBoard();

    public void stop(){
        this.running = false;
    }

    protected boolean move(int dx, int dy){
        int w = board.getWidth();
        int h = board.getHeight();

        int newX = (this.x + dx + w) % w;
        int newY = (this.y + dy + h) % h;

        Field currentField = board.getField(this.x, this.y);
        Field targetField = board.getField(newX, newY);

        currentField.lock.lock();
        try{
            if (currentField.getOccupant() != this) {
                return false;
            }

            if(targetField.lock.tryLock()){
                try{
                    if(targetField.isOccupied())
                        return false;

                    currentField.removeOccupant();
                    targetField.setOccupant(this);

                    this.x = newX;
                    this.y = newY;

                    return true;
                }finally{
                    targetField.lock.unlock();
                }
            }else
                return false;
        }finally{
            currentField.lock.unlock();
        }
    }
}
