package pl.edu.pwr.lifyentsyev.figures;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Field;
import java.util.Random;

/**
 * Abstrakcyjna klasa bazowa dla wszystkich figur w symulacji.
 * Każda figura jest osobnym wątkiem (implementuje {@code Runnable}).
 * Klasa ta zarządza cyklem życia wątku, pozycją na planszy oraz
 * bezpiecznym przemieszczaniem się między polami (metoda {@code move}).
 */
public abstract class Figure implements Runnable{
    protected int x; /** Aktualna współrzędna X na planszy. */
    protected int y; /** Aktualna współrzędna Y na planszy. */
    protected final Board board; /** Referencja do współdzielonej planszy. */
    /**
     * Flaga sterująca życiem wątku.
     * Użycie {@code volatile} zapewnia, że zmiana wartości przez inny wątek
     * jest natychmiast widoczna dla wątku tej figury.
     */
    protected volatile boolean running = true;
    protected final Random random = new Random();
    private Direction direction; /** Aktualny kierunek, w którym figura jest zwrócona. */

    /**
     * Konstruktor figury.
     * Inicjalizuje referencję do planszy i losuje początkowy kierunek.
     * @param board Plansza, po której figura będzie się poruszać.
     */
    public Figure(Board board) {
        this.board = board;
        Direction[] values = Direction.values();
        this.direction = values[random.nextInt(values.length)];
    }

    public Direction getDirection() {return direction;}
    public void setDirection(Direction direction) {this.direction = direction;}

    /**
     * Ustawia wewnętrzne współrzędne figury.
     * @param x Nowa współrzędna X.
     * @param y Nowa współrzędna Y.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Główna pętla życia wątku figury.
     * Realizuje schemat: Sprawdź pauzę -> Śpij -> Sprawdź czy żyjesz -> Wykonaj akcję.
     */
    @Override
    public void run(){
        while(running){
            try{
                //Obsługa Pauzy - czekamy aktywnie, jeśli plansza jest zatrzymana
                while (board.isPaused()) {
                    Thread.sleep(100);
                }

                //Symulacja czasu ruchu (losowe opóźnienie)
                Thread.sleep(random.nextInt(500)+500);

                // Sprawdzamy, czy w trakcie snu nie zostaliśmy zabici.
                // Jeśli running == false, przerywamy pętlę, zanim wykonamy jakąkolwiek akcję na planszy
                if (!running) {
                    break;
                }

                //Wykonanie specyficznej logiki dla danej figury (ruch, strzał, zbieranie)
                action();
            }catch (InterruptedException e) {
                running = false;
            }
        }
    }

    /**
     * Abstrakcyjna metoda definiująca zachowanie konkretnego typu figury.
     */
    protected abstract void action();
    /**
     * Zwraca symbol reprezentujący figurę.
     * @return Znak char.
     */
    public abstract char getSymbol();
    /**
     * Metoda wywoływana w momencie usunięcia figury z planszy.
     * Służy głównie do aktualizacji statystyk w klasie Board.
     */
    protected abstract void onRemoveFromBoard();

    /**
     * Zatrzymuje wątek figury.
     * Ustawia flagę running na false, co spowoduje wyjście z pętli run() w najbliższym obiegu.
     */
    public void stop(){
        this.running = false;
    }

    /**
     * Próbuje przesunąć figurę o zadany wektor (dx, dy).
     * Metoda realizuje bezpieczne przesunięcie z blokadami:
     * 1. Oblicza nowe współrzędne.
     * 2. Blokuje obecne pole.
     * 3. Sprawdza, czy figura nadal tam jest (zabezpieczenie przed byciem zabitym w kolejce do blokady).
     * 4. Próbuje zablokować pole docelowe.
     * 5. Jeśli pole docelowe jest wolne - przesuwa figurę.
     * @param dx Przesunięcie w poziomie.
     * @param dy Przesunięcie w pionie.
     * @return true, jeśli ruch się udał; false, jeśli pole docelowe było zajęte lub zablokowane.
     */
    protected boolean move(int dx, int dy){
        int w = board.getWidth();
        int h = board.getHeight();

        int newX = (this.x + dx + w) % w;
        int newY = (this.y + dy + h) % h;

        Field currentField = board.getField(this.x, this.y);
        Field targetField = board.getField(newX, newY);

        // KROK 1: Blokujemy pole, na którym stoimy
        currentField.lock.lock();
        try{
            // KROK 2: Walidacja tożsamości
            // Mogliśmy zostać zabici przez Strzelca chwilę przed uzyskaniem tej blokady.
            if (currentField.getOccupant() != this) {
                return false;
            }

            // KROK 3: Próba blokady pola docelowego
            if(targetField.lock.tryLock()){
                try{
                    // KROK 4: Sprawdzenie, czy docelowe pole jest puste
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
                return false; // Pole docelowe jest zablokowane przez inny wątek
        }finally{
            currentField.lock.unlock();
        }
    }
}
