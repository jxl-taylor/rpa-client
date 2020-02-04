package com.mr.rpa.assistant.ui.main.toolbar;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;

public class ToolbarController implements Initializable {

    @FXML
    private JFXButton loginButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void loadAddMember(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/addmember/member_add.fxml"), "Add New Member", null);
    }

    @FXML
    private void loadAddTask(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"), "添加任务", null);
    }

    @FXML
    private void loadMemberTable(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/listmember/member_list.fxml"), "Member List", null);
    }

    @FXML
    private void loadBookTable(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/listbook/book_list.fxml"), "Book List", null);
    }

    @FXML
    private void loadSettings(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/settings/settings.fxml"), "Settings", null);
    }

    @FXML
    private void loadIssuedBookList(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/login/login.fxml"), "登录", null);

    }

}
