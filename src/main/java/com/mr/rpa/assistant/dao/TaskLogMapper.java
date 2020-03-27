package com.mr.rpa.assistant.dao;

import com.mr.rpa.assistant.data.model.TaskLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by feng on 2020/3/25
 */
@Mapper
public interface TaskLogMapper {

	List<TaskLog> queryTaskLogList(@Param("maxRow") Integer maxRow,
								   @Param("taskLogId") String taskLogId,
								   @Param("taskId") String taskId,
								   @Param("status") Integer status);

	void insertNewTaskLog(TaskLog taskLog);

	void deleteTaskLog(@Param("taskLogId") String taskLogId, @Param("taskId") String taskId);

	void updateTaskLog(TaskLog taskLog);

}
