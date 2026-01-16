package com.tuyenvp.spring_boot_app.Services.Impl;

import com.tuyenvp.spring_boot_app.Repository.OpenAIRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL_GPT_3_5 = "gpt-3.5-turbo";
    private static final String MODEL_GPT_4 = "gpt-4.5-turbo";
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 2000;
    private static final int MAX_TOKENS = 200;
    private static final double TEMPERATURE = 0.7;

    private final OpenAIRepository repository;
    private final Map<String, String> cache;
    private final RestTemplate restTemplate;

    @Autowired
    public ChatService(OpenAIRepository repository) {
        this.repository = repository;
        this.cache = new HashMap<>();
        this.restTemplate = new RestTemplate();
    }

    public String getChatResponse(String message) {
        return cache.computeIfAbsent(message, this::callOpenAI);
    }

    public String getAnswerFromOpenAi(String question) {
        OpenAiService service = new OpenAiService(getApiKey(), Duration.ofSeconds(30));
        String prompt = "Dựa trên dữ liệu sau đây, hãy trả lời câu hỏi:\n" + question;

        ChatCompletionRequest request = buildChatCompletionRequest(prompt, MODEL_GPT_4);
        ChatCompletionResult result = service.createChatCompletion(request);

        return result.getChoices().get(0).getMessage().getContent();
    }

    private String getApiKey() {
        return repository.findById(1L)
                .orElseThrow(() -> new RuntimeException("API Key not found"))
                .getApiKey();
    }

    private String callOpenAI(String message) {
        int retryDelay = INITIAL_RETRY_DELAY_MS;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return executeOpenAIRequest(message);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempt < MAX_RETRIES - 1) {
                    handleRateLimitError(retryDelay);
                    retryDelay *= 2;
                } else {
                    return "Lỗi OpenAI API: " + e.getMessage();
                }
            }
        }
        return "Lỗi 429: Quá nhiều request, hãy thử lại sau!";
    }

    private String executeOpenAIRequest(String message) {
        HttpEntity<Map<String, Object>> entity = buildHttpEntity(message);
        ResponseEntity<Map> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, entity, Map.class);
        return extractResponseContent(response);
    }

    private HttpEntity<Map<String, Object>> buildHttpEntity(String message) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL_GPT_3_5);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", message)));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(requestBody, headers);
    }

    private String extractResponseContent(ResponseEntity<Map> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private ChatCompletionRequest buildChatCompletionRequest(String prompt, String model) {
        ChatMessage message = new ChatMessage("user", prompt);
        return ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(message))
                .maxTokens(MAX_TOKENS)
                .temperature(TEMPERATURE)
                .build();
    }

    private void handleRateLimitError(int retryDelay) {
        logger.warn("Lỗi 429: Đợi {} giây trước khi thử lại...", retryDelay / 1000);
        try {
            Thread.sleep(retryDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Bị gián đoạn trong khi chờ retry", e);
        }
    }
}
