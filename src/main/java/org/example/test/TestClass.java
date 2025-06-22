package org.example.test;


import org.example.annotation.*;

public class TestClass {

    @BeforeSuite
    public static void setupSuite() {
        System.out.println("----Setting up test suite static");
    }

    @AfterSuite
    public static void teardownSuite() {
        System.out.println("----Tearing down test suite static");
    }

    @BeforeTest
    public void beforeEachTest() {
        System.out.println("--Before test");
    }

    @AfterTest
    public void afterEachTest() {
        System.out.println("--After test");
    }

    @Test(priority = 1)
    public void myTestPriority1() {
        System.out.println("Running my test priority 1");
    }

    @Test(priority = 10)
    public void myTestPriority10() {
        System.out.println("Running my test priority 10");
    }

    @Test(priority = 3)
    @CsvSource("5, Java, 15, false")
    public void parameterizedTestWithPriority(int a, String b, int c, boolean d) {
        System.out.printf("Running param test with priority 3. Parameters: %d %s %d %b%n", a, b, c, d);
    }

    @Test
    @CsvSource("10, Java, 20, true")
    public void parameterizedTestWithoutPriority(int a, String b, int c, boolean d) {
        System.out.printf("Running param test with default priority 5. Parameters: %d %s %d %b%n", a, b, c, d);
    }

    @Test(priority = 2)
    private void myTestPriority2() {
        System.out.println("Running my test priority 2");
    }

    @Test(priority = 3)
    private void myTestPriority3() {
        System.out.println("Running my test priority 3");
    }

    @Test(priority = 3)
    private void myTestPrioritySecond3() {
        System.out.println("Running my test priority second 3");
    }
}
