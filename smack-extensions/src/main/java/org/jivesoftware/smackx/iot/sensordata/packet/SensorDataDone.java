package org.jivesoftware.smackx.iot.sensordata.packet;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;

public class SensorDataDone implements ExtensionElement {
	private int seqNo;
	
	public SensorDataDone(int seqNo) {
		this.seqNo = seqNo;
	}
	
	@Override
	public String getElementName() {
		return SensorDataConstants.DONE_NAME;
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
		buf.append("<done xmlns='").append(getNamespace()).append("' seqnr='").append(getSeqNo()).append("'/>");
		return buf.toString();
	}
}
