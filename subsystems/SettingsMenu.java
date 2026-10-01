package subsystems;

import org.jline.terminal.Size;

import java.io.*;
import java.util.*;

public final class SettingsMenu {

    private static final String SETTINGS_FILE = "settings.json";

    private static final String[] OPTIONS = {
            "Text Speed",
            "Volume",
            "Difficulty",
            "Update Terminal Size",
            "Return to Menu"
    };

    private static final String[][] SUB_OPTIONS = {
            {"Fast", "Medium", "Slow"},
            {"Loud", "Medium", "Low"},
            {"Hard", "Medium", "Easy"},
            {},
            {""}
    };

    private static final String[] KEYS = {
            "textSpeed",
            "volume",
            "difficulty",
            "termWidth",
            "termHeight"
    };

    private static final Map<String, String> DEFAULT_SETTINGS = Map.of(
            "textSpeed", "Medium",
            "volume", "Medium",
            "difficulty", "Medium",
            "termWidth", "80",
            "termHeight", "24"
    );

    private SettingsMenu() {
        // Utility class
    }

    public static int show() throws Exception {

        Map<String, String> settings = loadSettings();

        int column = 0;
        int row = getSavedRow(settings, column);

        int[] savedRows = new int[OPTIONS.length];
        Arrays.fill(savedRows, -1);

        int[] columnWidths = calculateColumnWidths();

        while (true) {

            render(settings, column, row, savedRows, columnWidths);
            int key = Inputs.readKey();

            switch (key) {

                case 27 -> {
                    int[] direction = readArrowKey();

                    if (direction != null) {
                        switch (direction[0]) {
                            case 0 -> { // Up
                                if (column > 0) {
                                    column--;
                                    row = clampRow(row, column);
                                }
                            }

                            case 1 -> { // Down
                                if (column < OPTIONS.length - 1) {
                                    column++;
                                    row = clampRow(row, column);
                                }
                            }

                            case 2 -> { // Right
                                if (row < SUB_OPTIONS[column].length - 1) {
                                    row++;
                                }
                            }

                            case 3 -> { // Left
                                if (row > 0) {
                                    row--;
                                }
                            }
                        }
                    }
                }

                case 10, 13 -> {
                    if (column == OPTIONS.length - 1) {
                        return -1;
                    }

                    if (column == 3) {
                        return updateTerminalSize(settings);
                    }

                    settings.put(KEYS[column], SUB_OPTIONS[column][row]);
                    savedRows[column] = row;

                    saveSettings(settings);
                    return column;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    private static void render(
            Map<String, String> settings,
            int selectedColumn,
            int selectedRow,
            int[] savedRows,
            int[] columnWidths
    ) throws Exception {

        Inputs.clearScreen();

        for (int i = 0; i < OPTIONS.length - 1; i++) {

            boolean selected = selectedColumn == i;

            printSelected("  " + OPTIONS[i], selected);

            if (i == 3) {
                printTerminalSize(settings, columnWidths[i]);
            }

            if (savedRows[i] >= 0) {
                Inputs.getTerminal().writer().print(" <");
            }

            Inputs.getTerminal().writer().println();

            renderSubOptions(
                    i,
                    selectedColumn,
                    selectedRow,
                    columnWidths[i]
            );
        }

        printSelected(
                "  " + OPTIONS[OPTIONS.length - 1],
                selectedColumn == OPTIONS.length - 1
        );

        Inputs.getTerminal().writer().println();
        Inputs.flush();
    }

    private static void renderSubOptions(
            int column,
            int selectedColumn,
            int selectedRow,
            int width
    ) {

        for (int row = 0; row < SUB_OPTIONS[column].length; row++) {

            String option = SUB_OPTIONS[column][row];

            Inputs.getTerminal().writer().print("    ");

            boolean selected =
                    selectedColumn == column &&
                    selectedRow == row;

            printSelected(option, selected);

            Inputs.getTerminal().writer().print(
                    " ".repeat(
                            Math.max(1, width - option.length())
                    )
            );
        }

        Inputs.getTerminal().writer().println();
    }

    private static void printSelected(String text, boolean selected) {

        if (selected) {
            Inputs.getTerminal().writer().print("\033[7m");
        }

        Inputs.getTerminal().writer().print(text);

        if (selected) {
            Inputs.getTerminal().writer().print("\033[27m");
        }
    }

    private static void printTerminalSize(
            Map<String, String> settings,
            int width
    ) {

        String w = settings.getOrDefault("termWidth", "");
        String h = settings.getOrDefault("termHeight", "");

        if (w.isEmpty() || h.isEmpty()) {
            return;
        }

        String display = w + "  x  " + h;

        int padding = Math.max(
                1,
                width - display.length()
        );

        Inputs.getTerminal().writer().print(
                " " + display + " ".repeat(padding)
        );
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private static int[] readArrowKey() throws Exception {

        int second = Inputs.readKey();
        int third = Inputs.readKey();

        if ((second != 'O' && second != '[')) {
            return null;
        }

        return switch (third) {
            case 'A' -> new int[]{0}; // Up
            case 'B' -> new int[]{1}; // Down
            case 'C' -> new int[]{2}; // Right
            case 'D' -> new int[]{3}; // Left
            default -> null;
        };
    }

    private static int clampRow(int row, int column) {

        int optionCount = SUB_OPTIONS[column].length;

        if (optionCount == 0) {
            return 0;
        }

        return Math.min(row, optionCount - 1);
    }

    private static int getSavedRow(
            Map<String, String> settings,
            int column
    ) {

        if (column >= KEYS.length) {
            return 0;
        }

        String value = settings.get(KEYS[column]);

        if (value == null) {
            return 0;
        }

        for (int i = 0; i < SUB_OPTIONS[column].length; i++) {
            if (SUB_OPTIONS[column][i].equals(value)) {
                return i;
            }
        }

        return 0;
    }

    // -------------------------------------------------------------------------
    // Terminal size
    // -------------------------------------------------------------------------

    private static int updateTerminalSize(
            Map<String, String> settings
    ) throws Exception {

        Size size = Inputs.getTerminal().getSize();

        String width = String.valueOf(size.getColumns());
        String height = String.valueOf(size.getRows());

        settings.put("termWidth", width);
        settings.put("termHeight", height);

        saveSettings(settings);

        ProcessBuilder process = new ProcessBuilder(
                "java",
                "-cp",
                ".",
                "GenerateAssets",
                width,
                height
        );

        process.directory(new File("."));
        process.inheritIO();
        process.start();

        return 3;
    }

    // -------------------------------------------------------------------------
    // Column sizing
    // -------------------------------------------------------------------------

    private static int[] calculateColumnWidths() {

        int[] widths = new int[OPTIONS.length];

        for (int i = 0; i < OPTIONS.length; i++) {

            int width = OPTIONS[i].length();

            for (String option : SUB_OPTIONS[i]) {
                width = Math.max(width, option.length());
            }

            widths[i] = width + 4;
        }

        return widths;
    }

    // -------------------------------------------------------------------------
    // Settings
    // -------------------------------------------------------------------------

    private static Map<String, String> loadSettings() {

        Map<String, String> settings =
                new LinkedHashMap<>(DEFAULT_SETTINGS);

        File file = new File(SETTINGS_FILE);

        if (!file.exists()) {
            return settings;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (!line.startsWith("\"")) {
                    continue;
                }

                int keyEnd = line.indexOf('"', 1);
                int colon = line.indexOf(':', keyEnd);

                if (keyEnd == -1 || colon == -1) {
                    continue;
                }

                String key = line.substring(1, keyEnd);

                int valueStart = line.indexOf('"', colon);

                if (valueStart == -1) {
                    continue;
                }

                int valueEnd = line.indexOf('"', valueStart + 1);

                if (valueEnd == -1) {
                    continue;
                }

                String value = line.substring(
                        valueStart + 1,
                        valueEnd
                );

                if (DEFAULT_SETTINGS.containsKey(key)) {
                    settings.put(key, value);
                }
            }

        } catch (IOException ignored) {
            // Use defaults if settings cannot be read.
        }

        return settings;
    }

    private static void saveSettings(
            Map<String, String> settings
    ) throws IOException {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(SETTINGS_FILE))) {

            writer.println("{");

            for (int i = 0; i < KEYS.length; i++) {

                String key = KEYS[i];
                String value = settings.getOrDefault(
                        key,
                        DEFAULT_SETTINGS.getOrDefault(key, "")
                );

                writer.printf(
                        "  \"%s\": \"%s\"%s%n",
                        key,
                        value,
                        i < KEYS.length - 1 ? "," : ""
                );
            }

            writer.println("}");
        }
    }
}
