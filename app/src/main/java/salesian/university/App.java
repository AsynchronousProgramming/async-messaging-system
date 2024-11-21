package salesian.university;

public class App {
    public String getGreeting() {
        return "Welcome to the Asynchronous Messaging System!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
