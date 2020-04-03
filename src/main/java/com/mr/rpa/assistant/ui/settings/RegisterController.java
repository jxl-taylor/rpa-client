package com.mr.rpa.assistant.ui.settings;

import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

@Log4j
public class RegisterController implements Initializable {

	private String mac;

	@FXML
	private HBox userType;
	@FXML
	private JFXTextField companyName;
	@FXML
	private JFXTextField companyAddress;
	@FXML
	private JFXTextField applicant;
	@FXML
	private JFXTextField applyPhone1;
	@FXML
	private JFXTextField applyPhone2;
	@FXML
	private JFXTextField applyMail;

	@FXML
	private StackPane rootPane;
	@FXML
	private VBox vFormBox;
	@FXML
	private HBox actionBox;

	@FXML
	private ScrollPane mainContainer;

	private ToggleGroup toggleGroup;
	private SysConfigService configService = ServiceFactory.getService(SysConfigService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		toggleGroup = new ToggleGroup();
		toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
			if (toggleGroup.getSelectedToggle() != null) {
				if (newToggle.getUserData().equals(SystemContants.USER_TYPE_IND)) {
					if (vFormBox.getChildren().contains(companyName)) {
						vFormBox.getChildren().remove(companyName);
						vFormBox.getChildren().remove(companyAddress);
					}
				} else {
					if (!vFormBox.getChildren().contains(companyName)) {
						vFormBox.getChildren().add(1, companyName);
						vFormBox.getChildren().add(2, companyAddress);
					}
				}
			}
		});
		userType.getChildren().forEach(item -> {
			if (item instanceof JFXRadioButton) {
				((JFXRadioButton) item).setToggleGroup(toggleGroup);
			}
		});

		//如果已经注册的话不能再注册
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		if(StringUtils.isNotBlank(sysConfig.getUserType())){
			vFormBox.getChildren().remove(userType);
			vFormBox.getChildren().remove(actionBox);
			companyName.setEditable(false);
			companyAddress.setEditable(false);
			applicant.setEditable(false);
			applyPhone1.setEditable(false);
			applyPhone2.setEditable(false);
			applyMail.setEditable(false);
		}
		try {
			mac = CommonUtil.getLocalMac();

		} catch (Exception e) {
			log.error(e);
		}
	}

	@FXML
	private void apply(ActionEvent event) {
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		if (toggleGroup.getSelectedToggle().getUserData().equals(SystemContants.USER_TYPE_IND)){
			sysConfig.setUserType(SystemContants.USER_TYPE_IND);
		}else{
			sysConfig.setUserType(SystemContants.USER_TYPE_ENT);
		}
		sysConfig.setCompanyName(companyName.getText());
		sysConfig.setCompanyAddress(companyAddress.getText());
		sysConfig.setApplicant(applicant.getText());
		sysConfig.setApplyPhone1(applyPhone1.getText());
		sysConfig.setApplyPhone2(applyPhone2.getText());
		sysConfig.setApplyMail(applyMail.getText());
		//调用控制中心API申请接口，成功返回后保存，失败后还原
		configService.update();
		closeWindow();
	}

	@FXML
	private void cancel(ActionEvent event) {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) rootPane.getScene().getWindow();
		stage.close();
	}
}
