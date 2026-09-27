package subsystems;
import java.io.*;

public class DialogueSystem {

    public static void main(String[] args) throws Exception {
        String path = "./dump.txt";

        BufferedReader bfro = new BufferedReader(
                new FileReader(path));

        String character = null;
        String st;

        StringBuilder dialogue = new StringBuilder();

        while ((st = bfro.readLine()) != null) {

            // Blank line = completely new character
            if (st.trim().isEmpty()) {

                if (character != null && dialogue.length() > 0) {
                    outputText(dialogue.toString(), character);
                    // waitForEnter();
                }

                character = null;
                dialogue.setLength(0);
            }

            // * = new textbox
            else if (st.startsWith("*")) {

                // Print previous textbox
                if (character != null && dialogue.length() > 0) {
                    outputText(dialogue.toString(), character);
                    // waitForEnter();
                }

                // Start the new textbox
                dialogue.setLength(0);
                dialogue.append(st);
            }

            // First line = character name
            else if (character == null) {
                character = st;
            }

            // Normal dialogue line
            else {

                if (dialogue.length() > 0) {
                    dialogue.append("\n");
                }

                dialogue.append(st);
            }
        }

        // Print final textbox
        if (character != null && dialogue.length() > 0) {
            outputText(dialogue.toString(), character);
            // waitForEnter();
        }

        bfro.close();
    }


    public static void outputText(String input, String character) {

        System.out.print("╔════ ");
        System.out.print(character);
        System.out.print(" ");

        // Extra space after character name
        for (int a = 0; a < 58 - (5 + character.length()); a++) {
            System.out.print("═");
        }

        System.out.println("╗");

        String[] lines = input.split("\n");

        int lineCount = 0;

        for (String line : lines) {

            int length = line.length();
            int repeats = Math.max(1, (int) Math.ceil(length / 59.0));

            for (int i = 0; i < repeats; i++) {

                System.out.print("║");

                int start = i * 59;
                int end = Math.min(start + 59, length);

                String part = line.substring(start, end);

                System.out.print(part);

                for (int j = part.length(); j < 59; j++) {
                    System.out.print(" ");
                }

                System.out.println("║");

                lineCount++;
            }
        }

        // Every textbox must be at least 2 lines tall
        while (lineCount < 2) {

            System.out.print("║");

            for (int j = 0; j < 59; j++) {
                System.out.print(" ");
            }

            System.out.println("║");

            lineCount++;
        }

        System.out.println(
                "╚═══════════════════════════════════════════════════════════╝"
        );
    }


    // public static void waitForEnter() throws Exception {

        // Temporary version:
        //System.in.read();
    // }
}