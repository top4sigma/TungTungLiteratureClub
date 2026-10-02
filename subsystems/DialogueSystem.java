package subsystems;

import java.io.*;
import java.util.*;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class DialogueSystem {

    private static Terminal terminal;

    public static void main(String[] args) throws Exception {

        // Create JLine terminal
        terminal = TerminalBuilder.builder()
                .system(true)
                .build();

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
        terminal.close();
    }


    private static long getDelay() {
        String speed = "Medium";

        try (BufferedReader br = new BufferedReader(
                new FileReader("settings.json"))) {

            StringBuilder json = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                json.append(line.trim());
            }

            String content = json.toString();

            int idx = content.indexOf("\"textSpeed\"");

            if (idx >= 0) {
                idx = content.indexOf(':', idx) + 1;

                int end = content.indexOf('"', idx + 1);

                if (end > idx) {
                    speed = content.substring(idx + 1, end);
                }
            }

        } catch (IOException ignored) {
        }

        switch (speed.toLowerCase()) {
            case "fast":
                return 20;

            case "slow":
                return 100;

            default:
                return 60;
        }
    }


    private static void printSlow(
            String text,
            long delay) throws Exception {

        for (char c : text.toCharArray()) {
            System.out.print(c);
            System.out.flush();
            Thread.sleep(delay);
        }
    }


    public static void outputText(
            String input,
            String character) throws Exception {

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


        /*
         * Get actual terminal dimensions from JLine.
         */
        int terminalWidth = terminal.getWidth();
        int terminalHeight = terminal.getHeight();


        /*
         * Center the textbox horizontally.
         */
        int leftPadding = Math.max(
                0,
                (terminalWidth - BOX_WIDTH) / 2
        );

        String padding = " ".repeat(leftPadding);


        /*
         * Calculate textbox height.
         */
        int boxHeight = rows.size() + 2;


        /*
         * Calculate the top row of the textbox.
         */
        int topRow = Math.max(
                1,
                terminalHeight - boxHeight + 1
        );


        /*
         * Move cursor to the top-left corner
         * of the textbox.
         */
        System.out.print(
                "\033[" +
                topRow +
                ";" +
                (leftPadding + 1) +
                "H"
        );


        /*
         * Top border.
         */
        String title = "════ " + character + " ";

        int remaining = BOX_WIDTH - title.length();

        System.out.println(
                "╔" +
                title +
                "═".repeat(
                        Math.max(0, remaining)
                ) +
                "╗"
        );


        /*
         * Dialogue text.
         */
        for (String row : rows) {

            String text = row;

            if (text.length() < textWidth) {
                text += " ".repeat(
                        textWidth - text.length()
                );
            }

            System.out.println(
                    "║" +
                    " ".repeat(SPRITE_SPACE + 1) +
                    text +
                    "║"
            );
        }


        /*
         * Bottom border.
         */
        System.out.println(
                "╚" +
                "═".repeat(BOX_WIDTH) +
                "╝"
        );


        /*
         * Move back to the first dialogue line.
         */
        System.out.print(
                "\033[" +
                (rows.size() + 1) +
                "A"
        );


        /*
         * Type out the dialogue.
         */
        for (int i = 0; i < rows.size(); i++) {

            /*
             * Move to the text area.
             *
             * leftPadding:
             *   centers the box.
             *
             * SPRITE_SPACE:
             *   reserves space for the sprite.
             */
            System.out.print(
                    "\033[" +
                    (leftPadding + SPRITE_SPACE + 2) +
                    "G"
            );

            printSlow(
                    rows.get(i),
                    delay
            );

            if (i < rows.size() - 1) {
                System.out.print("\033[1B");
            }
        }


        /*
         * Move cursor below the textbox.
         */
        System.out.print(
                "\033[" +
                (rows.size() + 1) +
                "B"
        );

        System.out.print("\r");
        System.out.flush();
    }
}
