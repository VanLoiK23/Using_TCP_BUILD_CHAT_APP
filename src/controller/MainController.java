package controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.mysql.cj.x.protobuf.MysqlxSession.Reset;

import controller.ServerAndClientSocket.SocketClient;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.ChatMessage;
import model.Packet;
import model.User;
import service.RedisUserService;
import service.UserService;
import util.RedisUtil;

public class MainController implements Initializable {

	private UserService userService = new UserService();
	private RedisUserService redisUserService = new RedisUserService(RedisUtil.getClient());
	private User user;
	private String activeSelect;
	private SocketClient socketClient;

	private void setActiveSelect(String activeSelect) {
		this.activeSelect = activeSelect;
	}

	private String lastSenderId = null;

	@FXML
	private Circle avatarUser;

	@FXML
	private StackPane chat_all;

	@FXML
	private StackPane chat_group;

	@FXML
	private StackPane chat_message;

	@FXML
	private StackPane closeSearch;

	@FXML
	private StackPane clear;

	@FXML
	private AnchorPane container_chat;

	@FXML
	private Text lastMessage;

	@FXML
	private TextField messageText;

	@FXML
	private ScrollPane scrollMessage;

	@FXML
	private TextField searchTextFiled;

	@FXML
	private Text statusActive;

	@FXML
	private Text textUserOrGroup;

	@FXML
	private Text userName;

	@FXML
	private VBox vboxInScroll;

