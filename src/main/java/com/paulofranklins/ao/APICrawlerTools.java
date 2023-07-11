package com.paulofranklins.ao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class APICrawlerTools {
    private static final String URL = "https://render.albiononline.com/v1/item/";
    private static final String TYPE_NAMES_FILE = "src/main/resources/tools_names.txt";
    private static final String DOWNLOADED_FILE = "src/main/resources/tools_downloaded.txt";

    public static void main(String[] args) {
        List<String> names;
        try {
            names = Files.readAllLines(Paths.get(TYPE_NAMES_FILE));
        } catch (IOException e) {
            System.err.println("Error reading tools_names.txt file: " + e.getMessage());
            return;
        }

        Set<String> savedImages = loadDownloadedImages();

        int newDownloads = 0;
        int skippedImages = 0;

        for (String name : names) {
            for (int i = 1; i <= 5; i++) {
                String imageUrl = URL + name + ".png" + "?quality=" + i;
                String fileName = name + "_" + getQualityName(i) + ".png";

                if (savedImages.contains(fileName)) {
                    System.out.println("Skipping " + fileName + " (already saved)");
                    skippedImages++;
                    continue;
                }

                // Download and save the image
                try {
                    InputStream inputStream = new java.net.URL(imageUrl).openStream();
                    OutputStream outputStream = new FileOutputStream(fileName);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                    System.out.println("Saved " + fileName);
                    savedImages.add(fileName);
                    newDownloads++;
                } catch (IOException e) {
                    System.err.println("Error retrieving " + imageUrl + ": " + e.getMessage());
                }
            }
        }

        saveDownloadedImages(savedImages);

        System.out.println("New downloads: " + newDownloads);
        System.out.println("Skipped images: " + skippedImages);
    }

    private static String getQualityName(int quality) {
        switch (quality) {
            case 1:
                return "normal";
            case 2:
                return "good";
            case 3:
                return "outstanding";
            case 4:
                return "excellent";
            case 5:
                return "masterpiece";
            default:
                return "";
        }
    }

    private static Set<String> loadDownloadedImages() {
        Set<String> savedImages = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DOWNLOADED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                savedImages.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error loading downloaded images: " + e.getMessage());
        }
        return savedImages;
    }

    private static void saveDownloadedImages(Set<String> savedImages) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOWNLOADED_FILE, true))) {
            for (String fileName : savedImages) {
                writer.write(fileName);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving downloaded images: " + e.getMessage());
        }
    }
}
