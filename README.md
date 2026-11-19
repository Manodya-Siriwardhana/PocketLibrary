
# PocketLibrary

PocketLibrary is a Kotlin-based Android application for discovering books, viewing book details, and managing a personal reading library. Built using Jetpack Compose, Room Database, and Firebase for authentication and cloud synchronization.

## Features

- **Book Discovery:** Search and explore books using the Open Library API.
- **User Authentication:** Register and log in securely using Firebase Authentication.
- **Personal Library:** Save books to a "Want to Read" collection.
- **Cloud Synchronization:** Synchronize saved books through Firebase Firestore.
- **Book Details:** View book information, ratings, reviews, and descriptions.
- **Local Storage:** Manage saved book data using Room Database.

## My Contributions

This was a group project. My primary contributions focused on backend functionality and user interface development.

- **Authentication:** Implemented Firebase user registration and login using `AuthViewModel` and `LoginScreen`.
- **Application Navigation:** Managed screen navigation and application flow within `MainActivity`.
- **Cloud Synchronization:** Implemented Firebase Firestore integration for syncing saved books using `FirebaseService` and `BookEntity`.
- **Book Details:** Developed `BookDetailScreen`, including book information display and the "Want to Read" functionality.

## Technologies Used

- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Room Database
- Open Library API
- Android Studio
- Gradle

## How to Run

1. Clone the repository:

   ```bash
   git clone <repository-url>
   ```

2. Open the project in Android Studio.

3. Create a Firebase project and register an Android app with the package name `com.example.pocketlibrary`.

4. Download `google-services.json` from Firebase and place it inside the `app/` directory.

5. Enable Firebase Authentication (Email/Password), Cloud Firestore, and Firebase Storage, and configure appropriate security rules.

6. Sync the Gradle project and run the application using an Android emulator or physical device running Android 8.0 (API 26) or later.

## Project Information

This application was developed as a group project for academic purposes.
