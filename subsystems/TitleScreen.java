package subsystems;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jline.terminal.Terminal;

public final class TitleScreen {

    private static final Path MENU_FILE = Path.of("assets", "menu.txt");
    private static final int OPT_ROW = 15;

    private static final String[] OPTIONS = {
            "Play",
            "Settings",
            "Quit"
    };

    private static final String[] MENU_LINES = loadMenu();

    private TitleScreen() {
        // Utility class
    }

    public static int show() throws Exception {

        int selected = 0;

        while (true) {

            render(selected);

            int key = Inputs.readKey();

            switch (key) {

                case 27 -> {
                    int direction = readArrowKey();

                    if (direction == -1) {
                        selected = (selected - 1 + OPTIONS.length)
                                % OPTIONS.length;
                    } else if (direction == 1) {
                        selected = (selected + 1)
                                % OPTIONS.length;
                    }
                }

                case 10, 13 -> {
                    return selected;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    private static void render(int selected) throws Exception {

        Inputs.clearScreen();

        Terminal terminal = Inputs.getTerminal();

        // Draw menu background.
        for (int i = 0; i < MENU_LINES.length; i++) {
            cursorTo(terminal, i + 1, 1);
            terminal.writer().print(MENU_LINES[i]);
        }

        // Draw menu options.
        for (int i = 0; i < OPTIONS.length; i++) {

            cursorTo(
                    terminal,
                    OPT_ROW + i,
                    1
            );

            boolean highlighted = selected == i;

            if (highlighted) {
                terminal.writer().print("\033[7m");
            }

            terminal.writer().print(
                    " " + OPTIONS[i] + " "
            );

            if (highlighted) {
                terminal.writer().print("\033[27m");
            }
        }

        Inputs.flush();
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    /**
     * Reads an ANSI arrow key sequence.
     *
     * @return -1 for up, 1 for down, 0 for anything else
     */
    private static int readArrowKey() throws Exception {

        int second = Inputs.readKey();
        int third = Inputs.readKey();

        if (second != 'O' && second != '[') {
            return 0;
        }

        return switch (third) {
            case 'A' -> -1; // Up
            case 'B' -> 1;  // Down
            default -> 0;
        };
    }

    // -------------------------------------------------------------------------
    // Menu loading
    // -------------------------------------------------------------------------

    private static String[] loadMenu() {

        try {
            return Files.readString(MENU_FILE)
                    .split("\\R", -1);

        } catch (IOException e) {
            System.err.println(
                    "Could not load " + MENU_FILE + ": "
                            + e.getMessage()
            );

            return new String[0];
        }
    }

    // -------------------------------------------------------------------------
    // Cursor
    // -------------------------------------------------------------------------

    private static void cursorTo(
            Terminal terminal,
            int row,
            int column
    ) {
        terminal.writer().print(
                "\033[" + row + ";" + column + "H"
        );
    }
}
