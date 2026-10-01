package subsystems;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public final class Inputs {

    private static Terminal terminal;
    private static boolean open;

    private Inputs() {
        // Utility class
    }

    public static void open() throws Exception {
        if (open) {
            return;
        }

        terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        terminal.enterRawMode();
        open = true;
    }

    public static void startScreen() throws Exception {
        checkOpen();

        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        clearScreen();
        terminal.flush();
    }

    public static void clearScreen() throws Exception {
        checkOpen();

        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
    }

    public static void flush() throws Exception {
        checkOpen();
        terminal.flush();
    }

    public static int readKey() throws Exception {
        checkOpen();
        return terminal.reader().read();
    }

    public static boolean isOpen() {
        return open;
    }

    public static Terminal getTerminal() {
        checkOpen();
        return terminal;
    }

    public static void stopScreen() {
        if (!open || terminal == null) {
            return;
        }

        try {
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.flush();
        } catch (Exception ignored) {
            // Terminal may already be unavailable.
        }
    }

    public static void close() {
        if (terminal == null) {
            open = false;
            return;
        }

        try {
            stopScreen();
        } finally {
            try {
                terminal.close();
            } catch (Exception ignored) {
                // Terminal may already be closed.
            }

            terminal = null;
            open = false;
        }
    }

    private static void checkOpen() {
        if (!open || terminal == null) {
            throw new IllegalStateException("Inputs is not open");
        }
    }
}
