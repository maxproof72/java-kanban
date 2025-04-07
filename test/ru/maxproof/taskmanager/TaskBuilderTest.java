package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskBuilderTest {

    @Test
    public void testSetName() {
        TaskBuilder taskBuilder = new TaskBuilder();
        taskBuilder.setName("test");
        assertEquals("test", taskBuilder.getName());
        Task task = taskBuilder.buildTask();
        assertEquals("test", task.getName());
    }

    @Test
    public void testSetDescription() {
        TaskBuilder taskBuilder = new TaskBuilder();
        taskBuilder.setDescription("test");
        assertEquals("test", taskBuilder.getDescription());
        Task task = taskBuilder.buildTask();
        assertEquals("test", task.getDescription());
    }

    @Test
    public void testSetStatus() {
        TaskBuilder taskBuilder = new TaskBuilder();
        taskBuilder.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, taskBuilder.getStatus());
        Task task = taskBuilder.buildTask();
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    public void testSetStartTime() {
        final LocalDateTime now = LocalDateTime.now();
        TaskBuilder taskBuilder = new TaskBuilder().setStartTime(now);
        assertEquals(now, taskBuilder.getStartTime());
        Task task = taskBuilder.buildTask();
        assertEquals(now, task.getStartTime());
    }

    @Test
    public void testSetDuration() {
        final LocalDateTime now = LocalDateTime.now();
        final Duration duration = Duration.ofHours(1);
        TaskBuilder taskBuilder = new TaskBuilder()
                .setStartTime(now)
                .setDuration(duration);
        assertEquals(duration, taskBuilder.getDuration());
        Task task = taskBuilder.buildTask();
        assertEquals(duration, task.getDuration());
        assertEquals(now.plusHours(1), task.getEndTime());
    }
}
