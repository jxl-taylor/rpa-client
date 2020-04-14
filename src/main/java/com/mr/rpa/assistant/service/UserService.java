package com.mr.rpa.assistant.service;

import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.ui.settings.MyInfoController;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by feng on 2020/4/14 0014
 */
public interface UserService {
	ObservableList<MyInfoController.User> getUIUserList();

	ObservableList<MyInfoController.User> getUIUserListByUsername(String username);

	ObservableList<MyInfoController.User> getUIUserListByNick(String nick);

	List<User> getUserListByUsername(String username);

	List<User> getUserListByNick(String nick);

	void deleteUser(String userId);

	void updateUser(User user);

	void addUser(User user);
}
