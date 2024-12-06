<p align="center">
  <img src="images/Liminal-Banner.png" alt="Liminal Banner"  />
</p>

# Liminal

**Liminal** is a simple Manga and Novel Reader Android application built using **Kotlin** and **Jetpack Compose**. The app fetches manga and novel data from Turkish websites using **web scraping**. Users can save their reading progress and download series for offline reading.

## Screenshots

<p align="center">
  <img width="18%" height="auto" src="images/home.png" />
  <img width="18%" height="auto" src="images/detail.png" />
  <img width="18%" height="auto" src="images/manga_reading.png" />
  <img width="18%" height="auto" src="images/novel_reading.png" />
  <img width="18%" height="auto" src="images/library.png" />
</p>

## Features

- Read manga and novels from Turkish sources.
- Save reading progress.
- Download series for offline access.
- Choose between scrolling horizontally (swipe right or left) or vertically (webtoon mode).

## Technologies Used

- **Kotlin:** The main programming language used to build the app.
- **Jetpack Compose:** Used for building the user interface with a modern, declarative approach.
- **Room:** A local database to store user data such as reading progress and downloaded content.
- **Hilt:** A dependency injection library to manage dependencies and improve code organization.
- **WorkManager:** Used to handle background tasks, such as downloading content for offline reading.
- **Jsoup:** A library for web scraping, fetching manga and novel data from Turkish websites.
- **Coroutines and Flow:** For managing asynchronous tasks and handling data streams in a reactive way.

## How to Build and Run

1. Clone the repository:

    ```bash
    git clone https://github.com/Neccar43/Liminal.git
    cd liminal
    
    ```

2. Open the project in Android Studio.
3. Build and run the app.

## Contributing

Contributions are welcome! If you'd like to contribute:  
1. Fork this repository.  
2. Create a new feature branch.  
3. Commit your changes and submit a pull request.

## Future Plans

- Improve the downloading process.
- Add support for the MangaDex API.
- Improve notification system for new chapters.
- Customize download options.
- Add support for downloading novels.
- Implement caching for better performance.
- Add advanced filters and search functionality.
