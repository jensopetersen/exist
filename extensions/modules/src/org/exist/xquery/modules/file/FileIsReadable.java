/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 The eXist Project
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
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id: FileRead.java 7488 2008-03-07 05:27:06Z chaeron $
 */
package org.exist.xquery.modules.file;

import java.io.File;

import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

/**
 * @author Andrzej Taramina
 * @author Loren Cahlander
 *
 */
public class FileIsReadable extends BasicFunction {

	private static final Logger logger = Logger.getLogger(FileIsReadable.class);
	
	public final static FunctionSignature signatures[] = {
		new FunctionSignature(
			new QName( "is-readable", FileModule.NAMESPACE_URI, FileModule.PREFIX ),
			"Tests if file is readable",
			new SequenceType[] {				
				new FunctionParameterSequenceType( "file", Type.ITEM, Cardinality.EXACTLY_ONE, "" )
				},				
			new FunctionParameterSequenceType( "is-readable", Type.BOOLEAN, Cardinality.EXACTLY_ONE, "File can be read" ) )
		};
	
	/**
	 * @param context
	 * @param signature
	 */
	public FileIsReadable( XQueryContext context, FunctionSignature signature ) 
	{
		super( context, signature );
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
	 */
	public Sequence eval( Sequence[] args, Sequence contextSequence ) throws XPathException 
	{
		logger.info("Entering " + FileModule.PREFIX + ":" + getName().getLocalName());
		Sequence readable 	= BooleanValue.FALSE;
		String path 		= args[0].itemAt(0).getStringValue();
		File file   		= new File( path );
		
		if( file.canRead() ) {
			readable = BooleanValue.TRUE;
		}
		
		logger.info("Exiting " + FileModule.PREFIX + ":" + getName().getLocalName());
		return( readable ); 
	}
}
