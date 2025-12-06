package pl.edu.pwr.lifyentsyev.figures;

public enum Direction {
    UP(0, -1),
    UP_RIGHT (1,-1),
    RIGHT (1,0),
    DOWN_RIGHT (1,1),
    DOWN (0, 1),
    DOWN_LEFT (-1,1),
    LEFT (-1,0),
    UP_LEFT (-1,-1);


    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction turnRight(){
        int currentIdx = this.ordinal();
        Direction[] all = values();
        int nextIdx = (currentIdx + 1) % all.length;
        return all[nextIdx];
    }

    public Direction turnLeft(){
        int currentIdx = this.ordinal();
        Direction[] all = values();
        int prevIdx = (currentIdx - 1 + all.length) % all.length;
        return all[prevIdx];
    }
}
