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
 * $Id: SensorDataMessageProvider.java 98 2013-09-26 07:56:26Z marcus@arendt.se $
 *
 * -----------------------------------------------------------------
 *
 * Main
 *
 * Authors : Marcus Arendt
 * Created : 12 jun 2013
 * Updated : $Date: 2013-09-26 09:56:26 +0200 (tor, 26 sep 2013) $
 *           $Revision: 98 $
 */

package org.jivesoftware.smackx.iot.sensordata.provider;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataMessage;
import org.jivesoftware.smackx.iot.sensordata.util.Node;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataValue;
import org.jivesoftware.smackx.iot.sensordata.util.TimestampType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SensorDataMessageProvider extends ExtensionElementProvider<SensorDataMessage> {

	@Override
	public SensorDataMessage parse(XmlPullParser parser, int arg1)
			throws XmlPullParserException, IOException, SmackException {
        SensorDataMessage rom = new SensorDataMessage();
        /* create default node and sensor data list */
        Node n = new Node();
        TimestampType ts = new TimestampType();

        rom.addNode(n);
        n.addTimestamp(ts);
        
        boolean done = false;
        int eventType = parser.getEventType();
        while (!done) {
            if (eventType == XmlPullParser.START_TAG) {
                if(parser.getName().equals("fields")) {
                    rom.setSeqNo(Integer.parseInt(parser.getAttributeValue("", "seqnr")));
                    rom.setDone(checkBooleanAttribute(parser, "done"));
                } else if (parser.getName().equals("node")) {					
                    n.setNodeId(parser.getAttributeValue("", "nodeId"));				
                } else if (parser.getName().equals("timestamp")) {					
                    ts.setTimestamp(parser.getAttributeValue("", "value"));				
                } else if (parser.getName().equals("boolean")) {
                	SensorDataValue bool = buildSensorDataValue(parser, "boolean");
                    ts.addSensorDataValue(bool);
                } else if (parser.getName().equals("date")) {
                	SensorDataValue date = buildSensorDataValue(parser, "date");
                    ts.addSensorDataValue(date);
                } else if (parser.getName().equals("dateTime")) {
                	SensorDataValue date = buildSensorDataValue(parser, "dateTime");
                    ts.addSensorDataValue(date);
                } else if (parser.getName().equals("duration")) {
                	SensorDataValue date = buildSensorDataValue(parser, "duration");
                    ts.addSensorDataValue(date);
                } else if (parser.getName().equals("enum")) {
                	SensorDataValue enumV = buildSensorDataValue(parser, "enum");
                    ts.addSensorDataValue(enumV);
                } else if (parser.getName().equals("int")) {
                	SensorDataValue intV = buildSensorDataValue(parser, "int");
                    ts.addSensorDataValue(intV);
                } else if (parser.getName().equals("long")) {
                	SensorDataValue longV = buildSensorDataValue(parser, "long");
                    ts.addSensorDataValue(longV);
                } else if (parser.getName().equals("numeric")) {
                    SensorDataValue num = new SensorDataValue();
                    num.setType("numeric");
                    num.setName(parser.getAttributeValue("", "name"));
                    num.setMomentary(checkBooleanAttribute(parser, "momentary"));
                    num.setAutomaticReadout(checkBooleanAttribute(parser, "automaticReadout"));
                    num.setValue(parser.getAttributeValue("", "value"));
                    num.setUnit(parser.getAttributeValue("", "unit"));
                    ts.addSensorDataValue(num);
                } else if (parser.getName().equals("string")) {
                	SensorDataValue stringV = buildSensorDataValue(parser, "string");
                    ts.addSensorDataValue(stringV);
                }  else if (parser.getName().equals("time")) {
                	SensorDataValue time = buildSensorDataValue(parser, "time");
                    ts.addSensorDataValue(time);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("fields")) {
                    done = true;
                }
            }
            if (!done) {
                eventType = parser.next();
            }
        }
        return rom;				
	}

	private SensorDataValue buildSensorDataValue(XmlPullParser parser, String type) {
		SensorDataValue sdValue = new SensorDataValue();
		sdValue.setType(type);
		sdValue.setName(parser.getAttributeValue("", "name"));
		sdValue.setMomentary(checkBooleanAttribute(parser, "momentary"));
		sdValue.setAutomaticReadout(checkBooleanAttribute(parser, "automaticReadout"));
		sdValue.setValue(parser.getAttributeValue("", "value"));
		int c = parser.getAttributeCount();
		for (int i = 0; i < c; i++) {
			String name = parser.getAttributeName(i);
			String value = parser.getAttributeValue(i);
			sdValue.setAttribute(name, value);
		}
		return sdValue;
	}
	
	private Boolean checkBooleanAttribute(XmlPullParser parser, String attributeName) {
        return parser.getAttributeValue("", attributeName)!=null ? Boolean.parseBoolean(parser.getAttributeValue("", attributeName)) : null;		
	}
}


/*
<fields xmlns='urn:xmpp:iot:sensordata' seqnr='1'>'
	<node nodeId='Device01'>
		<timestamp value='2013-06-05T12:48:00'>
			<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='C'/>
		</timestamp>
	</node>
</fields>
 */