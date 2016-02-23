package org.jivesoftware.smackx.iot.sensordata.packet;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;

public class SensorDataStarted implements ExtensionElement {
	private int seqNo=0;
	
	public SensorDataStarted() {
	}
	
	public SensorDataStarted(int seqNo) {
		this.seqNo = seqNo;
	}
	
	
	@Override
	public String getElementName() {
		return SensorDataConstants.STARTED_NAME;
	}

	@Override
	public String getNamespace() {
		return SensorDataConstants.SENSORDATA_NAMESPACE;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<"+getElementName()+" xmlns='").append(getNamespace()).append("' seqnr='").append(getSeqNo()).append("'/>");				
		return buf.toString();
	}
}
