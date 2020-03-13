package com.mr.rpa.assistant.ui.addtask;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Log4j
public class CronSettingController implements Initializable {

	private static final String CRON_START = "*";
	private static final String CRON_ASK = "?";
	private static final String CRON_COMMA = ",";
	private static final String CRON_SEPARATOR = "/";

	@FXML
	private AnchorPane mainContainer;

	@FXML
	private JFXTabPane cronTabPane;

	@FXML
	private VBox secondVBox;
	@FXML
	private VBox miniteVBox;
	@FXML
	private VBox hourVBox;
	@FXML
	private VBox dayVBox;
	@FXML
	private VBox monthVBox;
	@FXML
	private VBox weekVBox;

	@FXML
	private JFXTextField cronResult;

	private Map<String, HBox> secondHboxMap = Maps.newHashMap();
	private Map<String, HBox> miniteHboxMap = Maps.newHashMap();
	private Map<String, HBox> hourHboxMap = Maps.newHashMap();
	private Map<String, HBox> dayHboxMap = Maps.newHashMap();
	private Map<String, HBox> monthHboxMap = Maps.newHashMap();
	private Map<String, HBox> weekHboxMap = Maps.newHashMap();

	private List<Map<String, HBox>> hBoxMapList = Lists.newArrayList();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initComponents();
		initRadioButton(secondVBox, secondHboxMap, 0, 59);
		initRadioButton(miniteVBox, miniteHboxMap, 0, 59);
		initRadioButton(hourVBox, hourHboxMap, 0, 23);
		initRadioButton(dayVBox, dayHboxMap, 1, 31);
		initRadioButton(monthVBox, monthHboxMap, 1, 12);
		initRadioButton(weekVBox, weekHboxMap, 1, 7);
	}

	private void initRadioButton(VBox vBox, Map<String, HBox> hboxMap, int min, int max) {
		hBoxMapList.add(hboxMap);
		final ToggleGroup secondGroup = new ToggleGroup();
		for (int i = 0; i < vBox.getChildren().size(); i++) {
			HBox node = (HBox) vBox.getChildren().get(i);
			//指定的checkbox
			if (!(node.getChildren().get(0) instanceof JFXRadioButton)) {
				initCheckBox((JFXMasonryPane) node.getChildren().get(0), min, max);
				hboxMap.put(SystemContants.CRON_TYEP_SPECIFIED_ITEM, node);
				return;
			}

			JFXRadioButton secondRadio = (JFXRadioButton) node.getChildren().get(0);
			secondRadio.setToggleGroup(secondGroup);
			String radioKey = String.valueOf(secondRadio.getUserData());
			//range 处理
			if (radioKey.equals(SystemContants.CRON_TYEP_RANGE)) {
				JFXComboBox<String> monthBeginComboBox = (JFXComboBox<String>) node.getChildren().get(2);
				JFXComboBox<String> monthRateComboBox = (JFXComboBox<String>) node.getChildren().get(4);
				monthBeginComboBox.getItems().add("");
				monthRateComboBox.getItems().add("");
				for (int j = min; j <= max; j++) {
					monthBeginComboBox.getItems().add(String.valueOf(j));
					if (j != 0) monthRateComboBox.getItems().add(String.valueOf(j));
				}
			}
			//默认复制第一个，匹配 *
			if (i == 0) secondRadio.setSelected(true);
			hboxMap.put(radioKey, node);
		}
		secondGroup.selectedToggleProperty().addListener(
				(ObservableValue<? extends Toggle> ov, Toggle old_toggle,
				 Toggle new_toggle) -> {
					if (secondGroup.getSelectedToggle() != null) {
						String selected = secondGroup.getSelectedToggle().getUserData().toString();
						log.info(selected);
					}
				});
	}

	private void initCheckBox(JFXMasonryPane masonryPane, int min, int max) {
		for (int i = min; i <= max; i++) {
			JFXCheckBox checkBox = new JFXCheckBox(String.valueOf(i));
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> ov,
									Boolean old_val, Boolean new_val) {
					log.info(checkBox.getText());
				}
			});
			masonryPane.getChildren().add(checkBox);
		}

	}

	private void setCronValue() {

	}

	/**
	 * 通过cron表达式反向设置UI
	 * 0: second
	 * 1: minite
	 * 2: hour
	 * 3: day
	 * 4: month
	 * 5: year
	 */
	public void setCronUI(String cron) {
		if (StringUtils.isBlank(cron)) return;
		String[] cronArray = cron.trim().split("\\s+");
		if (cronArray.length != 6) return;
		for (int i = 0; i < cronArray.length; i++) {
			if (cronArray[i].equals(CRON_START)) {
				((JFXRadioButton) hBoxMapList.get(i).get(SystemContants.CRON_TYEP_EVERY).getChildren().get(0)).setSelected(true);
			} else if (cronArray[i].equals(CRON_ASK)) {
				((JFXRadioButton) hBoxMapList.get(i).get(SystemContants.CRON_TYEP_NO_SPECIFIED).getChildren().get(0)).setSelected(true);
			} else if (cronArray[i].contains(CRON_SEPARATOR)) {
				String[] ranges = cronArray[i].split(CRON_SEPARATOR);
				HBox hBox = hBoxMapList.get(i).get(SystemContants.CRON_TYEP_RANGE);
				((JFXRadioButton) hBox.getChildren().get(0)).setSelected(true);
				JFXComboBox<String> monthBeginComboBox = (JFXComboBox<String>) hBox.getChildren().get(2);
				JFXComboBox<String> monthRateComboBox = (JFXComboBox<String>) hBox.getChildren().get(4);
				// check input
				monthBeginComboBox.setValue(ranges[0].equals(CRON_START) ? "" : ranges[0]);
				monthRateComboBox.setValue(ranges[1].equals(CRON_START) ? "" : ranges[1]);
			} else if (cronArray[i].contains(CRON_COMMA) || Pattern.matches("^[1-9]\\d*$", cronArray[i])) {
				String[] specifieds = cronArray[i].split(CRON_COMMA);
				HBox hBox = hBoxMapList.get(i).get(SystemContants.CRON_TYEP_SPECIFIED);
				((JFXRadioButton) hBox.getChildren().get(0)).setSelected(true);
				JFXMasonryPane masonryPane = (JFXMasonryPane) hBoxMapList.get(i).get(SystemContants.CRON_TYEP_SPECIFIED_ITEM).getChildren().get(0);
				masonryPane.getChildren().forEach(item -> {
					for (int k = 0; k < specifieds.length; k++) {
						if (((JFXCheckBox) item).getText().equals(specifieds[k]))
							((JFXCheckBox) item).setSelected(true);
					}
				});
			}
		}
	}

	private void initComponents() {
		cronTabPane.tabMinWidthProperty().bind(cronTabPane.widthProperty().divide(cronTabPane.getTabs().size()).subtract(6));
	}

}
