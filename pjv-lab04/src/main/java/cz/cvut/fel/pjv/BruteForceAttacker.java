package cz.cvut.fel.pjv;

public class BruteForceAttacker extends Thief {

    private char[] combination;
    private int sizeOfPassword;
    private char[] chars;
    private int charCount;

    private boolean cracked;
    
    @Override
    public void breakPassword(int sizeOfPassword) {
        //init properties
        combination = new char[sizeOfPassword];
        this.sizeOfPassword = sizeOfPassword;
        chars = getCharacters();
        charCount = chars.length;
        cracked = false;

        //call recursive solver
        tryCombinations(0);

    }

    //recursively change combinations and try them
    private void tryCombinations(int startIndex){
        if(cracked) //stop when vault was opened
            return;

        //try to open vault with combinations of correct lenght
        if(startIndex == sizeOfPassword){
            if(tryOpen(combination)){
                cracked = true;
            }
            return;
        }

        //try all sub-combinations for every possible character on this position
        for(int i = 0; i < charCount; i++){
            combination[startIndex] = chars[i];
            tryCombinations(startIndex + 1);
        }
    }
    
}
