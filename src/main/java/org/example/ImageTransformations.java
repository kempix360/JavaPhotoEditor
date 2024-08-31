package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class ImageTransformations {

    private final BufferedImage originalImage;
    private final int transformationType;
    private final int width;
    private final int height;
    private BufferedImage transformedImage;

    public ImageTransformations(BufferedImage originalImage, int transformationType) {
        this.originalImage = originalImage;
        this.transformationType = transformationType;
        this.width = originalImage.getWidth();
        this.height = originalImage.getHeight();
        this.transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage transformImage() {
        switch (transformationType) {
            case 1:
                return convertToGrayscale();
            case 2:
                return convertToSepia();
            case 3:
                return convertToNegative();
            case 4:
                return applyGaussianBlur();
            case 5:
                return rotateImage(90);
            case 6:
                return applyNoise(50.0);
            case 7:
                return changeBrightness(3.0);
            case 8:
                return changeBrightness(0.3);
            case 9:
                return applyEdgeDetection();
            default:
                return originalImage;
        }
    }

    public BufferedImage convertToGrayscale() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int grayLevel = (r + g + b) / 3;
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                transformedImage.setRGB(x, y, gray);
            }
        }
        return transformedImage;
    }

    public BufferedImage convertToSepia() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);

                tr = Math.min(255, tr);
                tg = Math.min(255, tg);
                tb = Math.min(255, tb);

                int sepia = (tr << 16) | (tg << 8) | tb;
                transformedImage.setRGB(x, y, sepia);
            }
        }
        return transformedImage;
    }

    public BufferedImage convertToNegative() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int nr = 255 - r;
                int ng = 255 - g;
                int nb = 255 - b;

                int negative = (nr << 16) | (ng << 8) | nb;
                transformedImage.setRGB(x, y, negative);
            }
        }
        return transformedImage;
    }


    public BufferedImage applyGaussianBlur() {
        float[] matrix = {
                1/256f,  4/256f,  6/256f,  4/256f, 1/256f,
                4/256f, 16/256f, 24/256f, 16/256f, 4/256f,
                6/256f, 24/256f, 36/256f, 24/256f, 6/256f,
                4/256f, 16/256f, 24/256f, 16/256f, 4/256f,
                1/256f,  4/256f,  6/256f,  4/256f, 1/256f
        };

        BufferedImageOp op = new ConvolveOp(new Kernel(5, 5, matrix), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage transformedImage = originalImage;
        for (int i = 0; i < 6; i++)
            transformedImage = op.filter(transformedImage, null);

        return transformedImage;
    }


    public BufferedImage rotateImage(double degrees) {
        double radians = Math.toRadians(degrees);
        int newWidth = (int) Math.abs(width * Math.cos(radians)) + (int) Math.abs(height * Math.sin(radians));
        int newHeight = (int) Math.abs(width * Math.sin(radians)) + (int) Math.abs(height * Math.cos(radians));

        transformedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = transformedImage.createGraphics();

        g2d.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g2d.rotate(radians, (double) width / 2, (double) height / 2);
        g2d.drawRenderedImage(originalImage, null);
        g2d.dispose();

        return transformedImage;
    }

    public BufferedImage applyNoise(double noiseLevel) {
        Random random = new Random();
        transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int noiseR = (int) (random.nextGaussian() * noiseLevel);
                int noiseG = (int) (random.nextGaussian() * noiseLevel);
                int noiseB = (int) (random.nextGaussian() * noiseLevel);

                r = Math.min(255, Math.max(0, r + noiseR));
                g = Math.min(255, Math.max(0, g + noiseG));
                b = Math.min(255, Math.max(0, b + noiseB));

                int noisyPixel = (r << 16) | (g << 8) | b;
                transformedImage.setRGB(x, y, noisyPixel);
            }
        }

        return transformedImage;
    }

    public BufferedImage scaleImage(double scale) {
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        transformedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = transformedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return transformedImage;
    }


    public BufferedImage changeBrightness(double factor) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);

                int r = (int)(((rgb >> 16) & 0xFF) * factor);
                int g = (int)(((rgb >> 8) & 0xFF) * factor);
                int b = (int)((rgb & 0xFF) * factor);

                r = Math.min(255, r);
                g = Math.min(255, g);
                b = Math.min(255, b);

                int brighter = (r << 16) | (g << 8) | b;
                transformedImage.setRGB(x, y, brighter);
            }
        }
        return transformedImage;
    }


    public BufferedImage applyEdgeDetection() {
        float[] sobelX = {
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        };

        float[] sobelY = {
                -1, -2, -1,
                0,  0,  0,
                1,  2,  1
        };

        BufferedImageOp opX = new ConvolveOp(new Kernel(3, 3, sobelX), ConvolveOp.EDGE_NO_OP, null);
        BufferedImageOp opY = new ConvolveOp(new Kernel(3, 3, sobelY), ConvolveOp.EDGE_NO_OP, null);

        BufferedImage sobelXImage = opX.filter(originalImage, null);
        BufferedImage sobelYImage = opY.filter(originalImage, null);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelX = sobelXImage.getRGB(x, y);
                int pixelY = sobelYImage.getRGB(x, y);

                int r = (int) Math.min(255, Math.sqrt(Math.pow((pixelX >> 16) & 0xFF, 2) + Math.pow((pixelY >> 16) & 0xFF, 2)));
                int g = (int) Math.min(255, Math.sqrt(Math.pow((pixelX >> 8) & 0xFF, 2) + Math.pow((pixelY >> 8) & 0xFF, 2)));
                int b = (int) Math.min(255, Math.sqrt(Math.pow(pixelX & 0xFF, 2) + Math.pow(pixelY & 0xFF, 2)));

                int edgeColor = (r << 16) | (g << 8) | b;
                transformedImage.setRGB(x, y, edgeColor);
            }
        }

        return transformedImage;
    }

}
