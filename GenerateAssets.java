import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;

public class GenerateAssets {
    static final char[] QUAD = {
        ' ', '▗', '▖', '▄', '▝', '▐', '▘', '▟',
        ' ', '▚', '▌', '▙', '▀', '▞', '▛', '█'
    };

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java GenerateAssets <width> <height>");
            System.exit(1);
        }
        int termW = Integer.parseInt(args[0]);
        int termH = Integer.parseInt(args[1]);

        File assetsDir = new File("assets");
        processDir(assetsDir, termW, termH);
    }

    static void processDir(File dir, int termW, int termH) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                processDir(f, termW, termH);
                continue;
            }
            String name = f.getName().toLowerCase();
            if (!name.endsWith(".jpg") && !name.endsWith(".jpeg") &&
                !name.endsWith(".png") && !name.endsWith(".gif") &&
                !name.endsWith(".bmp")) continue;

            BufferedImage img = ImageIO.read(f);
            if (img == null) { System.err.println("Skip: " + f.getPath()); continue; }

            double scale = Math.min((double) img.getWidth() / 1920.0, (double) img.getHeight() / 1080.0);
            if (scale > 1.0) scale = 1.0;
            int charW = Math.max(1, (int) Math.round(termW * scale));

            double ratio = (img.getHeight() / 2.0) / img.getWidth() * (charW / 2.0);
            int height = Math.max((int) ratio, 1);
            if (height > termH) height = termH;

            img = brightness(img, 1.15);
            img = resize(img, charW, height * 2);

            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < height * 2; y += 2) {
                for (int x = 0; x < charW; x++) {
                    int[] rgb = {
                        getRGB(img, x, y),
                        getRGB(img, Math.min(x + 1, img.getWidth() - 1), y),
                        getRGB(img, x, y + 1),
                        getRGB(img, Math.min(x + 1, img.getWidth() - 1), y + 1)
                    };
                    int[] b = new int[4];
                    for (int i = 0; i < 4; i++) b[i] = bright(rgb[i]);

                    int thresh = (b[0] + b[1] + b[2] + b[3]) / 4;
                    int pattern =
                        (b[0] >= thresh ? 1 : 0) << 3 |
                        (b[1] >= thresh ? 1 : 0) << 2 |
                        (b[2] >= thresh ? 1 : 0) << 1 |
                        (b[3] >= thresh ? 1 : 0);

                    boolean swap = false;
                    if (pattern == 0b1000) { pattern = 0b0111; swap = true; }

                    char ch = QUAD[pattern];

                    int[] fg = avgBright(rgb, b, thresh);
                    int[] bg = avgDark(rgb, b, thresh);
                    if (swap) { int[] t = fg; fg = bg; bg = t; }

                    sb.append("\033[38;2;" + fg[0] + ";" + fg[1] + ";" + fg[2] + "m");
                    sb.append("\033[48;2;" + bg[0] + ";" + bg[1] + ";" + bg[2] + "m");
                    sb.append(ch);
                    sb.append("\033[0m");
                }
                sb.append('\n');
            }

            String outName = f.getName().replaceAll("\\.[^.]+$", "") + ".txt";
            File outFile = new File(f.getParent(), outName);
            java.io.FileWriter fw = new java.io.FileWriter(outFile);
            fw.write(sb.toString());
            fw.close();
            System.out.println("Wrote: " + outFile.getPath());
        }
    }

    static int getRGB(BufferedImage img, int x, int y) {
        return img.getRGB(x, Math.min(y, img.getHeight() - 1));
    }

    static int bright(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int) (r * 0.299 + g * 0.587 + b * 0.114);
    }

    static int[] avgBright(int[] rgb, int[] b, int thresh) {
        int sr = 0, sg = 0, sb2 = 0, n = 0;
        for (int i = 0; i < 4; i++) {
            if (b[i] >= thresh) {
                sr += (rgb[i] >> 16) & 0xFF;
                sg += (rgb[i] >> 8) & 0xFF;
                sb2 += rgb[i] & 0xFF;
                n++;
            }
        }
        if (n == 0) return avgAll(rgb);
        return new int[]{sr / n, sg / n, sb2 / n};
    }

    static int[] avgDark(int[] rgb, int[] b, int thresh) {
        int sr = 0, sg = 0, sb2 = 0, n = 0;
        for (int i = 0; i < 4; i++) {
            if (b[i] < thresh) {
                sr += (rgb[i] >> 16) & 0xFF;
                sg += (rgb[i] >> 8) & 0xFF;
                sb2 += rgb[i] & 0xFF;
                n++;
            }
        }
        if (n == 0) return avgAll(rgb);
        return new int[]{sr / n, sg / n, sb2 / n};
    }

    static int[] avgAll(int[] rgb) {
        int sr = 0, sg = 0, sb2 = 0;
        for (int i = 0; i < 4; i++) {
            sr += (rgb[i] >> 16) & 0xFF;
            sg += (rgb[i] >> 8) & 0xFF;
            sb2 += rgb[i] & 0xFF;
        }
        return new int[]{sr / 4, sg / 4, sb2 / 4};
    }

    static BufferedImage brightness(BufferedImage img, double factor) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * factor));
                int g = Math.min(255, (int) (((rgb >> 8) & 0xFF) * factor));
                int b = Math.min(255, (int) ((rgb & 0xFF) * factor));
                out.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    static BufferedImage resize(BufferedImage img, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}
