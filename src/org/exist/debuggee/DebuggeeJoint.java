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
 *  $Id:$
 */
package org.exist.debuggee;

import java.util.List;
import java.util.Map;

import org.exist.dom.QName;
import org.exist.xquery.Expression;
import org.exist.xquery.PathExpr;
import org.exist.xquery.Variable;
import org.exist.xquery.XQueryContext;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
//TODO: rename DebuggeeRuntime ?
public interface DebuggeeJoint {
	
	public void expressionStart(Expression expr);
	public void expressionEnd(Expression expr);

	public void reset();

	public boolean featureSet(String name, String value);

	public String stepInto();
	public String stepOut();
	public String stepOver();

	public List<Expression> stackGet();
	
	public Map<QName, Variable> getVariables();
	public Variable getVariable(String name);

}
