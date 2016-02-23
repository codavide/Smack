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
 * $Id: SensorDataMessage.java 216 2013-12-03 12:31:57Z nfi $
 *
 * -----------------------------------------------------------------
 *
 * Main
 *
 * Authors : Marcus Arendt
 * Created : 12 jun 2013
 * Updated : $Date: 2013-12-03 13:31:57 +0100 (tis, 03 dec 2013) $
 *           $Revision: 216 $
 */

package org.jivesoftware.smackx.iot.sensordata.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;
import org.jivesoftware.smackx.iot.sensordata.util.Node;


public class SensorDataMessage implements ExtensionElement {
	private List<Node> nodes = new ArrayList<Node>();
    private int seqNo;
    private Boolean done = null;

    public SensorDataMessage() {		
    }

    public SensorDataMessage(Node node, int seqNo) {
        addNode(node);
        this.seqNo = seqNo;
    }

    @Override
    public String getElementName() {
        return SensorDataConstants.MESSAGE_NAME;
    }

    @Override
    public String getNamespace() {
        return SensorDataConstants.SENSORDATA_NAMESPACE;
    }

    public Node[] getNodes() {
        return nodes.toArray(new Node[0]);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public Boolean getDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}
    
    @Override
    public String toXML() {
        // <fields xmlns='urn:xmpp:iot:sensordata' seqnr='4'>
        StringBuilder buf = new StringBuilder();

        buf.append("<").append(getElementName());
        buf.append(" xmlns='").append(getNamespace());
        buf.append("' seqnr='").append(getSeqNo());
        if(done!=null) {
        	buf.append("' done='").append(Boolean.toString(getDone()));	
        }
        buf.append("'>");

        Node[] narr = getNodes();
        for (int i = 0; i < narr.length; i++) {
            buf.append(narr[i].toXML());
        }
        
        buf.append("</fields>");
        return buf.toString();
    }
}
