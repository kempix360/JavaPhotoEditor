package org.example;
import org.apache.commons.lang3.tuple.Pair;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.stream.Stream;


public class Main {
    public static void main(String[] args) {
        int num_of_threads = 10;
        String inputDirectory = "src/main/resources/images";
        String outputDirectory = "src/main/resources/images_processed";

        List<Path> files;
        Path source = Path.of(inputDirectory);

        try (Stream<Path> stream = Files.list(source)) {
            files = stream.collect(Collectors.toList());
            ExecutorService executor = Executors.newFixedThreadPool(num_of_threads);
            long start_time = System.currentTimeMillis();

            files.parallelStream()
                    .map(path ->
                    {
                        try {
                            BufferedImage image = ImageIO.read(path.toFile());
                            String name = String.valueOf(path.getFileName());
                            return Pair.of(name, image);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .forEach(pair -> executor.submit(() ->
                    {
                        Pair<String, BufferedImage> new_pair = processImage(pair);
                        saveImage(new_pair, outputDirectory);
                    }));

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Number of threads: " + num_of_threads);
            long time_elapsed = System.currentTimeMillis() - start_time;
            System.out.println("Time elapsed: " + (float) time_elapsed / 1000 + " s");

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Pair<String, BufferedImage> processImage(Pair<String, BufferedImage> pair) {
        BufferedImage transformedImage = transformImage(pair.getRight());
        System.out.println("Transformed a file: " + pair.getLeft());
        return Pair.of(pair.getLeft(), transformedImage);
    }

    private static void saveImage(Pair<String, BufferedImage> pair, String outputDirectory) {
        try {
            String outputPath = outputDirectory + File.separator + pair.getLeft();
            ImageIO.write(pair.getRight(), "jpg", new File(outputPath));
            System.out.println("Saved a file: " + pair.getLeft());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage transformImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = originalImage.getRGB(i, j);
                Color color = new Color(rgb);

                int red = color.getRed();
                int green = color.getBlue();
                int blue = color.getGreen();

                Color newColor = new Color(red, green, blue);
                transformedImage.setRGB(i, j, newColor.getRGB());
            }
        }
        return transformedImage;
    }
}