package org.example;

import org.example.runner.TestRunner;
import org.example.test.TestClass;

public class Main {
    public static void main(String[] args) throws Exception {
        TestRunner.runTests(TestClass.class);
    }
}