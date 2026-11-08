package todo;

import java.io.IOException;

interface CommandSource {
    String nextCommand()throws IOException;
}
