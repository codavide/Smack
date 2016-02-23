package org.jivesoftware.smackx.iot.sensordata.provider;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataStarted;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SensorDataStartedProvider extends ExtensionElementProvider<SensorDataStarted> {

	@Override
	public SensorDataStarted parse(XmlPullParser parser, int arg1)
			throws XmlPullParserException, IOException, SmackException {
		SensorDataStarted sd = null;

		int eventType = parser.getEventType();

		while (eventType != XmlPullParser.END_TAG) {
			if ((eventType == XmlPullParser.START_TAG) &&  parser.getName().equals("started")) {
				sd = new SensorDataStarted(Integer.parseInt(parser.getAttributeValue("", "seqnr")));
			}
			eventType = parser.next();
		}
		return sd;
	}
}
