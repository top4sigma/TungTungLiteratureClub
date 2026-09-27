public class TTLCMain {
    public static void main(String[] args) throws Exception {
        Inputs.open();
        Inputs.startScreen();

        int choice = TitleScreen.show();

        String[] options = { "Play", "Settings", "Quit" };
        if (options[choice].equals("Settings")) {
            int setting = SettingsMenu.show();
            System.out.println("Setting chosen: " + setting);
        }

        Inputs.close();
    }
}
