import api.ConsoleApi;
import domain.TaskManager;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {

        TaskManager manager = new TaskManager();
        ConsoleApi api = new ConsoleApi(manager);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Shutting down...")));
        api.run();
    }

}

