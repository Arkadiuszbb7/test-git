import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Task> tasks = new ArrayList<>();
        System.out.println("Todo app. Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equals("exit")) {

                System.out.println("Bye!");

                break;

            } else if (input.startsWith("add ")) {
                String title = input.substring(4);

                tasks.add(new Task(title));
                System.out.println("Added: " + title);


            } else if (input.equals("list")) {


                for (int i = 0; i < tasks.size(); i++) {
                    String mark;
                    if (tasks.get(i).done) {
                        mark = "[x]";

                    } else {
                        mark = "[ ]";

                    }

                    System.out.println((i + 1) + ". " + mark + " " + tasks.get(i).title);

                }
            }else if(input.startsWith("done ")){

                String numberText = input.substring(5);
                try {
                    int number = Integer.parseInt(numberText);
                    if (number < 1 || number > tasks.size()){
                        System.out.println("No task with number " + number);
                    } else {
                        tasks.get(number - 1).done = true;
                        System.out.println("Done: " + tasks.get(number - 1).title);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Not a number: " + numberText);
                }

            }
            else if (input.startsWith("delete ")) {
                String numberText = input.substring(7);
                try {
                    int number = Integer.parseInt(numberText);
                    if (number < 1 || number > tasks.size()) {
                        System.out.println("No task with number " + number);
                    } else {
                        Task removed = tasks.remove(number - 1);
                        System.out.println("Deleted: " + removed.title);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Not a number: " + numberText);
                }
            }

            else {
                System.out.println("Unknown command: " + input);
            }

        }
    }
}

class Task {
    String title;
    boolean done;

    Task(String title) {

        this.title = title;
        this.done = false;

    }

}