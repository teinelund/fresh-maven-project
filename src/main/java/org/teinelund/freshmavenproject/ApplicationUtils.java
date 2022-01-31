package org.teinelund.freshmavenproject;

public class ApplicationUtils {
    public void exit() {
        System.exit(0);
    }

    public void exitError() {
        System.exit(1);
    }

    public void printInteractive(String message) {
        System.out.println(message);
    }

    public void printInteractiveQuestion(String message) {
        System.out.print(message);
    }

    public void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public void printInformation(String message) {
        System.out.println("[INFO] " + message);
    }

    public void printVerbose(String message, Verbosable verbosable) {
        if (verbosable.isVerbose()) {
            System.out.println("[VERBOSE] " + message);
        }
    }
}
