package acqua.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	public static String StringToMD5(String str)  {
		
	    String md5Hex = DigestUtils
	      .md5Hex(str).toUpperCase();
	         
	    return md5Hex;
	}
}
