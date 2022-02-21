package cz.cvut.fel.pjv.featuretesting;

import java.util.Random;

public class RandMoveGenerator {
    Random rand;

    public RandMoveGenerator(){
        rand = new Random();
    }

    public float[] generateMove(float maxRadius){
        float lenght = rand.nextFloat() * maxRadius;
        float direction = (float) (rand.nextFloat() * 2 * Math.PI);

        float move[] = new float[2];
        move[0] = (float) (lenght * Math.cos(direction));
        move[1] = (float) (lenght * Math.sin(direction));
        return  move;
    }
}
