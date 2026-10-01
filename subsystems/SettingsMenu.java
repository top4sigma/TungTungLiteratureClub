package subsystems;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import java.io.*;
import java.util.*;

public class SettingsMenu {

    private static final String[] options = {"Text Speed", "Volume", "Difficulty", "Update Terminal Size", "Return to Menu"};
    private static final String[][] subOptions = {
        {"Fast", "Medium", "Slow"},
        {"Loud", "Medium", "Low"},
        {"Hard", "Medium", "Easy"},
        {},
        {""}
    };
    private static final String[] keys = {"textSpeed", "volume", "difficulty", "termWidth", "termHeight"};

    public static int show() throws Exception {
        Map<String, String> settings = loadSettings();

        int col = 0;
        int row = 0;
        int[] savedRows = new int[options.length];
        Arrays.fill(savedRows, -1);

        if (col < keys.length && settings.containsKey(keys[col])) {
            String val = settings.get(keys[col]);
            for (int i = 0; i < subOptions[col].length; i++) {
                if (subOptions[col][i].equals(val)) { row = i; break; }
            }
        }

        int[] colWidths = new int[options.length];
        for (int i = 0; i < options.length; i++) {
            colWidths[i] = options[i].length();
            for (int j = 0; j < subOptions[i].length; j++) {
                if (subOptions[i][j].length() > colWidths[i]) {
                    colWidths[i] = subOptions[i][j].length();
                }
            }
            colWidths[i] += 4;
        }

        try {
            while (true) {

                Inputs.clearScreen();

                int flatIdx = 0;
                for (int i = 0; i < options.length - 1; i++) {
                    if (col == i) Inputs.getTerminal().writer().print("\033[7m");
                    Inputs.getTerminal().writer().print("  " + options[i]);
                    if (i == 3) {
                        String w = settings.getOrDefault("termWidth", "");
                        String h = settings.getOrDefault("termHeight", "");
                        if (!w.isEmpty() && !h.isEmpty()) {
                            String display = w + "  x  " + h;
                            int pad = Math.max(1, colWidths[i] - display.length());
                            Inputs.getTerminal().writer().print(" " + display + " ".repeat(pad));
                        }
                    }
                    if (savedRows[i] >= 0) Inputs.getTerminal().writer().print(" <");
                    if (col == i) Inputs.getTerminal().writer().print("\033[27m");
                    Inputs.getTerminal().writer().println();
                    flatIdx++;

                    for (int j = 0; j < subOptions[i].length; j++) {
                        Inputs.getTerminal().writer().print("    ");
                        if (col == i && row == j) Inputs.getTerminal().writer().print("\033[7m");
                        Inputs.getTerminal().writer().print(subOptions[i][j]);
                        if (col == i && row == j) Inputs.getTerminal().writer().print("\033[27m");
                        Inputs.getTerminal().writer().print(" ".repeat(Math.max(1, colWidths[i] - subOptions[i][j].length())));
                        flatIdx++;
                    }
                    Inputs.getTerminal().writer().println();
                }

                if (col == options.length - 1) Inputs.getTerminal().writer().print("\033[7m");
                Inputs.getTerminal().writer().print("  " + options[options.length - 1]);
                if (col == options.length - 1) Inputs.getTerminal().writer().print("\033[27m");
                Inputs.getTerminal().writer().println();
                flatIdx++;

                Inputs.flush();

                int key = Inputs.readKey();

                if (key == 27) {
                    int second = Inputs.readKey();
                    int third = Inputs.readKey();

                    if ((second == 'O' || second == '[') && third == 'A') {
                        if (col > 0) {
                            col--;
                            if (row >= subOptions[col].length) row = subOptions[col].length - 1;
                        }
                    } else if ((second == 'O' || second == '[') && third == 'B') {
                        if (col < options.length - 1) {
                            col++;
                            if (row >= subOptions[col].length) row = subOptions[col].length - 1;
                        }
                    } else if ((second == 'O' || second == '[') && third == 'C') {
                        if (row < subOptions[col].length - 1) row++;
                    } else if ((second == 'O' || second == '[') && third == 'D') {
                        if (row > 0) row--;
                    }
                } else if (key == 13 || key == 10) {
                    if (col == options.length - 1) {
                        return -1;
                    }
                    if (col == 3) {
                        org.jline.terminal.Size sz = Inputs.getTerminal().getSize();
                        String w = String.valueOf(sz.getColumns());
                        String h = String.valueOf(sz.getRows());
                        settings.put("termWidth", w);
                        settings.put("termHeight", h);
                        saveSettings(settings);

                        ProcessBuilder compile = new ProcessBuilder("javac", "-cp", ".", "GenerateAssets.java");
                        compile.directory(new File("."));
                        compile.inheritIO();
                        compile.start();

                        ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "GenerateAssets", w, h);
                        pb.directory(new File("."));
                        pb.inheritIO();
                        pb.start();
                        return col;
                    }
                    settings.put(keys[col], subOptions[col][row]);
                    savedRows[col] = row;
                    saveSettings(settings);
                    return col;
                }
            }
        } finally {
        }
    }

    private static Map<String, String> loadSettings() {
        Map<String, String> settings = new LinkedHashMap<>();
        settings.put("textSpeed", "Medium");
        settings.put("volume", "Medium");
        settings.put("difficulty", "Medium");
        settings.put("termWidth", "80");
        settings.put("termHeight", "24");

        try (BufferedReader br = new BufferedReader(new FileReader("settings.json"))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line.trim());
            }
            String content = json.toString();
            for (String k : keys) {
                int idx = content.indexOf("\"" + k + "\"");
                if (idx >= 0) {
                    idx = content.indexOf('"', content.indexOf(':', idx) + 1);
                    if (idx > 0) {
                        int end = content.indexOf('"', idx + 1);
                        if (end > idx) {
                            settings.put(k, content.substring(idx + 1, end));
                        }
                    }
                }
            }
        } catch (IOException ignored) {}

        return settings;
    }

    private static void saveSettings(Map<String, String> settings) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter("settings.json"))) {
            pw.println("{");
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                String v = settings.getOrDefault(k, "Medium");
                pw.print("  \"" + k + "\": \"" + v + "\"");
                pw.println(i < keys.length - 1 ? "," : "");
            }
            pw.println("}");
        }
    }
}
