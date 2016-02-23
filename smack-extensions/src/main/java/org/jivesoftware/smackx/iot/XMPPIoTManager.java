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
 * $Id: SensorDataManager.java 216 2013-12-03 12:31:57Z nfi $
 *
 * -----------------------------------------------------------------
 *
 * SensorDataManager - handle XEP 323 (parts of so far) for reading
 * out sensor data over XMPP
 * 
 * Specify which sensor data should be read per JID in the config file.
 * Multiple nodes can be specified and multiple values / field per node.
 *
 * Authors : Marcus Arendt, Joakim Eriksson
 * Created : 12 jun 2013
 * Updated : $Date: 2013-12-03 13:31:57 +0100 (tis, 03 dec 2013) $
 *           $Revision: 216 $
 */

package org.jivesoftware.smackx.iot;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.iot.control.packet.SensorDataControl;
import org.jivesoftware.smackx.iot.control.packet.SensorDataControlResponse;
import org.jivesoftware.smackx.iot.control.provider.SensorDataControlProvider;
import org.jivesoftware.smackx.iot.control.util.SensorDataControlConstants;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataAccepted;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataDone;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataMessage;
import org.jivesoftware.smackx.iot.sensordata.packet.SensorDataRequest;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataAcceptedProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataCancelProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataCancelledProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataDoneProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataFailureProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataMessageProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataRequestProvider;
import org.jivesoftware.smackx.iot.sensordata.provider.SensorDataStartedProvider;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;
import org.jxmpp.util.XmppStringUtils;
import org.json.JSONArray;

public class XMPPIoTManager {

    private static final Logger log = LoggerFactory.getLogger(XMPPIoTManager.class);

    private ServiceDiscoveryManager discoManager;
    private XMPPConnection connection;
    private StanzaListener stanzaListener;
    private String name;
    private int seqNo=1;
    private Map<String, String> requestedJid = new HashMap<String, String>();
    private String[] matches;
    
    private static Map<XMPPConnection, XMPPIoTManager> instances = new WeakHashMap<>();

    
    /**
     * Returns the XMPPIoTManager instance associated with a given XMPPConnection.
     * 
     * @param connection the connection used to look for the proper ServiceDiscoveryManager.
     * @param name to be used
     * 
     * @return the XMPPIoTManager associated with a given XMPPConnection.
     */
    public static synchronized XMPPIoTManager getInstanceFor(XMPPConnection connection, String name) {
    	XMPPIoTManager sdm = instances.get(connection);
        if (sdm == null) {
            sdm = new XMPPIoTManager(connection,  name);
            // Register the new instance and associate it with the connection
            instances.put(connection, sdm);
        }
        return sdm;
    }
    
    private XMPPIoTManager(XMPPConnection conn,  String name) {
        this.connection = conn;
        this.name = name;
        init();
    }


