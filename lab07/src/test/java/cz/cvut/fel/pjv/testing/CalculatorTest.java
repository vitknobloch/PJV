package cz.cvut.fel.pjv.testing;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    static Calculator instance;

    @BeforeAll
    static void setInstance(){
        instance = new Calculator();
    }

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void add() {

    }

    @Test
    void subtract() {

    }

    @Test
    void multiply() {

    }

    @Test
    void divide() {
        int a = 100;
        int b = 5;
        assertEquals(instance.divide(a, b), 20);
        b = -5;
        assertEquals(instance.divide(a, b), -20);
    }

    @Test
    void testDivideException(){
        int a = 100;
        int b = 0;
        assertThrows(ArithmeticException.class, () -> { instance.divide(a,b); });
    }
}