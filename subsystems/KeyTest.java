package subsystems;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class KeyTest {

    public static void main(String[] args) throws Exception {

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        terminal.enterRawMode();

        System.out.println("Press an arrow key or Enter. Press Q to quit.");

        try {
            while (true) {

                int key = terminal.reader().read();

                System.out.println("Key code: " + key);

                if (key == 'q' || key == 'Q') {
                    break;
                }
            }
        } finally {
            terminal.close();
        }
    }
}