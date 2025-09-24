package service;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import model.ChatMessage;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import util.MongoUtil;
import util.RedisUtil;

public class ChatService {
	
	private final MongoCollection<Document> messageCollection;
    private final MongoCollection<Document> userCollection;
    private final JedisPooled jedisPooled;
    private RedisMessageService redisMessageService;

    public ChatService() {
    	 MongoDatabase db = MongoUtil.getDatabase();
         messageCollection = db.getCollection("messages");
         userCollection = db.getCollection("user");
         jedisPooled = RedisUtil.getClient();
         redisMessageService=new RedisMessageService(jedisPooled);
    }

    public void saveMessage(ChatMessage msg) {
        messageCollection.insertOne(msg.toDocument());
        redisMessageService.pushRecentMessage(msg.getContent()); // catch lưu tin nhắn mới nhất
    }

 
}
 