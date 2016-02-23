package org.jivesoftware.smackx.iot.sensordata.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimestampType {

    private String timestamp;
    private List<SensorDataValue> values = new ArrayList<SensorDataValue>();

    public TimestampType(long time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date(time);
        this.timestamp = df.format(date);
    }

    public TimestampType() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        this.timestamp = df.format(date);
    }

    public int getSensorDataValueCount() {
        return values.size();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void addSensorDataValue(SensorDataValue n) {
        synchronized (values) {
            values.add(n);
        }
    }		

    public List<SensorDataValue> getSensorDatas() {
        return values;
    }

    public String toXML() {

        StringBuilder buf = new StringBuilder();

        buf.append("<timestamp value='").append(getTimestamp()).append("'>");
        synchronized (values) {
            for (int i = 0; i < values.size(); i++) {
                SensorDataValue n = values.get(i);
                buf.append(n.toXML());
            }
        }
        buf.append("</timestamp>");
        return buf.toString();
    }
}
