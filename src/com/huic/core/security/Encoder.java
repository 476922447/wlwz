package com.huic.core.security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.huic.core.misc.Base64;
import com.huic.core.util.MailUtil;

public class Encoder {

    private static Log log = LogFactory.getLog(Encoder.class);

    // Please note that 2 below methods are used in #getMD5_Base64 only
    // use them in other methods will make it not thread-safe
    private static MessageDigest digest = null;
    private static boolean isInited = false;

    private Encoder() {
    }

    /**
     * This method return a String that has been encrypted as MD5 and then escaped using Base64.<p>
     * This method should be used to encrypt all password for maximum security.
     * @param input String the string that need encrypted
     * @return String the string after encrypted
     */
    public static synchronized String getMD5_Base64(String input) {
        // please note that we dont use digest, because if we
        // cannot get digest, then the second time we have to call it
        // again, which will fail again
        if (isInited == false) {
            isInited = true;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (Exception ex) {
                log.error("Cannot get MessageDigest. Application may fail to run correctly.", ex);
            }
        }
        if (digest == null) return input;

        // now everything is ok, go ahead
        try {
            digest.update(input.getBytes("UTF-8"));
        } catch (java.io.UnsupportedEncodingException ex) {
            log.error("Assertion: This should never occur.");
        }
        byte[] rawData = digest.digest();
        byte[] encoded = Base64.encode(rawData);
        String retValue = new String(encoded);
        return retValue;
    }

    /**
     * This method just call URLEncoder.encode() so we can get rid of
     * the deprecation warning from using URLEncoder.encode() directly.
     * @param input String
     * @return String
     */
    public static String encodeURL(String input){
        // The following line will cause a warning if compile with jdk1.4
        // However, we cannot use the new method String encode(String s, String enc)
        // in jdk1.4, because it wont be compiled with jdk1.3
        // Currently, there is no way to get rid of this wanring
        String ret=null;
        try {
			ret=URLEncoder.encode(input,"GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		//return URLEncoder.encode(input);
		return ret;
    }

    /**
     * 返回url,url由协议+路径构成.而且此url将完全转换成小写字母的形式.
     * Filter a url to make it safe, this method is used in class URLFilter
     * @param url String a url to be filtered
     * @return String a url that has been filtered
     */
    public static String filterUrl(String url) {
        String lowerUrl = url.toLowerCase();
        if ( (lowerUrl.indexOf("javascript:") >= 0) ||
             lowerUrl.indexOf("file:") >= 0) {
            return "";
        }

        String protocol = "http://";//default protocol
        String name = null;
        if (url.startsWith("http://")) {
            protocol = "http://";
            name = url.substring(protocol.length());// must duplicate it because of the default protocol
        } else if (url.startsWith("https://")) {
            protocol = "https://";
            name = url.substring(protocol.length());// must duplicate it because of the default protocol
        } else if (url.startsWith("ftp://")) {
            protocol = "ftp://";
            name = url.substring(protocol.length());// must duplicate it because of the default protocol
        } else if (url.startsWith("mailto:")) {
            protocol = "mailto:";
            name = url.substring(protocol.length());// must duplicate it because of the default protocol
        } else {
            name = url;
        }
        String ret;
        if (protocol.equals("mailto:")) {
            try {
                MailUtil.checkGoodEmail(name);
                ret = protocol + name;
            } catch (Exception ex) {
                ret = "";
            }
        } else {
            ret = protocol + encodePath(name);
        }
        return ret;
    }

    /**
     *调用 removeInvalidUserInURL(path)移除不合法的url
     * @param path the path, something like this localhost:8080/image/index.html
     * @return the path removeInvalidUserInURLed
     */
    private static String encodePath(String path) {
        path = removeInvalidUserInURL(path);
        return path;
        /*
        String ret = "";
        int indexFirstSlash = path.indexOf('/');
        if ( indexFirstSlash != -1 ) {
            String hostport = path.substring(0, indexFirstSlash);
            int indexFirstColon = hostport.indexOf(':');
            if (indexFirstColon != -1) {
                String host = hostport.substring(0, indexFirstColon);
                String port = hostport.substring(indexFirstColon + 1);
                hostport = Encoder.encodeURL(host) + ":" + Encoder.encodeURL(port);
            } else {
                hostport = Encoder.encodeURL(hostport);
            }
            String filename = path.substring(indexFirstSlash + 1);
            filename = Encoder.encodeURL(filename);
            ret = hostport + "/" + filename;
        } else {
            ret = Encoder.encodeURL(path);
        }
        return ret;
        */
    }

    /**
     * 如果path中有%和@,且%在@前面,则返回@后面的内容.
     * This method is used to fix IE spoof url bug:
     * http://originalsite.com%00@www.badsite.com
     * @param path String
     * @return String
     */
    private static String removeInvalidUserInURL(String path) {
        // atIndex is the RIGHT most of @
        int atIndex = path.lastIndexOf('@');
        if (atIndex != -1) {
            // has the user info part
            // percentIndex is the LEFT most of %
            int pecentIndex = path.indexOf('%');
            if ((pecentIndex != -1) && (pecentIndex < atIndex)) {
                // user info part has % in it, very likely a spoof url
                return path.substring(atIndex + 1);// get the char right after @
            }
        }
        return path;
    }

    ///*
    public static void main(String[] args){
		String testString1 ="abc中文字%@abc中文字.com ad";
        //test data should be
        //a1            iou3zTQ6oq2Zt9diAwhXog==
        //Hello World   sQqNsWTgdUEFt6mb5y4/5Q==

        String testString = "a1";
        String encrypted = getMD5_Base64(testString);
        System.out.println(testString+"==>encrypted ==> " + encrypted);
        System.out.println("length = " + encrypted.length());
        
        String path=Encoder.encodePath(testString1);//abc中文字.com ad
		String url=Encoder.encodeURL(testString1);//abc%D6%D0%CE%C4%D7%D6%25%40abc%D6%D0%CE%C4%D7%D6.com+ad
        int a=0;
    }
    //*/
}
