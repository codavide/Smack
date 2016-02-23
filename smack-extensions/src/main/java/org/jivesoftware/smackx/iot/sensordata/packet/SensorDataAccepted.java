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
 * $Id: SensorDataAccepted.java 38 2013-06-28 09:20:44Z marcus@arendt.se $
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

package org.jivesoftware.smackx.iot.sensordata.packet;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.sensordata.util.SensorDataConstants;

public class SensorDataAccepted extends IQ {
	private int seqno=0;
	private Boolean queued = null;
	
	public SensorDataAccepted() {
		super(SensorDataConstants.ACCEPT_NAME, SensorDataConstants.SENSORDATA_NAMESPACE);
	}
	
	public SensorDataAccepted(int seqno) {
		super(SensorDataConstants.ACCEPT_NAME, SensorDataConstants.SENSORDATA_NAMESPACE);
		this.seqno = seqno;
	}

	public int getSeqno() {
		return seqno;
	}

	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}
	
	public Boolean getQueued() {
		return queued;
	}

	public void setQueued(Boolean queued) {
		this.queued = queued;
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder buf) {
		buf.append("<"+SensorDataConstants.ACCEPT_NAME+" xmlns='"+SensorDataConstants.SENSORDATA_NAMESPACE+"' seqnr='");
		buf.append(String.valueOf(seqno));
		if(queued!=null) {
			buf.append(" queued='").append(queued.toString()).append("'");
		}
		buf.append("'/>");

		return buf;
	}

	/*
		 <iq type='result'
	       from='master@clayster.com/amr'
	       to='device@clayster.com'
	       id='1'>
	      <accepted xmlns='urn:xmpp:iot:sensordata' seqnr='1'/>
	   </iq>
	 */

}


