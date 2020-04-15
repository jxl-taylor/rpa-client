package com.mr.rpa.assistant.ui.settings;

import com.google.common.collect.Lists;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.service.UserService;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.Pair;
import de.schlichtherle.license.LicenseContent;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Log4j
public class MyInfoController implements Initializable {

	@FXML
	private JFXTextField userNick;
	@FXML
	private JFXTextField expireTime;
	@FXML
	private JFXTextField username;
	@FXML
	private JFXTextField duration;

	@FXML
	private JFXTextField usernameQuery;
	@FXML
	private JFXTextField nickQuery;
	@FXML
	private TableView<User> tableView;
	@FXML
	private TableColumn<User, Integer> seqCol;
	@FXML
	private TableColumn<User, String> usernameCol;
	@FXML
	private TableColumn<User, String> nickCol;
	@FXML
	private TableColumn<User, String> mailCol;
	@FXML
	private TableColumn<User, String> phoneCol;
	@FXML
	private TableColumn<User, String> createdTimeCol;
	@FXML
	private TableColumn<User, String> updatedTimeCol;
	@FXML
	private TableColumn<User, HBox> operatingCol;

	private static JFXButton cancelBtn;

	public static UserService userService = ServiceFactory.getService(UserService.class);
	private GlobalProperty globalProperty = GlobalProperty.getInstance();
	private static ObservableList<User> userList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		refreshExpireTime();
		duration.textProperty().bind(globalProperty.getRunningDuration());
		globalProperty.setMyInfoController(this);
		initCol();
		tableView.setItems(userList);
		tableView.setRowFactory(tv -> {
			TableRow<User> row = new TableRow<User>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty()) {
					if (event.getClickCount() == 2) {
						showEditOption(row.getItem());
					}
				}
			});
			return row;
		});
		queryUsers();
		cancelBtn = new JFXButton("取消");
		cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			StackPane rootPane = globalProperty.getRootPane();
			rootPane.getChildren().get(0).setEffect(null);
		});
	}


	private static void showEditOption(User selectedForEdit) {
		if (selectedForEdit == null) {
			AlertMaker.showErrorMessage("未选择用户", "请选择一个用户编辑.");
			return;
		}
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(MyInfoController.class.getClassLoader()
				.getResource("assistant/ui/myinfo/add_user.fxml"), "编辑用户", null);

		UserAddController controller = (UserAddController) pair.getObject2();
		controller.inflateUI(selectedForEdit.getUsername());
		Stage stage = pair.getObject1();
		stage.setOnHiding((e) -> {
			queryUsers();
		});

		stage.setOnCloseRequest((event) -> {
			AssistantUtil.closeWinow(MyInfoController.class.getClassLoader().getResource("assistant/ui/myinfo/add_user.fxml"));
		});

	}


	public void refreshExpireTime() {
		LicenseContent licenseContent = globalProperty.getLicenseContent();
		if (licenseContent == null) return;
		expireTime.setText(DateFormatUtils.format(licenseContent.getNotAfter(), "yyyy-MM-dd"));
	}

	public void initCurrentUser() {
		com.mr.rpa.assistant.data.model.User currentUser = globalProperty.getCurrentUser();
		userNick.setText(currentUser.getNick());
		username.setText(currentUser.getUsername());
	}

	private void initCol() {
		seqCol.setCellValueFactory(new PropertyValueFactory<>("seq"));
		usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
		nickCol.setCellValueFactory(new PropertyValueFactory<>("nick"));
		mailCol.setCellValueFactory(new PropertyValueFactory<>("mail"));
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		createdTimeCol.setCellValueFactory(new PropertyValueFactory<>("createdTime"));
		updatedTimeCol.setCellValueFactory(new PropertyValueFactory<>("updatedTime"));
		operatingCol.setCellValueFactory(new PropertyValueFactory<>("operatingBox"));
	}

	public static ObservableList<User> queryUsers() {
		return queryUsers(null, null);
	}

	public static ObservableList<User> queryUsers(String username, String nick) {
		userList.clear();
		if (StringUtils.isNotEmpty(username)) {
			userList.addAll(userService.getUIUserListByUsername(username));
		} else if (StringUtils.isNotEmpty(nick)) {
			userList.addAll(userService.getUIUserListByNick(nick));
		} else {
			userList.addAll(userService.getUIUserList());
		}
		return userList;
	}

	@FXML
	private void loadUsers(ActionEvent event) {
		queryUsers(usernameQuery.getText(), nickQuery.getText());
	}

	@FXML
	private void clearSearch(ActionEvent event) {
		usernameQuery.clear();
		nickQuery.clear();
		queryUsers();
	}

	@FXML
	private void loadAddUser(ActionEvent event) {
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader()
				.getResource("assistant/ui/myinfo/add_user.fxml"), "添加任务", null);
		pair.getObject1().setOnCloseRequest((e) -> {
			AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/myinfo/add_user.fxml"));
		});
	}

	public static class User {
		private final SimpleIntegerProperty seq;
		private final SimpleStringProperty username;
		private final SimpleStringProperty nick;
		private final SimpleStringProperty mail;
		private final SimpleStringProperty phone;
		private final SimpleBooleanProperty locking;
		private final SimpleStringProperty createdTime;
		private final SimpleStringProperty updatedTime;

		private HBox operatingBox;

		private Hyperlink deleteLink = new Hyperlink("删除");
		private Hyperlink updateLink = new Hyperlink("修改");
		private Hyperlink lockLink = new Hyperlink("禁用");

		public User(int seq, String username, String nick, String mail, String phone,
					boolean locking, String createdTime, String updatedTime) {
			this.seq = new SimpleIntegerProperty(seq);
			this.username = new SimpleStringProperty(username);
			this.nick = new SimpleStringProperty(nick);
			this.mail = new SimpleStringProperty(mail);
			this.phone = new SimpleStringProperty(phone);
			this.locking = new SimpleBooleanProperty(locking);
			this.createdTime = new SimpleStringProperty(createdTime);
			this.updatedTime = new SimpleStringProperty(updatedTime);
			initOperatingBox();
		}

		private void initOperatingBox() {
			lockLink.setText(locking.get() ? "启用" : "禁用");
			lockLink.setOnAction(event -> {
				try {
					lockOrUnlockUser(getUsername(), !this.locking.get());
					queryUsers();
				} catch (Exception e) {
					log.error(e);
					AlertMaker.showErrorMessage("启用禁用", e.getMessage());
				}
			});

			deleteLink.setOnAction(event -> {
				try {
					deleteUser(getUsername());
				} catch (Exception e) {
					log.error(e);
					AlertMaker.showErrorMessage("删除用户", e.getMessage());
				}
			});
			updateLink.setOnAction(event -> {
				try {
					//Fetch the selected row
					updateUser(getUsername());
				} catch (Throwable e) {
					log.error(e);
					AlertMaker.showErrorMessage("修改用户", e.getMessage());
				}
			});
			operatingBox = new HBox();
			operatingBox.getChildren().add(updateLink);
			operatingBox.getChildren().add(lockLink);
			operatingBox.getChildren().add(deleteLink);
			operatingBox.setSpacing(20);
			operatingBox.setAlignment(Pos.CENTER);
		}

		private void lockOrUnlockUser(String username, boolean status) {
			com.mr.rpa.assistant.data.model.User user = userService.getUserListByUsername(username).get(0);
			user.setLocking(status);
			JFXButton confirmBtn = new JFXButton("确定");
			confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				userService.updateUser(user);
				AlertMaker.showSimpleAlert(status ? "启用" : "禁用", "执行成功");
				queryUsers();
			});
			GlobalProperty globalProperty = GlobalProperty.getInstance();
			AlertMaker.showMaterialDialog(globalProperty.getRootPane(),
					globalProperty.getRootPane().getChildren().get(0),
					Lists.newArrayList(confirmBtn, cancelBtn), status ? "启用" : "禁用",
					"确定执行么?", false);
		}

		private void updateUser(String username) {
			User user = userService.getUIUserListByUsername(username).get(0);
			//Fetch the selected row
			showEditOption(user);
		}

		private void deleteUser(String username) {
			com.mr.rpa.assistant.data.model.User user = userService.getUserListByUsername(username).get(0);
			JFXButton confirmBtn = new JFXButton("确定");
			confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				userService.deleteUser(user.getId());
				//todo 用户下的任务归属

				AlertMaker.showSimpleAlert("删除用户", "删除成功");
				queryUsers();
			});
			GlobalProperty globalProperty = GlobalProperty.getInstance();
			AlertMaker.showMaterialDialog(globalProperty.getRootPane(),
					globalProperty.getRootPane().getChildren().get(0),
					Lists.newArrayList(confirmBtn, cancelBtn), "删除用户",
					"确定删除用户：" + username + " ?", false);
		}

		public int getSeq() {
			return seq.get();
		}

		public SimpleIntegerProperty seqProperty() {
			return seq;
		}

		public String getUsername() {
			return username.get();
		}

		public SimpleStringProperty usernameProperty() {
			return username;
		}

		public String getNick() {
			return nick.get();
		}

		public SimpleStringProperty nickProperty() {
			return nick;
		}

		public String getMail() {
			return mail.get();
		}

		public SimpleStringProperty mailProperty() {
			return mail;
		}

		public String getPhone() {
			return phone.get();
		}

		public SimpleStringProperty phoneProperty() {
			return phone;
		}

		public String getCreatedTime() {
			return createdTime.get();
		}

		public SimpleStringProperty createdTimeProperty() {
			return createdTime;
		}

		public String getUpdatedTime() {
			return updatedTime.get();
		}

		public SimpleStringProperty updatedTimeProperty() {
			return updatedTime;
		}

		public HBox getOperatingBox() {
			return operatingBox;
		}
	}

}
