import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BruteForceSeamCarving {
    public static void main(String[] args) {
        try {
            // Load the input image
            File inputFile = new File("input.jpg");
            BufferedImage image = ImageIO.read(inputFile);

            if (image == null) {
                System.out.println("Error: Failed to load image.");
                return;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            System.out.println("Original Image Size: " + width + " x " + height);

            // Compute initial energy map
            double[][] energyMap = computeEnergy(image);

            // Save energy map visualization
            saveEnergyMap(energyMap, "energy_map.png");

            // Reduce width by removing seams (adjust dynamically)
            int seamsToRemove = Math.min(120, width - 1);
            for (int step = 0; step < seamsToRemove; step++) {
                int[] seam = findLowestEnergySeam(energyMap, width, height);
                image = removeSeam(image, seam, width, height);
                width--; // Reduce width
                energyMap = computeEnergy(image); // Recompute energy map
            }

            // Save the output image
            File outputFile = new File("output.jpg");
            ImageIO.write(image, "jpg", outputFile);

            System.out.println("Brute Force Seam Carving Applied Successfully!");
            System.out.println("New Image Size: " + width + " x " + height);

        } catch (IOException e) {
            System.out.println("Error processing the image: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes the energy map of the given image using gradient magnitude.
     */
    private static double[][] computeEnergy(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] energy = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int left = (x > 0) ? getBrightness(image, x - 1, y) : 0;
                int right = (x < width - 1) ? getBrightness(image, x + 1, y) : 0;
                int top = (y > 0) ? getBrightness(image, x, y - 1) : 0;
                int bottom = (y < height - 1) ? getBrightness(image, x, y + 1) : 0;

                int dx = right - left;
                int dy = bottom - top;

                energy[y][x] = Math.sqrt(dx * dx + dy * dy);
            }
        }
        return energy;
    }

    /**
     * Saves the energy map as a grayscale image.
     */
    private static void saveEnergyMap(double[][] energyMap, String filename) {
        int height = energyMap.length;
        int width = energyMap[0].length;
        BufferedImage energyImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Find min and max energy values for normalization
        double minEnergy = Double.MAX_VALUE;
        double maxEnergy = Double.MIN_VALUE;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                minEnergy = Math.min(minEnergy, energyMap[y][x]);
                maxEnergy = Math.max(maxEnergy, energyMap[y][x]);
            }
        }

        // Normalize and map energy values to grayscale (0-255)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grayValue = (int) (255 * (energyMap[y][x] - minEnergy) / (maxEnergy - minEnergy));
                Color color = new Color(grayValue, grayValue, grayValue);
                energyImage.setRGB(x, y, color.getRGB());
            }
        }

        // Save the energy map image
        try {
            ImageIO.write(energyImage, "png", new File(filename));
            System.out.println("Energy map saved as: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving energy map: " + e.getMessage());
        }
    }

    /**
     * Gets the brightness of a pixel by averaging the RGB values.
     */
    private static int getBrightness(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }

    /**
     * Finds the lowest energy vertical seam in the energy map.
     */
    private static int[] findLowestEnergySeam(double[][] energy, int width, int height) {
        int[] seam = new int[height];
        double minEnergy = Double.MAX_VALUE;

        for (int x = 0; x < width; x++) { 
            double seamEnergy = 0;
            int[] currentSeam = new int[height];
            int posX = x;

            for (int y = 0; y < height; y++) {
                currentSeam[y] = posX;
                seamEnergy += energy[y][posX];

                if (y < height - 1) {
                    int nextX = posX;
                    if (posX > 0 && energy[y + 1][posX - 1] < energy[y + 1][nextX]) {
                        nextX = posX - 1;
                    }
                    if (posX < width - 1 && energy[y + 1][posX + 1] < energy[y + 1][nextX]) {
                        nextX = posX + 1;
                    }
                    posX = nextX;
                }
            }

            if (seamEnergy < minEnergy) {
                minEnergy = seamEnergy;
                seam = currentSeam.clone();
            }
        }
        return seam;
    }

    /**
     * Removes the specified seam from the image.
     */
    private static BufferedImage removeSeam(BufferedImage image, int[] seam, int width, int height) {
        BufferedImage newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            int newX = 0;
            for (int x = 0; x < width; x++) {
                if (x != seam[y]) { // Skip the pixel in the seam
                    newImage.setRGB(newX, y, image.getRGB(x, y));
                    newX++;
                }
            }
        }
        return newImage;
    }
}
