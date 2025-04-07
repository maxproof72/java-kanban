package ru.maxproof.taskmanager;

import org.junit.jupiter.api.BeforeEach;
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }
}