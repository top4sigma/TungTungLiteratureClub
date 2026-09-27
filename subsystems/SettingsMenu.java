package subsystems;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public class SettingsMenu {

    public static int show() throws Exception {
        String[] options = new String[]{"Text Speed", "Volume", "Difficulty", "Return to Menu"};
        String[][] subOptions = new String[][]{
            new String[]{"Fast", "Medium", "Slow"},
            new String[]{"Loud", "Medium", "Low"},
            new String[]{"Hard", "Medium", "Easy"},
            new String[]{""}
        };

        int[] colWidths = new int[options.length];
        for (int i = 0; i < options.length; i++) {
            colWidths[i] = options[i].length();
            for (int j = 0; j < subOptions[i].length; j++) {
                if (subOptions[i][j].length() > colWidths[i]) {
                    colWidths[i] = subOptions[i][j].length();
                }
            }
            colWidths[i] += 4;
        }

        int col = 0;
        int row = 0;

        try {
            while (true) {

                Inputs.clearScreen();

                int flatIdx = 0;
                for (int i = 0; i < options.length - 1; i++) {
                    if (col == i) Inputs.getTerminal().writer().print("\033[7m");
                    Inputs.getTerminal().writer().print("  " + options[i]);
                    if (col == i) Inputs.getTerminal().writer().print("\033[27m");
                    Inputs.getTerminal().writer().println();
                    flatIdx++;

                    for (int j = 0; j < subOptions[i].length; j++) {
                        Inputs.getTerminal().writer().print("    ");
                        if (col == i && row == j) Inputs.getTerminal().writer().print("\033[7m");
                        Inputs.getTerminal().writer().print(subOptions[i][j]);
                        if (col == i && row == j) Inputs.getTerminal().writer().print("\033[27m");
                        Inputs.getTerminal().writer().print(" ".repeat(Math.max(1, colWidths[i] - subOptions[i][j].length())));
                        flatIdx++;
                    }
                    Inputs.getTerminal().writer().println();
                }

                // Render "Return to Menu" once at the end
                if (col == options.length - 1) Inputs.getTerminal().writer().print("\033[7m");
                Inputs.getTerminal().writer().print("  " + options[options.length - 1]);
                if (col == options.length - 1) Inputs.getTerminal().writer().print("\033[27m");
                Inputs.getTerminal().writer().println();
                flatIdx++;

                Inputs.flush();

                int key = Inputs.readKey();

                if (key == 27) {
                    int second = Inputs.readKey();
                    int third = Inputs.readKey();

                    if ((second == 'O' || second == '[') && third == 'A') {
                        if (col > 0) {
                            col--;
                            if (row >= subOptions[col].length) row = subOptions[col].length - 1;
                        }
                    } else if ((second == 'O' || second == '[') && third == 'B') {
                        if (col < options.length - 1) {
                            col++;
                            if (row >= subOptions[col].length) row = subOptions[col].length - 1;
                        }
                    } else if ((second == 'O' || second == '[') && third == 'C') {
                        if (row < subOptions[col].length - 1) row++;
                    } else if ((second == 'O' || second == '[') && third == 'D') {
                        if (row > 0) row--;
                    }
                } else if (key == 13 || key == 10) {
                    if (col == options.length - 1) {
                        return -1; // Return to menu
                    }
                    int fi = 0;
                    for (int i = 0; i < col; i++) fi += 1 + subOptions[i].length;
                    fi += 1 + row;
                    return fi;
                }
            }
        } finally {
        }
    }
}
