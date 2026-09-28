package subsystems;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import java.nio.file.*;

public class TitleScreen {

    private static String[] menuLines;
    private static final int OPT_ROW = 15;

    static {
        try {
            menuLines = new String(Files.readAllBytes(Paths.get("assets/menu.txt")))
                .split("\n");
        } catch (Exception e) {
            menuLines = new String[0];
        }
    }

    public static int show() throws Exception {
        String[] options = {"Play", "Settings", "Quit"};
        int selected = 0;

        try {
            while (true) {
                Inputs.clearScreen();
                Terminal t = Inputs.getTerminal();

                // menu.txt background
                for (int i = 0; i < menuLines.length; i++) {
                    cursorTo(i + 1, 1);
                    t.writer().print(menuLines[i]);
                }

                for (int i = 0; i < options.length; i++) {
                    cursorTo(OPT_ROW + i, 1);
                    t.writer().print("\033[0m");
                    if (selected == i) t.writer().print("\033[7m");
                    t.writer().print(" " + options[i] + " ");
                    if (selected == i) t.writer().print("\033[27m");
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

    private static void cursorTo(int row, int col) {
        Inputs.getTerminal().writer().print("\033[" + row + ";" + col + "H");
    }
}
