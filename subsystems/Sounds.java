package subsystems;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class Sounds {

    private static final String SETTINGS_FILE = "settings.json";

    private static float volumeScale = -1f;

    private Sounds() {
        // Utility class
    }

    // -------------------------------------------------------------------------
    // Volume
    // -------------------------------------------------------------------------

    public static void setVolume(String level) {

        volumeScale = switch (level.toLowerCase()) {
            case "low" -> 0.33f;
            case "loud" -> 1.0f;
            case "medium" -> 0.66f;
            default -> 0.66f;
        };
    }

    private static void loadVolume() {

        if (volumeScale >= 0) {
            return;
        }

        String volume = "Medium";

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(SETTINGS_FILE))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (!line.startsWith("\"volume\"")) {
                    continue;
                }

                int colon = line.indexOf(':');

                if (colon == -1) {
                    break;
                }

                int firstQuote = line.indexOf('"', colon);

                if (firstQuote == -1) {
                    break;
                }

                int secondQuote = line.indexOf('"', firstQuote + 1);

                if (secondQuote == -1) {
                    break;
                }

                volume = line.substring(
                        firstQuote + 1,
                        secondQuote
                );

                break;
            }

        } catch (IOException ignored) {
            // Use Medium volume if settings cannot be read.
        }

        setVolume(volume);
    }

    // -------------------------------------------------------------------------
    // Sound playback
    // -------------------------------------------------------------------------

    public static void playSound(String path) throws Exception {

        loadVolume();

        if (volumeScale <= 0) {
            return;
        }

        File file = new File(path);

        if (!file.isFile()) {
            System.err.println("Sound not found: " + path);
            return;
        }

        try (AudioInputStream stream =
                     AudioSystem.getAudioInputStream(file)) {

            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(
                    Clip.class,
                    format
            );

            if (!AudioSystem.isLineSupported(info)) {
                throw new UnsupportedAudioFileException(
                        "Audio line not supported: " + format
                );
            }

            Clip clip = (Clip) AudioSystem.getLine(info);

            clip.open(stream);
            setClipVolume(clip);

            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();
        }
    }

    private static void setClipVolume(Clip clip) {

        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain =
                (FloatControl) clip.getControl(
                        FloatControl.Type.MASTER_GAIN
                );

        float decibels =
                (float) (20.0 * Math.log10(volumeScale));

        decibels = Math.max(
                gain.getMinimum(),
                Math.min(decibels, gain.getMaximum())
        );

        gain.setValue(decibels);
    }
}
