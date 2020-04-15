package com.mr.rpa.assistant.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import com.google.common.collect.Lists;
import com.mr.rpa.assistant.dao.UserMapper;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.service.UserService;
import com.mr.rpa.assistant.ui.settings.MyInfoController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.ibatis.session.SqlSession;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMAT;

/**
 * Created by feng on 2020/4/13
 */
public class UserServiceImpl implements UserService {

	private UserMapper userMapper;

	public UserServiceImpl(SqlSession session) {
		userMapper = session.getMapper(UserMapper.class);
	}

	@Override
	public ObservableList<MyInfoController.User> getUIUserList() {
		return getUIUserListByUsername(null);
	}

	@Override
	public ObservableList<MyInfoController.User> getUIUserListByUsername(String username) {
		List<User> users = getUserListByUsername(username);
		return toUIList(users);
	}

	private ObservableList<MyInfoController.User> toUIList(List<User> users) {
		ObservableList<MyInfoController.User> userObservableList = FXCollections.observableArrayList();
		if (CollectionUtil.isEmpty(users)) return userObservableList;
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			userObservableList.add(new MyInfoController.User(i,
					user.getUsername(),
					user.getNick(),
					user.getMail(),
					user.getPhone(),
					user.isLocking(),
					DatePattern.NORM_DATETIME_FORMAT.format(user.getCreateTime().getTime()),
					DatePattern.NORM_DATETIME_FORMAT.format(user.getUpdateTime().getTime())));
		}
		return userObservableList;
	}

	@Override
	public ObservableList<MyInfoController.User> getUIUserListByNick(String nick) {
		List<User> users = getUserListByUsername(nick);
		return toUIList(users);
	}

	@Override
	public List<User> getUserListByUsername(String username) {
		List<User> users = userMapper.queryUserList(username, null);
		return CollectionUtil.isEmpty(users) ? Lists.newArrayList() : users;
	}

	@Override
	public List<User> getUserListByNick(String nick) {
		List<User> users = userMapper.queryUserList(null, nick);
		return CollectionUtil.isEmpty(users) ? Lists.newArrayList() : users;
	}

	@Override
	public void deleteUser(String userId) {
		userMapper.deleteUser(userId);
	}

	@Override
	public void updateUser(User user) {
		userMapper.updateUser(user);
	}

	@Override
	public void addUser(User user) {
		userMapper.addUser(user);
	}
}
