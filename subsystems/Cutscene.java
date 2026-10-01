package subsystems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import subsystems.DialogueSystem;

public class Cutscene {
    public static void main(String[] args){

    }


    /**
     * Make a text based cutscene using this function. Pass in a path to a file as a string. 
     */
    public static void CutsceneStart(String path) throws IOException, Exception {
        BufferedReader bfro = new BufferedReader(new FileReader(path));

        String character = null;
        String st;

        StringBuilder dialogue = new StringBuilder();

        while ((st = bfro.readLine()) != null) {
            if (st.trim().isEmpty()) {

                if (character != null && dialogue.length() > 0) {
                    subsystems.DialogueSystem.outputText(dialogue.toString(), character);
                    // waitForEnter();
                }

                character = null;
                dialogue.setLength(0);
            }

            else if (st.startsWith("*")) {

                // Print previous textbox
                if (character != null && dialogue.length() > 0) {
                    subsystems.DialogueSystem.outputText(dialogue.toString(), character);
                    // waitForEnter();
                }

                // Start the new textbox
                dialogue.setLength(0);
                dialogue.append(st);
            }
        }
    }
}
