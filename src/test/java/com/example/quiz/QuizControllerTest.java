package com.example.quiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void completeQuizScoresAndRendersPass() throws Exception {
        MockHttpSession session = startQuiz();

        assertQuestionPage(session, 1, 10, "10:00");
        submitAnswers(session, new int[]{0, 2, 1, 1, 2, 1, 2, 0, 1, 2});

        mockMvc.perform(get("/result").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("100%")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("10/10")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PASS")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Time limit reached"))));
    }

    @Test
    void completeQuizScoresAndRendersFail() throws Exception {
        MockHttpSession session = startQuiz();
        submitAnswers(session, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0});

        mockMvc.perform(get("/result").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("20%")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2/10")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FAIL")));
    }

    @Test
    void timeoutEndsQuizWithoutCountingRemainingAnswers() throws Exception {
        MockHttpSession session = startQuiz();
        session.setAttribute("deadline", java.time.Instant.now().minusSeconds(1));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/result"));

        mockMvc.perform(get("/result").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Time limit reached")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("0/10")));
    }

    @Test
    void retryClearsPreviousAttemptAndStartsAtOne() throws Exception {
        MockHttpSession firstSession = startQuiz();
        submitAnswers(firstSession, new int[]{0, 2, 1, 1, 2, 1, 2, 0, 1, 2});
        mockMvc.perform(get("/result").session(firstSession)).andExpect(status().isOk());

        MockHttpSession retrySession = startQuiz();
        assertQuestionPage(retrySession, 1, 10, "10:00");
        assertThat(retrySession.getAttribute("answers")).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST).isEmpty();
    }

    @Test
    void correctOptionIndexesAreNotRenderedBeforeSubmission() throws Exception {
        MockHttpSession session = startQuiz();

        String response = mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("correctOption", "answer-0", "correctAnswer");
        assertThat(response).contains("name=\"answer\"");
        assertThat(response).contains("value=\"0\"", "value=\"1\"", "value=\"2\"", "value=\"3\"");
    }

    private MockHttpSession startQuiz() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/quiz/start"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"))
                .andReturn().getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private void assertQuestionPage(MockHttpSession session, int number, int total, String timer) throws Exception {
        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Question <strong>" + number + "</strong> of <span>" + total + "</span>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("aria-valuenow=\"" + (number * 10) + "\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(timer)));
    }

    private void submitAnswers(MockHttpSession session, int[] answers) throws Exception {
        for (int answer : answers) {
            mockMvc.perform(post("/quiz/answer").session(session).param("answer", String.valueOf(answer)))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
