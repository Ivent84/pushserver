package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {

	List<String> list = new ArrayList<String>();

	String lastKey, value, rootKey;

	Map<String, Object> map = new HashMap<String, Object>();

	List<String> arrayNames = new ArrayList<String>();
	List<String> emptyObjs = new ArrayList<String>();

	@Override
	public void startDocument() throws SAXException {
		emptyObjs.add("richmanmembers");
		arrayNames.add("richmanmember");
		arrayNames.add("shortnumbermemberdetailinfomation");
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		list.remove(lastKey);
		map.remove(lastKey);
		String value = new String(ch, start, length);
		if (StringUtils.isNotEmpty(value)) {
			this.value = value;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (map.containsKey(qName)) {
			String parentKey = list.get(list.size() - 1);
			Object obj = map.get(parentKey);
			if (obj instanceof JSONObject) {
				JSONObject jo = ((JSONObject) obj);
				if (jo.containsKey(qName)) {
					Object ck = jo.get(qName);
					if (ck instanceof JSONObject) {
						JSONArray array = new JSONArray();
						jo.remove(qName);
						array.add(ck);
						jo.put(qName, array);
					}
				} else {
					JSONArray array = new JSONArray();
					jo.put(qName, array);
					array.add(map.get(qName));
				}
			}
		}
		map.put(qName, new JSONObject());
		list.add(qName);
		lastKey = qName;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		String parentKey = list.get(list.size() - 1); 
		if (value != null) {
			put(parentKey, qName, value);
			value = null;
			return;
		} else if (qName.equals(lastKey)) {
			list.remove(lastKey);
			map.remove(lastKey);
			parentKey = list.get(list.size() - 1);
			if (emptyObjs.contains(qName)) {
				put(parentKey, qName, new JSONObject());
			} else {
				put(parentKey, qName, "");
			}
			return;
		} else if (qName.equals(parentKey)) {
			list.remove(qName);
			if (list.size() > 0) {
				parentKey = list.get(list.size() - 1);
				put(parentKey, qName, map.get(qName));
			}
		}
		rootKey = parentKey;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	void put(String parentKey, String key, Object value) {
		Object obj = map.get(parentKey);
		if (obj instanceof JSONObject) {
			JSONObject jo = ((JSONObject) obj);
			if (arrayNames.contains(key)) {
				JSONArray array = null;
				if (jo.get(key) == null) {
					array = new JSONArray();
				} else {
					array = jo.getJSONArray(key);
				}
				array.add(value);
				jo.put(key, array);
			} else if (jo.containsKey(key)) {
				Object ck = jo.get(key);
				if (ck instanceof JSONArray) {
					((JSONArray) ck).add(value);
				} else {
					JSONArray array = new JSONArray();
					jo.remove(key);
					jo.put(key, array);
					array.add(ck);
				}
			} else
				jo.put(key, value);
		} else {
			((JSONArray) obj).add(value);
		}

	}

	public String toJson() {
		JSONObject obj = (JSONObject) map.get(rootKey);
		return obj.toString();
	}
}
