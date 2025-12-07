package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

/**
 * Klasa reprezentująca Szperacza.
 * Porusza się po planszy i sprawdza, czy na odwiedzanych polach znajdują się skarby.
 * Po zebraniu 10 skarbów, Szperacz przechodzi transformację i staje się Strzelcem.
 */
public class Scavenger extends Figure{

    /**
     * Konstruktor Szperacza.
     * @param board Plansza, na której figura będzie działać.
     */
    public Scavenger(Board board) {
        super(board);
    }

    /**
     * Zwraca symbol reprezentujący Szperacza.
     * @return Znak 'S'.
     */
    @Override
    public char getSymbol() {
        return 'S';
    }

    /**
     * Aktualizuje statystyki planszy po usunięciu Szperacza.
     * Zmniejsza globalny licznik aktywnych Szperaczy.
     */
    @Override
    protected void onRemoveFromBoard() {
        board.scavengerCount.decrementAndGet();
    }

    int collectedTreasures = 0; /** Licznik skarbów zebranych przez tę konkretną instancję Szperacza. */

    /**
     * Definiuje zachowanie Szperacza w każdej turze (metoda wywoływana cyklicznie przez wątek).
     * Algorytm działania:
     * 1. Losowa decyzja (10% szans): Obrót w miejscu (w lewo lub w prawo).
     * 2. Ruch (90% szans): Próba przesunięcia się do przodu.
     * - Jeśli ruch się udał: Sprawdza pole pod nogami. Jeśli jest skarb -> zbiera go.
     * - Jeśli ruch zablokowany: Obraca się w prawo.
     * 3. Sprawdzenie warunku transformacji:
     * - Jeśli zebrano >= 10 skarbów, następuje zamiana w Strzelca.
     */
    @Override
    protected void action() {
        // KROK 1: Losowa decyzja o obrocie
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
            // KROK 2: Próba ruchu
            Direction current = getDirection();
            boolean moved = move(current.dx, current.dy);

            if (!moved)
                setDirection(current.turnRight());
            else{
                // Ruch udany - sprawdzamy czy jest tu skarb
                if(board.getField(this.x, this.y).removeTreasure()){
                    collectedTreasures++;
                    board.treasureCount.decrementAndGet();
                }
            }
        }
        // KROK 3: Logika Transformacji (Szperacz -> Strzelec)
        if(collectedTreasures >= 10){
            Field currentField = board.getField(this.x, this.y);

            currentField.lock.lock(); //Blokujemy pole, aby nikt nas nie zabił/przesunął w trakcie przemiany
            try {
                //Sprawdzamy, czy nadal jesteśmy na tym polu.
                // Mogło się zdarzyć, że Strzelec nas zabił ułamek sekundy przed założeniem tej blokady.
                if(currentField.getOccupant() != this)
                    return;

                board.scavengerCount.decrementAndGet();
                board.transformations.incrementAndGet();

                // Tworzenie nowej figury (Strzelec) w tym samym miejscu i kierunku
                Shooter shooter = new Shooter(board);
                shooter.setPosition(this.x, this.y);
                shooter.setDirection(this.getDirection());

                currentField.setOccupant(shooter); // Podmiana obiektu na planszy
                board.shooterCount.incrementAndGet();

                new Thread(shooter).start(); // Uruchomienie nowego wątku Strzelca
                this.running = false; // Zakończenie życia obecnego wątku (Szperacza)
            }finally {
                currentField.lock.unlock();
            }
        }
    }
}
