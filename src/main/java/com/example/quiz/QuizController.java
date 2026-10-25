package com.example.quiz;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Controller
public class QuizController {

    private static final String QUIZ_QUESTIONS = "quizQuestions";
    private static final String CURRENT_INDEX = "currentIndex";
    private static final String ANSWERS = "answers";
    private static final String DEADLINE = "deadline";
    private static final Duration QUIZ_TIME = Duration.ofMinutes(10);

    private static final List<Question> QUESTIONS = List.of(
            new Question("Which keyword is used to create a class in Java?",
                    List.of("class", "object", "struct", "define"), 0),
            new Question("Which of these is a valid Java entry point?",
                    List.of("public void start()", "static void main()", "public static void main(String[] args)", "void main(String args)"), 2),
            new Question("Which data type stores a true or false value?",
                    List.of("int", "boolean", "char", "double"), 1),
            new Question("Which operator compares two values for equality?",
                    List.of("=", "==", "!=", "==="), 1),
            new Question("What is the size of an int in Java?",
                    List.of("8 bits", "16 bits", "32 bits", "64 bits"), 2),
            new Question("Which keyword prevents a class from being inherited?",
                    List.of("static", "final", "private", "sealed"), 1),
            new Question("Which collection stores unique elements?",
                    List.of("ArrayList", "LinkedList", "HashSet", "Vector"), 2),
            new Question("Which method converts a String to an integer?",
                    List.of("Integer.parseInt()", "Integer.valueOfString()", "String.toInt()", "Convert.toInteger()"), 0),
            new Question("Which exception is thrown when dividing by zero in integer arithmetic?",
                    List.of("NullPointerException", "ArithmeticException", "NumberFormatException", "IOException"), 1),
            new Question("Which feature allows a class to have many methods with the same name?",
                    List.of("Inheritance", "Encapsulation", "Overloading", "Abstraction"), 2)
    );

    @GetMapping("/")
    public String home(HttpSession session) {
        clearQuiz(session);
        return "home";
    }

    @PostMapping("/quiz/start")
    public String startQuiz(HttpSession session) {
        clearQuiz(session);
        session.setAttribute(QUIZ_QUESTIONS, QUESTIONS);
        session.setAttribute(CURRENT_INDEX, 0);
        session.setAttribute(ANSWERS, new ArrayList<Integer>(QUESTIONS.size()));
        session.setAttribute(DEADLINE, Instant.now().plus(QUIZ_TIME));
        return "redirect:/quiz";
    }

    @GetMapping("/quiz")
    public String showQuestion(Model model, HttpSession session) {
        List<Question> questions = getQuestions(session);
        if (questions == null) {
            return "redirect:/";
        }

        int index = getIndex(session);
        if (Instant.now().isAfter(getDeadline(session))) {
            return "redirect:/result";
        }
        if (index >= questions.size()) {
            return "redirect:/result";
        }

        model.addAttribute("question", questions.get(index));
        model.addAttribute("questionNumber", index + 1);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("progress", (int) (((index + 1) / (double) questions.size()) * 100));
        model.addAttribute("secondsLeft", Math.max(0, (int) Duration.between(Instant.now(), getDeadline(session)).toSeconds()));
        model.addAttribute("lastQuestion", index == questions.size() - 1);
        return "quiz";
    }

    @PostMapping("/quiz/answer")
    public String submitAnswer(@RequestParam(name = "answer", required = false) Integer answer,
                               HttpSession session) {
        List<Question> questions = getQuestions(session);
        if (questions == null || Instant.now().isAfter(getDeadline(session))) {
            return "redirect:/result";
        }

        int index = getIndex(session);
        if (index >= questions.size()) {
            return "redirect:/result";
        }

        List<Integer> answers = getAnswers(session);
        answers.add(answer);
        session.setAttribute(CURRENT_INDEX, index + 1);

        return index + 1 == questions.size() ? "redirect:/result" : "redirect:/quiz";
    }

    @GetMapping("/result")
    public String showResult(Model model, HttpSession session) {
        List<Question> questions = getQuestions(session);
        if (questions == null) {
            return "redirect:/";
        }

        List<Integer> answers = getAnswers(session);
        int score = 0;
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i) != null && answers.get(i) == questions.get(i).correctOption()) {
                score++;
            }
        }

        int percentage = (int) Math.round(score * 100.0 / questions.size());
        model.addAttribute("score", score);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("percentage", percentage);
        model.addAttribute("passed", percentage >= 50);
        model.addAttribute("timeExpired", Instant.now().isAfter(getDeadline(session)));
        clearQuiz(session);
        return "result";
    }

    private List<Question> getQuestions(HttpSession session) {
        return (List<Question>) session.getAttribute(QUIZ_QUESTIONS);
    }

    private int getIndex(HttpSession session) {
        return (int) session.getAttribute(CURRENT_INDEX);
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getAnswers(HttpSession session) {
        return (List<Integer>) session.getAttribute(ANSWERS);
    }

    private Instant getDeadline(HttpSession session) {
        return (Instant) session.getAttribute(DEADLINE);
    }

    private void clearQuiz(HttpSession session) {
        session.removeAttribute(QUIZ_QUESTIONS);
        session.removeAttribute(CURRENT_INDEX);
        session.removeAttribute(ANSWERS);
        session.removeAttribute(DEADLINE);
    }
}
