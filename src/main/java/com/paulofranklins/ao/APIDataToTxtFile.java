package com.paulofranklins.ao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class APIDataToTxtFile {
    private static final String FILE_PATH = "src/main/resources/file_names.txt";
    private static final long DELAY = 0; // Delay before the first execution (in milliseconds)
    private static final long PERIOD = 60 * 1000; // Repeat interval (in milliseconds)

    public static void main(String[] args) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    String apiUrl = "https://gameinfo.albiononline.com/api/gameinfo/events";
                    List<String> newNames = fetchDataAndExtractNames(apiUrl);
                    saveNewNamesToFile(newNames);
                } catch (IOException | JSONException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, DELAY, PERIOD);
    }

    private static List<String> fetchDataAndExtractNames(String apiUrl) throws IOException, JSONException {
        HttpURLConnection connection = null;
        List<String> newNames = new ArrayList<>();

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                }

                JSONArray jsonArray = new JSONArray(response.toString());
                List<String> typeNames = extractTypeNames(jsonArray);
                Set<String> existingNames = loadExistingNames();

                newNames = filterExistingNames(typeNames, existingNames);
                displayNewTypeNames(newNames);
            } else {
                System.err.println("Error: Failed to fetch data. Response Code: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return newNames;
    }

    private static List<String> extractTypeNames(JSONArray jsonArray) throws JSONException {
        List<String> typeNames = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject event = jsonArray.getJSONObject(i);
            JSONObject equipment = event.getJSONObject("Killer").getJSONObject("Equipment");

            for (String category : equipment.keySet()) {
                JSONObject item = equipment.optJSONObject(category);
                if (item != null && item.has("Type")) {
                    String typeName = item.getString("Type");
                    typeNames.add(typeName);
                }
            }
        }

        return typeNames;
    }

    private static Set<String> loadExistingNames() {
        Set<String> existingNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                existingNames.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error loading existing names: " + e.getMessage());
        }

        return existingNames;
    }

    private static List<String> filterExistingNames(List<String> typeNames, Set<String> existingNames) {
        List<String> newNames = new ArrayList<>();

        for (String typeName : typeNames) {
            if (!existingNames.contains(typeName)) {
                newNames.add(typeName);
            }
        }

        return newNames;
    }

    private static void displayNewTypeNames(List<String> newNames) {
        System.out.println("New type names:");
        for (String typeName : newNames) {
            System.out.println(typeName);
        }
    }

    private static void saveNewNamesToFile(List<String> newNames) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            for (String name : newNames) {
                writer.write(name);
                writer.newLine();
            }
            System.out.println("New type names saved to " + FILE_PATH);
            System.out.println("Number of new names added: " + newNames.size());
        } catch (IOException e) {
            System.err.println("Error saving new type names: " + e.getMessage());
        }
    }
}
