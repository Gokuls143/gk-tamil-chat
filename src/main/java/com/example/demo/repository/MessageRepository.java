package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
	// fetch recent messages for chat history
	List<Message> findTop50ByOrderByTimestampDesc();
	
	// delete messages by sender (for admin clear chat function)
	@Modifying
	@Transactional
	@Query("DELETE FROM Message m WHERE m.sender = :sender")
	void deleteBySender(@Param("sender") String sender);
}

