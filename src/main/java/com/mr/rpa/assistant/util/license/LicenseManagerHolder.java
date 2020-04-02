package com.mr.rpa.assistant.util.license;

import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.SystemContants;
import com.mr.rpa.assistant.util.encryption.Encipher;
import de.schlichtherle.license.*;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Created by feng on 2020/2/28
 */
@Log4j
public class LicenseManagerHolder {

	//common param
	private static String PUBLICALIAS = "";
	private static String STOREPWD = "";
	private static String SUBJECT = "";
	private static String licPath = "";
	private static String pubPath = "";

	private LicenseManager licenseManager;

	private static LicenseManagerHolder licenseManagerHolder = new LicenseManagerHolder();

	private LicenseManagerHolder() {
		setParam();
		licenseManager = new LicenseManager(initLicenseParams());
	}

	public static LicenseManagerHolder getLicenseManagerHolder() {
		return licenseManagerHolder;
	}

	private void setParam() {
		// 获取参数
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream(SystemContants.LICENSE_PROPERTY_FILE_PATH);
		try {
			prop.load(in);
			in.close();
			PUBLICALIAS = prop.getProperty("PUBLICALIAS");
			STOREPWD = Encipher.DecodePasswd(SystemContants.LIC_KEY_PWD);
			SUBJECT = CommonUtil.getLocalMac();
			licPath = prop.getProperty("licPath");
			pubPath = prop.getProperty("pubPath");
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("加载资源文件[verifyParam.properties]出错");
		}

	}

	/**
	 * 返回验证证书需要的参数
	 */
	private LicenseParam initLicenseParams() {
		CipherParam cipherParam = new DefaultCipherParam(STOREPWD);
		KeyStoreParam privateStoreParam = new MyKeyStoreParam(
				LicenseManagerHolder.class, pubPath, PUBLICALIAS, STOREPWD, null);
		LicenseParam licenseParams = new DefaultLicenseParam(SUBJECT,
				Preferences.userNodeForPackage(LicenseManagerHolder.class),
				privateStoreParam, cipherParam);
		return licenseParams;
	}

	/**
	 * 安装证书
	 *
	 * @return
	 */
	public boolean verifyInstall() {
		if(GlobalProperty.getInstance().isDebug()) return true;
		try {
			licenseManager.install(new File(licPath));
			System.out.println("客户端安装证书成功!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("客户端证书安装失败!");
			return false;
		}
		return true;
	}

	/**
	 * 验证证书
	 *
	 * @return
	 */
	public boolean verifyCert() {
		if(GlobalProperty.getInstance().isDebug()) return true;
		try {
			LicenseContent licenseContent = licenseManager.verify();
			GlobalProperty.getInstance().setLicenseContent(licenseContent);
			System.out.println("客户端验证证书成功!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("客户端证书验证失效!");
			return false;
		}
		return true;
	}

	public static void main(String[] s) {
		LicenseManagerHolder.getLicenseManagerHolder().verifyInstall();
		LicenseManagerHolder.getLicenseManagerHolder().verifyCert();
	}
}
