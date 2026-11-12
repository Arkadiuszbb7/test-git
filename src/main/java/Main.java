import api.ConsoleApi;
import domain.TaskManager;
import java.io.IOException;
public class Main {
    public static void main(String[] args){
        TaskManager manager = new TaskManager();
        ConsoleApi api = new ConsoleApi(manager);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Shutting down...")));

        Thread consoleThread = new Thread(() -> {
            try{
                api.run();
            } catch(IOException e){
                System.out.println("input error: " + e.getMessage());
            }
        });
        consoleThread.start();
        System.out.println("main: przeniesienie wykonania run do innego wątku");
    }

}

