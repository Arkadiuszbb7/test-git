package api;
import domain.TaskNotFoundException;
import domain.TaskManager;
import domain.Task;
import java.io.IOException;
import java.util.ArrayList;
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
            if(input == null) {
                break;
            }
            if (input.equals("exit")) {
                System.out.println("Bye!");
                break;
            } else if (input.startsWith("add ")) {
               String title = input.substring(4);
               manager.addTask(title);
               System.out.println("Added: " + title);
            } else if (input.equals("list")) {
                ArrayList<Task> tasks = manager.getTasks();
                for (int i = 0; i < tasks.size(); i++) {
                    String mark;
                    if (tasks.get(i).isDone()) {
                        mark = "[x]";
                    } else {
                        mark = "[ ]";
                    }
                    System.out.println((i + 1) + ". " + mark + " " + tasks.get(i).getTitle());
                }
             } else if (input.startsWith("done ")) {
                String numberText = input.substring(5);
                try {
                    int number = Integer.parseInt(numberText);
                    Task task = manager.markDone(number);
                    System.out.println("Done: " + task.getTitle());
                } catch (NumberFormatException e) {
                    System.out.println("Not a number: " + numberText);
                } catch (TaskNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            } else if (input.startsWith("delete ")) {
                String numberText = input.substring(7);
                try {
                    int number = Integer.parseInt(numberText);
                    Task task = manager.deleteTask(number);
                    System.out.println("Deleted: " + task.getTitle());
                } catch (NumberFormatException e) {
                    System.out.println("Not a number: " + numberText);
                } catch (TaskNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("Unknown command: " + input);
            }
        }

    }


}
