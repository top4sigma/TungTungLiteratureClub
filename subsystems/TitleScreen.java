package subsystems;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public class TitleScreen {

    public static int show() throws Exception {
        String[] options = { "Play", "Settings", "Quit" };
        int selected = 0;

        try {
            while (true) {

                Inputs.clearScreen();

                // ASCII art area here
                for (int i = 0; i < 8; i++) {
                    Inputs.getTerminal().writer().println();
                }

                for (int i = 0; i < options.length; i++) {
                    String option = options[i];
                    if (selected == i) Inputs.getTerminal().writer().print("\033[7m");
                    Inputs.getTerminal().writer().print(option);
                    if (selected == i) Inputs.getTerminal().writer().print("\033[27m");
                    Inputs.getTerminal().writer().println();
                }

                Inputs.flush();

                int key = Inputs.readKey();

                if (key == 27) {
                    int second = Inputs.readKey();
                    int third = Inputs.readKey();

                    if ((second == 'O' || second == '[') && third == 'A') {
                        selected = (selected - 1 + options.length) % options.length;
                    } else if ((second == 'O' || second == '[') && third == 'B') {
                        selected = (selected + 1) % options.length;
                    }
                } else if (key == 13 || key == 10) {
                    return selected;
                }
            }
        } finally {
        }
    }
}
