package todo;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();

    public synchronized Task addTask(String title) {
        Task task = new Task(title);
        tasks.add(task);
        return task;
    }

    public synchronized List<Task> getTasks() {
        return new ArrayList<>(tasks);   // kopia, żeby nikt z zewnątrz nie zmienił naszej listy
    }

    public synchronized Task markDone(int number) {
        Task task = findTask(number);
        task.setDone(true);
        return task;
    }

    public synchronized Task deleteTask(int number) {
        findTask(number);                // rzuci wyjątek, jeśli numer zły
        return tasks.remove(number - 1);
    }

    private Task findTask(int number) {
        if (number < 1 || number > tasks.size()) {
            throw new TaskNotFoundException(number);
        }
        return tasks.get(number - 1);
    }
}