package pl.edu.pwr.lifyentsyev.figures;

/**
 * Typ wyliczeniowy reprezentujący możliwe kierunki ruchu i patrzenia figur.
 * Definiuje 8 kierunków.
 * Każdy kierunek posiada wektor przesunięcia (dx, dy), który określa,
 * jak zmienią się współrzędne figury po wykonaniu ruchu w tym kierunku.
 */
public enum Direction {
    // Kolejność definicji jest ważna dla metod turnRight/turnLeft (ruch wskazówek zegara)
    UP(0, -1),
    UP_RIGHT (1,-1),
    RIGHT (1,0),
    DOWN_RIGHT (1,1),
    DOWN (0, 1),
    DOWN_LEFT (-1,1),
    LEFT (-1,0),
    UP_LEFT (-1,-1);

    /** Zmiana współrzędnej X przy ruchu w tym kierunku (-1, 0 lub 1). */
    public final int dx;
    /** Zmiana współrzędnej Y przy ruchu w tym kierunku (-1, 0 lub 1). */
    public final int dy;

    /**
     * Konstruktor przypisujący wektor przesunięcia do kierunku.
     * @param dx Przesunięcie w poziomie.
     * @param dy Przesunięcie w pionie.
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Zwraca kierunek obrócony o 45 stopni w prawo.
     * Metoda wykorzystuje kolejność stałych w enumie.
     * Dzięki operatorowi modulo (%), po dojściu do ostatniego elementu (UP_LEFT),
     * następuje powrót do pierwszego (UP).
     * @return Nowy kierunek.
     */
    public Direction turnRight(){
        int currentIdx = this.ordinal();
        Direction[] all = values();
        int nextIdx = (currentIdx + 1) % all.length;
        return all[nextIdx];
    }

    /**
     * Zwraca kierunek obrócony o 45 stopni w lewo.
     * Wzór {@code (currentIdx - 1 + all.length) % all.length} obsługuje
     * bezpieczne cofanie się z indeksu 0 na ostatni indeks tablicy.
     * @return Nowy kierunek.
     */
    public Direction turnLeft(){
        int currentIdx = this.ordinal();
        Direction[] all = values();
        int prevIdx = (currentIdx - 1 + all.length) % all.length;
        return all[prevIdx];
    }
}
