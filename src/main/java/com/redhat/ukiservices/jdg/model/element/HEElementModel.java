package com.redhat.ukiservices.jdg.model.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import io.vertx.core.json.JsonObject;
import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoMessage;

@ProtoDoc("@Indexed")
@ProtoMessage(name = "HEElementModel")
public class HEElementModel implements Serializable {

	private static final long serialVersionUID = 6790831463277341973L;

	private String guid;

	private String title;

	private Date pubDate;

	private String road;

	private String region;

	private String county;

	private ArrayList<HEElementCategoryModel> categories;

	private String description;

	public HEElementModel() {
		// Default constructor for protobuf compatibility
	}

	/**
	 * @return the guid
	 */
	@ProtoField(number = 1, required = true)
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid
	 *            the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @return the title
	 */
	@ProtoField(number = 2, required = true)
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the publishDate
	 */
	@ProtoField(number = 3)
	public Date getPubDate() {
		return pubDate;
	}

	/**
	 * @param pubDate
	 *            the publishDate to set
	 */
	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	/**
	 * @return the road
	 */
	@ProtoField(number = 4, required = true)
	public String getRoad() {
		return road;
	}

	/**
	 * @param road
	 *            the road to set
	 */
	public void setRoad(String road) {
		this.road = road;
	}

	/**
	 * @return the region
	 */
	@ProtoField(number = 5, required = true)
	public String getRegion() {
		return region;
	}

	/**
	 * @param region
	 *            the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * @return the county
	 */
	@ProtoField(number = 6, required = true)
	public String getCounty() {
		return county;
	}

	/**
	 * @param county
	 *            the county to set
	 */
	public void setCounty(String county) {
		this.county = county;
	}

	/**
	 * @return the catergories
	 */
	@ProtoField(number = 7)
	public ArrayList<HEElementCategoryModel> getCategories() {
		return categories;
	}

	/**
	 * @param categories
	 *            the categories to set
	 */
	public void setCategories(ArrayList<HEElementCategoryModel> categories) {
		this.categories = categories;
	}

	/**
	 * @return the description
	 */
	@ProtoField(number = 8, required = true)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((county == null) ? 0 : county.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((pubDate == null) ? 0 : pubDate.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((road == null) ? 0 : road.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HEElementModel other = (HEElementModel) obj;
		if (categories == null) {
			if (other.categories != null) {
				return false;
			}
		} else if (!categories.equals(other.categories)) {
			return false;
		}
		if (county == null) {
			if (other.county != null) {
				return false;
			}
		} else if (!county.equals(other.county)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (guid == null) {
			if (other.guid != null) {
				return false;
			}
		} else if (!guid.equals(other.guid)) {
			return false;
		}
		if (pubDate == null) {
			if (other.pubDate != null) {
				return false;
			}
		} else if (!pubDate.equals(other.pubDate)) {
			return false;
		}
		if (region == null) {
			if (other.region != null) {
				return false;
			}
		} else if (!region.equals(other.region)) {
			return false;
		}
		if (road == null) {
			if (other.road != null) {
				return false;
			}
		} else if (!road.equals(other.road)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer builder = new StringBuffer();
		builder.append("HEElementModel [getGuid()=");
		builder.append(getGuid());
		builder.append(", getTitle()=");
		builder.append(getTitle());
		builder.append(", getPublishDate()=");
		builder.append(getPubDate());
		builder.append(", getRoad()=");
		builder.append(getRoad());
		builder.append(", getRegion()=");
		builder.append(getRegion());
		builder.append(", getCounty()=");
		builder.append(getCounty());
		builder.append(", getCategories()=");
		builder.append(getCategories());
		builder.append(", getDescription()=");
		builder.append(getDescription());
		builder.append("]");
		return builder.toString();
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.put("guid", getGuid());
		obj.put("title", getTitle());
		obj.put("publishDate", getPubDate());
		obj.put("road", getRoad());
		obj.put("region", getRegion());
		obj.put("county", getCounty());
		obj.put("categories", getCategories());
		obj.put("description", getDescription());

		return obj;

	}

}
