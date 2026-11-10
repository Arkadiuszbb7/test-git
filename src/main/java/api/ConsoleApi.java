package api;

import domain.TaskManager;

import java.io.IOException;

public class ConsoleApi {
    private final CommandSource source;
    private final TaskManager manager;

    public ConsoleApi(CommandSource source, TaskManager manager) {
        this.source = source;
        this.manager = manager;
    }

    public void run() throws IOException {
        System.out.println("Todo app. Type 'exit' to quit.");
        while (true) {
            String input = source.nextCommand();
            if (input.equals("exit")) {
                System.out.println("Bye!");
                break;
            } else if (input.startsWith("add ")) {
                manager.addTask(input.substring(4));
            } else if (input.equals("list")) {
                manager.printTasks();
            } else if (input.startsWith("done ")) {
                manager.markDone(input.substring(5));
            } else if (input.startsWith("delete ")) {
                manager.deleteTask(input.substring(7));
            } else {
                System.out.println("Unknown command: " + input);
            }
        }

    }


}
