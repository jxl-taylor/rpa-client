package com.mr.rpa.assistant.util;


import com.mr.rpa.assistant.dao.TaskMapper;
import com.mr.rpa.assistant.data.model.Task;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Created by feng on 2020/3/25
 */
public class TestHello {
	public static void main(String[] args) {

		String resource = "MyBatisConfig.xml";
		Reader reader = null;
		SqlSession session;
		try {
			reader = Resources.getResourceAsReader(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder()
				.build(reader);
		session = sqlMapper.openSession();
		TaskMapper taskMapper = session.getMapper(TaskMapper.class);
		Task task = taskMapper.findByName("demo1");
		System.out.println(task.getName());
		List<Task> tasks = taskMapper.queryTaskList();
		System.out.println(tasks);
		session.close();
	}

}
