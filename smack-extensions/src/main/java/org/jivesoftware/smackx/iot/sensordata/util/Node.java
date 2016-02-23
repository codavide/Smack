package org.jivesoftware.smackx.iot.sensordata.util;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smackx.iot.control.util.NodeReference;

public class Node extends NodeReference {
    
    private List<TimestampType> sensordata = new ArrayList<TimestampType>();

    public Node() {
    }
    
    public Node(String nodeId) {
    	this.nodeId = nodeId;
    }

    public Node(String nodeId, TimestampType timestamp) {
        this.nodeId = nodeId;
        addTimestamp(timestamp);
    }
    
    public TimestampType getTimestamp() {
        return sensordata.size() > 0 ? sensordata.get(0) : null;
    }

    public void addTimestamp(TimestampType timestamp) {
        sensordata.add(timestamp);
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<node nodeId='").append(getNodeId()).append("'");
        if(getSourceId()!=null) {
        	buf.append("' sourceId='"+getSourceId()).append("'");
        }
        if(getCacheType()!=null) {
        	buf.append("' cacheType='"+getCacheType()).append("'");
        }        
        buf.append("'>");
        TimestampType[] data = sensordata.toArray(new TimestampType[0]);
        for (int j = 0; j < data.length; j++) {
            buf.append(data[j].toXML());
        }
        buf.append("</node>");
        return buf.toString();
    }
}