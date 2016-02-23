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
 * $Id: SensorDataControlProvider.java 136 2013-10-07 10:51:21Z marcus@arendt.se $
 *
 * -----------------------------------------------------------------
 *
 * Main
 *
 * Authors : Marcus Arendt
 * Created : 12 jun 2013
 * Updated : $Date: 2013-10-07 12:51:21 +0200 (mån, 07 okt 2013) $
 *           $Revision: 136 $
 */

package org.jivesoftware.smackx.iot.control.provider;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.iot.control.packet.SensorDataControl;
import org.jivesoftware.smackx.iot.control.util.BooleanParameter;
import org.jivesoftware.smackx.iot.control.util.ColorParameter;
import org.jivesoftware.smackx.iot.control.util.DateParameter;
import org.jivesoftware.smackx.iot.control.util.DateTimeParameter;
import org.jivesoftware.smackx.iot.control.util.DoubleParameter;
import org.jivesoftware.smackx.iot.control.util.DurationParameter;
import org.jivesoftware.smackx.iot.control.util.IntParameter;
import org.jivesoftware.smackx.iot.control.util.LongParameter;
import org.jivesoftware.smackx.iot.control.util.NodeReference;
import org.jivesoftware.smackx.iot.control.util.StringParameter;
import org.jivesoftware.smackx.iot.control.util.TimeParameter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SensorDataControlProvider extends IQProvider<SensorDataControl> {

	private static final String CACHE_TYPE_ATTRIBUTE = "cacheType";
	private static final String SOURCE_ID_ATTRIBUTE = "sourceId";
	private static final String VALUE_ATTRIBUTE = "value";
	private static final String NAME_ATTRIBUTE = "name";

	@Override
	public SensorDataControl parse(XmlPullParser parser, int arg1)
			throws XmlPullParserException, IOException, SmackException {
        SensorDataControl sdc = new SensorDataControl();

        boolean done = false;
        int eventType = parser.getEventType();

        while (!done) {
            if (eventType == XmlPullParser.START_TAG) {
                System.out.println("START TAG: " + parser.getName());
                if (parser.getName().equals("node")) {
                	NodeReference node = new NodeReference(parser.getAttributeValue("", "nodeId"));
                    System.out.println(">>>>>> ADDING NODE: " + node.getNodeId());
                    if(parser.getAttributeValue("", SOURCE_ID_ATTRIBUTE) != null) {
                    	node.setSourceId(parser.getAttributeValue("", SOURCE_ID_ATTRIBUTE));
                    }
                    if(parser.getAttributeValue("", CACHE_TYPE_ATTRIBUTE) != null) {
                    	node.setSourceId(parser.getAttributeValue("", CACHE_TYPE_ATTRIBUTE));
                    }
                    sdc.addNode(node);
                }
                if (parser.getName().equals("boolean")) {
                    BooleanParameter b =
                            new BooleanParameter();
            		b.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		b.setValue(Boolean.valueOf(parser.getAttributeValue("", VALUE_ATTRIBUTE)));
            		sdc.add(b);
                }
                if (parser.getName().equals("color")) {
                    ColorParameter c =
                            new ColorParameter();
                    c.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		c.setValue(parser.getAttributeValue("", VALUE_ATTRIBUTE));
            		sdc.add(c);
                }
                if (parser.getName().equals("date")) {
                    DateParameter d =
                            new DateParameter();
            		d.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd");
            		Date date;
					try {
						date = parserSDF.parse(parser.getAttributeValue("", VALUE_ATTRIBUTE));
						GregorianCalendar c = new GregorianCalendar();
						c.setTime(date);
						XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
						d.setValue(date2);
						sdc.add(d);
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (DatatypeConfigurationException e) {
						e.printStackTrace();
					}
                }
                if (parser.getName().equals("dateTime")) {
                    DateTimeParameter d =
                            new DateTimeParameter();
            		d.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            		Date date;
					try {
						date = parserSDF.parse(parser.getAttributeValue("", VALUE_ATTRIBUTE));
						GregorianCalendar c = new GregorianCalendar();
						c.setTime(date);
						XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
						d.setValue(date2);
						sdc.add(d);
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (DatatypeConfigurationException e) {
						e.printStackTrace();
					}
                }
                if (parser.getName().equals("double")) {
                    DoubleParameter d =
                            new DoubleParameter();
            		d.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		d.setValue(Double.valueOf(parser.getAttributeValue("", VALUE_ATTRIBUTE)));
            		sdc.add(d);
                }
                if (parser.getName().equals("duration")) {
                    DurationParameter d =
                            new DurationParameter();
            		d.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		Duration duration = null;
            		try {
            			duration=DatatypeFactory.newInstance().newDuration(parser.getAttributeValue("", VALUE_ATTRIBUTE));
            		}
            		catch (  DatatypeConfigurationException e) {
            		}
            		d.setValue(duration);
            		sdc.add(d);
                }
                if (parser.getName().equals("int")) {
                    IntParameter i =
                            new IntParameter();
            		i.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		i.setValue(Integer.valueOf(parser.getAttributeValue("", VALUE_ATTRIBUTE)));
            		sdc.add(i);
                }
                if (parser.getName().equals("long")) {
                    LongParameter l =
                            new LongParameter();
            		l.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		l.setValue(Long.valueOf(parser.getAttributeValue("", VALUE_ATTRIBUTE)));
            		sdc.add(l);
                }
                if (parser.getName().equals("string")) {
                    StringParameter s =
                            new StringParameter();
            		s.setName(parser.getAttributeValue("", NAME_ATTRIBUTE));
            		s.setValue(parser.getAttributeValue("", VALUE_ATTRIBUTE));
            		sdc.add(s);
                }
                if (parser.getName().equals("time")) {
                    TimeParameter t =
                            new TimeParameter();
            		SimpleDateFormat parserSDF = new SimpleDateFormat("HH:mm:ss");
            		Date date;
					try {
						date = parserSDF.parse(parser.getAttributeValue("", VALUE_ATTRIBUTE));
						GregorianCalendar c = new GregorianCalendar();
						c.setTime(date);
						XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
						t.setValue(date2);
						sdc.add(t);
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (DatatypeConfigurationException e) {
						e.printStackTrace();
					}

                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("set")) {
                    done = true;
                }
            }
            if (!done) {
                eventType = parser.next();
            }
        }
        return sdc;	
     }
}
