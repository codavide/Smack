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
 * $Id: SensorDataFailureProvider.java 38 2013-06-28 09:20:44Z marcus@arendt.se $
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

package org.jivesoftware.smackx.iot.sensordata.provider;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smackx.iot.control.util.NodeReference;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataFailure;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataRequest;
import org.jivesoftware.smackx.iot.sensordata.util.Error;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SensorDataFailureProvider extends ExtensionElementProvider<SensorDataFailure> {

	@Override
	public SensorDataFailure parse(XmlPullParser parser, int arg1)
			throws XmlPullParserException, IOException, SmackException {
		SensorDataFailure df = null;
		
		 int eventType = parser.getEventType();
	        /* End only when we are at end with the failure tag */
	        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("failure"))) {
	            
	            if ((eventType == XmlPullParser.START_TAG)) {
	            	if(parser.getName().equals("failure")) {
	                    int seqnr = Integer.parseInt(parser.getAttributeValue("", "seqnr"));
	                    df = new SensorDataFailure(seqnr);
	                    df.setDone(checkBooleanAttribute(parser, "done"));
	                } else if(parser.getName().equals("error")) {
	                	Error error = new Error();
	                	error.setNodeId(parser.getAttributeValue("", "nodeId"));
	                	error.setTimestamp(parser.getAttributeValue("", "timestamp"));
	                	error.setSourceId(parser.getAttributeValue("", "sourceId"));
	                	error.setCacheType(parser.getAttributeValue("", "cacheType"));
	                	df.getErrors().add(error);
	                }
	                eventType = parser.next(); 
	            }
	        }
	        return df;
	}

	private Boolean checkBooleanAttribute(XmlPullParser parser, String attributeName) {
        return parser.getAttributeValue("", attributeName)!=null ? Boolean.parseBoolean(parser.getAttributeValue("", attributeName)) : null;		
	}
}
