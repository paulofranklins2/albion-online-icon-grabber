# Albion Online Icon.png Grabber

## Description
This project enables the retrieval of Albion Online icon images. The code is designed to fetch all the `icon.png` files associated with the game.

## Features
- Downloads and saves Albion Online icon images
- Skips downloading images that have already been saved
- Supports specifying `?quality=1` in the image URL for improved image quality
- Provides error handling for file reading, HTTP requests, and image saving

## Usage
1. Ensure that you have the necessary dependencies installed.
2. Update the file paths (`harvest_names.txt` and `file_downloaded.txt`) in the code if the project's directory structure or file locations have changed.
3. Run the `APICrawlerResources` class to initiate the image retrieval process.
4. The program will read the list of icon names from `harvest_names.txt` and download the corresponding icon images from Albion Online.
5. New images will be saved while skipping those that have already been downloaded.
6. The downloaded image filenames will be stored in `file_downloaded.txt` for future reference.
7. Monitor the console output for progress updates, including the number of new downloads and skipped images.

## Note
Please note that the code assumes a specific directory structure and file locations. Make sure to adjust the file paths accordingly if your project's structure differs.

This project enhances the functionality of the application by automating the retrieval of Albion Online icon images. It improves efficiency by avoiding redundant downloads and provides better code organization and error handling for maintainability.

