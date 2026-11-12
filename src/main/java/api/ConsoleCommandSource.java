package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ConsoleCommandSource {
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public String nextCommand() throws IOException {
        System.out.print("> ");
        return reader.readLine();
    }
}
