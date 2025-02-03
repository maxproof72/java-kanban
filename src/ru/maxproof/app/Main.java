package ru.maxproof.app;

import ru.maxproof.demo.Demo;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        if (List.of(args).contains("--demo"))
            new Demo().run();
    }
}
