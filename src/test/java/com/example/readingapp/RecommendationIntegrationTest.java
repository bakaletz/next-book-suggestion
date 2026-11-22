package com.example.readingapp;

import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationIntegrationTest {

    private static final String URL = "/api/recommendations";

    private static final String SEVEN_BOOKS_REQUEST = """
            {
              "recentBooks": [
                {"title":"Sapiens","author":"Yuval Noah Harari","genre":"History","finishedTimeStamp":"2026-08-05T20:00:00Z"},
                {"title":"Dune","author":"Frank Herbert","genre":"Science Fiction","finishedTimeStamp":"2026-11-01T18:30:00Z"},
                {"title":"Brave New World","author":"Aldous Huxley","genre":"Dystopian","finishedTimeStamp":"2026-05-01T08:00:00Z"},
                {"title":"The Hobbit","author":"J.R.R. Tolkien","finishedTimeStamp":"2026-09-20T12:00:00Z"},
                {"title":"1984","author":"George Orwell","genre":"Dystopian","finishedTimeStamp":"2026-07-10T10:00:00Z"},
                {"title":"Project Hail Mary","author":"Andy Weir","genre":"Science Fiction","finishedTimeStamp":"2026-10-15T09:00:00Z"},
                {"title":"Neuromancer","author":"William Gibson","genre":"Cyberpunk","finishedTimeStamp":"2026-06-01T08:00:00Z"}
              ],
              "limit": 2
            }
            """;

    private static final String MODEL_ANSWER = """
            {"recommendations":[
              {"title":"Hyperion","author":"Dan Simmons","genre":"Science Fiction","reason":"Epic space opera like Dune"},
              {"title":"The Name of the Wind","author":"Patrick Rothfuss","genre":"Fantasy","reason":"Rich fantasy like The Hobbit"}
            ]}
            """;

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean(answers = Answers.CALLS_REAL_METHODS)
    private ChatModel chatModel;

    @Test
    void returnsRecommendationsFromOpenAi() {
        when(chatModel.call(any(Prompt.class))).thenReturn(modelResponse(MODEL_ANSWER));

        assertThat(performPost(SEVEN_BOOKS_REQUEST))
                .hasStatusOk()
                .bodyJson()
                .convertTo(RecommendationResponse.class)
                .isEqualTo(new RecommendationResponse(List.of(
                        new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Epic space opera like Dune"),
                        new RecommendationDto("The Name of the Wind", "Patrick Rothfuss", "Fantasy", "Rich fantasy like The Hobbit"))));
    }

    @Test
    void sendsFiveMostRecentBooksAndLimitToOpenAi() {
        when(chatModel.call(any(Prompt.class))).thenReturn(modelResponse(MODEL_ANSWER));

        performPost(SEVEN_BOOKS_REQUEST);

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText().replace("\r\n", "\n")).startsWith("""
                Recently finished books, newest first:
                - "Dune" by Frank Herbert, genre: Science Fiction, finished: 2026-11-01T18:30:00Z
                - "Project Hail Mary" by Andy Weir, genre: Science Fiction, finished: 2026-10-15T09:00:00Z
                - "The Hobbit" by J.R.R. Tolkien, genre: unknown, finished: 2026-09-20T12:00:00Z
                - "Sapiens" by Yuval Noah Harari, genre: History, finished: 2026-08-05T20:00:00Z
                - "1984" by George Orwell, genre: Dystopian, finished: 2026-07-10T10:00:00Z

                Recommend 2 books.
                """);
    }

    @Test
    void rejectsInvalidRequestWithoutCallingOpenAi() {
        String fourBooksRequest = """
                {
                  "recentBooks": [
                    {"title":"Dune","author":"Frank Herbert","finishedTimeStamp":"2026-11-01T18:30:00Z"},
                    {"title":"Sapiens","author":"Yuval Noah Harari","finishedTimeStamp":"2026-08-05T20:00:00Z"},
                    {"title":"1984","author":"George Orwell","finishedTimeStamp":"2026-07-10T10:00:00Z"},
                    {"title":"The Hobbit","author":"J.R.R. Tolkien","finishedTimeStamp":"2026-09-20T12:00:00Z"}
                  ]
                }
                """;

        assertThat(performPost(fourBooksRequest)).hasStatus(HttpStatus.BAD_REQUEST);
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void returnsBadGatewayWhenOpenAiFails() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("OpenAI is unavailable"));

        assertThat(performPost(SEVEN_BOOKS_REQUEST))
                .hasStatus(HttpStatus.BAD_GATEWAY)
                .bodyJson()
                .isLenientlyEqualTo("""
                        {
                          "status": 502,
                          "detail": "Recommendations are temporarily unavailable, please try again later"
                        }
                        """);
    }

    @Test
    void removesAlreadyReadBooksFromOpenAiAnswer() {
        when(chatModel.call(any(Prompt.class))).thenReturn(modelResponse("""
                {"recommendations":[
                  {"title":"Neuromancer","author":"William Gibson","genre":"Cyberpunk","reason":"Already read"},
                  {"title":"Hyperion","author":"Dan Simmons","genre":"Science Fiction","reason":"Epic space opera like Dune"}
                ]}
                """));

        assertThat(performPost(SEVEN_BOOKS_REQUEST))
                .hasStatusOk()
                .bodyJson()
                .convertTo(RecommendationResponse.class)
                .isEqualTo(new RecommendationResponse(List.of(
                        new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Epic space opera like Dune"))));
    }

    private MvcTestResult performPost(String body) {
        return mockMvc.post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }

    private static ChatResponse modelResponse(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
