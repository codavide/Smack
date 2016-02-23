package org.jivesoftware.smackx.iot.sensordata.provider;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataCancel;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SensorDataCancelProvider extends IQProvider<SensorDataCancel> {

	@Override
	public SensorDataCancel parse(XmlPullParser parser, int arg1)
			throws XmlPullParserException, IOException, SmackException {
		SensorDataCancel rd = null;

		int eventType = parser.getEventType();

		while (eventType != XmlPullParser.END_TAG) {
			if ((eventType == XmlPullParser.START_TAG) &&  parser.getName().equals("cancel")) {
				rd = new SensorDataCancel(Integer.parseInt(parser.getAttributeValue("", "seqnr")));				
			}
			eventType = parser.next();
		}
		return rd;
	}

}
