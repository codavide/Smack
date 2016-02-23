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
 * $Id: SensorDataRequest.java 96 2013-09-17 11:40:42Z marcus@arendt.se $
 *
 * -----------------------------------------------------------------
 *
 * Main
 *
 * Authors : Marcus Arendt
 * Created : 12 jun 2013
 * Updated : $Date: 2013-09-17 13:40:42 +0200 (tis, 17 sep 2013) $
 *           $Revision: 96 $
 */

package org.jivesoftware.smackx.iot.sensordata.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.control.util.NodeReference;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;

public class SensorDataRequest extends IQ {
    private int seqno=0;
    private Boolean momentary=false;
    private boolean all = false;
    private boolean historical = false;
    private String fromDate = null;
    private String toDate = null;

    private List<NodeReference> nodes = new ArrayList<NodeReference>();
    private Map<String, String> attributes = new HashMap<String,String>();
    private List<String> fields = new ArrayList<String>();
    private Map<String, String> fieldsMap = new HashMap<String, String>();

    private static HashMap<String, Integer> historicalTable;

    public SensorDataRequest() {
    	super(SensorDataConstants.REQUEST_NAME, SensorDataConstants.SENSORDATA_NAMESPACE);
        if (historicalTable == null) {
            historicalTable = new HashMap<String, Integer>();
            /* crappy hack to setup historical value resolution */
            historicalTable.put("Second", 1); /* resolution 1 */        
            historicalTable.put("Minute", 60); /* resolution 60 */
            historicalTable.put("Hour", 60 * 60); 
            historicalTable.put("Day", 24 * 60 * 60);
            historicalTable.put("Week", 7 * 24 * 60 * 60);
            historicalTable.put("Month", 30 * 24 * 60 * 60);
            /* anyone sucker requesting anything else get's the default... */
        }
        this.nodes = new ArrayList<NodeReference>();
    }

    public SensorDataRequest(int seqno) {
        this();
        this.seqno = seqno;
    }


    public int getHistoricalResolution() {
        String[] atts = getAttributeNames();
        /* this should probably be handled earlier??? - likely in the message object
         * itself - refactor this!!!!
         */
        /* in seconds */
        int historicalResolution = 0;
        boolean resolutionSet = false;
        
        for(int i = 0; i < atts.length; i++) {
            String v = getAttribute(atts[i]);
            if (atts[i].startsWith("historical")) {
                if (v.equals("true")) {
                    String res = atts[i].substring("historical".length());
                    if (res != null) {
                        Integer rI = historicalTable.get(res);
                        if (rI != null) {
                            resolutionSet = true;
                            if (rI < historicalResolution) {
                                historicalResolution = rI;
                            }
                        }
                    }
                    if (!resolutionSet) {
                        historicalResolution = 60 * 60; /* default is hour */
                    }
                }
            }
        }
        return historicalResolution;
    }
    
    public int getSeqno() {
        return seqno;
    }

    public void setSeqNo(int seqno) {
        this.seqno = seqno;
    }

    public Boolean getMomentary() {
        return momentary;
    }

    public void setMomentary(Boolean momentary) {
        this.momentary = momentary;
    }

    public boolean getAll() {
        return all;
    }
    
    public void setAll(boolean all) {
        this.all = all;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String value) {
        fromDate = value;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String value) {
        toDate = value;
    }

    public boolean getHistorical() {
        return historical;
    }

    public void setHistorical(boolean value) {
        historical = value;
    }

    public void addNode(NodeReference node) {
        nodes.add(node);
    }

    public List<NodeReference> getNodes() {
        return this.nodes;
    }

    public boolean checkField(String name) {
        /* if no fields specified - all is ok */
        if (fields.size() == 0) return true;
        return fieldsMap.get(name) != null;
    }
    
    public String[] getFieldNames() {
        return (String[]) fields.toArray(new String[0]);
    }

    public String[] getAttributeNames() {
        return (String[]) attributes.keySet().toArray(new String[0]);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public void addField(String name) {
        fields.add(name);
        fieldsMap.put(name, name);
    }

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder buf) {
        buf.append("<"+SensorDataConstants.REQUEST_NAME+" xmlns='"+SensorDataConstants.SENSORDATA_NAMESPACE+"' seqnr='");
        buf.append(String.valueOf(seqno));
        buf.append("' momentary='");
        buf.append(Boolean.toString(momentary));
        
        String[] atts = getAttributeNames();
        for (String attribute : atts) {
            buf.append("' " + attribute + "='");
            buf.append(getAttribute(attribute));
        }
        
        if ((nodes == null || nodes.size() == 0) &&
                (fields == null || fields.size() == 0)) {
            buf.append("'/>");
        } else {
            /* first nodes and then fields */
            buf.append("'>");
            for (int i=0; i<nodes.size(); i++) {
                buf.append(nodes.get(i).toXML());
            }        		
            for (int i = 0; i < fields.size(); i++) {
                buf.append("<field name='" + fields.get(i) + "'/>");
            }
            buf.append("</req>");
        }

        return buf;
	}

    /*
	 <iq type='get'
       from='master@clayster.com/amr'
       to='device@clayster.com'
       id='1'>
      <req xmlns='urn:xmpp:iot:sensordata' seqnr='1' momentary='true'/>
   </iq>
     */

}