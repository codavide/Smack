package org.jivesoftware.smackx.iot.sensordata.util;

import java.util.HashMap;
import java.util.Map;

public class SensorDataValue {
    private String type;
    private String name;
    private boolean momentary;
    private boolean automaticReadout;
    private String value;
    private String unit;

    private Map<String, String> attributes = new HashMap<String,String>();
    
    /* We assume that this is an identity data if unit == null && it is
     * not momentary and automaticReadout - should be flagged explicitly
     * later.
     */
    public SensorDataValue(String type, String name, java.lang.Boolean momentary,
    		java.lang.Boolean automaticReadout, String value, String unit) {
        this.name = name;
        this.momentary = momentary;
        this.automaticReadout = automaticReadout;
        this.value = value;
        this.unit = unit;
        this.type = type;
    }

    public SensorDataValue() {
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public java.lang.Boolean getMomentary() {
        return momentary;
    }
    public void setMomentary(java.lang.Boolean momentary) {
        this.momentary = momentary;
    }
    public java.lang.Boolean getAutomaticReadout() {
        return automaticReadout;
    }
    public void setAutomaticReadout(java.lang.Boolean automaticReadout) {
        this.automaticReadout = automaticReadout;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setType(String tval) {
        type = tval;
    }

    public String[] getAttributeNames() {
        return attributes.keySet().toArray(new String[0]);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(type).append(" name='");
        buf.append(getName());

        /* HACK for indicating "identity" flag */
        if (!momentary && !automaticReadout && unit == null) {
            buf.append("' identity='true");
        } else {
            /* no flags sent as false - seems like a waste of bandwidth */
            if (getMomentary()) {
                buf.append("' momentary='true");
            }
            if (getAutomaticReadout()) {
                buf.append("' automaticReadout='true");
            }
            
            String[] atts = getAttributeNames();
            for (String attribute : atts) {
                buf.append("' " + attribute + "='");
                buf.append(getAttribute(attribute));
            }
        }
        buf.append("' value='");
        buf.append(getValue());
        if (unit != null) {
            buf.append("' unit='");
            buf.append(getUnit());
        }
        buf.append("'/>");

        return buf.toString();
    }
    // <numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='C'/>
}
