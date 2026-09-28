package subsystems;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public class Inputs {

    private static Terminal terminal;
    private static boolean open = false;

    public static void open() throws Exception {
        terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        terminal.enterRawMode();
        open = true;
    }

    public static void startScreen() throws Exception {
        if (!open) throw new IllegalStateException("Inputs not open");
        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
    }

    public static void clearScreen() throws Exception {
        if (!open) throw new IllegalStateException("Inputs not open");
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
    }

    public static void flush() throws Exception {
        if (!open) throw new IllegalStateException("Inputs not open");
        terminal.flush();
    }

    public static void stopScreen() {
        if (terminal != null) {
            try {
                terminal.puts(InfoCmp.Capability.exit_ca_mode);
                terminal.flush();
            } catch (Exception ignored) {}
        }
    }

    public static void close() {
        stopScreen();
        if (terminal != null) {
            try { terminal.close(); } catch (Exception ignored) {}
        }
        open = false;
    }

    public static int readKey() throws Exception {
        if (!open) throw new IllegalStateException("Inputs not open");
        return terminal.reader().read();
    }

    public static boolean isOpen() {
        return open;
    }

    public static Terminal getTerminal() {
        return terminal;
    }
}
