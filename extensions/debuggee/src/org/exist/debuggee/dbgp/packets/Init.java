/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2009 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.debuggee.dbgp.packets;

import org.apache.mina.core.session.IoSession;
import org.exist.security.xacml.XACMLSource;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Init extends AbstractCommandContinuation {

	private XACMLSource fileuri;
	private String idekey = "1";
	private String idesession = "1";
	
	public Init(IoSession session) {
		super(session, "");
	}

	public Init(IoSession session, String idesession, String idekey) {
		super(session, "");
		this.idekey = idekey;
		this.idesession = idesession;
	}

	public void setFileURI(XACMLSource source){
		fileuri = source;

		if (!session.isClosing())
			session.write(this);
	}

	public byte[] responseBytes() {
		String init_message = "<init " +
			"xmlns=\"urn:debugger_protocol_v1\" " +
			"xmlns:xdebug=\"http://xdebug.org/dbgp/xdebug\" " +
			"appid=\"eXist050705\" " + //keep this as memory of creation
			"idekey=\""+idekey+"\" " +
			"session=\""+idesession+"\" " +
			//"thread=\"1\" " +
			//"parent=\"1\" " +
			"language=\"XQuery\" " +
			"protocol_version=\"1.0\" " +
			"fileuri=\""+Command.getFileuri(fileuri)+"\"></init>";

		return init_message.getBytes();
	}

	@Override
	public void exec() {
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getType() {
		return INIT;
	}

	public boolean is(int type) {
		return (type == INIT);
	}

	public byte[] commandBytes() {
		return new byte[0];
	}
}
