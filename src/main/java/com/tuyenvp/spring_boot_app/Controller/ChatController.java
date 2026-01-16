package com.tuyenvp.spring_boot_app.Controller;

import com.tuyenvp.spring_boot_app.DTO.ChatRequest;
import com.tuyenvp.spring_boot_app.DTO.ChatResponse;
import com.tuyenvp.spring_boot_app.Services.Impl.JsonDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "https://nckh-fe-beta.vercel.app")
public class ChatController {

    private final JsonDataService jsonDataService;

    @Autowired
    public ChatController(JsonDataService jsonDataService) {
        this.jsonDataService = jsonDataService;
    }

    @PostMapping("/test")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        String answer = jsonDataService.searchAnswer(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
