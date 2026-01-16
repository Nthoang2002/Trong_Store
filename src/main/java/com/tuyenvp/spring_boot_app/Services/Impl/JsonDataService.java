package com.tuyenvp.spring_boot_app.Services.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class JsonDataService {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataService.class);
    private static final String DATA_FILE = "data.json";

    private final Map<String, String> chatbotData;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonDataService(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
        this.chatbotData = new HashMap<>();
        loadData();
    }

    private void loadData() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DATA_FILE)) {
            if (inputStream == null) {
                logger.error("Không tìm thấy file: {}", DATA_FILE);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, String> data = objectMapper.readValue(inputStream, Map.class);
            chatbotData.putAll(data);
            logger.info("Đã tải {} câu hỏi từ file {}", chatbotData.size(), DATA_FILE);

        } catch (IOException e) {
            logger.error("Lỗi khi đọc file {}: {}", DATA_FILE, e.getMessage(), e);
        }
    }

    public String searchAnswer(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Xin chào! Tôi có thể giúp gì cho bạn về P Store?";
        }

        String normalizedQuestion = question.trim().toLowerCase();

        // Tìm kiếm chính xác (case-insensitive)
        for (Map.Entry<String, String> entry : chatbotData.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedQuestion)) {
                return entry.getValue();
            }
        }

        // Tìm kiếm gần đúng (partial match)
        String fuzzyAnswer = findFuzzyMatch(normalizedQuestion);
        if (fuzzyAnswer != null) {
            return fuzzyAnswer;
        }

        // Fallback sang ChatService với context từ data.json
        String context = buildContext();
        String prompt = String.format(
            "Bạn là trợ lý ảo của P Store - cửa hàng phụ kiện và quà tặng.\n\n" +
            "Thông tin về P Store:\n%s\n\n" +
            "Câu hỏi của khách hàng: %s\n\n" +
            "Hãy trả lời ngắn gọn, thân thiện và chuyên nghiệp. " +
            "Nếu không có thông tin, hãy gợi ý khách hàng liên hệ hotline hoặc ghé cửa hàng.",
            context, question
        );

        return chatService.getChatResponse(prompt);
    }

    private String findFuzzyMatch(String normalizedQuestion) {
        // Tìm key có chứa từ khóa trong câu hỏi
        for (Map.Entry<String, String> entry : chatbotData.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (normalizedQuestion.contains(key) || key.contains(normalizedQuestion)) {
                return entry.getValue();
            }
        }

        // Tìm theo từ khóa chính
        String[] keywords = normalizedQuestion.split("\\s+");
        for (String keyword : keywords) {
            if (keyword.length() < 3) continue; // Bỏ qua từ quá ngắn

            for (Map.Entry<String, String> entry : chatbotData.entrySet()) {
                if (entry.getKey().toLowerCase().contains(keyword)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private String buildContext() {
        StringBuilder context = new StringBuilder();
        chatbotData.forEach((key, value) ->
            context.append("- ").append(key).append(": ").append(value).append("\n")
        );
        return context.toString();
    }
}
