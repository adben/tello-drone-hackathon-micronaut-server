package nl.codefoundry.tellodroneserver.services;

import java.awt.image.BufferedImage;

public class ImageRecognitionUtil {

    public static Color TRACK_COLOR = Color.RED;

    public enum Color {
        RED, YELLOW, GREEN, BLUE
    }
    
    public static BufferedImage detectBalloon(BufferedImage image) {
        removeAllButColor(image, TRACK_COLOR);
        return image;
    }

    public static void removeAllButColor(BufferedImage image, Color color) {
        /* Remove RGB Value for Group Tag */
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        /* Set all pixels within +- range of base RGB to black */
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i]>>24)     &0xFF; //alpha
            int r = (pixels[i]>>16)     &0xFF; //red
            int g = (pixels[i]>>8)      &0xFF; //green
            int b = (pixels[i]>>0)      &0xFF; //blue

            // blue ~ 80 | 120 | 170
            // red ~ 140 | 60 | 50
            // green ~ 100 | 130 | 65
            // yellow ~ 150 | 150 | 30

            switch(color) {
                case RED: {
                    if (r < 100 || g > 80 || b > 70) {
                        pixels[i] = 0xFF000000; //Set to black
                    }
                    break;
                }
                case YELLOW: {
                    if (r < 130 || g < 130 || b > 80 || Math.abs(r - g) > 10) {
                        pixels[i] = 0xFF000000; //Set to black
                    }
                    break;
                }
                case GREEN: { // works
                    if (r < 80 || g < 100 || b > 80 || Math.abs(r - g) < 10) {
                        pixels[i] = 0xFF000000; //Set to black
                    }
                    break;
                }
                case BLUE: { // works
                    if (r > 100 || (g < 100 && g > 150) || b < 150) {
                        pixels[i] = 0xFF000000; //Set to black
                    }
                    break;
                }
            }
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
    }
}
