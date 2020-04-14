package com.mr.rpa.assistant.data.model;

import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by feng on 2020/3/16
 */
@Data
@AllArgsConstructor
public class User {
	private String id;
	private String username;
	private String password;
	private String nick;
	private String phone;
	private String mail;
	private boolean locking;
	private String createBy;
	private String updateBy;
	private Timestamp createTime;
	private Timestamp updateTime;

	public User() {

	}
}
