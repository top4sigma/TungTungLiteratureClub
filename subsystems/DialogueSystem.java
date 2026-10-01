package subsystems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DialogueSystem {

    private static final int TEXT_WIDTH = 59;
    private static final int MIN_ROWS = 2;

    public static void main(String[] args) throws Exception {
        readDialogueFile("./dump.txt");
    }

    private static void readDialogueFile(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            String character = null;
            String line;
            StringBuilder dialogue = new StringBuilder();

            while ((line = reader.readLine()) != null) {

                // Blank line = end of dialogue block
                if (line.isBlank()) {
                    outputDialogue(character, dialogue);
                    character = null;
                    dialogue.setLength(0);
                    continue;
                }

                // Line beginning with * = new dialogue textbox
                if (line.startsWith("*")) {
                    outputDialogue(character, dialogue);

                    dialogue.setLength(0);
                    dialogue.append(line);
                    continue;
                }

                // First non-dialogue line = character name
                if (character == null) {
                    character = line;
                    continue;
                }

                // Additional text belonging to the current textbox
                if (dialogue.length() > 0) {
                    dialogue.append('\n');
                }

                dialogue.append(line);
            }

            // Handle final textbox if file doesn't end with a blank line
            outputDialogue(character, dialogue);
        }
    }

    private static void outputDialogue(String character, StringBuilder dialogue)
            throws IOException {

        if (character != null && dialogue.length() > 0) {
            try {
                outputText(dialogue.toString(), character);
            } catch (Exception e) {
                throw new IOException("Failed to display dialogue", e);
            }
        }
    }

    private static long getDelay() {
        String speed = "Medium";

        try (BufferedReader reader =
                     new BufferedReader(new FileReader("settings.json"))) {

            StringBuilder json = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line.trim());
            }

            String content = json.toString();
            int start = content.indexOf("\"textSpeed\"");

            if (start != -1) {
                int colon = content.indexOf(':', start);

                if (colon != -1) {
                    int firstQuote = content.indexOf('"', colon);
                    int secondQuote = content.indexOf('"', firstQuote + 1);

                    if (firstQuote != -1 && secondQuote != -1) {
                        speed = content.substring(
                                firstQuote + 1,
                                secondQuote
                        );
                    }
                }
            }

        } catch (IOException ignored) {
            // Use Medium speed if settings cannot be read.
        }

        return switch (speed.toLowerCase()) {
            case "fast" -> 20;
            case "slow" -> 100;
            default -> 60;
        };
    }

    private static void printSlow(String text, long delay) throws InterruptedException {
        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i));
            System.out.flush();
            Thread.sleep(delay);
        }
    }

    public static void outputText(String input, String character)
            throws InterruptedException {

        long delay = getDelay();

        List<String> rows = new ArrayList<>();

        for (String line : input.split("\\R", -1)) {

            if (line.isEmpty()) {
                rows.add("");
                continue;
            }

            for (int start = 0; start < line.length(); start += TEXT_WIDTH) {
                rows.add(
                    line.substring(
                        start,
                        Math.min(start + TEXT_WIDTH, line.length())
                    )
                );
            }
        }

        // Every textbox must be at least two lines tall.
        while (rows.size() < MIN_ROWS) {
            rows.add("");
        }

        drawTextbox(character, rows);

        // Move cursor back into the textbox.
        System.out.print("\033[" + (rows.size() + 1) + "A");

        for (int i = 0; i < rows.size(); i++) {
            System.out.print("\033[2G");

            printSlow(rows.get(i), delay);

            if (i < rows.size() - 1) {
                System.out.print("\033[1B");
            }
        }

        // Move cursor below the textbox.
        System.out.print("\033[2B\r");
        System.out.flush();
    }

    private static void drawTextbox(String character, List<String> rows) {

        int nameLength = 5 + character.length();
        int remainingWidth = Math.max(0, TEXT_WIDTH - nameLength);

        System.out.println(
                "╔════ " + character
                        + " ".repeat(remainingWidth)
                        + "╗"
        );

        for (int i = 0; i < rows.size(); i++) {
            System.out.println(
                    "║" + " ".repeat(TEXT_WIDTH) + "║"
            );
        }

        System.out.println(
                "╚" + "═".repeat(TEXT_WIDTH) + "╝"
        );
    }
}
