package org.jivesoftware.smackx.iot.control.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.control.util.NodeReference;
import org.jivesoftware.smackx.iot.control.util.SensorDataControlConstants;

public class SensorDataControlGetForm extends IQ {
	private List<NodeReference> nodes = new ArrayList<NodeReference>();
	
	private String serviceToken = null;
	private String deviceToken = null;
	private String userToken = null;

	
	public SensorDataControlGetForm() {
		super(SensorDataControlConstants.GET_FORM_NAME, SensorDataControlConstants.CONTROL_NAMESPACE);
	}
	
	
	public String getServiceToken() {
		return serviceToken;
	}

	public void setServiceToken(String serviceToken) {
		this.serviceToken = serviceToken;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	
	
	public List<NodeReference> getNodes()  {
		return this.nodes;
	}
	
	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder buf) {
		buf.append("<").append(SensorDataControlConstants.GET_FORM_NAME).append(" xmlns='").append(SensorDataControlConstants.CONTROL_NAMESPACE).append("'");
	    if(serviceToken!=null) {
    		buf.append(" serviceToken='").append(serviceToken).append("'");
	    }
	    if(deviceToken!=null) {
    		buf.append(" deviceToken='").append(deviceToken).append("'");
	    }
	    if(userToken!=null) {
    		buf.append(" userToken='").append(userToken).append("'");
	    }
	    buf.append(">");
		for (NodeReference node: nodes) {
            buf.append(node.toXML());
        }
        return buf;
	}
}