    private void init() {
        log.debug("%%%%%%%%%%% XMPPIoTManager.init");
        // IQ XEP-0323
        ProviderManager.addIQProvider(SensorDataConstants.ACCEPT_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataAcceptedProvider());
        ProviderManager.addIQProvider(SensorDataConstants.CANCEL_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataCancelProvider());
        ProviderManager.addIQProvider(SensorDataConstants.CANCELLED_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataCancelledProvider());
        ProviderManager.addIQProvider(SensorDataConstants.REQUEST_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataRequestProvider());

        // Messages XEP-0323
        ProviderManager.addExtensionProvider(SensorDataConstants.DONE_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataDoneProvider());
        ProviderManager.addExtensionProvider(SensorDataConstants.FAILURE_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataFailureProvider());
        ProviderManager.addExtensionProvider(SensorDataConstants.MESSAGE_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataMessageProvider());
        ProviderManager.addExtensionProvider(SensorDataConstants.STARTED_NAME, SensorDataConstants.SENSORDATA_NAMESPACE, 
                new SensorDataStartedProvider());

        // IQ XEP-0325
        ProviderManager.addIQProvider(SensorDataControlConstants.CONTROL_NAME, SensorDataControlConstants.CONTROL_NAMESPACE, 
                new SensorDataControlProvider());
        ProviderManager.addIQProvider(SensorDataControlConstants.GET_FORM_NAME, SensorDataControlConstants.CONTROL_NAMESPACE, 
                new SensorDataControlProvider());
        ProviderManager.addIQProvider(SensorDataControlConstants.RESPONCE_NAME, SensorDataControlConstants.CONTROL_NAMESPACE, 
                new SensorDataControlProvider());

        discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
        discoManager.addFeature(SensorDataConstants.SENSORDATA_NAMESPACE);
        discoManager.addFeature("urn:xmpp:iot:control");

        stanzaListener = new StanzaListener() {
			@Override
			public void processPacket(Stanza packet) throws NotConnectedException {
                if (log.isDebugEnabled()) {
                    log.debug("################# processPacket called packet={}", packet.toXML());
                }

                try {
                    if (packet instanceof IQ) {
                        IQ iq = (IQ) packet;
                        handleSensorIQMessage(iq);
                    } else if (packet instanceof Message) {
                        Message m = (Message) packet;
                        handleSensorMessage(m);
                    } else if (packet instanceof Presence) {
                        Presence p = (Presence) packet;
                        if (p.getType() == Presence.Type.available) {
                            String bareJid = p.getFrom().substring(0, p.getFrom().indexOf('/')); 
                            log.debug("Got presence message available from jid={} bareJid={}", p.getFrom(), bareJid);
                        }
                        if (p.getType() == Presence.Type.unavailable) {
                            String bareJid = p.getFrom().substring(0, p.getFrom().indexOf('/')); 
                            log.debug("Got presence message unavailable from jid={} bareJid={}", p.getFrom(), bareJid);
                        }
                        if (p.getType() == Presence.Type.subscribe) {
                            System.out.println("**** Got presence subscription request!!!");
                            /* Check matches */
                            if (matches != null) {
                                boolean match = false;
                                String sender = XmppStringUtils.parseBareJid(p.getFrom());
                                for (int i = 0; i < matches.length; i++) {
                                    if (sender.matches(matches[i])) {
                                        System.out.println("FOUND MATCH - ACCEPT");

                                        Presence response = new Presence(Presence.Type.subscribed);
                                        response.setTo(p.getFrom());
                                        connection.sendStanza(response);
                                        
                                        response = new Presence(Presence.Type.subscribe);
                                        response.setTo(p.getFrom());
                                        connection.sendStanza(response);

                                        match = true;
                                        break;
                                    }
                                }
                                if (!match) {
                                    /* no match => reject */
                                    System.out.println("NO MATCH - REJECTING");
                                    Presence response = new Presence(Presence.Type.unsubscribed);
                                    response.setTo(p.getFrom());
                                    connection.sendStanza(response);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("failed to handle message", e);
                }
            }
        };
        connection.addAsyncStanzaListener(stanzaListener, null);
    }

    private void  handleSensorMessage(Message message) {
        SensorDataMessage rom = (SensorDataMessage) message.getExtension("fields", SensorDataConstants.SENSORDATA_NAMESPACE);
        if (rom != null) {
            handleSensorDataMessage(rom, message);
            return;
        }
        SensorDataDone sdd = (SensorDataDone) message.getExtension("done", SensorDataConstants.SENSORDATA_NAMESPACE);
        if (sdd != null) {
            handleDone(sdd, message);
//            log.debug("Got done message on seqno={}", sdd.getSeqNo());
            return;
        }
    }

    private void handleDone(SensorDataDone sdd, Message message) {
        String key = requestedJid.remove("" + sdd.getSeqNo());
        if (key != null) {
            log.debug("Setting done for key: " + key);
            ruleEngine.setKB(key + ".done", System.currentTimeMillis());
        } else {
            log.debug("No key to set done for with seqno: " + sdd.getSeqNo());
        }
        
    }


    /* Quick hack to replace jid name to valid name */
    private String makeSafeName(String jid) {
        return jid.replace("-", "_");
    }
    private void handleSensorDataMessage(SensorDataMessage m, Message message) {

        Node node = m.getNodes()[0];
        /* FIX THIS!!!! - this handles only the first node */
        String jidName = requestedJid.get("" + m.getSeqNo());
        String prefix = jidName + "." + node.getNodeId() + ".";
        /* FIX THIS!!!! - this handles only the first timestamp!!!!*/
        ruleEngine.setKB(prefix + "timestamp", node.getTimestamp().getTimestamp());
        List<SensorDataMessage.SensorDataValue> n = node.getTimestamp().getSensorDatas();
        StringBuilder names = new StringBuilder();
        for(int i=0;i<n.size();i++) {
            SensorDataValue n1 = n.get(i);
            log.debug("Setting data in KB: " + prefix + n1.getName() + " to " + n1.getValue());
            if ("numeric".equals(n1.getType())) {
                try {
                    double dv = Double.parseDouble(n1.getValue());
                    ruleEngine.setKB(prefix + n1.getName() + ".value", dv);
                } catch(Exception e) {
                    log.debug("Exception while parsing double", e);
                }
            } else {
                ruleEngine.setKB(prefix + n1.getName() + ".value", n1.getValue());
            }
            ruleEngine.setKB(prefix + n1.getName() + ".unit", n1.getUnit());
            if (i > 0) {
                names.append(' ');
            }
            names.append(n1.getName());
        }
        /* set nodes and sensors list here */
        ruleEngine.setKB(prefix + "sensors", names.toString().trim());
        String nodes = (String) ruleEngine.getKB(jidName + ".nodes", "");
        nodes = (nodes.length() == 0 ? "" : nodes + "," ) + node.getNodeId();
        // create a nodes list that can be used for creating history
        ruleEngine.setKB(jidName + ".nodes", nodes);
    }

    private String getName(String cname) {
        int i = cname.indexOf(',');
        if (i >= 0) {
            return cname.substring(0, i);
        } else {
            /* no ',' in the string */
            return cname;
        }
    }
    
    private String getType(String cname) {
        int i = cname.indexOf(',');
        if (i >= 0) {
            return cname.substring(i);
        } else {
            /* no ',' in the string => numeric is default */
            return "numeric";
        }
    }    
    private String checkWrite(String key, ArrayList<String> nodes) {
        if (nodes.size() > 0 ) {
            for (String node : nodes) {
                String nm = name + "." + node + ".control." + key;
                String cname = (String) ruleEngine.getKB(nm);
                log.debug("##==-- Checking: {} => {}", nm, cname);
                if (cname != null) {
                    return cname;
                }
            }
        } else {
            /* no node - pick from name.control */
            String nm = name + ".control." + key;
            String cname = (String) ruleEngine.getKB(nm);
            log.debug("##==-- Checking: {} => {}", nm, cname);
            return cname;
        }
        return null;
    }

    private void handleSensorDataControlMessage(SensorDataControl m, IQ message) {
        String error = "";
        int count = m.getSensorDataCount();
        for (int i = 0; i < count; i++) {
            SensorDataControl.Parameter sd = m.get(i);
            if(sd instanceof SensorDataControl.SensorDataInt) {
                SensorDataControl.SensorDataInt sdi = (SensorDataControl.SensorDataInt) sd;
                log.debug("Setting Numeric: {} -> {}", sdi.getName(), sdi.getValue());

                /* NOTE: this only returns one key - the first - FIX THIS! */
                String key = checkWrite(sdi.getName(), m.getNodes());
                if (key != null) {
                    /* remove the type */
                    key = getName(key);
                    log.debug("Setting INT in KB {} = {}", key, sdi.getValue());
                    ruleEngine.setKB(key, sdi.getValue());
                } else {
                    error += "No parameter: " + sdi.getName() + "\n";
                }
            }
            if(sd instanceof SensorDataControl.SensorDataBoolean) {
                SensorDataControl.SensorDataBoolean sdb = (SensorDataControl.SensorDataBoolean) sd;
                String key = checkWrite(sdb.getName(), m.getNodes());
                if (key != null) {
                    /* remove the type */
                    key = getName(key);
                    log.debug("Setting INT in KB {} = {}", key, sdb.getValue());
                    ruleEngine.setKB(key, sdb.getValue());
                } else {
                    error += "No parameter: " + sdb.getName() + "\n";
                }
            }
        }

        /* produce a IQ result message */        
        SensorDataControlResponse response = new SensorDataControlResponse(message);
        if (error.equals("")) {
            response.setResponse(SensorDataControlResponse.OK);
        } else {
            response.setResponse(SensorDataControlResponse.OTHER_ERROR, error);
        }
        //        if (log.isDebugEnabled()) {
        //            log.debug("------------------");
        //            log.debug("Sending response: {}", response.toXML());
        //            log.debug("------------------");
        //        }
        connection.sendStanza(response);
    }

    private void handleSensorIQMessage(IQ iq) {	
        if ((iq instanceof SensorDataRequest) && (iq.getType() == IQ.Type.get)) {
            SensorDataRequest ro = (SensorDataRequest) iq;
            if (log.isDebugEnabled()) {
                log.debug("request: {}", ro.toXML());
            }

            ArrayList<ReadOutMessage.Node> n = ro.getNodes();

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < n.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(n.get(i).getNodeId());
            }
            String nodeString = sb.toString();
            log.debug("Request contained nodes: {}", nodeString);

            SensorDataAccepted roa = new SensorDataAccepted(ro.getSeqno());
            roa.setType(IQ.Type.result);
            roa.setStanzaId(ro.getStanzaId());	
            roa.setFrom(ro.getTo());
            roa.setTo(ro.getFrom());

            //Save state info in KB using transaction id as key?

            if (log.isDebugEnabled()) {
                log.debug("response: {}", roa.toXML());
            }
            connection.sendStanza(roa);

            /* patch to nothing if node happens to be null */
            if (nodeString.equals("null")) {
                nodeString = "";
            }
            /* if nothing was specified readout all nodes */
            if (nodeString.length() == 0) {
                nodeString = ruleEngine.getKB(String.class, name + ".node", "");
                log.debug("Got nodes from KB: {}.node -> {}", name, nodeString);
            }
            String att = ro.getAttribute("momentary");
            boolean momentary = att != null && att.equals("true");
            att = ro.getAttribute("all");
            boolean all = att != null && att.equals("true");
            momentary |= all;

            log.debug("Using nodes: '{}'", nodeString);
            String[] nodes = nodeString.split(",");

            /* send all the nodes */
            for (String nodeName : nodes) {
                if (momentary) {
                    /* send all the nodes as separate messages... */
                    Message m = new Message();
                    m.setTo(ro.getFrom());

                    m.setFrom(ro.getTo());
                    m.setStanzaId(ro.getStanzaId());

                    SensorDataMessage sdm = createReadOutMessage(nodeName, ro);
                    if (sdm != null) {
                        m.addExtension(sdm);
                        if (log.isDebugEnabled()) {
                            log.debug("ReadOutMessage: {}", m.toXML());
                        }
                        connection.sendStanza(m);
                    }
                }

                /* check for historical data to send */
                if (ro.getHistoricalResolution() > 0 || all) {
                    /* we have history! - need to send data for this node here! */
                    SensorDataMessage sdm = createSensorHistory(nodeName, ro);
                    if (sdm != null) {
                        Message m = new Message();
                        m.setTo(ro.getFrom());
                        m.setFrom(ro.getTo());
                        m.setStanzaId(ro.getStanzaId());

                        m.addExtension(sdm);
                        if (log.isDebugEnabled()) {
                            log.debug("HistoryMessage: {}", m.toXML());
                        }
                        connection.sendStanza(m);
                    }
                }
            }

            Message m2 = new Message();
            m2.setTo(ro.getFrom());

            m2.setFrom(ro.getTo());
            m2.setStanzaId(ro.getStanzaId());

            SensorDataDone sdd = new SensorDataDone(ro.getSeqno());
            m2.addExtension(sdd);
            if (log.isDebugEnabled()) {
                log.debug("SensorDataDone: {}", m2.toXML());
            }
            connection.sendStanza(m2);

            return;
        } else if ((iq instanceof SensorDataAccepted) && (iq.getType() == IQ.Type.result)) {
            try {
                SensorDataAccepted roa = (SensorDataAccepted) iq;
                if (log.isDebugEnabled()) {
                    log.debug("accept: {}", roa.toXML());
                }
                // verify that id is correct etc.
            } catch (Exception e) {
                log.error("failed to handle SensorDataAccepted", e);
            }
        } else if ((iq instanceof SensorDataControl)) {
        	SensorDataControl sdc = (SensorDataControl) iq;
        	//log.debug("Got control message name={} value={}", sdc.getSdb().getName(), sdc.getSdb().getValue());
        	handleSensorDataControlMessage(sdc, iq);
        	return;
        }
    }

    /* do a read out all request - with an optional key 
     * This will query all logins for a JID to get all data from it */
    public void postData(String device, String key) {
        RosterEntry jid = Roster.getInstanceFor(connection).getEntry(device);
        int sent = 0;
        if (jid == null) {
            log.debug("Recipient not in roster: " + device);
            return;
        }
        List<Presence> preList = Roster.getInstanceFor(connection).getPresences(jid.getUser());
        
        for(Presence pre : preList) {
//            log.debug("\n-----------");
//            log.debug("JID:" + jid.getUser() + " Presence: " + pre.isAvailable() + " Prio: " + pre.getPriority() +
//                    " Mode: " + pre.getMode() + " Type: " + pre.getType() + " from:" + pre.getFrom());
            try {
                DiscoverInfo info = discoManager.discoverInfo(pre.getFrom());
                if (info.containsFeature(SensorDataConstants.SENSORDATA_NAMESPACE)) {
//                    log.debug("JID/RSC Has IOT!!!");
                    if (pre.isAvailable()) {

                        SensorDataRequest ro = new SensorDataRequest(seqNo, true);
                        ro.setType(IQ.Type.get);
                        ro.setFrom(connection.getUser());

                        ro.setTo(pre.getFrom());

                        if (log.isDebugEnabled()) {
                            log.debug("SensorDataManager.postData: from={}", ro.toXML());
                        }

                        connection.sendStanza(ro);
                        if (key == null) {
                            key = makeSafeName(StringUtils.parseName(pre.getFrom()));
                        }
                        requestedJid.put("" + seqNo, key);
                        seqNo++;
                        sent++;
                        ruleEngine.setKB(key + ".nodes", "");
                    }
                }
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            log.debug("\n-----------");
        }
        
        if (sent == 0) {
            log.debug(">>> Recipient not present can not post... XEP 323 query to " + device);
            return;
        }
        

    }


    public static String generateMessage(String from, String to, String device, String data, RuleEngine ruleEngine) {
        SensorDataMessage.TimestampType t = new SensorDataMessage.TimestampType();
        SensorDataMessage.Node node = new SensorDataMessage.Node(device, t);
        node.addTimestamp(t);

        Pattern pattern = Pattern.compile("\\{(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*\\}");
        //Pattern pattern = Pattern.compile("\\{([A-Za-z0-9].*?),([A-Za-z0-9].*?),([A-Za-z0-9].*?)\\}");
        Matcher matcher = pattern.matcher(data);
        int count = 0;
        while(matcher.find()){
            String variable = matcher.group(1);
            String value = TemplateUtil.processTemplate(matcher.group(2), ruleEngine);
            String  unit = matcher.group(3);
            log.debug("variable={} value={}<{}> unit={}", variable, value, matcher.group(2), unit);

            SensorDataMessage.SensorDataValue n = new SensorDataMessage.SensorDataValue("numeric", variable, true, true, value, unit);
            t.addSensorDataValue(n);
            count++;
        }
        if (count == 0) {
            log.debug("No matches found in data, ignoring request");
            return null;
        }

        log.debug("postData: from={} to={}", from, to);

        SensorDataMessage rom = new SensorDataMessage(node, 1);

        return rom.toXML();

    }

    public void postMessage(String from, String to, String device, String data) {

        Message m = new Message();
        m.setTo(to);
        //m.setFrom(from);
        m.setFrom(connection.getUser());
        m.setType(Type.chat);

        SensorDataMessage.TimestampType t = new SensorDataMessage.TimestampType();
        SensorDataMessage.Node node = new SensorDataMessage.Node(device, t);
        node.addTimestamp(t);

        Pattern pattern = Pattern.compile("\\{(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*\\}");
        //Pattern pattern = Pattern.compile("\\{([A-Za-z0-9].*?),([A-Za-z0-9].*?),([A-Za-z0-9].*?)\\}");
        Matcher matcher = pattern.matcher(data);
        int count = 0;
        while(matcher.find()){
            String variable = matcher.group(1);
            String value = TemplateUtil.processTemplate(matcher.group(2), ruleEngine);
            String  unit = matcher.group(3);
            log.debug("variable={} value={}<{}> unit={}", variable, value, matcher.group(2), unit);

            SensorDataMessage.SensorDataValue n = new SensorDataMessage.SensorDataValue("numeric", variable, true, true, value, unit);
            t.addSensorDataValue(n);
            count++;
        }
        if (count == 0) {
            log.debug("No matches found in data, ignoring request");
            return;
        }

        log.debug("SensorDataManager.postData: from={} to={}", from, to);

        SensorDataMessage rom = new SensorDataMessage(node, 1);
        //m.setPacketID(ro.getPacketID());
        m.addExtension(rom);
        if (log.isDebugEnabled()) {
            log.debug("ReadOutMessage: {}", m.toXML());
        }
        connection.sendStanza(m);	
    }

    /* create read out message with a response to a sensor data request */
    private SensorDataMessage createReadOutMessage(String nodename, SensorDataRequest ro) {
        int seqNo = ro.getSeqno();
        SensorDataMessage.TimestampType t = new SensorDataMessage.TimestampType();
        log.debug("createReadOutMessage nodename={}", nodename);
        SensorDataMessage.Node node = new SensorDataMessage.Node(nodename, t);

        //        String delim = "[ ]";
        //        String s = (String) ruleEngine.getKB(name + "." + nodename + ".sensors");
        //        log.debug("createReadOutMessage s={}", s);
        //        String[] sensors = s.split(delim);
        /* 
         * Get all the <name>.<nodename>.sensor.<sensorname>=<key>,<unit>
         * and send this back as response.
         */
        List<String> identity = ruleEngine.findAllKBNames(name + "." + nodename + ".identity.+?", null);
        for (int i = 0; i < identity.size(); i++) {
            String idkey = identity.get(i);
            String val = ruleEngine.getKB(String.class, idkey);
            if (val == null) {
                log.warn("*** value of {} is null - should be: value", idkey);
                continue;
            }
            int lastIndex = idkey.lastIndexOf('.');
            idkey = idkey.substring(lastIndex + 1);

            /* check that this field should be there in the response */
            if (ro.checkField(idkey)) {
                /* at the moment only strings are used... */
                SensorDataMessage.SensorDataValue n = new SensorDataMessage.SensorDataValue("string", idkey, false, false, val, null);
                /* add a numeric sensor data value to the container - today wrongly called timestamp in the XEP */
                t.addSensorDataValue(n);
            }
        }

        List<String> sensors = ruleEngine.findAllKBNames(name + "." + nodename + ".sensor.+?", null);
        ArrayList<String> nodes = new ArrayList<String>();
        nodes.add(nodename);
        //TODO Felhantering
        for (int i = 0; i < sensors.size(); i++) {
            String sensor = sensors.get(i);
            /* the key-unit should be two parts */
            String keyunit = ruleEngine.getKB(String.class, sensor);
            if (keyunit == null) {
                log.warn("*** value of {} is null - should be: key,unit - skipping...", sensor);
                continue;
            }

            //log.debug("Getting key from: {}", keyunit);

            String[] parts = keyunit.split(","); 
            /* TODO: check parts - needs to have two parts!!!! */

            /* pick the value from the key */
            if (parts == null || parts.length < 2) {
                log.error("Error not 2 parts: " + keyunit + " at sensor: " + sensor);
                /* skip this sensor */
                continue;
            }

            Object v = ruleEngine.getKB(parts[0]);
            if (v == null) {
                log.debug("Value of {} is null - skipping", parts[0]);
                continue;
            }
            String type = v instanceof Number ? "numeric" : "string";
            String value = v.toString();

            int lastIndex = sensor.lastIndexOf('.');
            sensor = sensor.substring(lastIndex + 1);
            /* check that this field should be there in the response */
            if (ro.checkField(sensor)) {
                String cname = checkWrite(sensor, nodes);
                if (cname != null) {
                    type = getType(cname);
                    System.out.println("***** GOT TYPE for writable attribute: " + type + " from " + cname);
                }
                SensorDataMessage.SensorDataValue n = new SensorDataMessage.SensorDataValue(type, sensor, true, true, value, parts[1]);
                /* add a numeric sensor data value to the container - today wrongly called timestamp in the XEP */
                t.addSensorDataValue(n);
                /* if writable - then set writable flag */
                if (cname != null) {
                    n.setAttribute("writable", "true");
                }
            }
        }
        if (t.getSensorDataValueCount() > 0) {
            SensorDataMessage rom = new SensorDataMessage(node, seqNo);
            return rom;
        }
        return null;
    }

    /*******************************************************************************/

    private SensorDataMessage createSensorHistory(String nodeName,
            SensorDataRequest ro) {

//        List<String> sensors = ruleEngine.findAllKBNames(name + "." + nodeName + ".sensor.+?", null);
        List<String> sensors = ruleEngine.findAllKBNames(name + "." + nodeName + ".+?.value", null);

        //TODO Felhantering
        for (int i = 0; i < sensors.size(); i++) {
            String sensor = sensors.get(i);
            /* the key-unit should be two parts */
            /* pick the Sensor Name full name */
            /* jid.node.SENSOR.value */
            int lastIndex = sensor.lastIndexOf('.');
            String sensorName = sensor.substring(0, lastIndex);
            lastIndex = sensorName.lastIndexOf('.');
            sensorName = sensorName.substring(lastIndex + 1);
            /* check that this field should be there in the response */
            System.out.println("||>>-- Checking sensor for history: " + sensor + " Name:" + sensorName);

            if (ro.checkField(sensorName)) {
                /* here we should create time-stamped data */

                /* get the key for sicsthsense! */
                String key = (String) ruleEngine.getKB(name + ".autohistory.key", null);
                String url = (String) ruleEngine.getKB(name + ".autohistory.url", null);
                String unit = (String) ruleEngine.getKB(name + "." + nodeName + "." + sensorName + ".unit", "");
                if (key == null || url == null) {
                    log.debug("No history for :", sensorName);
                    continue;
                } else {
                    try {
                        String id = getSenseID(url, key , sensor);
                        if (id == null) {
                            log.debug("No Sicsthsense ID for sensor:" + sensor);
                            continue;
                        }
//                        String toDate = ro.getAttribute("to");
                        String fromDate = ro.getAttribute("from");
                        /* default returns last days data??? - this should be checked with from and to args. */
                        long lastTime = 1000 * 60 * 60 * 24;
                        long from = System.currentTimeMillis() - lastTime;
                        if (fromDate != null) {
                            /* take a new from! */
                            Calendar fdata = DatatypeConverter.parseDateTime(fromDate);
                            from = fdata.getTimeInMillis();
                            log.debug("Taking from from in the message => " + fromDate);
                        }
                        
                        URL jsonurl = new URL(url + "/streams/" + id + "/data?key=" + key + "&from=" + from);
                        JSONArray jsonArray = (JSONArray) JSONObject.parseJSON(new InputStreamReader(jsonurl.openStream()));

                        SensorDataMessage.Node node = new SensorDataMessage.Node();
                        node.setNodeId(nodeName);

                        log.debug("Generating " + jsonArray.length() + " historical values");
                        /* reduce to less total amount of data over XMPP */
                        int step = jsonArray.length() / 100;
                        if (step == 0) step = 1;
                        for (int j = 0; j < jsonArray.length(); j += step) {
                            JSONObject entry = (JSONObject) jsonArray.get(j);
                            long time = entry.getLong("timestamp");
                            if (time != -1) {
                                SensorDataMessage.TimestampType t = new SensorDataMessage.TimestampType(time);
                                /* fix this - this sucker can only handle one timestamp... fix this!!! */
                                node.addTimestamp(t);

                                SensorDataMessage.SensorDataValue data = new SensorDataMessage.SensorDataValue();
                                data.setName(sensorName);
                                data.setUnit(unit);
                                data.setValue("" + entry.get("value"));
                                data.setType("numeric");
                                data.setAttribute("historical", "true");
                                /* add a numeric sensor data value to the container - today wrongly called timestamp in the XEP */
                                t.addSensorDataValue(data);
                            }
                        }
                        SensorDataMessage message = new SensorDataMessage(node, ro.getSeqno());
                        return message;
                    } catch (Exception e) {
                        log.debug("Exception", e);
                    }
                }
            }
        }
        return null;
    }

    private String getSenseID(String url, String key, String sensor) {
        String id = (String) ruleEngine.getKB(sensor + ".id");
        if (id == null || id.length() == 0) {
            URL jsonurl = null;
            try {
                jsonurl = new URL(url + "/streams/?key=" + key);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            JSONArray json = null;
            try {
                /* get the array with streams */
                json = (JSONArray) JSONObject.parseJSON(new InputStreamReader(jsonurl.openStream()));
                
                int max = json.length();
                for(int i = 0; i < max; i++) {
                    JSONObject obj = (JSONObject) json.get(i);
                    id = obj.getString("id");
                    String label = obj.getString("label");
                    label = label.substring(1);
                    ruleEngine.setKB(label + ".id", id);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            /* Store the key-value mapping */
            System.out.println(">>>> JSON OBJECT: " + json);
        }
        /* with some luck the sensor should be there now !!! */
        return (String) ruleEngine.getKB(sensor + ".id");
    }

    /*******************************************************************************/


    public void postControlMessage(String from, String to, String key, String value) {
        SensorDataControl sdc = new SensorDataControl(key, Integer.parseInt(value));
        
        RosterEntry jid = Roster.getInstanceFor(connection).getEntry(to);
        Presence pre = Roster.getInstanceFor(connection).getPresenceResource(jid.getName());
        System.out.println("JID:" + jid.getName() + " Presence: " + pre.isAvailable());

        if (jid == null || !pre.isAvailable()) {
            log.debug("Recipient not present");
            return;
        }

        sdc.setTo(jid.getName());
        sdc.setFrom(from);
        if (log.isDebugEnabled()) {
            log.debug("postControlMessage: {}", sdc.toXML());
        }
        connection.sendStanza(sdc);

    }
}
