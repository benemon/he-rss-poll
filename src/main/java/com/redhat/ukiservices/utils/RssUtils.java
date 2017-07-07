package com.redhat.ukiservices.utils;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RssUtils {

	private static final String CHANNEL = "channel";
	private static final String ITEM = "item";
	private static final String TITLE = "title";
	private static final String PUBLISH_DATE = "pubDate";
	private static final String ROAD = "road";
	private static final String REGION = "region";
	private static final String COUNTY = "county";
	private static final String CATEGORIES = "categories";
	private static final String CATEGORY = "category";
	private static final String DESCRIPTION = "description";

	public static List<JsonObject> toJson(Document doc) {

		List<JsonObject> entries = new ArrayList<JsonObject>();

		List<Element> list = doc.getRootElement().getChild(CHANNEL).getChildren(ITEM);

		for (Element el : list) {

			JsonObject obj = new JsonObject();
			obj.put(TITLE, el.getChildText(TITLE));
			obj.put(PUBLISH_DATE, el.getChildText(PUBLISH_DATE));
			obj.put(ROAD, el.getChildText(ROAD));
			obj.put(REGION, el.getChildText(REGION));
			obj.put(COUNTY, el.getChildText(COUNTY));

			JsonArray categories = new JsonArray();
			for (Element cat : el.getChildren(CATEGORY)) {
				categories.add(new JsonObject().put(CATEGORY, cat.getText()));
			}
			obj.put(CATEGORIES, categories);

			obj.put(DESCRIPTION, el.getChildText(DESCRIPTION));

			entries.add(obj);
		}

		return entries;

	}

}
