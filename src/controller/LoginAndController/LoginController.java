package controller.LoginAndController;

import java.io.IOException;

import controller.ClientController;
import controller.MainController;
import controller.Common.CommonController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.User;
import service.UserService;

public class LoginController {
	@FXML
	private ImageView img;

	@FXML
	public void exit(MouseEvent event) {
		Stage stage = (Stage) img.getScene().getWindow();
		stage.close();
	}

	@FXML
	private PasswordField password;

	@FXML
	private TextField username;

	private CommonController commonController;

	private UserService userService = new UserService();
	
	@FXML
	void submit(MouseEvent event) throws IOException {
		if (username.getText() != null && password.getText() != null && !username.getText().isEmpty()
				&& !password.getText().isEmpty()) {


			Boolean isAuthentication = userService.authenticationLogin(username.getText(), password.getText());
			commonController = new CommonController();

			if (isAuthentication) {
				User user = userService.getUserByUserName(username.getText());
				
				System.out.println(user);

				commonController.alertInfo(AlertType.CONFIRMATION, "Success!!!!", "Bạn đã đăng nhập thành công");
				
				userService.setUpLogin(user);

				if (user.getRole().equals("admin")) {
					MainController mainController = commonController.loaderToResource(event, "Chat/form_Chat")
							.getController();
					mainController.setUser(user);
				} else {
					MainController mainController = commonController.loaderToResource(event, "Chat/form_Chat")
							.getController();
					mainController.setUser(user);
				}
			} else {
				commonController.alertInfo(AlertType.WARNING, "Cảnh báo!!!!", "Không tìm thấy thông tin tài khoản!");

			}
			clean();


		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Cảnh báo!");
			alert.setHeaderText(null);
			alert.setContentText("Vui lòng nhập đầy đủ thông tin!");
			alert.showAndWait();
			clean();
		}
	}

	void clean() {
		username.setText(null);
		password.setText(null);
	}

}
