package pl.edu.pwr.lifyentsyev;

import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Creator;

public class Main {
    public static void main(String[] args){

        Board board = new Board(10, 10);

        Creator creator = new Creator(board);
        Thread creatorThread = new Thread(creator);
        creatorThread.start();

        while (true) {
            try {

                System.out.println("\n------------------------------------------------");
                System.out.println("STATYSTYKI:");
                System.out.println("Skarby: " + board.treasureCount.get());
                System.out.println("Szperacze (S): " + board.scavengerCount.get());
                System.out.println("Strzelcy (H): " + board.shooterCount.get());
                System.out.println("Spychacze (B): " + board.bulldozerCount.get());
                System.out.println("------------------------------------------------\n");

                board.printBoard();

                Thread.sleep(200);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
