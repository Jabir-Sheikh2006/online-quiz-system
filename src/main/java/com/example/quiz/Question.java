package com.example.quiz;

import java.util.List;

public record Question(String text, List<String> options, int correctOption) {
}
