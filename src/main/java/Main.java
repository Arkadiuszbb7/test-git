import api.ConsoleApi;
import api.ConsoleCommandSource;
import domain.TaskManager;

import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        ConsoleCommandSource source = new ConsoleCommandSource();
        TaskManager manager = new TaskManager();
        ConsoleApi api = new ConsoleApi(source, manager);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Shutting down...")));
        api.run();
    }

}

