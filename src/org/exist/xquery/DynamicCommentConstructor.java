/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 Wolfgang M. Meier
 *  wolfgang@exist-db.org
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
package org.exist.xquery;

import org.exist.memtree.DocumentImpl;
import org.exist.memtree.MemTreeBuilder;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;


/**
 * Implements a dynamic comment constructor. Contrary to {@link org.exist.xquery.CommentConstructor},
 * the character content of a DynamicCommentConstructor is determined only at evaluation time.
 * 
 * @author wolf
 */
public class DynamicCommentConstructor extends NodeConstructor {

    final private Expression content;
    
    /**
     * @param context
     */
    public DynamicCommentConstructor(XQueryContext context, Expression contentExpr) {
        super(context);
        this.content = new Atomize(context, contentExpr);
    }

    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#analyze(org.exist.xquery.AnalyzeContextInfo)
     */
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
    	contextInfo.setParent(this);
        content.analyze(contextInfo);
    }
    
    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#eval(org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
     */
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);       
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT ITEM", contextItem.toSequence());
        }
        
        Sequence result;
        Sequence contentSeq = content.eval(contextSequence, contextItem);
       
        if(contentSeq.isEmpty())
            result = Sequence.EMPTY_SEQUENCE;
        else {            
            MemTreeBuilder builder = context.getDocumentBuilder();
    		context.proceed(this, builder);
            
    		StringBuffer buf = new StringBuffer();
            for(SequenceIterator i = contentSeq.iterate(); i.hasNext(); ) {
                context.proceed(this, builder);
                Item next = i.nextItem();
                if(buf.length() > 0)
                    buf.append(' ');
                buf.append(next.toString());
            }
            
            if (buf.indexOf("--") != Constants.STRING_NOT_FOUND|| buf.toString().endsWith("-")) {
            	throw new XPathException("XQDY0072 '" + buf.toString() + "' is not a valid comment");            	
            }
            
            int nodeNr = builder.comment(buf.toString());
            result = ((DocumentImpl)builder.getDocument()).getNode(nodeNr);
        }
        
        if (context.getProfiler().isEnabled())           
            context.getProfiler().end(this, "", result);  
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
        dumper.display("comment {");
        dumper.startIndent();
        content.dump(dumper);
        dumper.endIndent();
        dumper.nl().display("}");
    }
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	result.append("comment {");        
    	result.append(content.toString());        
    	result.append("} ");
    	return result.toString();
    }
    
    public void resetState(boolean postOptimization) {
    	super.resetState(postOptimization);
    	content.resetState(postOptimization);
    }
}
