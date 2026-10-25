# Online Quiz System

A beginner-friendly FSJP mini project built with Java 21, Spring Boot, Thymeleaf, HTML, CSS and JavaScript. The quiz uses an in-memory HTTP session, so no database or external service is required.

## Features

- 10 Java multiple-choice questions
- Start, retry, next and submit actions
- Progress bar and 10-minute countdown
- Automatic scoring and pass/fail result
- Responsive dark navy and purple interface
- Server-side quiz deadline and session state

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer

## Run the Project

```bash
cd /workspaces/online-quiz-system
mvn spring-boot:run
```

Open <http://localhost:8080> in your browser. To stop the server, press `Ctrl+C` in the terminal.

Alternatively, create and run the packaged application:

```bash
mvn clean package
java -jar target/online-quiz-system-1.0.0.jar
```

## Project Structure

```text
src/main/java/com/example/quiz/
  OnlineQuizApplication.java  # Starts Spring Boot
  QuizController.java         # Routes, timer, answers and score
  Question.java               # Question data model

src/main/resources/
  templates/                  # Thymeleaf HTML pages
  static/css/style.css        # Website design
  static/js/quiz.js           # Client-side countdown
  application.properties      # Server configuration
```

## How It Works

1. The controller stores questions, the current question number, answers and deadline in `HttpSession`.
2. Thymeleaf receives the current question and renders it from Java objects.
3. Each answer is posted to the controller and stored as a zero-based option index.
4. The controller compares answers with the correct-option indexes and calculates percentage and pass/fail.
5. The quiz needs at least 50% to pass.

## Viva Notes

- **Why Spring Boot:** It provides embedded server, dependency management and automatic configuration.
- **Why Thymeleaf:** It renders dynamic server-side HTML using Java model values.
- **Why no database:** An in-memory session is sufficient for a single quiz attempt and keeps the project simple.
- **How score works:** `correctAnswers / totalQuestions * 100`.
- **How timeout works:** The server stores an `Instant` deadline; JavaScript displays the remaining time, and submission is checked again server-side.
- **Session:** A browser session keeps one user's answers separate from other users while the app is running.
