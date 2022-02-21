package fel.pjv.knoblvit.hw01;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        //Get operation
        System.out.println("Vyber operaci (1-soucet, 2-rozdil, 3-soucin, 4-podil):");
        int op = getIntFromRange(sc, 1, 4);

        //init result variables
        char opChar = ' ';
        float[] nums = {0f, 0f};
        float result = 0f;
        boolean zeroDiv = false;

        //perform operation
        switch (op){
            case 1:
                nums = getTwoFloats(sc, "scitanec", "scitanec");
                result = nums[0] + nums[1];
                opChar = '+';
                break;
            case 2:
                nums = getTwoFloats(sc, "mensenec", "mensitel");
                result = nums[0] - nums[1];
                opChar = '-';
                break;
            case 3:
                nums = getTwoFloats(sc, "cinitel", "cinitel");
                result = nums[0] * nums[1];
                opChar = '*';
                break;
            case 4:
                nums = getTwoFloats(sc, "delenec", "delitel");
                if(nums[1] == 0f){
                    zeroDiv = true;
                    break;
                }
                result = nums[0] / nums[1];
                opChar = '/';
                break;
        }

        if(zeroDiv){
            System.out.println("Pokus o deleni nulou!");
        }else{
            System.out.print("Zadej pocet desetinnych mist: ");
            int decimalDigits = getIntFromRange(sc, 0, 20);
            String formatStr = String.format("%%.%1$df %2$c %%.%1$df = %%.%1$df%%n", decimalDigits, opChar);
            System.out.printf(formatStr, nums[0], nums[1], result);
        }
    }

    private static int getIntFromRange(Scanner sc, int min, int max){
        boolean correct = false;
        int input = 0;
        while(!correct){
            if(sc.hasNextInt()){
                input = sc.nextInt();
                if(min <= input && max >=input)
                    correct = true;
            }
            if(!correct)
                System.out.printf("Neplatna volba, zadejte cele cislo v rozsahu %d - %d:\n", min, max);
        }
        return input;
    }

    private static float[] getTwoFloats(Scanner sc, String name1, String name2){
        float[] ret = new float[2];
        System.out.printf("Zadej %s: ", name1);
        ret[0] = getFloat(sc);
        System.out.printf("Zadej %s: ", name2);
        ret[1] = getFloat(sc);

        return ret;
    }

    private static float getFloat(Scanner sc){
        boolean correct = false;
        float input = 0f;
        while(!correct){
            if(sc.hasNextFloat()){
                input = sc.nextFloat();
                correct = true;
            }

            if(!correct)
                System.out.printf("Neplatná volba, zadejte cele nebo desetinné cislo\n");
        }
        return input;
    }
}
