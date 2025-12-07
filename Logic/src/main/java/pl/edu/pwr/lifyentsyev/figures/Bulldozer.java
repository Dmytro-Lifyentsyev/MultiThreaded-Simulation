package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;

/**
 * Klasa reprezentująca Spychacza.
 * Jego unikalną cechą jest to,
 * że nie zatrzymuje się, gdy napotka inną figurę na swojej drodze.
 * Zamiast tego próbuje ją przepchnąć na kolejne pole.
 * Jest również odporny na strzały Strzelca.
 */
public class Bulldozer extends Figure {

    /**
     * Konstruktor Spychacza.
     * @param board Plansza, na której figura będzie działać.
     */
    public Bulldozer(Board board) {
        super(board);
    }

    /**
     * Zwraca symbol reprezentujący Spychacza.
     * @return Znak 'B'.
     */
    @Override
    public char getSymbol() {
        return 'B';
    }

    /**
     * Aktualizuje statystyki planszy po usunięciu Spychacza.
     * Zmniejsza globalny licznik aktywnych Spychaczy.
     */
    @Override
    protected void onRemoveFromBoard() {
        board.bulldozerCount.decrementAndGet();
    }

    /**
     * Definiuje zachowanie Spychacza w każdej turze.
     * 1. 10% szans: Obrót w miejscu.
     * 2. 90% szans: Próba ruchu do przodu (z ewentualnym pchaniem).
     * Jeśli ruch się nie uda (np. ściana z dwóch figur), Spychacz się obraca.
     */
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

    /**
     * Nadpisana metoda ruchu, implementująca mechanikę "spychania".
     * Jest to operacja, która może angażować do 3 pól:
     * 1. Pole A (current): Gdzie stoi Spychacz.
     * 2. Pole B (target): Gdzie Spychacz chce wejść.
     * 3. Pole C (behind): Gdzie zostanie przepchnięta ofiara stojąca na B.
     * Algorytm:
     * 1. Blokuje pole A.
     * 2. Próbuje zablokować pole B.
     * 3. Jeśli B jest puste -> Zwykły ruch (A -> B).
     * 4. Jeśli B jest zajęte -> Próbuje zablokować pole C.
     * - Jeśli C jest puste: Przesuwa ofiarę z B na C, a siebie z A na B.
     * Aktualizuje pozycje obu figur (x, y).
     * - Jeśli C jest zajęte: Ruch niemożliwy, rezygnacja.
     * @param dx Przesunięcie w poziomie.
     * @param dy Przesunięcie w pionie.
     * @return true, jeśli ruch (zwykły lub z pchaniem) się udał; false w przeciwnym przypadku.
     */
    @Override
    protected boolean move(int dx, int dy){
        int w = board.getWidth();
        int h = board.getHeight();

        // Współrzędne pola docelowego (Krok 1)
        int newX = (this.x + dx + w) % w;
        int newY = (this.y + dy + h) % h;
        // Współrzędne pola za celem (Krok 2 - dla ofiary)
        int newX_2 = (this.x + dx*2 + w) % w;
        int newY_2 = (this.y + dy*2 + h) % h;

        Field currentField = board.getField(this.x, this.y);
        Field targetField = board.getField(newX, newY);

        // KROK 1: Blokada pola startowego
        currentField.lock.lock();
        try{
            // KROK 2: Próba blokady pola docelowego
            if(targetField.lock.tryLock()){
                try{
                    // WARIANT A: Pole docelowe puste -> Zwykły ruch
                    if(!targetField.isOccupied()){
                        currentField.removeOccupant();
                        targetField.setOccupant(this);
                        this.setPosition(newX, newY);
                        return true;
                    } else { // WARIANT B: Pole docelowe zajęte -> Próba pchania
                        Field behindField = board.getField(newX_2, newY_2);

                        // KROK 3: Próba blokady pola "za plecami" ofiary
                        if (behindField.lock.tryLock()) {
                            try {
                                // Jeśli pole za ofiarą też jest zajęte, nie możemy pchać
                                if (behindField.isOccupied())
                                    return false;

                                // Wykonanie przesunięcia dwóch figur naraz
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
