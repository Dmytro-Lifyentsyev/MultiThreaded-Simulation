package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

public class Bulldozer extends Figure {

    public Bulldozer(Board board) {
        super(board);
    }

    @Override
    public char getSymbol() {
        return 'B';
    }

    @Override
    protected void onRemoveFromBoard() {
        board.bulldozerCount.decrementAndGet();
    }

    @Override
    protected void action() {
        int choice = random.nextInt(10);
        if(choice==0) {
            boolean turnRight = random.nextBoolean();
            Direction current = getDirection();

            if(turnRight)
                setDirection(current.turnRight());
            else
                setDirection(current.turnLeft());
        }
        else{
            Direction current = getDirection();

            boolean moved = move(current.dx, current.dy);
            if (!moved){
                setDirection(current.turnRight());
            }
        }
    }

    @Override
    protected boolean move(int dx, int dy){
        int w = board.getWidth();
        int h = board.getHeight();

        int newX = (this.x + dx + w) % w;
        int newY = (this.y + dy + h) % h;
        int newX_2 = (this.x + dx*2 + w) % w;
        int newY_2 = (this.y + dy*2 + h) % h;

        Field currentField = board.getField(this.x, this.y);
        Field targetField = board.getField(newX, newY);

        currentField.lock.lock();
        try{
            if(targetField.lock.tryLock()){
                try{
                    if(!targetField.isOccupied()){
                        currentField.removeOccupant();
                        targetField.setOccupant(this);
                        this.setPosition(newX, newY);
                        return true;
                    }else {
                        Field behindField = board.getField(newX_2, newY_2);

                        if (behindField.lock.tryLock()) {
                            try {
                                if (behindField.isOccupied())
                                    return false;

                                Figure victim = targetField.getOccupant();
                                victim.setPosition(newX_2, newY_2);
                                targetField.removeOccupant();
                                behindField.setOccupant(victim);

                                currentField.removeOccupant();
                                targetField.setOccupant(this);
                                this.setPosition(newX, newY);
                                return true;
                            } finally {
                                behindField.lock.unlock();
                            }
                        }
                    }
                }finally {
                    targetField.lock.unlock();
                }
            }
        }finally {
            currentField.lock.unlock();
        }
        return false;
    }
}
