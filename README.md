# rpa-client
rpa client management

### lisence 生成 
参考:（https://blog.csdn.net/jfengamarsoft/article/details/104557968）
使用keytool生成密钥对：
1、首先要用KeyTool工具来生成私匙库：（-alias别名 –validity 3650表示10年有效）

keytool -genkey -alias privatekey -keystore privateKeys.store -keysize 1024 -validity 3650

2、然后把私匙库内的公匙导出到一个文件当中：

keytool -export -alias privatekey -file certfile.cer -keystore privateKeys.store
3、然后再把这个证书文件导入到公匙库：

keytool -import -alias publiccert -file certfile.cer -keystore publicCerts.store
最后生成文件privateKeys.store、publicCerts.store拷贝出来备用。


### 更新包制作：
1、新建文件夹，名字随便取  
2、将sql脚本文件(例如1.sql) 和rpa-client.jar(客户端程序) 放到文件夹中  
3、将这个文件夹压缩为zip包，并改后缀名为mbot
