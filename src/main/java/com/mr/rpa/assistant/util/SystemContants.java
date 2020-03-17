package com.mr.rpa.assistant.util;

/**
 * Created by feng on 2020/2/23 0023
 */
public class SystemContants {

	public static final long HEARTBEAT_INTERVAL = 5000;
	public static final String API_SUCCESS = "1";
	public static final String API_FAIL = "0";

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

	public static final String TASK_SUFFIX_KJB = "kjb";
	public static final String TASK_SUFFIX_KTR = "ktr";

	/**
	 * 冒号转义字符
	 */
	public static final String COLON = "${COLON}";
	/**
	 * 冒号转义字符
	 */
	public static final String SEPARATER = "${SEPARATER}";

	public static final String CRON_TYEP_EVERY = "every";
	public static final String CRON_TYEP_RANGE = "range";
	public static final String CRON_TYEP_NO_SPECIFIED = "no_specified";
	public static final String CRON_TYEP_SPECIFIED = "specified";
	public static final String CRON_TYEP_SPECIFIED_ITEM = "specified_item";

	public static final String RESULT_PATH_CODE = "bankcode";

	public static final String PRIVATE_KEY = "MR-BOT";
	public static final String 	CLIENT_VERSION_1_0 = "1.0";

}
