package com.mr.rpa.assistant.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.ui.callback.ControlCenterCallback;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.license.LicenseManagerHolder;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by feng on 2020/3/10 0010
 */

public class CommonUtil {

	private final static Logger log = LogManager.getLogger(CommonUtil.class.getName());

	public static void copyAndCoverFile(String oldPath, String newPath) throws Exception {
		int bytesum = 0;
		int byteread = 0;
		FileInputStream inPutStream = null;
		FileOutputStream outPutStream = null;

		try {

			// oldPath的文件copy到新的路径下，如果在新路径下有同名文件，则覆盖源文件
			inPutStream = new FileInputStream(oldPath);

			outPutStream = new FileOutputStream(newPath);
			byte[] buffer = new byte[4096];

			while ((byteread = inPutStream.read(buffer)) != -1) {

				// byte ファイル
				bytesum += byteread;
				outPutStream.write(buffer, 0, byteread);
			}
		} finally {

			// inPutStreamを关闭
			if (inPutStream != null) {
				inPutStream.close();
				inPutStream = null;
			}

			// inPutStream关闭
			if (outPutStream != null) {
				outPutStream.close();
				outPutStream = null;
			}

		}

	}

	public static String toPropString(Properties props) {
		StringBuilder reuslt = new StringBuilder();
		for (String key : props.stringPropertyNames()) {
			reuslt.append(key).append("=").append(props.get(key)).append("\n");
		}
		return reuslt.toString();
	}

	public static String getLocalMac() throws Exception {
		InetAddress ia = InetAddress.getLocalHost();
		//获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			//字节转换为整数
			int temp = mac[i] & 0xff;
			String str = Integer.toHexString(temp);
			if (str.length() == 1) {
				sb.append("0" + str);
			} else {
				sb.append(str);
			}
		}
		return sb.toString().toUpperCase();
	}

	public static final String getProcessID() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return runtimeMXBean.getName().split("@")[0];
	}

	private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	public static int getCpuLoad() {
		double cpuLoad = osmxb.getSystemCpuLoad();
		int percentCpuLoad = (int) (cpuLoad * 100);
		return percentCpuLoad;
	}


	public static int getFreeMemory() {
		return (int) (osmxb.getFreePhysicalMemorySize() / 1024 / 1024 / 1024);
	}

	public static int getTotalMemory() {
		return (int) (osmxb.getTotalPhysicalMemorySize() / 1024 / 1024 / 1024);

	}

	public static int getFreeDisk() {
		String os = System.getProperty("os.name");
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String path = sysConfig.getLogPath();
		if (os.startsWith("Windows")) {
			String rootPath = path.substring(0, path.indexOf(":") + 1);
			File file = new File(rootPath);
			return (int) (file.getFreeSpace() / (1024 * 1024 * 1024));
		} else {
			File rootFile = new File(path).getParentFile();
			return (int) (rootFile.getFreeSpace() / (1024 * 1024 * 1024));
		}
	}

	public synchronized static boolean downLoadAndInstallLic(String downloadUrl) throws Exception {
		if (StringUtils.isBlank(downloadUrl)) return false;
		byte[] result = HttpRequest.get(downloadUrl)
				.header("serviceId", SystemContants.API_SERVICE_ID_LIC_DOWNLOAD)
				.header("clientVersion", SystemContants.CLIENT_VERSION_1_0)
				.header("privateKey", SystemContants.PRIVATE_KEY)
				.header("licExpireDays", String.valueOf(GlobalProperty.getInstance().getLicExpireDays()))
				.header("mac", CommonUtil.getLocalMac())
				.header("processId", CommonUtil.getProcessID())
				.execute().bodyBytes();
		String licZipPath = System.getProperty("user.dir") + File.separator + CommonUtil.getLocalMac() + ".zip";
		FileUtil.writeBytes(result, licZipPath);
		ZipUtil.unzip(licZipPath, System.getProperty("user.dir"));
		FileUtil.del(licZipPath);
		return LicenseManagerHolder.getLicenseManagerHolder().verifyInstall()
				&& LicenseManagerHolder.getLicenseManagerHolder().verifyCert();
	}

	public static void requestControlCenter(String controlUrl,
														 String serviceId,
														 String body,
														 ControlCenterCallback callback) throws Throwable {
		if (StringUtils.isBlank(controlUrl)) {
			log.warn("控制中心地址为空");
			return;
		}
		String result =HttpRequest.post(controlUrl)
				.header("serviceId", serviceId)
				.header("clientVersion", SystemContants.CLIENT_VERSION_1_0)
				.header("privateKey", SystemContants.PRIVATE_KEY)
				.header("licExpireDays", String.valueOf(GlobalProperty.getInstance().getLicExpireDays()))
				.header("mac", CommonUtil.getLocalMac())
				.header("processId", CommonUtil.getProcessID())
				.body(body)
				.execute().body();
		JSONObject resultJson = JSON.parseObject(result);
		if (resultJson == null) throw new RuntimeException(String.format("[%s]控制中心地址无法识别", controlUrl));
		String resultCode = resultJson.getString("resultcode");
		if (resultCode != null && resultCode.equals(SystemContants.API_SUCCESS)) {
			callback.processReturn(resultJson);
		} else {
			log.warn(resultJson.getString("message"));
		}
	}
}
