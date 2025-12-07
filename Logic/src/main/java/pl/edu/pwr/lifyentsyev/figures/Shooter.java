package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

/**
 * Klasa reprezentująca Strzelca.
 * Oprócz standardowego poruszania się,
 * posiada zdolność "strzelania" na dystans do 3 pól.
 * Strzał eliminuje inne figury z planszy (z wyjątkiem Spychacza).
 */
public class Shooter extends  Figure {
    /**
     * Konstruktor Strzelca.
     * @param board Plansza, na której figura będzie działać.
     */
    public Shooter(Board board) {
        super(board);
    }

    /**
     * Zwraca symbol reprezentujący Strzelca.
     * @return Znak 'H'.
     */
    @Override
    public char getSymbol() {
        return 'H';
    }

    /**
     * Aktualizuje statystyki planszy po usunięciu Strzelca.
     * Zmniejsza globalny licznik aktywnych Strzelców.
     */
    @Override
    protected void onRemoveFromBoard() {
        board.shooterCount.decrementAndGet();
    }

    /**
     * Definiuje zachowanie Strzelca w każdej turze.
     * Algorytm działania (losowy wybór akcji):
     * 1. 10% szans: Obrót w miejscu.
     * 2. 20% szans: Oddanie strzału.
     * - Strzał propaguje się przez maksymalnie 3 pola.
     * - Na każdym polu Strzelec zakłada blokadę i czeka 200ms.
     * - Jeśli trafi ofiarę: zabija ją (chyba że to Spychacz) i kończy strzał.
     * 3. 70% szans: Ruch do przodu (jeśli zablokowany, to obrót).
     */
    @Override
    protected void action() {
        int choice = random.nextInt(10);
        //WARIANT 1: Obrót (10%)
        if(choice==0) {
            boolean turnRight = random.nextBoolean();
            Direction current = getDirection();

            if(turnRight)
                setDirection(current.turnRight());
            else
                setDirection(current.turnLeft());
        }
        //WARIANT 2: Strzał (20%)
        else if(choice==1 || choice==2) {
            board.shotsFired.incrementAndGet();

            Direction current = getDirection();
            int w = board.getWidth();
            int h = board.getHeight();

            // Pętla propagacji strzału (zasięg do 3 pól)
            for(int i=0; i<3; i++){
                int multiplier = i + 1;
                // Obliczenie współrzędnych kolejnego pola w linii strzału
                int targetX = (this.x + current.dx * multiplier + w) % w;
                int targetY = (this.y + current.dy * multiplier + h) % h;

                Field targetField = board.getField(targetX, targetY);

                targetField.lock.lock(); // Blokujemy pole (ofiara nie może uciec)
                try{
                    // Symulacja "stopniowego przejmowania pola".
                    // W tym czasie inna figura nie może wejść na to pole ani z niego wyjść.
                    Thread.sleep(200);
                    if(targetField.isOccupied()){
                        Figure victim = targetField.getOccupant();
                        if(victim instanceof Bulldozer)
                            break;
                        else{
                            // Eliminacja ofiary
                            victim.stop();
                            targetField.removeOccupant();
                            victim.onRemoveFromBoard();

                            board.kills.incrementAndGet();
                            break;
                        }
                    }
                }catch (InterruptedException e){
                }finally {
                    targetField.lock.unlock();
                }
            }
        }
        //WARIANT 3: Ruch (70%)
        else{
            Direction current = getDirection();

            boolean moved = move(current.dx, current.dy);
            if (!moved)
                setDirection(current.turnRight()); // Jeśli nie udało się ruszyć, obróć się
        }
    }
}
