package subsystems;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public final class Inputs {

    private static Terminal terminal;

    private Inputs() {
        // Utility class
    }

    /**
     * Opens the JLine terminal and enters raw mode.
     */
    public static void open() throws Exception {
        if (isOpen()) return;

        terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        terminal.enterRawMode();
    }

    /**
     * Starts the alternate screen and clears the terminal.
     */
    public static void startScreen() throws Exception {
        requireOpen();

        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        clearScreen();
        terminal.flush();
    }

    /**
     * Clears the terminal and moves the cursor to the top-left.
     */
    public static void clearScreen() throws Exception {
        requireOpen();

        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
    }

    /**
     * Flushes pending terminal output.
     */
    public static void flush() throws Exception {
        requireOpen();
        terminal.flush();
    }

    /**
     * Reads a single key in raw mode.
     */
    public static int readKey() throws Exception {
        requireOpen();
        return terminal.reader().read();
    }

    /**
     * Stops the alternate screen.
     */
    public static void stopScreen() {
        if (!isOpen()) return;

        try {
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.flush();
        } catch (Exception ignored) {
            // Terminal may already be unavailable.
        }
    }

    /**
     * Closes the terminal.
     */
    public static void close() {
        if (!isOpen()) return;

        stopScreen();

        try {
            terminal.close();
        } catch (Exception ignored) {
            // Ignore cleanup errors.
        } finally {
            terminal = null;
        }
    }

    /**
     * Returns whether the terminal is currently open.
     */
    public static boolean isOpen() {
        return terminal != null;
    }

    /**
     * Returns the active JLine terminal.
     */
    public static Terminal getTerminal() {
        requireOpen();
        return terminal;
    }

    /**
     * Ensures the terminal is available before an operation.
     */
    private static void requireOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("Inputs not open");
        }
    }
}
