package subsystems;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class Sounds {

    private static float volumeScale = -1f;

    private static void loadVolume() {
        if (volumeScale >= 0) return;
        try (BufferedReader br = new BufferedReader(new FileReader("settings.json"))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) json.append(line.trim());
            String content = json.toString();
            int idx = content.indexOf("\"volume\"");
            if (idx >= 0) {
                idx = content.indexOf(':', idx) + 1;
                int end = content.indexOf('"', idx + 1);
                if (end > idx) setVolume(content.substring(idx + 1, end));
            }
        } catch (IOException ignored) {
            setVolume("Medium");
        }
    }

    public static void setVolume(String level) {
        switch (level.toLowerCase()) {
            case "low":   volumeScale = 0.33f; break;
            case "medium":volumeScale = 0.66f; break;
            case "loud":  volumeScale = 1.0f;  break;
            default:      volumeScale = 0.66f; break;
        }
    }

    public static void playSound(String path) throws Exception {
        loadVolume();
        if (volumeScale <= 0) return;

        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Sound not found: " + path);
            return;
        }

        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = stream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            stream.close();
            throw new UnsupportedAudioFileException("Line not supported: " + format);
        }

        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(stream);
        stream.close();

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (20.0 * Math.log10(volumeScale));
            dB = Math.max(dB, gain.getMinimum());
            dB = Math.min(dB, gain.getMaximum());
            gain.setValue(dB);
        }

        clip.start();
    }
}
