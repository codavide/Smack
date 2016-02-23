package org.jivesoftware.smackx.iot.sensordata.packet;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;


public class SensorDataCancelled extends IQ {
	private int seqno=0;

	public SensorDataCancelled() {
		super(SensorDataConstants.CANCEL_NAME, SensorDataConstants.SENSORDATA_NAMESPACE);
	}
	
	public SensorDataCancelled(int seqno) {
		super(SensorDataConstants.CANCEL_NAME, SensorDataConstants.SENSORDATA_NAMESPACE);
		this.seqno = seqno;
	}

	public int getSeqno() {
		return seqno;
	}

	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder buf) {
        buf.append("<"+SensorDataConstants.CANCEL_NAME+" xmlns='"+SensorDataConstants.SENSORDATA_NAMESPACE+"' seqnr='").append(String.valueOf(seqno)).append("'/>");        
        return buf;
	}

}
