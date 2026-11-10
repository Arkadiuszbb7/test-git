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

    public void markDone(String numberText) {
        try {
            int number = Integer.parseInt(numberText);
            if (number < 1 || number > tasks.size()) {
                System.out.println("No task with number " + number);
            } else {
                tasks.get(number - 1).done = true;
                System.out.println("Done: " + tasks.get(number - 1).title);
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a number: " + numberText);
        }
    }

    public void deleteTask(String numberText) {
        try {
            int number = Integer.parseInt(numberText);
            if (number < 1 || number > tasks.size()) {
                System.out.println("No task with number " + number);
            } else {
                Task removed = tasks.remove(number - 1);
                System.out.println("Deleted: " + removed.title);
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a number: " + numberText);
        }
    }
}