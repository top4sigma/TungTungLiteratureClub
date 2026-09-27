import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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

    public static void close() {
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