package cz.cvut.fel.pjv.featuretesting;

public class Main {

    public static void main(String[] args) {
	// write your code here
        RandMoveGenerator rmg = new RandMoveGenerator();
        for (int i = 0; i < 15; i++) {
            float[] move = rmg.generateMove(1.0f);
            System.out.printf("Move %d: %.03f %.03f%n", i, move[0], move[1]);
        }

    }
}
