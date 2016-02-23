/*
 * Copyright (c) 2013, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the enbygg server software.
 *
 * $Id: SensorDataControl.java 109 2013-09-30 19:49:10Z marcus@arendt.se $
 *
 * -----------------------------------------------------------------
 *
 * Main
 *
 * Authors : Marcus Arendt
 * Created : 12 jun 2013
 * Updated : $Date: 2013-09-30 21:49:10 +0200 (mån, 30 sep 2013) $
 *           $Revision: 109 $
 */

package org.jivesoftware.smackx.iot.control.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.control.util.BooleanParameter;
import org.jivesoftware.smackx.iot.control.util.IntParameter;
import org.jivesoftware.smackx.iot.control.util.NodeReference;
import org.jivesoftware.smackx.iot.control.util.Parameter;
import org.jivesoftware.smackx.iot.control.util.SensorDataControlConstants;

public class SensorDataControl extends IQ {
	private List<Parameter> parameters = new ArrayList<Parameter>();
	private List<NodeReference> nodes = new ArrayList<NodeReference>();
	
	private String serviceToken = null;
	private String deviceToken = null;
	private String userToken = null;
	
	public SensorDataControl() {
		super(SensorDataControlConstants.CONTROL_NAME, SensorDataControlConstants.CONTROL_NAMESPACE);
	}
	
	public SensorDataControl(String nodeId) {
		super(SensorDataControlConstants.CONTROL_NAME, SensorDataControlConstants.CONTROL_NAMESPACE);
		nodes.add(new NodeReference(nodeId));
	}
	
	public SensorDataControl(String name, Boolean value) {
		super(SensorDataControlConstants.CONTROL_NAME, SensorDataControlConstants.CONTROL_NAMESPACE);
		BooleanParameter parameter = new BooleanParameter();
		parameter.setName(name);
		parameter.setValue(value);
	    parameters.add(parameter);
	}
	
	public SensorDataControl(String name, int value) {
		super(SensorDataControlConstants.CONTROL_NAME, SensorDataControlConstants.CONTROL_NAMESPACE);
		IntParameter parameter = new IntParameter();
		parameter.setName(name);
		parameter.setValue(value);
	    parameters.add(parameter);
	}

	public void addNode(NodeReference node) {
	    nodes.add(node);
	}
	
	public List<NodeReference> getNodes() {
	    return nodes;
	}

	public void add(Parameter data) {
	    parameters.add(data);
	}
	
	public int getSensorDataCount() {
	    return parameters.size();
	}
	
	public Parameter get(int index) {
	    return parameters.get(index);
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

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder buf) {
	    buf.append("<").append(SensorDataControlConstants.CONTROL_NAME).append(" xmlns='").append(SensorDataControlConstants.CONTROL_NAMESPACE).append("'");
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
	    for (Parameter parameter : parameters) {
	        buf.append(parameter .toXML());
	    }
	    buf.append("</set>");
	    return buf;		
	}
}
