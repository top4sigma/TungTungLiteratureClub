package subsystems;
import java.io.*;
import java.util.*;

public class DialogueSystem {

    public static void main(String[] args) throws Exception {
        String path = "./dump.txt";

        BufferedReader bfro = new BufferedReader(
                new FileReader(path));

        String character = null;
        String st;

        StringBuilder dialogue = new StringBuilder();

        while ((st = bfro.readLine()) != null) {

            if (st.trim().isEmpty()) {

                if (character != null && dialogue.length() > 0) {
                    outputText(dialogue.toString(), character);
                }

                character = null;
                dialogue.setLength(0);
            }

            else if (st.startsWith("*")) {

                if (character != null && dialogue.length() > 0) {
                    outputText(dialogue.toString(), character);
                }

                dialogue.setLength(0);
                dialogue.append(st);
            }

            else if (character == null) {
                character = st;
            }

            else {

                if (dialogue.length() > 0) {
                    dialogue.append("\n");
                }

                dialogue.append(st);
            }
        }

        if (character != null && dialogue.length() > 0) {
            outputText(dialogue.toString(), character);
        }

        bfro.close();
    }


    private static long getDelay() {
        String speed = "Medium";
        try (BufferedReader br = new BufferedReader(new FileReader("settings.json"))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) json.append(line.trim());
            String content = json.toString();
            int idx = content.indexOf("\"textSpeed\"");
            if (idx >= 0) {
                idx = content.indexOf(':', idx) + 1;
                int end = content.indexOf('"', idx + 1);
                if (end > idx) speed = content.substring(idx + 1, end);
            }
        } catch (IOException ignored) {}
        switch (speed.toLowerCase()) {
            case "fast":   return 20;
            case "slow":   return 100;
            default:       return 60;
        }
    }

    private static void printSlow(String text, long delay) throws Exception {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            System.out.flush();
            Thread.sleep(delay);
        }
    }


    public static void outputText(String input, String character) throws Exception {
        long delay = getDelay();

        final int BOX_WIDTH = 59;
        final int SPRITE_SPACE = 10;

        List<String> rows = new ArrayList<>();

        int textWidth = BOX_WIDTH - SPRITE_SPACE - 1;

        for (String line : input.split("\n")) {
            if (line.isEmpty()) {
                rows.add("");
                continue;
            }

            for (int i = 0; i < line.length(); i += textWidth) {
                rows.add(line.substring(
                        i,
                        Math.min(i + textWidth, line.length())
                ));
            }
        }

        while (rows.size() < 2) {
            rows.add("");
        }

        // Automatically detect terminal dimensions
        int[] terminalSize = getTerminalSize();
        int terminalWidth = terminalSize[0];
        int terminalHeight = terminalSize[1];

        // Center the textbox horizontally
        int leftPadding = Math.max(0, (terminalWidth - BOX_WIDTH) / 2);
        String padding = " ".repeat(leftPadding);

        // Calculate how far down the textbox needs to be
        int boxHeight = rows.size() + 2;
        int topPadding = Math.max(0, terminalHeight - boxHeight);

        // Move to the bottom of the terminal
        System.out.print("\033[" + terminalHeight + ";1H");

        // Move up to where the textbox should start
        if (topPadding > 0) {
            System.out.print("\033[" + topPadding + "A");
        }

        // Top border
        String title = "════ " + character + " ";
        int remaining = BOX_WIDTH - title.length();

        System.out.println(
                padding +
                "╔" +
                title +
                "═".repeat(Math.max(0, remaining)) +
                "╗"
        );

        // Dialogue text
        for (String row : rows) {

            String text = row;

            if (text.length() < textWidth) {
                text += " ".repeat(textWidth - text.length());
            }

            System.out.println(
                    padding +
                    "║" +
                    " ".repeat(SPRITE_SPACE + 1) +
                    text +
                    "║"
            );
        }

        // Bottom border
        System.out.println(
                padding +
                "╚" +
                "═".repeat(BOX_WIDTH) +
                "╝"
        );

        /*
        * Move back to the first dialogue line.
        */
        System.out.print("\033[" + (rows.size() + 1) + "A");

        for (int i = 0; i < rows.size(); i++) {

            // Move to the text area
            System.out.print(
                    "\033[" +
                    (leftPadding + SPRITE_SPACE + 2) +
                    "G"
            );

            printSlow(rows.get(i), delay);

            if (i < rows.size() - 1) {
                System.out.print("\033[1B");
            }
        }

        // Move cursor below the textbox
        System.out.print("\033[" + (rows.size() + 1) + "B");
        System.out.print("\r");
        System.out.flush();
    }


    /**
     * Gets the current terminal width and height.
     *
     * Returns:
     * [0] = width
     * [1] = height
     */
    private static int[] getTerminalSize() {

        // Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {

            try {
                Process process = new ProcessBuilder(
                        "cmd", "/c", "mode con"
                ).redirectErrorStream(true).start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;
                int width = -1;
                int height = -1;

                while ((line = reader.readLine()) != null) {

                    line = line.trim();

                    if (line.startsWith("Columns:")) {
                        width = Integer.parseInt(
                                line.substring("Columns:".length()).trim()
                        );
                    }

                    else if (line.startsWith("Lines:")) {
                        height = Integer.parseInt(
                                line.substring("Lines:".length()).trim()
                        );
                    }
                }

                process.waitFor();

                if (width > 0 && height > 0) {
                    return new int[] { width, height };
                }

            } catch (Exception ignored) {
            }
        }

        // Linux / macOS
        try {
            String columns = System.getenv("COLUMNS");
            String lines = System.getenv("LINES");

            if (columns != null && lines != null) {
                int width = Integer.parseInt(columns);
                int height = Integer.parseInt(lines);

                if (width > 0 && height > 0) {
                    return new int[] { width, height };
                }
            }
        } catch (Exception ignored) {
        }

        // Safe fallback
        return new int[] { 120, 30 };
    }
}
