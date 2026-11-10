package api;

import java.io.IOException;

public interface CommandSource {
    String nextCommand()throws IOException;
}
