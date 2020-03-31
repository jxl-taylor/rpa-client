package com.mr.rpa.assistant.dao;

import com.mr.rpa.assistant.data.model.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by feng on 2020/3/25
 */
@Mapper
public interface SysConfigMapper {

	List<Map<String, Object>> queryList();

	void insert(SysConfig sysConfig);

	void delete();
}
