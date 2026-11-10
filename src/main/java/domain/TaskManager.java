package domain;

import java.util.ArrayList;

public class TaskManager {
    private ArrayList<Task> tasks = new ArrayList<>();

    public void addTask(String title) {
        tasks.add(new Task(title));

    }

    public ArrayList<Task> getTasks(){
        return new ArrayList<>(tasks);
    }

    public Task markDone(int number) {
        if (number < 1 || number > tasks.size()) {
            throw new TaskNotFoundException(number);
        }
        Task task = tasks.get(number - 1);
        task.done = true;
        return task;
    }

    public Task deleteTask(int number) {
        if (number < 1 || number > tasks.size()) {
            throw new TaskNotFoundException(number);
        }
        return tasks.remove(number - 1);
    }
}