	public void setUser(User user) {
		this.user = user;

		userName.setText(user.getUsername());

		Image image = new Image(user.getAvatarUrl(), true);

		image.progressProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.doubleValue() == 1.0 && !image.isError()) {
				avatarUser.setFill(new ImagePattern(image));
			}
		});
	}

	@FXML
	void clearTextSearch(MouseEvent event) {
		searchTextFiled.setText(null);
	}

	@FXML
	void closeSearch(MouseEvent event) {

	}

	@FXML
	void clickChat(MouseEvent event) {
		setActiveSelect("message");
		assignActiveSelect(getActiveSelect());

	}

	@FXML
	void clickChatAll(MouseEvent event) {
		setActiveSelect("all");
		assignActiveSelect(getActiveSelect());
	}

	@FXML
	void clickChatGroup(MouseEvent event) {
		setActiveSelect("group");
		assignActiveSelect(getActiveSelect());

	}

	private void onSend(String messenger, String receiverId) throws IOException {
		if (messageText.getText() != null && !messageText.getText().isEmpty()) {

			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setSenderId(user.getIdHex());
			chatMessage.setReceiverId(receiverId);
			chatMessage.setContent(messenger);
			chatMessage.setType("text");
			chatMessage.setRead(false);
			chatMessage.setTimestamp(LocalDateTime.now());
//
//			out.println(gson.toJson(chatMessage));
//			out.flush();

			Packet packetRequest = new Packet();
			packetRequest.setType("MESSAGE");
			packetRequest.setData(chatMessage);

			socketClient.sendPacket(packetRequest);

			
			onSendAndReceiveMessenge(chatMessage, true);

			messageText.clear();
		}
	}

	class styleDifferenceClass {
		private Pos position;
		private String styleCss;

		styleDifferenceClass(Pos position, String styleCss) {
			this.position = position;
			this.styleCss = styleCss;
		}
	}

	// tách ra MessageRender
	public void onSendAndReceiveMessenge(ChatMessage chatMessage, Boolean isSend) throws MalformedURLException {

		String messageContent = chatMessage.getContent();

		Map<Boolean, styleDifferenceClass> mapStyleMessenger = new HashMap<>();
		mapStyleMessenger.put(true, new styleDifferenceClass(Pos.CENTER_RIGHT, "-fx-text-fill: rgb(239,242,255);"
				+ "-fx-background-color: rgb(15,125,242);" + "-fx-background-radius: 20px;"));
		mapStyleMessenger.put(false, new styleDifferenceClass(Pos.CENTER_LEFT,
				"-fx-text-fill: black;" + "-fx-background-color: rgb(233,233,235);" + "-fx-background-radius: 20px;"));

		// fetch from cache tang toc do
		User userSender = new User();
		userSender.setUsername(redisUserService.getCachedUsername(chatMessage.getSenderId()));
		userSender.setAvatarUrl(redisUserService.getCachedAvatar(chatMessage.getSenderId()));

		// Avatar hình tròn

		Image image = new Image(userSender.getAvatarUrl(), true);

		ImageView avatar = new ImageView();
		avatar.setFitWidth(30);
		avatar.setFitHeight(30);
		avatar.setClip(new Circle(15, 15, 15));

		image.progressProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.doubleValue() == 1.0 && !image.isError()) {
				avatar.setImage(image);
			}
		});

		// Bong bóng tin nhắn
		Text text = new Text(messageContent);

		// dinh dang kieu tin nhan
		if (isSend) {
			text.setFill(Color.color(0.934, 0.945, 0.996));
		}
		TextFlow textFlow = new TextFlow(text);
		textFlow.setStyle(mapStyleMessenger.get(isSend).styleCss);
		textFlow.setPadding(new Insets(5, 10, 5, 10));
		textFlow.setMaxWidth(300); 

		// HBox chứa avatar + tin nhắn
		HBox hBox = new HBox(10);
		hBox.setAlignment(mapStyleMessenger.get(isSend).position);
		hBox.setPadding(new Insets(5, 5, 5, 10));
		hBox.setStyle("-fx-cursor: text;");

		if (isSend) {
			hBox.getChildren().addAll(textFlow);
		} else {
			hBox.getChildren().addAll(avatar, textFlow); // avatar bên trái
		}

		// VBox chứa tên + HBox
		VBox messageBox = new VBox(2);

		if (isSend) {
			messageBox.getChildren().addAll(hBox);

//			messageBox.setOnMouseClicked(event -> {
//			    messageBox.setStyle("-fx-translate-y:10px;-fx-text-fill:orange");
//			    messageBox.
//			});
			vboxInScroll.getChildren().add(messageBox);
		} else {
			// Tên người gửi
			// nếu trùng người gửi thì ko hiển thị tên và avatar
			System.out.println("lastSender: "+lastSenderId);
			System.out.println("SenderID message: "+chatMessage.getSenderId());
			if (!chatMessage.getSenderId().equals(lastSenderId)) {
				Label nameLabel = new Label(userSender.getUsername());
				nameLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 3 5;");
				messageBox.getChildren().addAll(nameLabel, hBox);
			} else {
				hBox.setStyle("-fx-translate-x:40px");
				hBox.setSpacing(1.0);
				hBox.getChildren().remove(avatar); // ẩn avatar
				messageBox.getChildren().add(hBox); // không có tên
			}
			

//			Label nameLabel = new Label(userSender.getUsername());
//			nameLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 3 5;");
//
//			messageBox.getChildren().addAll(nameLabel, hBox);

			Platform.runLater(() -> vboxInScroll.getChildren().add(messageBox));
		}
		
		lastSenderId = chatMessage.getSenderId();
	}

	private void reset() {
		lastSenderId = null;
		vboxInScroll.getChildren().clear();
	}
	
	public void actionForMessageBox() {

	}

	@FXML
	void selecteUserOrGroup(MouseEvent event) {

	}

	@FXML
	void infoClick(MouseEvent event) {

	}

	@FXML
	void openSearch(MouseEvent event) {

	}

	@FXML
	void searchSubmit(KeyEvent event) {

	}

	@FXML
	void sendMessage(KeyEvent event) throws IOException {
		if (event.getCode().toString().equals("ENTER")) {
			onSend(messageText.getText(), "all");
		}
	}

	private void assignActiveSelect(String select) {
		if (select.equals("message")) {
			chat_message.getStyleClass().add("active");
			chat_all.getStyleClass().remove("active");
			chat_group.getStyleClass().remove("active");
		} else if (select.equals("all")) {
			chat_message.getStyleClass().remove("active");
			chat_all.getStyleClass().add("active");
			chat_group.getStyleClass().remove("active");
		} else {
			chat_message.getStyleClass().remove("active");
			chat_all.getStyleClass().remove("active");
			chat_group.getStyleClass().add("active");
		}
	}

	private String getActiveSelect() {
		return activeSelect;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// assign ban đầu ở broadcast
		textUserOrGroup.setText("Nhóm cộng đồng");
		setActiveSelect("all");

		assignActiveSelect(getActiveSelect());
		avatarUser.setVisible(false);

		clear.setVisible(false);

		searchTextFiled.textProperty().addListener((obs, oldText, newText) -> {
			if (newText == null || newText.isEmpty()) {
				clear.setVisible(false);
			} else {
				clear.setVisible(true);
			}
		});

		//always scroll to end
		vboxInScroll.heightProperty().addListener((obs, oldVal, newVal) -> {
		    scrollMessage.setVvalue(1.0); 
		});
		scrollMessage.setFitToWidth(true);
		
		socketClient = SocketClient.getInstance();
		socketClient.setMessageHandler(chatMessage -> {
			try {
				onSendAndReceiveMessenge(chatMessage, false);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("Can't receive message");
			}
		});

	}

}
