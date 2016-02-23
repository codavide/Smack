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
 * $Id: SensorDataAccepted.java 38 2013-06-28 09:20:44Z marcus@arendt.se $
 *
 * -----------------------------------------------------------------
 *
 * Main
 *
 * Authors : Marcus Arendt
 * Created : 12 jun 2013
 * Updated : $Date: 2013-06-28 11:20:44 +0200 (fre, 28 jun 2013) $
 *           $Revision: 38 $
 */

package org.jivesoftware.smackx.iot.control.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.control.util.NodeReference;
import org.jivesoftware.smackx.iot.control.util.Parameter;
import org.jivesoftware.smackx.iot.control.util.SensorDataControlConstants;

public class SensorDataControlResponse extends IQ {
    private List<NodeReference> nodes = new ArrayList<NodeReference>();
    private List<Parameter> parameters = new ArrayList<Parameter>();
    
    /* create a reply */
    public SensorDataControlResponse(IQ message) {
    	super(message);
        //this.setFrom(message.getTo());
        this.setTo(message.getFrom());
        this.setType(IQ.Type.result);
    }

    List<NodeReference> getNodes() {
    	return nodes;
    }
    
    List<Parameter> getParamaters() {
    	return parameters;
    }

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder buf) {
        buf.append("<"+SensorDataControlConstants.RESPONCE_NAME+" xmlns='"+SensorDataControlConstants.CONTROL_NAMESPACE+"'>");
        for(NodeReference node : nodes) {
        	buf.append(node.toXML());
        }
        for(Parameter parameter : parameters) {
        	buf.append(parameter.toXML());
        }
        buf.append("</").append(SensorDataControlConstants.RESPONCE_NAME).append(">");
        return buf;
	}
}


