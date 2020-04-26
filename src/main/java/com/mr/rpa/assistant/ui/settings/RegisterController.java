package com.mr.rpa.assistant.ui.settings;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
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
	private JFXTextField serialNo;

	@FXML
	private StackPane rootPane;
	@FXML
	private VBox vFormBox;

	@FXML
	private ScrollPane mainContainer;

	private ToggleGroup toggleGroup;
	private SysConfigService configService = ServiceFactory.getService(SysConfigService.class);
	private SysConfig sysConfig;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		sysConfig = GlobalProperty.getInstance().getSysConfig();
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
		try {
			CommonUtil.requestControlCenter(sysConfig.getControlServer(),
					SystemContants.API_SERVICE_ID_REGISTER_QUERY,
					JSON.toJSONString(new HashMap<String, String>()),
					resultJson -> {
						userType.getChildren().forEach(item -> {
							if (item instanceof JFXRadioButton) {
								if(((JFXRadioButton) item).getUserData().equals(resultJson.getString("userType")))
									((JFXRadioButton) item).setSelected(true);
							}
						});
						companyName.setText(resultJson.getString("companyName"));
						companyAddress.setText(resultJson.getString("companyAddress"));
						applicant.setText(resultJson.getString("applicant"));
						applyPhone1.setText(resultJson.getString("applyPhone1"));
						applyPhone2.setText(resultJson.getString("applyPhone2"));
						applyMail.setText(resultJson.getString("applyMail"));
						return "";
					});
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	@FXML
	private void queryBySerialNo(ActionEvent event) {
		if(StringUtils.isBlank(serialNo.getText())) {
			AlertMaker.showErrorMessage("用户查询", "序列号不能为空");
			return;
		}
		String controlUrl = sysConfig.getControlServer();
		Map<String, String> jsonMap = Maps.newHashMap();
		jsonMap.put("serialNo", serialNo.getText());
		try {
			CommonUtil.requestControlCenter(controlUrl,
					SystemContants.API_SERVICE_ID_REGISTER_QUERY,
					JSON.toJSONString(jsonMap),
					resultJson -> {
						userType.getChildren().forEach(item -> {
							if (item instanceof JFXRadioButton) {
								if(((JFXRadioButton) item).getUserData().equals(resultJson.getString("userType")))
									((JFXRadioButton) item).setSelected(true);
							}
						});
						companyName.setText(resultJson.getString("companyName"));
						companyAddress.setText(resultJson.getString("companyAddress"));
						applicant.setText(resultJson.getString("applicant"));
						applyPhone1.setText(resultJson.getString("applyPhone1"));
						applyPhone2.setText(resultJson.getString("companyName"));
						applyMail.setText(resultJson.getString("applyMail"));
						return "";
					});
		} catch (Throwable e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
		}
	}

	@FXML
	private void apply(ActionEvent event) {
		applyAndSave();
	}

	private void applyAndSave() {
		if (toggleGroup.getSelectedToggle().getUserData().equals(SystemContants.USER_TYPE_IND)) {
			sysConfig.setUserType(SystemContants.USER_TYPE_IND);
		} else {
			sysConfig.setUserType(SystemContants.USER_TYPE_ENT);
		}
		sysConfig.setCompanyName(companyName.getText());
		sysConfig.setCompanyAddress(companyAddress.getText());
		sysConfig.setApplicant(applicant.getText());
		sysConfig.setApplyPhone1(applyPhone1.getText());
		sysConfig.setApplyPhone2(applyPhone2.getText());
		sysConfig.setApplyMail(applyMail.getText());
		String checkResult = checkInput();
		if (StringUtils.isNotBlank(checkResult))
			AlertMaker.showErrorMessage("注册", checkResult);
		//调用控制中心API申请接口，成功返回后保存，失败后还原
		String controlUrl = sysConfig.getControlServer();
		try {
			if (StringUtils.isBlank(controlUrl)) throw new RuntimeException("控制中心地址未设置");
			Map<String, String> jsonMap = Maps.newHashMap();
			jsonMap.put("userType", sysConfig.getUserType());
			jsonMap.put("companyName", sysConfig.getCompanyName());
			jsonMap.put("companyAddress", sysConfig.getCompanyAddress());
			jsonMap.put("applicant", sysConfig.getApplicant());
			jsonMap.put("applyPhone1", sysConfig.getApplyPhone1());
			jsonMap.put("applyPhone2", sysConfig.getApplyPhone2());
			jsonMap.put("applyMail", sysConfig.getApplyMail());
			CommonUtil.requestControlCenter(controlUrl,
					SystemContants.API_SERVICE_ID_REGISTER,
					JSON.toJSONString(jsonMap),
					resultJson -> {
						configService.update();
						AlertMaker.showSimpleAlert("注册", "提交成功");
						closeWindow();
						return "";
					});
			return;
		} catch (Throwable e) {
			AlertMaker.showErrorMessage("注册", String.format("注册失败，原因：%s", e.getMessage()));
		}
		sysConfig = configService.query();
	}

	private String checkInput() {
		if (sysConfig.getUserType().equals(SystemContants.USER_TYPE_ENT)) {
			if (StringUtils.isBlank(sysConfig.getCompanyName())) {
				return "公司名称不能为空";
			}
			if (StringUtils.isBlank(sysConfig.getCompanyAddress())) {
				return "公司地址不能为空";
			}
		}
		if (StringUtils.isBlank(sysConfig.getApplicant())) {
			return "申请人不能为空";
		}
		if (StringUtils.isBlank(sysConfig.getApplyPhone1()) || StringUtils.isBlank(sysConfig.getApplyPhone2())) {
			return "联系电话不能为空";
		}
		if (StringUtils.isBlank(sysConfig.getApplyMail())) {
			return "邮件地址不能为空";
		}
		return "";
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
