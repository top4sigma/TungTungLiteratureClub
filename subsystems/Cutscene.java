package subsystems;

import java.io.BufferedReader;
import java.io.FileReader;

public class Cutscene {

    /**
     * Make a text based cutscene using this function. Pass in a path to a file as a string. 
     * @throws Exception 
     */
    public static void CutsceneStart(String path) throws Exception{
        BufferedReader bfro = new BufferedReader(new FileReader(path));

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
                    outputDialogue(character, dialogue);

                    dialogue.setLength(0);
                    dialogue.append(line);
                }

                // First non-dialogue line is the character name
                else if (character == null) {
                    character = line;
                }

                // Additional dialogue lines
                else {
                    if (dialogue.length() > 0) {
                        dialogue.append('\n');
                    }
                    dialogue.append(line);
                }
            }

            // Output the final textbox
            outputDialogue(character, dialogue);
        }
    }

    private static void outputDialogue(
            String character,
            StringBuilder dialogue) throws Exception {

        if (character != null && dialogue.length() > 0) {
            DialogueSystem.outputText(dialogue.toString(), character);
        }
    }
}