package com.paulofranklins.ao;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class APICrawler {
    private static final String URL = "https://render.albiononline.com/v1/item/";
    private static final String TYPE_NAMES_FILE = "src/main/resources/file_names.txt";
    private static final String DOWNLOADED_FILE = "src/main/resources/file_downloaded.txt";
    private static final String DOWNLOAD_DIR = "src/main/resources/download";

    private static final HashMap<Integer, String> QUALITY_LABELS = new HashMap<Integer, String>() {{
        put(1, "_NORMAL");
        put(2, "_GOOD");
        put(3, "_OUTSTANDING");
        put(4, "_EXCELLENT");
        put(5, "_MASTERPIECE");
    }};

    public static void main(String[] args) {
        List<String> names;
        try {
            names = Files.readAllLines(Paths.get(TYPE_NAMES_FILE));
        } catch (IOException e) {
            System.err.println("Error reading file_names.txt file: " + e.getMessage());
            return;
        }

        Set<String> savedImages = loadDownloadedImages();

        HttpClient httpClient = HttpClientBuilder.create().build();
        int newDownloads = 0;
        int skippedImages = 0;

        for (String name : names) {
            for (int quality = 1; quality <= 5; quality++) {
                String imageUrl = URL + name + ".png?quality=" + quality;
                String fileName = name + "_" + QUALITY_LABELS.get(quality) + ".png";

                if (savedImages.contains(fileName)) {
                    System.out.println("Skipping " + fileName + " (already saved)");
                    skippedImages++;
                    continue;
                }

                HttpGet request = new HttpGet(imageUrl);
                try {
                    HttpResponse response = httpClient.execute(request);
                    HttpEntity entity = response.getEntity();
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200 && entity != null) {
                        Path outputPath = Paths.get(DOWNLOAD_DIR, fileName);
                        try (InputStream inputStream = entity.getContent(); OutputStream outputStream = new FileOutputStream(outputPath.toString())) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            System.out.println("Saved " + fileName);
                            savedImages.add(fileName);
                            newDownloads++;
                        }
                    } else {
                        System.err.println("Error retrieving " + fileName);
                    }
                    EntityUtils.consumeQuietly(entity);
                } catch (IOException e) {
                    System.err.println("Error retrieving " + imageUrl + ": " + e.getMessage());
                }
            }
        }

        saveDownloadedImages(savedImages);

        System.out.println("New downloads: " + newDownloads);
        System.out.println("Skipped images: " + skippedImages);
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
