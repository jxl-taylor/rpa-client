package com.mr.rpa.assistant.dao;

import com.mr.rpa.assistant.data.model.Task;

import java.util.List;

/**
 * Created by feng on 2020/3/25
 */
public interface TaskMapper {

	Task findByName(String id);

	List<Task> queryTaskList();
}
