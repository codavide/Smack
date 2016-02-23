package org.jivesoftware.smackx.iot.sensordata.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;
import org.jivesoftware.smackx.iot.sensordata.util.Error;

public class SensorDataFailure  implements ExtensionElement {
	private int seqno=0;
	private Boolean done = null;
	private List<Error> errors = new ArrayList<Error>();
	
	public SensorDataFailure(int seqno) {
		this.seqno = seqno;
	}

	public int getSeqno() {
		return seqno;
	}

	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}

	public Boolean getDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public List<Error> getErrors() {
		return errors;
	}

	@Override
	public String getElementName() {
		return SensorDataConstants.FAILURE_NAME;
	}

	@Override
	public String getNamespace() {
		return SensorDataConstants.SENSORDATA_NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();

		buf.append("<").append(getElementName());
		buf.append(" xmlns='").append(getNamespace());
		buf.append("' seqnr='").append(getSeqno());
		if(done!=null) {
			buf.append("' done='").append(Boolean.toString(getDone())).append("'");
		}
		buf.append(">");
		for(final Error error : getErrors()) {
			buf.append(error.toXML());	
		}
		buf.append("</").append(getNamespace()).append(">");
		return buf.toString();
	}
}
