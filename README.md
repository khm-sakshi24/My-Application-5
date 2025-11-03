# College Management Android App

## 1. Overview

This repository contains the source code for a College Management Android App, developed as part of a comprehensive software development project. The app provides a platform for both students and teachers to manage academic activities. It features secure user authentication, role-based access to features, and real-time communication through push notifications. The entire application is built using modern Android development practices with Kotlin, Jetpack Compose, and Firebase.

## 2. Features Implemented

*   **Secure User Authentication**: Full signup, login, and logout functionality using Firebase Authentication.
*   **Role-Based UI**: The user interface dynamically adapts based on the user's role (e.g., `student` or `teacher`), which is stored and retrieved from Firestore.
*   **Push Notifications**: Real-time push notifications using Firebase Cloud Messaging (FCM) to keep users informed.
*   **Firestore Database**: User profiles and roles are securely stored and managed in Google's Firestore NoSQL database.
*   **Modern UI**: A clean, polished, and responsive user interface built with Jetpack Compose.
*   **Automated Testing**: The project includes a suite of unit and integration tests to ensure code quality and stability.

## 3. Tech Stack & Architecture

*   **Language**: Kotlin
*   **UI**: Jetpack Compose
*   **Backend & Database**: Firebase (Authentication, Firestore, Cloud Messaging)
*   **Architecture**: Follows modern Android app architecture principles, with a focus on a reactive UI and separation of concerns.
*   **Testing**: JUnit 5 for unit tests, and Espresso for UI/integration tests.

## 4. Screenshots

*(You should add screenshots of your app here. For example: a screenshot of the login screen, the student dashboard, and the teacher dashboard.)*

<img src="path/to/your/screenshot1.png" width="250"> <img src="path/to/your/screenshot2.png" width="250">

## 5. Setup and Installation

To build and run this project, follow these steps:

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/your-repo-name.git
    ```
2.  **Open in Android Studio**: Open the cloned directory in the latest version of Android Studio.
3.  **Firebase Setup**:
    *   Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    *   Add an Android app to your Firebase project with the package name `com.example.myapplication4`.
    *   Download the `google-services.json` file provided by Firebase.
    *   Place the `google-services.json` file in the **`app/`** directory of the project.
4.  **Sync and Run**: Sync the project with Gradle files and click the "Run 'app'" button.

## 6. Testing

The project includes a suite of automated tests to ensure stability.

*   **To run local unit tests**:
    ```bash
    ./gradlew testDebugUnitTest
    ```
*   **To run integration tests** on a connected emulator or device:
    ```bash
    ./gradlew connectedAndroidTest
    ```
