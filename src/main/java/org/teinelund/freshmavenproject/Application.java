package org.teinelund.freshmavenproject;

/**
 * Main class
 */
public class Application {
    public static void main(String[] args) {
        Application application = new Application();
        application.execute(args);
    }

    public void execute(String[] args) {
        System.out.println("Fresh Maven Project.");
    }
}
