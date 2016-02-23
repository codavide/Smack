package org.jivesoftware.smackx.iot.sensordata.provider;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataDone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SensorDataDoneProvider extends ExtensionElementProvider<SensorDataDone> {

	@Override
	public SensorDataDone parse(XmlPullParser parser, int arg1) throws XmlPullParserException, IOException, SmackException {
		int seqNo = 0;
		
		int eventType = parser.getEventType();

		while (eventType != XmlPullParser.END_TAG) {
			if ((eventType == XmlPullParser.START_TAG) &&  parser.getName().equals("done")) {
				seqNo = Integer.parseInt(parser.getAttributeValue("", "seqnr"));				
			}
			eventType = parser.next();
		}

		return new SensorDataDone(seqNo);		
	}
	

/*
  <message from='device@clayster.com'
            to='master@clayster.com/amr'>
      <done xmlns='urn:xmpp:iot:sensordata' seqnr='4'/>
   </message>
 */
}
