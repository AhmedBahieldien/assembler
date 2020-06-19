package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Logic logic = new Logic();
        logic.start(1);
        startPhase2(logic);
    }

    private static void startPhase2(Logic logic) throws IOException {
        Phase2 p2 = new Phase2(logic);
        p2.readcopyfile();
    }
}
