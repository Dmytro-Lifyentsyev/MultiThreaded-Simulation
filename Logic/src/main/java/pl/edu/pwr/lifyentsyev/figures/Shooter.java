package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

public class Shooter extends  Figure {
    public Shooter(Board board) {
        super(board);
    }

    @Override
    public char getSymbol() {
        return 'H';
    }

    @Override
    protected void onRemoveFromBoard() {
        board.shooterCount.decrementAndGet();
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
        }else if(choice==1 || choice==2) {
            Direction current = getDirection();
            int w = board.getWidth();
            int h = board.getHeight();

            for(int i=0; i<3; i++){
                int multiplier = i + 1;
                int targetX = (this.x + current.dx * multiplier + w) % w;
                int targetY = (this.y + current.dy * multiplier + h) % h;

                Field targetField = board.getField(targetX, targetY);

                targetField.lock.lock();
                try{
                    Thread.sleep(200);
                    if(targetField.isOccupied()){
                        Figure victim = targetField.getOccupant();
                        if(victim instanceof Bulldozer)
                            break;
                        else{
                            victim.stop();
                            targetField.removeOccupant();
                            victim.onRemoveFromBoard();
                            break;
                        }
                    }
                }catch (InterruptedException e){
                }finally {
                    targetField.lock.unlock();
                }
            }
        } else{
            Direction current = getDirection();

            boolean moved = move(current.dx, current.dy);
            if (!moved)
                setDirection(current.turnRight());
        }
    }

}
