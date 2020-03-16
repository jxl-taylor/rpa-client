package com.mr.rpa.assistant.job;

import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;

/**
 * Created by feng on 2020/3/16 0016
 */
@Log4j
public class HeartBeat implements Runnable{

	GlobalProperty globalProperty = GlobalProperty.getInstance();
	@Override
	public void run() {
		SysConfig sysConfig = globalProperty.getSysConfig();
		while (true){
			action();
			try {
				Thread.sleep(SystemContants.HEARTBEAT_INTERVAL);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}

	}

	public static void action(){
		log.info("*********************************");
	}
}
