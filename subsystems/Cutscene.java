package subsystems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Cutscene {

    /**
     * Makes a text-based cutscene using the specified file.
     *
     * @param path path to the cutscene file
     * @throws IOException if the file cannot be read
     */
    public static void cutsceneStart(String path) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            String character = null;
            String line;
            StringBuilder dialogue = new StringBuilder();

            while ((line = reader.readLine()) != null) {

                // Blank line = end of textbox
                if (line.isBlank()) {
                    outputDialogue(character, dialogue);
                    character = null;
                    dialogue.setLength(0);
                }

                // Lines beginning with * are dialogue
                else if (line.startsWith("*")) {

                    // Output previous textbox
                    outputDialogue(character, dialogue);

                    // Start new textbox
                    dialogue.setLength(0);
                    dialogue.append(line);
                }
            }

            // Output the final textbox if the file doesn't end with a blank line
            outputDialogue(character, dialogue);
        }
    }

    private static void outputDialogue(String character, StringBuilder dialogue) {
        if (character != null && dialogue.length() > 0) {
            DialogueSystem.outputText(dialogue.toString(), character);
        }
    }
}
