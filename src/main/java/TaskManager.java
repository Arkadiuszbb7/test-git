import java.util.ArrayList;

class TaskManager {
    private ArrayList<Task> tasks = new ArrayList<>();

    void addTask(String title) {
        tasks.add(new Task(title));
        System.out.println("Added: " + title);
    }

    void printTasks() {
        for (int i = 0; i < tasks.size(); i++) {
            String mark;
            if (tasks.get(i).done) {
                mark = "[x]";
            } else {
                mark = "[ ]";
            }
            System.out.println((i + 1) + ". " + mark + " " + tasks.get(i).title);
        }
    }

    void markDone(String numberText) {
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

    void deleteTask(String numberText) {
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