/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-03 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
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
package org.exist.xquery.modules.http;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.util.serializer.SAXSerializer;
import org.exist.util.serializer.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;


/**
 * @author Adam Retter <adam.retter@devon.gov.uk>
 */
public class POSTFunction extends BasicFunction {

	public final static FunctionSignature signature =
		new FunctionSignature(
			new QName("post", HTTPModule.NAMESPACE_URI, HTTPModule.PREFIX),
			"Performs a HTTP POST request. $a is the URL, $b is the XML POST, $c determines if cookies persist for the query lifetime.",
			new SequenceType[] {
				new SequenceType(Type.ANY_URI, Cardinality.EXACTLY_ONE),
				new SequenceType(Type.NODE, Cardinality.EXACTLY_ONE),
				new SequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE)
			},
			new SequenceType(Type.INTEGER, Cardinality.EXACTLY_ONE));

	public POSTFunction(XQueryContext context)
	{
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException
	{
		// must be a URL
		if(args[0].isEmpty())
			return Sequence.EMPTY_SEQUENCE;
		
		//get the url
		String url = args[0].itemAt(0).getStringValue();
		//get the node
		Item node = args[1].itemAt(0);
		//get the persist cookies
		boolean persistCookies = args[2].effectiveBooleanValue();
		
		//serialize the node to SAX
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(baos);
		XMLWriter xmlWriter = new XMLWriter(osw);
		SAXSerializer sax = new SAXSerializer();
		sax.setReceiver(xmlWriter);
		try
		{
			node.toSAX(context.getBroker(), sax);
			osw.flush();
			osw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//setup post content
		PostMethod post = new PostMethod(url);
		RequestEntity entity = new ByteArrayRequestEntity(baos.toByteArray(), "text/xml; utf-8");
        post.setRequestEntity(entity);
		
        //use existing cookies
        if(persistCookies)
        {
	        Cookie[] cookies = (Cookie[])context.getXQueryContextVar(HTTPModule.HTTP_MODULE_PERSISTENT_COOKIES);
			if(cookies != null)
			{
				for(int c = 0; c < cookies.length; c++)
				{
					post.setRequestHeader("Cookie", cookies[c].toExternalForm());
				}
			}
        }
        
		//execute the request
		int result = -1;
		try
		{
			HttpClient http = new HttpClient();
			result = http.executeMethod(post);
			
			//persist cookies
			if(persistCookies)
			{
				HttpState state = http.getState();
				Cookie[] incomingCookies = state.getCookies();
				Cookie[] currentCookies = (Cookie[])context.getXQueryContextVar(HTTPModule.HTTP_MODULE_PERSISTENT_COOKIES);
				context.setXQueryContextVar(HTTPModule.HTTP_MODULE_PERSISTENT_COOKIES, HTTPModule.mergeCookies(currentCookies, incomingCookies));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			post.releaseConnection();
		}
		
		return new IntegerValue(result);
	}
}
