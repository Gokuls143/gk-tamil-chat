package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ChatIntegrationTests {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void repositorySavesMessage() {
        Message m = new Message("alice", "hello");
        messageRepository.save(m);
        List<Message> all = messageRepository.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all.get(0).getContent()).isEqualTo("hello");
    }

    // NOTE: Full websocket integration test would require a stomp client + session cookie handling; omitted for brevity
}
