package pl.edu.pwr.lifyentsyev.core;

import pl.edu.pwr.lifyentsyev.figures.Bulldozer;
import pl.edu.pwr.lifyentsyev.figures.Figure;
import pl.edu.pwr.lifyentsyev.figures.Scavenger;
import pl.edu.pwr.lifyentsyev.figures.Shooter;
import java.util.Random;

/**
 * Klasa Kreatora działająca w osobnym wątku.
 * Jego zadaniem jest cykliczne monitorowanie stanu planszy i dospawnianie
 * brakujących figur oraz skarbów do zadanych limitów.
 * Kreator symuluje czasochłonny proces tworzenia (poprzez uśpienie wątku),
 * blokując w tym czasie pole, na którym pracuje.
 */
public class Creator implements Runnable{
    //Limity konfiguracyjne (parametry symulacji)
    private final int maxTreasures;
    private final int maxBulldozers;
    private final int maxShooters;
    private final int maxScavengers;

    private final Random random = new Random();
    /** Referencja do współdzielonej planszy. */
    private final Board board;
    /** Flaga sterująca pętlą życia wątku. */
    private volatile boolean running = true;

    /**
     * Konstruktor Kreatora.
     * Ustawia planszę oraz limity ilościowe dla poszczególnych elementów.
     * @param board          Współdzielona plansza.
     * @param maxTreasures   Maksymalna liczba skarbów.
     * @param maxScavengers  Maksymalna liczba Szperaczy.
     * @param maxShooters    Maksymalna liczba Strzelców.
     * @param maxBulldozers  Maksymalna liczba Spychaczy.
     */
    public Creator(Board board, int maxTreasures, int maxScavengers, int maxShooters, int maxBulldozers) {
        this.board = board;
        this.maxTreasures = maxTreasures;
        this.maxScavengers = maxScavengers;
        this.maxShooters = maxShooters;
        this.maxBulldozers = maxBulldozers;
    }

    /**
     * Główna pętla wątku Kreatora.
     * Cyklicznie sprawdza liczniki na planszy i jeśli są poniżej limitów,
     * podejmuje próbę stworzenia nowego obiektu.
     */
    @Override
    public void run(){
        while(running){
            try{
                // Obsługa Pauzy - Kreator przestaje pracować, gdy symulacja jest wstrzymana
                while (board.isPaused()) {
                    Thread.sleep(100);
                }

                //Odstęp czasu między próbami spawnu
                Thread.sleep(random.nextInt(300));

                // Sprawdzanie warunków i próba spawnu odpowiedniego typu
                if(board.bulldozerCount.get() < maxBulldozers){
                    tryToSpawn('B');
                }else if(board.scavengerCount.get() < maxScavengers){
                    tryToSpawn('S');
                }else if(board.shooterCount.get() < maxShooters){
                    tryToSpawn('H');
                }else if(board.treasureCount.get() < maxTreasures){
                    tryToSpawn('T');
                }
            }catch (InterruptedException e) {
                running = false;
            }
        }
    }

    /**
     * Próbuje umieścić figurę lub skarb na losowym polu.
     * 1. Jeśli pole jest wolne, Kreator je blokuje.
     * 2. Symuluje pracę przez 500ms (w tym czasie nikt inny nie może wejść na to pole).
     * 3. Wstawia obiekt i zwalnia blokadę.
     * Jeśli pole jest zajęte lub zablokowane przez inny wątek, Kreator natychmiast rezygnuje
     * (nie czeka w kolejce), co zapobiega zakleszczeniom.
     * @param type Typ obiektu do stworzenia ('T' - Skarb, 'S' - Szperacz, 'H' - Strzelec, 'B' - Spychacz).
     * @throws InterruptedException Gdy wątek zostanie przerwany podczas symulacji pracy (sleep).
     */
    private void tryToSpawn(char type) throws  InterruptedException{
        int x = random.nextInt(board.getWidth());
        int y = random.nextInt(board.getHeight());
        Field field = board.getField(x,y);

        // Próba zajęcia pola. Nie czeka, jeśli pole jest zablokowane.
        if(field.lock.tryLock())
        {
            try {
                Thread.sleep(500);
                //Czy pole jest puste
                if (!field.isOccupied()) {
                    if (type == 'T') {
                        // Tworzenie skarbu
                        if(!field.hasTreasure()){
                        field.addTreasure();
                        board.treasureCount.incrementAndGet();
                        }
                    }else{
                        // Tworzenie figur
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
                            // Uruchomienie wątku nowej figury
                            new Thread(figure).start();
                        }
                    }
                }
            }finally {
                // ZAWSZE zwalnia blokadę, niezależnie od tego co się stało
                field.lock.unlock();
            }
        }
    }
}
