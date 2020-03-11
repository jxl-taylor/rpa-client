package com.mr.rpa.assistant.util;

/**
 * Created by feng on 2020/2/23 0023
 */
public class SystemContants {

	public static final int TASK_RUNNING_STATUS_RUN = 1;

	public static final int TASK_RUNNING_STATUS_PAUSE = 0;

	public static final int TASK_LOG_STATUS_RUNNING = 0;
	public static final int TASK_LOG_STATUS_SUCCESS= 1;
	public static final int TASK_LOG_STATUS_FAIL = 2;
	public static final int TASK_LOG_STATUS_UNKNOWN = -1;

	public static final String TASK_LOG_KEY = "taskLogKey";

	public static final String LICENSE_PROPERTY_FILE_PATH = "/verifyParam.properties";

	public static final String UPDATE_CHECK_LIST = "checklist.properties";
	public static final String UPDATE_CHECK_LIST_TYPE_SQL = "sql";
	public static final String UPDATE_CHECK_LIST_TYPE_FILE = "file";
	public static final String UPDATE_CHECK_LIST_TYPE_DIR = "dir";

	/**
	 * 冒号转义字符
	 */
	public static final String COLON = "${COLON}";
	/**
	 * 冒号转义字符
	 */
	public static final String SEPARATER = "${SEPARATER}";

}
