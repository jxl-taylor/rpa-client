package lisence;

/**
 * Created by feng on 2020/2/28
 */
public class licenseCreateTest {
	public static void main(String[] args){
		CreateLicense cLicense = new CreateLicense();
		//获取参数
		cLicense.setParam("/createParam.properties");
		//生成证书
		cLicense.create();
	}
}