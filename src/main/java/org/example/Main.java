package org.example;
import org.apache.commons.lang3.tuple.Pair;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
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

        Scanner scanner = new Scanner(System.in);
        System.out.println("Which transformation would you like to apply?");
        System.out.println("1. Convert to grayscale");
        System.out.println("2. Convert to sepia");
        System.out.println("3. Convert to negative");
        System.out.println("4. Apply Gaussian blur");
        System.out.println("5. Rotate 90 degrees clockwise");
        System.out.println("6. Scale down by 50%");
        System.out.println("7. Increase brightness");
        System.out.println("8. Apply edge detection");

        System.out.println("Enter the number of the desired transformation:");
        int transformationType = scanner.nextInt();
        scanner.close();

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
                        Pair<String, BufferedImage> new_pair = processImage(pair, transformationType);
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

    private static Pair<String, BufferedImage> processImage(Pair<String, BufferedImage> pair, int transformationType) {
        ImageTransformations imageTransformations = new ImageTransformations(pair.getRight(), transformationType);
        BufferedImage transformedImage = imageTransformations.transformImage();
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
}