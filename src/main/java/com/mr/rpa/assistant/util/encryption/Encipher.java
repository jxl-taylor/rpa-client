package com.mr.rpa.assistant.util.encryption;

public class Encipher{
    // 加密  
    public static String EncodePasswd(String strPasswd){  
        String strEncodePasswd = new String("");  
        int i;  
        int n;  
        char code;  
        String des = new String();  
        String strKey = new String();  
        if((strPasswd == null) || strPasswd.length() == 0){  
            strEncodePasswd = "";  
            return strEncodePasswd;  
        }  
        strKey = EncipherConst.m_strKey1 + EncipherConst.m_strKey2 + EncipherConst.m_strKey3 + EncipherConst.m_strKey4 + EncipherConst.m_strKey5 + EncipherConst.m_strKey6;  
        while(strPasswd.length() < 8){  
            strPasswd = strPasswd + (char)1;  
        }  
        des = "";  
        for(n = 0; n <= strPasswd.length() - 1; n++){  
            while(true){  
                code = (char)Math.rint(Math.random() * 100);  
                while((code > 0) && (((code ^ strPasswd.charAt(n)) < 0) || ((code ^ strPasswd.charAt(n)) > 90))){  
                    code = (char)((int)code - 1);  
                }  
                char mid = 0;  
                int flag;  
                flag = code ^ strPasswd.charAt(n);  
                if(flag > 93){  
                    mid = 0;  
                }else{  
                    mid = strKey.charAt(flag);  
                }  
                if((code > 35) & (code < 122) & (code != 124) & (code != 39) & (code != 44) & (mid != 124) & (mid != 39) & (mid != 44)){  
                    break;  
                }  
                //确保生成的字符是可见字符并且在SQL语句中有效  
            }  
            char temp = 0;  
            temp = strKey.charAt(code ^ strPasswd.charAt(n));  
            des = des + (char)code + temp;  
        }  
        strEncodePasswd = des;  
        return strEncodePasswd;  
    }  
    // 解密  
    public static String DecodePasswd(String varCode){  
        int n;  
        String des = new String();  
        String strKey = new String();  
        if((varCode == null) || (varCode.length() == 0)){  
            return "";  
        }  
        strKey = EncipherConst.m_strKey1 + EncipherConst.m_strKey2 + EncipherConst.m_strKey3 + EncipherConst.m_strKey4 + EncipherConst.m_strKey5 + EncipherConst.m_strKey6;  
        if(varCode.length() % 2 == 1){
            varCode = varCode + "?";  
        }  
        des = "";  
        for(n = 0; n <= varCode.length() / 2 - 1; n++){  
            char b;  
            b = varCode.charAt(n * 2);  
            int a;  
            a = (int)strKey.indexOf(varCode.charAt(n * 2 + 1));  
            des = des + (char)((int)b ^ a);  
        }  
        n = des.indexOf(1);  
        if(n > 0){  
            return des.substring(0, n);  
        } else{  
            return des;  
        }  
    }  
      
    static class EncipherConst{
        public final static String m_strKey1 = "zxcvbnm,./asdfg";
        public final static String m_strKeya = "cjk;";  
        public final static String m_strKey2 = "hjkl;'qwertyuiop";  
        public final static String m_strKeyb = "cai2";  
        public final static String m_strKey3 = "[]\\1234567890-";  
        public final static String m_strKeyc = "%^@#";  
        public final static String m_strKey4 = "=` ZXCVBNM<>?:LKJ";  
        public final static String m_strKeyd = "*(N";  
        public final static String m_strKey5 = "HGFDSAQWERTYUI";  
        public final static String m_strKeye = "%^HJ";  
        public final static String m_strKey6 = "OP{}|+_)(*&^%$#@!~";  
    }  
    // 测试  
    public static void  main(String[] args) throws Exception{  
        String pas = "123" ;
        String encodePas = EncodePasswd(pas);  
        System.out.println(encodePas);
        String decodePas = DecodePasswd(encodePas);  
        System.out.println(decodePas);
    }  
}