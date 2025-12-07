package pl.edu.pwr.lifyentsyev.core;

import pl.edu.pwr.lifyentsyev.figures.Figure;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reprezentuje pojedyncze pole (komórkę) na planszy symulacji.
 * Klasa ta pełni rolę kontenera dla Figur oraz Skarbów.
 * Jest kluczowym elementem synchronizacji w systemie - każde pole posiada własną
 * blokadę (lock), co pozwala na precyzyjne sterowanie dostępem wątków
 */
public class Field {
    /**
     * Blokada wielowejściowa dedykowana dla tego konkretnego pola.
     * Jest publiczna, aby wątki (Figury, Kreator) mogły ją pobrać i zablokować
     * przed wykonaniem operacji na tym polu.
     */
    public final ReentrantLock lock = new ReentrantLock();

    /** Referencja do figury aktualnie zajmującej to pole (lub null, jeśli pole jest wolne). */
    private Figure occupant;
    /** Flaga określająca, czy na polu znajduje się ukryty skarb. */
    private boolean hasTreasure = false;

    /**
     * Sprawdza, czy pole jest zajęte przez jakąkolwiek figurę.
     * @return true, jeśli na polu stoi figura; false, jeśli pole jest puste.
     */
    public boolean isOccupied(){
        return occupant != null;
    }

    /**
     * Umieszcza podaną figurę na tym polu.
     * Metoda ta powinna być wywoływana tylko wtedy, gdy wątek posiada blokadę tego pola.
     * @param figure Figura, która ma stanąć na tym polu.
     */
    public void setOccupant(Figure figure){
        this.occupant = figure;
    }

    /**
     * Zwraca referencję do figury stojącej na tym polu.
     * @return Obiekt Figure lub null, jeśli pole jest puste.
     */
    public Figure getOccupant(){
        return occupant;
    }

    /**
     * Usuwa figurę z tego pola (ustawia referencję na null).
     * Wywoływane przy przesuwaniu figury na inne pole lub przy jej usunięciu z planszy.
     */
    public void removeOccupant() {
        this.occupant = null;
    }

    /**
     * Sprawdza, czy na polu znajduje się skarb.
     * @return true, jeśli skarb jest obecny.
     */
    public boolean hasTreasure() {
        return hasTreasure;
    }

    /**
     * Dodaje skarb do tego pola.
     * Wywoływane przez Kreatora.
     */
    public void addTreasure(){
        this.hasTreasure = true;
    }

    /**
     * Próbuje usunąć (podnieść) skarb z tego pola.
     * @return true, jeśli skarb tam był i został usunięty (podniesiony przez Szperacza).
     * false, jeśli pola nie miało skarbu.
     */
    public boolean removeTreasure(){
        if(hasTreasure){
            hasTreasure = false;
            return true;
        }
        return false;
    }
}
