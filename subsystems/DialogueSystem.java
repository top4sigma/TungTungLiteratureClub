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

        List<String> rows = new ArrayList<>();
        for (String line : input.split("\n")) {
            if (line.isEmpty()) {
                rows.add("");
                continue;
            }
            for (int i = 0; i < line.length(); i += 59) {
                rows.add(line.substring(i, Math.min(i + 59, line.length())));
            }
        }
        while (rows.size() < 2) rows.add("");

        System.out.println("╔════ " + character + " "
                + "═".repeat(Math.max(0, 58 - (5 + character.length()))) + "╗");
        for (int i = 0; i < rows.size(); i++) {
            System.out.println("║" + " ".repeat(59) + "║");
        }
        System.out.println("╚" + "═".repeat(59) + "╝");

        System.out.print("\033[" + (rows.size() + 1) + "A");

        for (int i = 0; i < rows.size(); i++) {
            System.out.print("\033[2G");
            printSlow(rows.get(i), delay);
            if (i < rows.size() - 1) System.out.print("\033[1B");
        }

        System.out.print("\033[2B\r");
        System.out.flush();
    }
}
