package ru.maxproof.app;

import ru.maxproof.demo.Demo;
import ru.maxproof.taskmanager.Managers;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        if (List.of(args).contains("--demo")) {
            Demo demo = new Demo(Managers.getDefault());
            demo.run();
        }
    }
}
