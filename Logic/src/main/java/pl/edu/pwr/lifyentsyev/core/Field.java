package pl.edu.pwr.lifyentsyev.core;

import pl.edu.pwr.lifyentsyev.figures.Figure;
import java.util.concurrent.locks.ReentrantLock;

public class Field {
    public final ReentrantLock lock = new ReentrantLock();

    private Figure occupant;
    private boolean hasTreasure = false;

    public boolean isOccupied(){
        return occupant != null;
    }

    public void setOccupant(Figure figure){
        this.occupant = figure;
    }

    public Figure getOccupant(){
        return occupant;
    }

    public void removeOccupant() {
        this.occupant = null;
    }

    public boolean hasTreasure() {
        return hasTreasure;
    }

    public void addTreasure(){
        this.hasTreasure = true;
    }

    public boolean removeTreasure(){
        if(hasTreasure){
            hasTreasure = false;
            return true;
        }
        return false;
    }
}
