package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
	// fetch recent messages for chat history
	List<Message> findTop50ByOrderByTimestampDesc();
}

