import subsystems.Inputs;
import subsystems.TitleScreen;
import subsystems.SettingsMenu;

public class TTLCMain {
    public static void main(String[] args) throws Exception {
        Inputs.open();
        Inputs.startScreen();

        while (true) {
            int choice = TitleScreen.show();

            String[] options = { "Play", "Settings", "Quit" };
            if (options[choice].equals("Settings")) {
                while (true) {
                    int setting = SettingsMenu.show();
                    if (setting == -1) break;
                    System.out.println("Setting chosen: " + setting);
                }
            } else if (options[choice].equals("Quit")) {
                break;
            }
        }

        Inputs.close();
    }
}
