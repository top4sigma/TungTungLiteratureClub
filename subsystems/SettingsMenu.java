package subsystems;

import org.jline.terminal.Terminal;
import org.jline.terminal.Size;
import org.jline.utils.InfoCmp;

import java.io.*;
import java.util.*;

public class SettingsMenu {

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
            {}
    };

    private static final String[] KEYS = {
            "textSpeed",
            "volume",
            "difficulty",
            "termWidth",
            "termHeight"
    };

    private static final int RESET = 27;
    private static final int ENTER = 13;
    private static final int LF = 10;

    public static int show() throws Exception {

        Terminal terminal = Inputs.getTerminal();
        PrintWriter out = terminal.writer();

        Map<String, String> settings = loadSettings();

        int selectedOption = 0;
        int selectedSubOption = getSelectedSubOption(
                settings,
                selectedOption
        );

        int[] savedRows = new int[OPTIONS.length];
        Arrays.fill(savedRows, -1);

        int[] columnWidths = calculateColumnWidths();

        try {

            while (true) {

                render(
                        terminal,
                        out,
                        settings,
                        selectedOption,
                        selectedSubOption,
                        savedRows,
                        columnWidths
                );

                out.flush();

                int key = Inputs.readKey();

                /*
                 * Arrow keys
                 */
                if (key == RESET) {

                    int second = Inputs.readKey();
                    int third = Inputs.readKey();

                    if (second == '[' || second == 'O') {

                        switch (third) {

                            // Up
                            case 'A':
                                if (selectedOption > 0) {
                                    selectedOption--;

                                    selectedSubOption =
                                            getValidSubOption(
                                                    selectedOption,
                                                    selectedSubOption
                                            );
                                }
                                break;

                            // Down
                            case 'B':
                                if (selectedOption < OPTIONS.length - 1) {
                                    selectedOption++;

                                    selectedSubOption =
                                            getValidSubOption(
                                                    selectedOption,
                                                    selectedSubOption
                                            );
                                }
                                break;

                            // Right
                            case 'C':
                                if (selectedSubOption <
                                        SUB_OPTIONS[selectedOption].length - 1) {

                                    selectedSubOption++;
                                }
                                break;

                            // Left
                            case 'D':
                                if (selectedSubOption > 0) {
                                    selectedSubOption--;
                                }
                                break;
                        }
                    }

                    continue;
                }

                /*
                 * Enter
                 */
                if (key == ENTER || key == LF) {

                    // Return to menu
                    if (selectedOption == OPTIONS.length - 1) {
                        return -1;
                    }

                    // Update terminal size
                    if (selectedOption == 3) {

                        updateTerminalSize(settings);

                        savedRows[selectedOption] =
                                selectedSubOption;

                        return selectedOption;
                    }

                    /*
                     * Save selected setting.
                     */
                    settings.put(
                            KEYS[selectedOption],
                            SUB_OPTIONS[selectedOption][selectedSubOption]
                    );

                    savedRows[selectedOption] =
                            selectedSubOption;

                    saveSettings(settings);

                    return selectedOption;
                }
            }

        } finally {

            /*
             * Leave the menu in a clean state.
             */
            out.print(
                    terminal.getStringCapability(
                            InfoCmp.Capability.cursor_show
                    )
            );

            out.flush();
        }
    }


    /*
     * Draw the entire settings menu.
     */
    private static void render(
            Terminal terminal,
            PrintWriter out,
            Map<String, String> settings,
            int selectedOption,
            int selectedSubOption,
            int[] savedRows,
            int[] columnWidths) {

        clearScreen(terminal, out);

        for (int i = 0; i < OPTIONS.length - 1; i++) {

            boolean selected = selectedOption == i;

            /*
             * Main option
             */
            if (selected) {
                out.print("\033[7m");
            }

            out.print("  ");
            out.print(OPTIONS[i]);

            /*
             * Show current terminal dimensions.
             */
            if (i == 3) {

                String width =
                        settings.getOrDefault("termWidth", "");

                String height =
                        settings.getOrDefault("termHeight", "");

                if (!width.isEmpty() && !height.isEmpty()) {

                    String display =
                            width + "  x  " + height;

                    int padding =
                            Math.max(
                                    1,
                                    columnWidths[i] -
                                    display.length()
                            );

                    out.print(" ");
                    out.print(display);
                    out.print(" ".repeat(padding));
                }
            }

            /*
             * Show saved indicator.
             */
            if (savedRows[i] >= 0) {
                out.print(" <");
            }

            if (selected) {
                out.print("\033[27m");
            }

            out.println();


            /*
             * Sub-options
             */
            for (int j = 0;
                 j < SUB_OPTIONS[i].length;
                 j++) {

                boolean subSelected =
                        selectedOption == i &&
                        selectedSubOption == j;

                out.print("    ");

                if (subSelected) {
                    out.print("\033[7m");
                }

                out.print(SUB_OPTIONS[i][j]);

                if (subSelected) {
                    out.print("\033[27m");
                }

                int padding =
                        Math.max(
                                1,
                                columnWidths[i] -
                                SUB_OPTIONS[i][j].length()
                        );

                out.print(" ".repeat(padding));
                out.println();
            }

            out.println();
        }


        /*
         * Return to Menu
         */
        boolean returnSelected =
                selectedOption == OPTIONS.length - 1;

        if (returnSelected) {
            out.print("\033[7m");
        }

        out.print("  ");
        out.print(OPTIONS[OPTIONS.length - 1]);

        if (returnSelected) {
            out.print("\033[27m");
        }

        out.println();
    }


    /*
     * Calculate the width needed by each setting column.
     */
    private static int[] calculateColumnWidths() {

        int[] widths =
                new int[OPTIONS.length];

        for (int i = 0; i < OPTIONS.length; i++) {

            widths[i] =
                    OPTIONS[i].length();

            for (String option : SUB_OPTIONS[i]) {

                widths[i] =
                        Math.max(
                                widths[i],
                                option.length()
                        );
            }

            widths[i] += 4;
        }

        return widths;
    }


    /*
     * Find which sub-option matches the current setting.
     */
    private static int getSelectedSubOption(
            Map<String, String> settings,
            int option) {

        if (option >= KEYS.length ||
                SUB_OPTIONS[option].length == 0) {

            return 0;
        }

        String current =
                settings.get(KEYS[option]);

        if (current != null) {

            for (int i = 0;
                 i < SUB_OPTIONS[option].length;
                 i++) {

                if (SUB_OPTIONS[option][i]
                        .equalsIgnoreCase(current)) {

                    return i;
                }
            }
        }

        return 0;
    }


    /*
     * Keep the selected sub-option valid when
     * moving between settings.
     */
    private static int getValidSubOption(
            int option,
            int current) {

        int count =
                SUB_OPTIONS[option].length;

        if (count == 0) {
            return 0;
        }

        return Math.min(current, count - 1);
    }


    /*
     * Get the current terminal size through JLine
     * and save it to settings.json.
     */
    private static void updateTerminalSize(
            Map<String, String> settings)
            throws Exception {

        Terminal terminal =
                Inputs.getTerminal();

        Size size =
                terminal.getSize();

        String width =
                String.valueOf(size.getColumns());

        String height =
                String.valueOf(size.getRows());

        settings.put("termWidth", width);
        settings.put("termHeight", height);

        saveSettings(settings);

        generateAssets(width, height);
    }


    /*
     * Compile and run GenerateAssets.
     */
    private static void generateAssets(
            String width,
            String height)
            throws Exception {

        ProcessBuilder compile =
                new ProcessBuilder(
                        "javac",
                        "-cp",
                        ".",
                        "subsystems/GenerateAssets.java"
                );

        compile.directory(new File("."));
        compile.inheritIO();

        Process compileProcess =
                compile.start();

        int compileResult =
                compileProcess.waitFor();

        if (compileResult != 0) {
            return;
        }


        ProcessBuilder run =
                new ProcessBuilder(
                        "java",
                        "-cp",
                        ".",
                        "GenerateAssets",
                        width,
                        height
                );

        run.directory(new File("."));
        run.inheritIO();

        Process runProcess =
                run.start();

        runProcess.waitFor();
    }


    /*
     * Clear the terminal using JLine's terminal capability.
     */
    private static void clearScreen(
            Terminal terminal,
            PrintWriter out) {

        String clear =
                terminal.getStringCapability(
                        InfoCmp.Capability.clear_screen
                );

        if (clear != null) {
            out.print(clear);
        } else {
            out.print("\033[2J\033[H");
        }

        out.flush();
    }


    /*
     * Load settings.json.
     */
    private static Map<String, String> loadSettings() {

        Map<String, String> settings =
                new LinkedHashMap<>();

        /*
         * Defaults
         */
        settings.put("textSpeed", "Medium");
        settings.put("volume", "Medium");
        settings.put("difficulty", "Medium");
        settings.put("termWidth", "80");
        settings.put("termHeight", "24");

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(SETTINGS_FILE))) {

            StringBuilder json =
                    new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                json.append(line.trim());
            }

            String content =
                    json.toString();

            for (String key : KEYS) {

                int index =
                        content.indexOf(
                                "\"" + key + "\""
                        );

                if (index < 0) {
                    continue;
                }

                int colon =
                        content.indexOf(
                                ':',
                                index
                        );

                if (colon < 0) {
                    continue;
                }

                int start =
                        content.indexOf(
                                '"',
                                colon + 1
                        );

                if (start < 0) {
                    continue;
                }

                int end =
                        content.indexOf(
                                '"',
                                start + 1
                        );

                if (end < 0) {
                    continue;
                }

                settings.put(
                        key,
                        content.substring(
                                start + 1,
                                end
                        )
                );
            }

        } catch (IOException ignored) {
        }

        return settings;
    }


    /*
     * Save settings.json.
     */
    private static void saveSettings(
            Map<String, String> settings)
            throws IOException {

        try (PrintWriter pw =
                     new PrintWriter(
                             new FileWriter(
                                     SETTINGS_FILE))) {

            pw.println("{");

            for (int i = 0;
                 i < KEYS.length;
                 i++) {

                String key =
                        KEYS[i];

                String value =
                        settings.getOrDefault(
                                key,
                                "Medium"
                        );

                pw.print(
                        "  \"" +
                        key +
                        "\": \"" +
                        value +
                        "\""
                );

                pw.println(
                        i < KEYS.length - 1
                                ? ","
                                : ""
                );
            }

            pw.println("}");
        }
    }
}
