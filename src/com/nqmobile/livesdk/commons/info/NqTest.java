package com.nqmobile.livesdk.commons.info;

import java.io.File;
import java.io.FileInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import android.os.Environment;
import android.util.Xml;

import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.Tools;

public class NqTest {
	/** 是否启用NqTest，加载nqlive.xml **/
	public final static boolean isNqTestEnable = true;	
    /** DEBUG 是否打开 **/
	private static boolean debug = true;	
    /** 是否将日志写入文件 **/
	private static boolean writeFile = false;
    /** 是否使用本地路径，测试时可以使用此路径打印日记(注：这个对特殊机型， 在开关机是非常有用)  **/
	private static boolean isUseLocalPath = false;
	
	static {
		NqTest.parseData();
	}
	
	public static void init(){
		
	}
	
	public static boolean isDebug() {
		return debug;
	}

	public static boolean isWriteFile() {
		return writeFile;
	}

	public static boolean isUseLocalPath() {
		return isUseLocalPath;
	}

	public static boolean isNqTestEnable() {
		return isNqTestEnable;
	}
	
	static void parseData() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			return;
		}
		
		if (!isNqTestEnable){
			return;
		}
		
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(sdcard + "/nqtest/nqlive.xml");
        if(!file.exists()) {
        	return;
        }

        FileInputStream input = null;
        byte[] data = null;
        DataParser dp = null;
        try {
            input = new FileInputStream(file);
            int length = (int) file.length();
            data = new byte[length];
            input.read(data);
            input.close();
            input = null;
            file = null;
            
            dp = new DataParser();
            String str = new String(data);
            Xml.parse(str, dp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	FileUtil.closeStream(input);
        }
    }

	/**
	 * add by ysongren start for 不同版本设置:REL，TEST，VRF，CNS 
	 */
	private static void setVersionStyle(String vst) {
		int versionStyle = CommonDefine.VERSION_STYLE_SHOPPING;
		if (Tools.stringEquals(vst, "REL")) {
			versionStyle = CommonDefine.VERSION_STYLE_RELEASE;
		} else if(Tools.stringEquals(vst, "VRF")) {
			versionStyle = CommonDefine.VERSION_STYLE_VERIFY;
		} else if(Tools.stringEquals(vst, "CNS")) {
			versionStyle = CommonDefine.VERSION_STYLE_SHOPPING;
		} else if(Tools.stringEquals(vst, "TEST")) {
			versionStyle = CommonDefine.VERSION_STYLE_TEST;
		}
		
		CommonDefine.setStyleType(versionStyle);
	}

    static class DataParser extends DefaultHandler2 {
        private StringBuffer buf = new StringBuffer();

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);

            if(localName.equals("imsi")) {
                MobileInfo.setImsi(buf.toString());
            } else if(localName.equals("imei")) {
            	MobileInfo.setImei(buf.toString());
            } else if(localName.equals("lat")) {
            	MobileInfo.setLat(buf.toString());
            } else if(localName.equals("lon")) {
            	MobileInfo.setLon(buf.toString());
            } else if(localName.equals("country")) {
            	MobileInfo.setCountry(buf.toString());
            } else if(localName.equals("mcnc")) {
            	MobileInfo.setMcnc(buf.toString());
            } else if(localName.equals("networkcountry")){
            	MobileInfo.setNetworkCountry(buf.toString());
            } else if(localName.equals("networkmcnc")){
            	MobileInfo.setNetworkMcnc(buf.toString());
            } else if(Tools.stringEquals(localName, "debug")){
            	debug = Tools.stringEquals(buf.toString(), "true");
            } else if(Tools.stringEquals(localName, "writeLog")){
            	writeFile =  Tools.stringEquals(buf.toString(), "true");
            } else if(Tools.stringEquals(localName, "isUseLocalPath")){
            	isUseLocalPath =  Tools.stringEquals(buf.toString(), "true");
            } else if(Tools.stringEquals(localName, "serverType")){
            	setVersionStyle(buf.toString());
            }

            buf.setLength(0);
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            buf.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            buf.append(ch, start, length);
        }
    }
}
