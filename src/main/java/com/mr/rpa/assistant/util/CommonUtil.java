package com.mr.rpa.assistant.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by feng on 2020/3/10 0010
 */
public class CommonUtil {
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
}
