package com.nqmobile.livesdk.modules.update.network;

import android.content.ContentValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class AppUpdateHandler extends DefaultHandler2 {

	private StringBuffer buf;
	private ContentValues content;
	private int ADmessageNumber = 0;
	private int systemMessageNumber = 0;

	public AppUpdateHandler(ContentValues contentValues) {
		this.content = contentValues;
		buf = new StringBuffer();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buf.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		content.put("AppUpdateADMessageCount", Integer.toString(ADmessageNumber));
		content.put("AppUpdateSystemMsgCount", Integer.toString(systemMessageNumber));
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (localName.equals("Protocol")) {
			content.put("Protocol", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("Command")) {
			content.put("Command", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("SessionId")) {
			content.put("SessionId", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("Balance")) {
			content.put("Balance", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("UID")) {
			content.put("UID", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("NextConnectTime")) {//添加组件更新的下次联网时间字段
			content.put("NextConnectTime",buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("Level")) {
			buf.setLength(0);
		} else if (localName.equals("Status")) {
			content.put("Status", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("Expired")) {
			content.put("Expired", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("NextPayDay")) {
			content.put("NextPayDay", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("LevelName")) {
			content.put("LevelName", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("SecretSpaceUsable")) {
			content.put("SecretSpaceUsable", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("SmsFilterUsable")) {
			content.put("SmsFilterUsable", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("Message")) {
			// 只取第一条
			if(!content.containsKey("Prompt")) {
				content.put("Prompt", buf.toString().trim());
			}
			buf.setLength(0);
		} else if (localName.equals("mt")) {
			content.put("maintitle", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("st")) {
			content.put("subtitle", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("content")) {
			content.put("content", buf.toString().trim());
			buf.setLength(0);
		} else if (localName.equals("ErrorCode")) {
			buf.setLength(0);
		} else if (localName.equals("Module")) {
			buf.setLength(0);
		} else if (localName.equals("UpdateInfo")) {
			buf.setLength(0);
		}
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if (localName.equals("Protocol")) {
			buf.setLength(0);
		} else if (localName.equals("Command")) {
			buf.setLength(0);
		} else if (localName.equals("SessionId")) {
			buf.setLength(0);
		} else if (localName.equals("Balance")) {
			buf.setLength(0);
		} else if (localName.equals("UID")) {
			buf.setLength(0);
		} else if (localName.equals("Level")) {
			buf.setLength(0);
		} else if (localName.equals("NextConnectTime")) {
			buf.setLength(0);
		} else if (localName.equals("Status")) {
			buf.setLength(0);
		} else if (localName.equals("Expired")) {
			buf.setLength(0);
		} else if (localName.equals("NextPayDay")) {
			buf.setLength(0);
		} else if (localName.equals("LevelName")) {
			buf.setLength(0);
		} else if (localName.equals("SecretSpaceUsable")) {
			buf.setLength(0);
		} else if (localName.equals("SmsFilterUsable")) {
			buf.setLength(0);
		} else if (localName.equals("File")) {
			// String type = attributes.getValue("type");
			content.put("AppUpdateFileLength", attributes.getValue("length"));
			content.put("AppUpdateFileName", attributes.getValue("name"));
			content.put("AppUpdateSrc", attributes.getValue("src"));
			content.put("AppUpdateAction", attributes.getValue("action"));
			buf.setLength(0);
		} else if (localName.equals("Module")) {
			content.put("AppVersion", attributes.getValue("version"));
			buf.setLength(0);
		} else if (localName.equals("ErrorCode")) {
			buf.setLength(0);
		} else if (localName.equals("Message")) {
			buf.setLength(0);
		} else if (localName.equals("SystemMessages")) {
			if(attributes.getValue("display")!=null){
				if(attributes.getValue("display").length()>0){
					content.put("display", attributes.getValue("display"));
				}
			}
			buf.setLength(0);
		} else if (localName.equals("AVMessages")) {
			if (attributes.getValue("display") != null) {
				if (attributes.getValue("display").length() > 0){
					content.put("display", attributes.getValue("display"));
				}
			}
			buf.setLength(0);
		} else if (localName.equals("Module")) {
			buf.setLength(0);
		} else if (localName.equals("UpdateInfo")) {
			content.put("AppUpdateNecessary", attributes.getValue("necessary"));
			buf.setLength(0);
		}
	}
}
