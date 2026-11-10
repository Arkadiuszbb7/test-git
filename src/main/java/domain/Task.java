package domain;

public class Task {
    String title;
    boolean done;

    Task(String title) {

        this.title = title;
        this.done = false;
    }
    public String getTitle(){
        return title;
    }
    public boolean isDone(){
        return done;
    }
}
