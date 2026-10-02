package subsystems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class Cutscene {

    private Cutscene() {
        // Utility class
    }

    /**
     * Plays a text-based cutscene from a file.
     *
     * Format:
     *
     * Character Name
     * *First textbox
     * *Second textbox
     * *Third textbox
     *
     * Character Name
     * *Another textbox
     *
     * A blank line resets the character.
     *
     * @param path path to the cutscene file
     */
    public static void cutsceneStart(String path)
            throws IOException, InterruptedException {

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(path))) {

            String character = null;
            String line;

            while ((line = reader.readLine()) != null) {

                // Blank line = reset character
                if (line.isBlank()) {
                    character = null;
                    continue;
                }

                // Non-dialogue line = character name
                if (!line.startsWith("*")) {
                    if (character == null) {
                        character = line;
                    }

                    continue;
                }

                // Every '*' creates its own textbox
                if (character != null) {
                    DialogueSystem.outputText(line, character);
                }
            }
        }
    }
}
