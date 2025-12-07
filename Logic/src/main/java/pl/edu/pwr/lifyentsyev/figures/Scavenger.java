package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

public class Scavenger extends Figure{

    public Scavenger(Board board) {
        super(board);
    }

    @Override
    public char getSymbol() {
        return 'S';
    }

    @Override
    protected void onRemoveFromBoard() {
        board.scavengerCount.decrementAndGet();
    }

    int collectedTreasures = 0;

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
            if (!moved)
                setDirection(current.turnRight());
            else{
                if(board.getField(this.x, this.y).removeTreasure()){
                    collectedTreasures++;
                    board.treasureCount.decrementAndGet();
                }
            }
        }
        if(collectedTreasures >= 10){
            board.scavengerCount.decrementAndGet();
            board.transformations.incrementAndGet();

            Shooter shooter = new Shooter(board);
            shooter.setPosition(this.x, this.y);
            shooter.setDirection(this.getDirection());
            board.getField(this.x, this.y).setOccupant(shooter);
            board.shooterCount.incrementAndGet();

            new Thread(shooter).start();
            running = false;
        }
    }
}
