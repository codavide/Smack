package org.jivesoftware.smackx.iot.sensordata.util;

import java.text.SimpleDateFormat;

public class Error {
	private String nodeId;
	private String sourceId = null;
	private String cacheType = null;
	private String timestamp;

	public Error() {
	}

	public Error(String nodeId) {
		this.nodeId = nodeId;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		DateType date = new DateType();
		this.timestamp = df.format(date);
	}
	
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getCacheType() {
		return cacheType;
	}

	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<error nodeId='").append(getNodeId()).append("'");
		buf.append("timestamp='").append(getTimestamp()).append("'");
		if(getSourceId()!=null) {
			buf.append("' sourceId='"+getSourceId()).append("'");
		}
		if(getCacheType()!=null) {
			buf.append("' cacheType='"+getCacheType()).append("'");
		}        
		buf.append("'>");
		buf.append("</error>");
		return buf.toString();
	}
}