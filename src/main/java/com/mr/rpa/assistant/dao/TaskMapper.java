package com.mr.rpa.assistant.dao;

import com.mr.rpa.assistant.data.model.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by feng on 2020/3/25
 */
@Mapper
public interface TaskMapper {

	void deleteTask(@Param("taskId") String taskId);

	void updateTask(Task task);

	void updateTaskRunning(@Param("taskId") String taskId,
						   @Param("updateTime") Timestamp updateTime,
						   @Param("running") boolean running);

	void updateTaskStatus(@Param("taskId") String taskId,
						  @Param("updateTime") Timestamp updateTime,
						  @Param("status") Integer status);

	int getTotalTaskCount(@Param("running") boolean running);

	int getTotalTaskLogCount(@Param("taskId") String taskId, @Param("status") Integer status);

	boolean insertNewTask(Task task);

	List<Task> queryTaskList(@Param("taskId") String taskId,
							 @Param("taskName") String taskName,
							 @Param("nextTask") String nextTask);

}
