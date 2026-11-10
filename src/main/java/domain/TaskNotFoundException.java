package domain;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(int number) {
        super("No task with number " + number);
    }
}
