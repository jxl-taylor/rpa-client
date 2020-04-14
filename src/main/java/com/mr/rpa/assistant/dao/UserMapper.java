package com.mr.rpa.assistant.dao;

import com.mr.rpa.assistant.data.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by feng on 2020/3/25
 */
@Mapper
public interface UserMapper {

	void deleteUser(@Param("userId") String userId);

	void updateUser(User user);

	void addUser(User user);

	List<User> queryUserList(@Param("username") String username,
							 @Param("nick") String nick);

}
