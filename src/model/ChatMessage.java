package model;

import java.time.LocalDateTime;

import org.bson.Document;

import lombok.Data;

@Data
public class ChatMessage {

	private String id;
	private String senderId;
	private String receiverId; // hoặc roomId nếu chat nhóm
	private String content;
	private String type; // "text", "image", "file", "sticker", "call"
	private LocalDateTime timestamp;
	private boolean isRead;

	public Document toDocument() {
		return new Document("sender_id", senderId).append("receiver_id", receiverId).append("content", content)
				.append("type", type).append("timestamp", timestamp.toString()).append("is_read", isRead);
	}

}