import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public class TitleScreen {

    public static int show(String[] options) throws Exception {

        if (options.length > 4) {
            throw new IllegalArgumentException(
                "A maximum of 4 options is allowed."
            );
        }

        int selected = 0;

        Inputs.open();
        Terminal terminal = Inputs.getTerminal();

        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();

        try {
            while (true) {

                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.puts(InfoCmp.Capability.cursor_home);

                // ASCII art area here
                for (int i = 0; i < 8; i++) {
                    terminal.writer().println();
                }

                for (int i = 0; i < options.length; i++) {
                    String option = options[i];
                    if (selected == i) terminal.writer().print("\033[7m");
                    terminal.writer().print(option);
                    if (selected == i) terminal.writer().print("\033[27m");
                    terminal.writer().println();
                }

                terminal.writer().flush();

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
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.flush();
            Inputs.close();
        }
    }

    public static void main(String[] args) throws Exception {
        String[] options = { "Play", "Settings", "Quit" };
        int choice = show(options);
        System.out.println("You chose: " + options[choice]);
    }
}
