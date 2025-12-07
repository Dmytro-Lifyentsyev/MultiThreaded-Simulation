package pl.edu.pwr.lifyentsyev.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reprezentuje planszę symulacji.
 * Jest to główny zasób współdzielony, do którego dostęp mają wszystkie wątki (Figury, Kreator, GUI).
 * Klasa przechowuje siatkę pól oraz globalne statystyki symulacji.
 */
public class Board {
    private final int width; /** Szerokość planszy (liczba kolumn). */
    private final int height; /** Wysokość planszy (liczba wierszy). */
    /**
     * Dwuwymiarowa tablica pól reprezentująca siatkę symulacji.
     * Każde pole posiada własną blokadę (lock).
     */
    private final Field[][] grid;

    /** Liczba aktualnie znajdujących się na planszy skarbów. */
    public final AtomicInteger treasureCount = new AtomicInteger(0);
    /** Liczba aktywnych Szperaczy na planszy. */
    public final AtomicInteger scavengerCount = new AtomicInteger(0);
    /** Liczba aktywnych Strzelców na planszy. */
    public final AtomicInteger shooterCount = new AtomicInteger(0);
    /** Liczba aktywnych Spychaczy na planszy. */
    public final AtomicInteger bulldozerCount = new AtomicInteger(0);
    /** Całkowita liczba oddanych strzałów przez wszystkich Strzelców. */
    public final AtomicInteger shotsFired = new AtomicInteger(0);
    /** Całkowita liczba transformacji Szperaczy w Strzelców. */
    public final AtomicInteger transformations = new AtomicInteger(0);
    /** Całkowita liczba figur usuniętych z planszy w wyniku zestrzelenia. */
    public final AtomicInteger kills = new AtomicInteger(0);

    /**
     * Tworzy nową planszę o zadanych wymiarach.
     * Inicjalizuje siatkę i wypełnia ją pustymi obiektami Field.
     * @param width  Szerokość planszy.
     * @param height Wysokość planszy.
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Field[width][height];

        for(int i=0; i<width; i++){
            for(int j=0; j<height; j++){
                grid[i][j]= new Field();
            }
        }
    }

    /**
     * Pobiera obiekt pola o podanych współrzędnych.
     * @param x Współrzędna X (kolumna).
     * @param y Współrzędna Y (wiersz).
     * @return Obiekt Field pod adresem (x,y) lub null, jeśli współrzędne są poza planszą.
     */
    public Field getField(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        return grid[x][y];
    }

    public int getWidth() {return width;}
    public int getHeight() {return height;}

    /**
     * Flaga sterująca zatrzymaniem symulacji.
     * Słowo kluczowe 'volatile' gwarantuje, że zmiana wartości jest
     * natychmiast widoczna dla wszystkich wątków
     */
    private volatile boolean paused = false;

    /**
     * Sprawdza, czy symulacja jest w stanie pauzy.
     * Wątki figur i kreatora sprawdzają tę flagę, aby wstrzymać działanie.
     * @return true jeśli symulacja jest zatrzymana, false w przeciwnym razie.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Ustawia stan pauzy symulacji.
     * @param paused true aby zatrzymać symulację, false aby wznowić.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
