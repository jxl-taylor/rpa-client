package com.mr.rpa.assistant.dao;

import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import org.apache.ibatis.annotations.Mapper;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by feng on 2020/3/25
 */
@Mapper
public interface SysConfigMapper {

	SysConfig query();

	void insert(SysConfig sysConfig);

	void delete();
}
