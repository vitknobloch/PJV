package sample;

public class Controller {
    int value;

    Controller(){
        value = 0;
    }

    void clickUp(){
        value++;
    }

    void clickDown(){
        value--;
    }

    void clickReset(){
        value = 0;
    }

    int getCount(){
        return value;
    }
}
