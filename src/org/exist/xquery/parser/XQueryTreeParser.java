// $ANTLR 2.7.7 (2006-11-01): "XQueryTree.g" -> "XQueryTreeParser.java"$

	package org.exist.xquery.parser;

	import antlr.debug.misc.*;
	import java.io.StringReader;
	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Iterator;
	import java.util.Map;
	import java.util.Set;
	import java.util.TreeSet;
	import java.util.HashMap;
	import java.util.Stack;
	import javax.xml.XMLConstants;
	import org.exist.storage.BrokerPool;
	import org.exist.storage.DBBroker;
	import org.exist.EXistException;
	import org.exist.Namespaces;
	import org.exist.dom.persistent.DocumentSet;
	import org.exist.dom.persistent.DocumentImpl;
	import org.exist.dom.QName;
	import org.exist.security.PermissionDeniedException;
	import org.exist.util.XMLChar;
	import org.exist.xquery.*;
	import org.exist.xquery.Constants.ArithmeticOperator;
	import org.exist.xquery.Constants.Comparison;
	import org.exist.xquery.Constants.NodeComparisonOperator;
	import org.exist.xquery.value.*;
	import org.exist.xquery.functions.fn.*;
	import org.exist.xquery.update.*;
	import org.exist.storage.ElementValue;
	import org.exist.xquery.functions.map.MapExpr;
	import org.exist.xquery.functions.array.ArrayConstructor;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


/**
 * The tree parser: walks the AST created by {@link XQueryParser} and generates
 * an internal representation of the query in the form of XQuery expression objects.
 */
public class XQueryTreeParser extends antlr.TreeParser       implements XQueryTreeParserTokenTypes
 {

	private XQueryContext staticContext;
	private XQueryContext context;
	private ExternalModule myModule = null;
	protected ArrayList exceptions= new ArrayList(2);
	protected boolean foundError= false;
	protected Map declaredNamespaces = new HashMap();
	protected Set declaredGlobalVars = new TreeSet();

	public XQueryTreeParser(XQueryContext context) {
        this(context, null);
	}

	public XQueryTreeParser(XQueryContext context, ExternalModule module) {
		this();
        this.staticContext = new XQueryContext(context);
		this.context= context;
		this.myModule = module;
	}

	public ExternalModule getModule() {
		return myModule;
	}

	public boolean foundErrors() {
		return foundError;
	}

	public String getErrorMessage() {
		StringBuilder buf= new StringBuilder();
		for (Iterator i= exceptions.iterator(); i.hasNext();) {
			buf.append(((Exception) i.next()).toString());
			buf.append('\n');
		}
		return buf.toString();
	}

	public Exception getLastException() {
		return (Exception) exceptions.get(exceptions.size() - 1);
	}

	protected void handleException(Exception e) {
		foundError= true;
		exceptions.add(e);
	}

	private void throwException(XQueryAST ast, String message) throws XPathException {
		throw new XPathException(ast, message);
	}

	private static class ForLetClause {
		XQueryAST ast;
		String varName;
		SequenceType sequenceType= null;
		String posVar= null;
		Expression inputSequence;
		Expression action;
		FLWORClause.ClauseType type = FLWORClause.ClauseType.FOR;
		List<GroupSpec> groupSpecs = null;
		List<OrderSpec> orderSpecs = null;
		boolean allowEmpty = false;
	}

	/**
	 * Check QName of an annotation to see if it is in a reserved namespace.
	 */
	private boolean annotationValid(QName qname) {
		String ns = qname.getNamespaceURI();
        if (ns.equals(Namespaces.XPATH_FUNCTIONS_NS)) {
			String ln = qname.getLocalPart();
			return ("private".equals(ln) || "public".equals(ln));
		} else {
			return !(ns.equals(Namespaces.XML_NS)
                     || ns.equals(Namespaces.SCHEMA_NS)
                     || ns.equals(Namespaces.SCHEMA_INSTANCE_NS)
                     || ns.equals(Namespaces.XPATH_FUNCTIONS_MATH_NS)
                     || ns.equals(Namespaces.XQUERY_OPTIONS_NS));
		}
	}

	private static void processAnnotations(List annots, FunctionSignature signature) {
	    Annotation[] anns = new Annotation[annots.size()];

        //iterate the declare Annotations
        for(int i = 0; i < anns.length; i++) {
           List la = (List)annots.get(i);

           //extract the Value for the Annotation
           LiteralValue[] aValue;
           if(la.size() > 1) {

            PathExpr aPath = (PathExpr)la.get(1);

            aValue = new LiteralValue[aPath.getSubExpressionCount()];
            for(int j = 0; j < aValue.length; j++) {
                aValue[j] = (LiteralValue)aPath.getExpression(j);
            }
           } else {
            aValue = new LiteralValue[0];
           }

           Annotation a = new Annotation((QName)la.get(0), aValue, signature);
           anns[i] = a;
        }

        //set the Annotations on the Function Signature
        signature.setAnnotations(anns);
	}

	private static void processParams(List varList, UserDefinedFunction func, FunctionSignature signature)
	throws XPathException {
	    SequenceType[] types= new SequenceType[varList.size()];
        int j= 0;
        for (Iterator i= varList.iterator(); i.hasNext(); j++) {
            FunctionParameterSequenceType param= (FunctionParameterSequenceType) i.next();
            types[j]= param;
            func.addVariable(param.getAttributeName());
        }
        signature.setArgumentTypes(types);
	}
public XQueryTreeParser() {
	tokenNames = _tokenNames;
}

	public final void xpointer(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST xpointer_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST nc = null;
		Expression step = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case XPOINTER:
			{
				AST __t2 = _t;
				org.exist.xquery.parser.XQueryAST tmp1_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,XPOINTER);
				_t = _t.getFirstChild();
				step=expr(_t,path);
				_t = _retTree;
				_t = __t2;
				_t = _t.getNextSibling();
				break;
			}
			case XPOINTER_ID:
			{
				AST __t3 = _t;
				org.exist.xquery.parser.XQueryAST tmp2_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,XPOINTER_ID);
				_t = _t.getFirstChild();
				nc = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getNextSibling();
				_t = __t3;
				_t = _t.getNextSibling();
				
					    PathExpr p = new PathExpr(context);
						RootNode root = new RootNode(context);
						p.add(root);
						Function fun= new FunId(context, FunId.signature[0]);
						List params= new ArrayList(1);
						params.add(new LiteralValue(context, new StringValue(nc.getText())));
						fun.setArguments(params);
						p.addPath(fun);
						path.add(p);
					
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (EXistException e) {
			handleException(e);
		}
		catch (PermissionDeniedException e) {
			handleException(e);
		}
		_retTree = _t;
	}
	
/**
 * Process a top-level expression like FLWOR, conditionals, comparisons etc.
 */
	public final Expression  expr(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST expr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST eof = null;
		org.exist.xquery.parser.XQueryAST seq = null;
		org.exist.xquery.parser.XQueryAST c = null;
		org.exist.xquery.parser.XQueryAST astIf = null;
		org.exist.xquery.parser.XQueryAST astThen = null;
		org.exist.xquery.parser.XQueryAST astElse = null;
		org.exist.xquery.parser.XQueryAST someVarName = null;
		org.exist.xquery.parser.XQueryAST everyVarName = null;
		org.exist.xquery.parser.XQueryAST astTry = null;
		org.exist.xquery.parser.XQueryAST astCatch = null;
		org.exist.xquery.parser.XQueryAST code = null;
		org.exist.xquery.parser.XQueryAST desc = null;
		org.exist.xquery.parser.XQueryAST val = null;
		org.exist.xquery.parser.XQueryAST r = null;
		org.exist.xquery.parser.XQueryAST f = null;
		org.exist.xquery.parser.XQueryAST varName = null;
		org.exist.xquery.parser.XQueryAST posVar = null;
		org.exist.xquery.parser.XQueryAST l = null;
		org.exist.xquery.parser.XQueryAST letVarName = null;
		org.exist.xquery.parser.XQueryAST gb = null;
		org.exist.xquery.parser.XQueryAST groupVarName = null;
		org.exist.xquery.parser.XQueryAST groupCollURI = null;
		org.exist.xquery.parser.XQueryAST ob = null;
		org.exist.xquery.parser.XQueryAST collURI = null;
		org.exist.xquery.parser.XQueryAST w = null;
		org.exist.xquery.parser.XQueryAST switchAST = null;
		org.exist.xquery.parser.XQueryAST var = null;
		org.exist.xquery.parser.XQueryAST dvar = null;
		
			step= null;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EOF:
		{
			eof = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,Token.EOF_TYPE);
			_t = _t.getNextSibling();
			
			// Added for handling empty mainModule /ljo
			// System.out.println("EMPTY EXPR");
			if (eof.getText() == null || "".equals(eof.getText()))
			throw new XPathException(eof, "err:XPST0003: EOF or zero-length string found where a valid XPath expression was expected.");
			
			
			break;
		}
		case LITERAL_castable:
		case LITERAL_cast:
		{
			step=typeCastExpr(_t,path);
			_t = _retTree;
			break;
		}
		case SEQUENCE:
		{
			AST __t129 = _t;
			seq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,SEQUENCE);
			_t = _t.getFirstChild();
			
				   SequenceConstructor sc = new SequenceConstructor(context);
				   sc.setASTNode(seq);
				
			{
			_loop131:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					PathExpr seqPath = new PathExpr(context);
					step=expr(_t,seqPath);
					_t = _retTree;
					
						     sc.addPath(seqPath);
						
				}
				else {
					break _loop131;
				}
				
			} while (true);
			}
			
				   path.addPath(sc);
				   step = sc;
			
			_t = __t129;
			_t = _t.getNextSibling();
			break;
		}
		case CONCAT:
		{
			AST __t132 = _t;
			org.exist.xquery.parser.XQueryAST tmp3_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,CONCAT);
			_t = _t.getFirstChild();
			ConcatExpr concat = new ConcatExpr(context);
			{
			_loop134:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					PathExpr strPath = new PathExpr(context);
					step=expr(_t,strPath );
					_t = _retTree;
					
									concat.add(strPath);
								
				}
				else {
					break _loop134;
				}
				
			} while (true);
			}
			
						path.addPath(concat);
						step = concat;
					
			_t = __t132;
			_t = _t.getNextSibling();
			break;
		}
		case COMMA:
		{
			AST __t135 = _t;
			c = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMMA);
			_t = _t.getFirstChild();
			
						PathExpr left= new PathExpr(context);
						PathExpr right= new PathExpr(context);
					
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						SequenceConstructor sc= new SequenceConstructor(context);
						sc.setASTNode(c);
						sc.addPath(left);
						sc.addPath(right);
						path.addPath(sc);
						step = sc;
					
			_t = __t135;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_if:
		{
			AST __t136 = _t;
			astIf = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_if);
			_t = _t.getFirstChild();
			
						PathExpr testExpr= new PathExpr(context);
						PathExpr thenExpr= new PathExpr(context);
						PathExpr elseExpr= new PathExpr(context);
					
			step=expr(_t,testExpr);
			_t = _retTree;
			astThen = _t==ASTNULL ? null : (org.exist.xquery.parser.XQueryAST)_t;
			step=expr(_t,thenExpr);
			_t = _retTree;
			astElse = _t==ASTNULL ? null : (org.exist.xquery.parser.XQueryAST)_t;
			step=expr(_t,elseExpr);
			_t = _retTree;
			
			thenExpr.setASTNode(astThen);
			elseExpr.setASTNode(astElse);
						ConditionalExpression cond =
			new ConditionalExpression(context, testExpr, thenExpr,
			new DebuggableExpression(elseExpr));
						cond.setASTNode(astIf);
						path.add(cond);
						step = cond;
					
			_t = __t136;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_some:
		{
			AST __t137 = _t;
			org.exist.xquery.parser.XQueryAST tmp4_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_some);
			_t = _t.getFirstChild();
			
						List clauses= new ArrayList();
						PathExpr satisfiesExpr = new PathExpr(context);
					
			{
			_loop142:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==VARIABLE_BINDING)) {
					AST __t139 = _t;
					someVarName = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,VARIABLE_BINDING);
					_t = _t.getFirstChild();
					
										ForLetClause clause= new ForLetClause();
										PathExpr inputSequence = new PathExpr(context);
									
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case LITERAL_as:
					{
						AST __t141 = _t;
						org.exist.xquery.parser.XQueryAST tmp5_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,LITERAL_as);
						_t = _t.getFirstChild();
						SequenceType type= new SequenceType();
						sequenceType(_t,type);
						_t = _retTree;
						_t = __t141;
						_t = _t.getNextSibling();
						clause.sequenceType = type;
						break;
					}
					case EOF:
					case EQNAME:
					case PARENTHESIZED:
					case ABSOLUTE_SLASH:
					case ABSOLUTE_DSLASH:
					case WILDCARD:
					case PREFIX_WILDCARD:
					case FUNCTION:
					case UNARY_MINUS:
					case UNARY_PLUS:
					case VARIABLE_REF:
					case ELEMENT:
					case TEXT:
					case INLINE_FUNCTION_DECL:
					case LOOKUP:
					case ARRAY:
					case ATTRIBUTE_TEST:
					case COMP_ELEM_CONSTRUCTOR:
					case COMP_ATTR_CONSTRUCTOR:
					case COMP_TEXT_CONSTRUCTOR:
					case COMP_COMMENT_CONSTRUCTOR:
					case COMP_PI_CONSTRUCTOR:
					case COMP_NS_CONSTRUCTOR:
					case COMP_DOC_CONSTRUCTOR:
					case PRAGMA:
					case GTEQ:
					case SEQUENCE:
					case NCNAME:
					case EQ:
					case STRING_LITERAL:
					case LITERAL_element:
					case COMMA:
					case LCURLY:
					case STAR:
					case PLUS:
					case LITERAL_map:
					case LITERAL_try:
					case LITERAL_some:
					case LITERAL_every:
					case LITERAL_if:
					case LITERAL_switch:
					case LITERAL_typeswitch:
					case LITERAL_update:
					case LITERAL_preceding:
					case LITERAL_following:
					case UNION:
					case LITERAL_return:
					case LITERAL_or:
					case LITERAL_and:
					case LITERAL_instance:
					case LITERAL_treat:
					case LITERAL_castable:
					case LITERAL_cast:
					case BEFORE:
					case AFTER:
					case LITERAL_eq:
					case LITERAL_ne:
					case LITERAL_lt:
					case LITERAL_le:
					case LITERAL_gt:
					case LITERAL_ge:
					case GT:
					case NEQ:
					case LT:
					case LTEQ:
					case LITERAL_is:
					case CONCAT:
					case LITERAL_to:
					case MINUS:
					case LITERAL_div:
					case LITERAL_idiv:
					case LITERAL_mod:
					case BANG:
					case LITERAL_intersect:
					case LITERAL_except:
					case SLASH:
					case DSLASH:
					case LITERAL_text:
					case LITERAL_node:
					case LITERAL_attribute:
					case LITERAL_comment:
					case 190:
					case 191:
					case HASH:
					case SELF:
					case XML_COMMENT:
					case XML_PI:
					case AT:
					case PARENT:
					case LITERAL_child:
					case LITERAL_self:
					case LITERAL_descendant:
					case 204:
					case 205:
					case LITERAL_parent:
					case LITERAL_ancestor:
					case 208:
					case 209:
					case INTEGER_LITERAL:
					case DOUBLE_LITERAL:
					case DECIMAL_LITERAL:
					case XML_CDATA:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					step=expr(_t,inputSequence);
					_t = _retTree;
					
										clause.varName= someVarName.getText();
										clause.inputSequence= inputSequence;
										clauses.add(clause);
									
					_t = __t139;
					_t = _t.getNextSibling();
				}
				else {
					break _loop142;
				}
				
			} while (true);
			}
			step=expr(_t,satisfiesExpr);
			_t = _retTree;
			
						Expression action = satisfiesExpr;
						for (int i= clauses.size() - 1; i >= 0; i--) {
							ForLetClause clause= (ForLetClause) clauses.get(i);
							BindingExpression expr = new QuantifiedExpression(context, QuantifiedExpression.SOME);
							expr.setVariable(clause.varName);
							expr.setSequenceType(clause.sequenceType);
							expr.setInputSequence(clause.inputSequence);
							expr.setReturnExpression(action);
							satisfiesExpr= null;
							action= expr;
						}
						path.add(action);
						step = action;
					
			_t = __t137;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_every:
		{
			AST __t143 = _t;
			org.exist.xquery.parser.XQueryAST tmp6_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_every);
			_t = _t.getFirstChild();
			
						List clauses= new ArrayList();
						PathExpr satisfiesExpr = new PathExpr(context);
					
			{
			_loop148:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==VARIABLE_BINDING)) {
					AST __t145 = _t;
					everyVarName = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,VARIABLE_BINDING);
					_t = _t.getFirstChild();
					
										ForLetClause clause= new ForLetClause();
										PathExpr inputSequence = new PathExpr(context);
									
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case LITERAL_as:
					{
						AST __t147 = _t;
						org.exist.xquery.parser.XQueryAST tmp7_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,LITERAL_as);
						_t = _t.getFirstChild();
						SequenceType type= new SequenceType();
						sequenceType(_t,type);
						_t = _retTree;
						_t = __t147;
						_t = _t.getNextSibling();
						clause.sequenceType = type;
						break;
					}
					case EOF:
					case EQNAME:
					case PARENTHESIZED:
					case ABSOLUTE_SLASH:
					case ABSOLUTE_DSLASH:
					case WILDCARD:
					case PREFIX_WILDCARD:
					case FUNCTION:
					case UNARY_MINUS:
					case UNARY_PLUS:
					case VARIABLE_REF:
					case ELEMENT:
					case TEXT:
					case INLINE_FUNCTION_DECL:
					case LOOKUP:
					case ARRAY:
					case ATTRIBUTE_TEST:
					case COMP_ELEM_CONSTRUCTOR:
					case COMP_ATTR_CONSTRUCTOR:
					case COMP_TEXT_CONSTRUCTOR:
					case COMP_COMMENT_CONSTRUCTOR:
					case COMP_PI_CONSTRUCTOR:
					case COMP_NS_CONSTRUCTOR:
					case COMP_DOC_CONSTRUCTOR:
					case PRAGMA:
					case GTEQ:
					case SEQUENCE:
					case NCNAME:
					case EQ:
					case STRING_LITERAL:
					case LITERAL_element:
					case COMMA:
					case LCURLY:
					case STAR:
					case PLUS:
					case LITERAL_map:
					case LITERAL_try:
					case LITERAL_some:
					case LITERAL_every:
					case LITERAL_if:
					case LITERAL_switch:
					case LITERAL_typeswitch:
					case LITERAL_update:
					case LITERAL_preceding:
					case LITERAL_following:
					case UNION:
					case LITERAL_return:
					case LITERAL_or:
					case LITERAL_and:
					case LITERAL_instance:
					case LITERAL_treat:
					case LITERAL_castable:
					case LITERAL_cast:
					case BEFORE:
					case AFTER:
					case LITERAL_eq:
					case LITERAL_ne:
					case LITERAL_lt:
					case LITERAL_le:
					case LITERAL_gt:
					case LITERAL_ge:
					case GT:
					case NEQ:
					case LT:
					case LTEQ:
					case LITERAL_is:
					case CONCAT:
					case LITERAL_to:
					case MINUS:
					case LITERAL_div:
					case LITERAL_idiv:
					case LITERAL_mod:
					case BANG:
					case LITERAL_intersect:
					case LITERAL_except:
					case SLASH:
					case DSLASH:
					case LITERAL_text:
					case LITERAL_node:
					case LITERAL_attribute:
					case LITERAL_comment:
					case 190:
					case 191:
					case HASH:
					case SELF:
					case XML_COMMENT:
					case XML_PI:
					case AT:
					case PARENT:
					case LITERAL_child:
					case LITERAL_self:
					case LITERAL_descendant:
					case 204:
					case 205:
					case LITERAL_parent:
					case LITERAL_ancestor:
					case 208:
					case 209:
					case INTEGER_LITERAL:
					case DOUBLE_LITERAL:
					case DECIMAL_LITERAL:
					case XML_CDATA:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					step=expr(_t,inputSequence);
					_t = _retTree;
					
										clause.varName= everyVarName.getText();
										clause.inputSequence= inputSequence;
										clauses.add(clause);
									
					_t = __t145;
					_t = _t.getNextSibling();
				}
				else {
					break _loop148;
				}
				
			} while (true);
			}
			step=expr(_t,satisfiesExpr);
			_t = _retTree;
			
						Expression action = satisfiesExpr;
						for (int i= clauses.size() - 1; i >= 0; i--) {
							ForLetClause clause= (ForLetClause) clauses.get(i);
							BindingExpression expr = new QuantifiedExpression(context, QuantifiedExpression.EVERY);
							expr.setVariable(clause.varName);
							expr.setSequenceType(clause.sequenceType);
							expr.setInputSequence(clause.inputSequence);
							expr.setReturnExpression(action);
							satisfiesExpr= null;
							action= expr;
						}
						path.add(action);
						step = action;
					
			_t = __t143;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_try:
		{
			AST __t149 = _t;
			astTry = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_try);
			_t = _t.getFirstChild();
			
						PathExpr tryTargetExpr = new PathExpr(context);
					
			step=expr(_t,tryTargetExpr);
			_t = _retTree;
			
				    	TryCatchExpression cond = new TryCatchExpression(context, tryTargetExpr);
						cond.setASTNode(astTry);
			path.add(cond);
			
			{
			int _cnt156=0;
			_loop156:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LITERAL_catch)) {
					
									final List<QName> catchErrorList = new ArrayList<>(2);
					final List<QName> catchVars = new ArrayList<>(3);
									final PathExpr catchExpr = new PathExpr(context);
								
					AST __t151 = _t;
					astCatch = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_catch);
					_t = _t.getFirstChild();
					{
					catchErrorList(_t,catchErrorList);
					_t = _retTree;
					}
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case CATCH_ERROR_CODE:
					{
						
										        QName qncode = null;
										        QName qndesc = null;
										        QName qnval = null;
									
						code = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,CATCH_ERROR_CODE);
						_t = _t.getNextSibling();
						
						qncode = QName.parse(staticContext, code.getText());
						catchVars.add(qncode);
						
						{
						if (_t==null) _t=ASTNULL;
						switch ( _t.getType()) {
						case CATCH_ERROR_DESC:
						{
							desc = (org.exist.xquery.parser.XQueryAST)_t;
							match(_t,CATCH_ERROR_DESC);
							_t = _t.getNextSibling();
							
							qndesc = QName.parse(staticContext, desc.getText());
							catchVars.add(qndesc);
							
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case CATCH_ERROR_VAL:
							{
								val = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,CATCH_ERROR_VAL);
								_t = _t.getNextSibling();
								
								qnval = QName.parse(staticContext, val.getText());
								catchVars.add(qnval);
								
								break;
							}
							case EOF:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							break;
						}
						case EOF:
						case EQNAME:
						case PARENTHESIZED:
						case ABSOLUTE_SLASH:
						case ABSOLUTE_DSLASH:
						case WILDCARD:
						case PREFIX_WILDCARD:
						case FUNCTION:
						case UNARY_MINUS:
						case UNARY_PLUS:
						case VARIABLE_REF:
						case ELEMENT:
						case TEXT:
						case INLINE_FUNCTION_DECL:
						case LOOKUP:
						case ARRAY:
						case ATTRIBUTE_TEST:
						case COMP_ELEM_CONSTRUCTOR:
						case COMP_ATTR_CONSTRUCTOR:
						case COMP_TEXT_CONSTRUCTOR:
						case COMP_COMMENT_CONSTRUCTOR:
						case COMP_PI_CONSTRUCTOR:
						case COMP_NS_CONSTRUCTOR:
						case COMP_DOC_CONSTRUCTOR:
						case PRAGMA:
						case GTEQ:
						case SEQUENCE:
						case NCNAME:
						case EQ:
						case STRING_LITERAL:
						case LITERAL_element:
						case COMMA:
						case LCURLY:
						case STAR:
						case PLUS:
						case LITERAL_map:
						case LITERAL_try:
						case LITERAL_some:
						case LITERAL_every:
						case LITERAL_if:
						case LITERAL_switch:
						case LITERAL_typeswitch:
						case LITERAL_update:
						case LITERAL_preceding:
						case LITERAL_following:
						case UNION:
						case LITERAL_return:
						case LITERAL_or:
						case LITERAL_and:
						case LITERAL_instance:
						case LITERAL_treat:
						case LITERAL_castable:
						case LITERAL_cast:
						case BEFORE:
						case AFTER:
						case LITERAL_eq:
						case LITERAL_ne:
						case LITERAL_lt:
						case LITERAL_le:
						case LITERAL_gt:
						case LITERAL_ge:
						case GT:
						case NEQ:
						case LT:
						case LTEQ:
						case LITERAL_is:
						case CONCAT:
						case LITERAL_to:
						case MINUS:
						case LITERAL_div:
						case LITERAL_idiv:
						case LITERAL_mod:
						case BANG:
						case LITERAL_intersect:
						case LITERAL_except:
						case SLASH:
						case DSLASH:
						case LITERAL_text:
						case LITERAL_node:
						case LITERAL_attribute:
						case LITERAL_comment:
						case 190:
						case 191:
						case HASH:
						case SELF:
						case XML_COMMENT:
						case XML_PI:
						case AT:
						case PARENT:
						case LITERAL_child:
						case LITERAL_self:
						case LITERAL_descendant:
						case 204:
						case 205:
						case LITERAL_parent:
						case LITERAL_ancestor:
						case 208:
						case 209:
						case INTEGER_LITERAL:
						case DOUBLE_LITERAL:
						case DECIMAL_LITERAL:
						case XML_CDATA:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(_t);
						}
						}
						}
						break;
					}
					case EOF:
					case EQNAME:
					case PARENTHESIZED:
					case ABSOLUTE_SLASH:
					case ABSOLUTE_DSLASH:
					case WILDCARD:
					case PREFIX_WILDCARD:
					case FUNCTION:
					case UNARY_MINUS:
					case UNARY_PLUS:
					case VARIABLE_REF:
					case ELEMENT:
					case TEXT:
					case INLINE_FUNCTION_DECL:
					case LOOKUP:
					case ARRAY:
					case ATTRIBUTE_TEST:
					case COMP_ELEM_CONSTRUCTOR:
					case COMP_ATTR_CONSTRUCTOR:
					case COMP_TEXT_CONSTRUCTOR:
					case COMP_COMMENT_CONSTRUCTOR:
					case COMP_PI_CONSTRUCTOR:
					case COMP_NS_CONSTRUCTOR:
					case COMP_DOC_CONSTRUCTOR:
					case PRAGMA:
					case GTEQ:
					case SEQUENCE:
					case NCNAME:
					case EQ:
					case STRING_LITERAL:
					case LITERAL_element:
					case COMMA:
					case LCURLY:
					case STAR:
					case PLUS:
					case LITERAL_map:
					case LITERAL_try:
					case LITERAL_some:
					case LITERAL_every:
					case LITERAL_if:
					case LITERAL_switch:
					case LITERAL_typeswitch:
					case LITERAL_update:
					case LITERAL_preceding:
					case LITERAL_following:
					case UNION:
					case LITERAL_return:
					case LITERAL_or:
					case LITERAL_and:
					case LITERAL_instance:
					case LITERAL_treat:
					case LITERAL_castable:
					case LITERAL_cast:
					case BEFORE:
					case AFTER:
					case LITERAL_eq:
					case LITERAL_ne:
					case LITERAL_lt:
					case LITERAL_le:
					case LITERAL_gt:
					case LITERAL_ge:
					case GT:
					case NEQ:
					case LT:
					case LTEQ:
					case LITERAL_is:
					case CONCAT:
					case LITERAL_to:
					case MINUS:
					case LITERAL_div:
					case LITERAL_idiv:
					case LITERAL_mod:
					case BANG:
					case LITERAL_intersect:
					case LITERAL_except:
					case SLASH:
					case DSLASH:
					case LITERAL_text:
					case LITERAL_node:
					case LITERAL_attribute:
					case LITERAL_comment:
					case 190:
					case 191:
					case HASH:
					case SELF:
					case XML_COMMENT:
					case XML_PI:
					case AT:
					case PARENT:
					case LITERAL_child:
					case LITERAL_self:
					case LITERAL_descendant:
					case 204:
					case 205:
					case LITERAL_parent:
					case LITERAL_ancestor:
					case 208:
					case 209:
					case INTEGER_LITERAL:
					case DOUBLE_LITERAL:
					case DECIMAL_LITERAL:
					case XML_CDATA:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					step=expr(_t,catchExpr);
					_t = _retTree;
					
					catchExpr.setASTNode(astCatch);
					cond.addCatchClause(catchErrorList, catchVars, catchExpr);
					
					_t = __t151;
					_t = _t.getNextSibling();
				}
				else {
					if ( _cnt156>=1 ) { break _loop156; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt156++;
			} while (true);
			}
			
						step = cond;
					
			_t = __t149;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_return:
		{
			AST __t157 = _t;
			r = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_return);
			_t = _t.getFirstChild();
			
						List<ForLetClause> clauses= new ArrayList<ForLetClause>();
						Expression action= new PathExpr(context);
						action.setASTNode(r);
						PathExpr whereExpr= null;
						List<OrderSpec> orderBy= null;
					
			{
			int _cnt188=0;
			_loop188:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_for:
				{
					AST __t159 = _t;
					f = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_for);
					_t = _t.getFirstChild();
					{
					int _cnt166=0;
					_loop166:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==VARIABLE_BINDING)) {
							AST __t161 = _t;
							varName = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
							match(_t,VARIABLE_BINDING);
							_t = _t.getFirstChild();
							
														ForLetClause clause= new ForLetClause();
														clause.ast = varName;
														PathExpr inputSequence= new PathExpr(context);
													
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_as:
							{
								AST __t163 = _t;
								org.exist.xquery.parser.XQueryAST tmp8_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,LITERAL_as);
								_t = _t.getFirstChild();
								clause.sequenceType= new SequenceType();
								sequenceType(_t,clause.sequenceType);
								_t = _retTree;
								_t = __t163;
								_t = _t.getNextSibling();
								break;
							}
							case EOF:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case POSITIONAL_VAR:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case LITERAL_empty:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_empty:
							{
								org.exist.xquery.parser.XQueryAST tmp9_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,LITERAL_empty);
								_t = _t.getNextSibling();
								clause.allowEmpty = true;
								break;
							}
							case EOF:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case POSITIONAL_VAR:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case POSITIONAL_VAR:
							{
								posVar = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,POSITIONAL_VAR);
								_t = _t.getNextSibling();
								clause.posVar= posVar.getText();
								break;
							}
							case EOF:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							step=expr(_t,inputSequence);
							_t = _retTree;
							
														clause.varName= varName.getText();
														clause.inputSequence= inputSequence;
														clauses.add(clause);
													
							_t = __t161;
							_t = _t.getNextSibling();
						}
						else {
							if ( _cnt166>=1 ) { break _loop166; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt166++;
					} while (true);
					}
					_t = __t159;
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_let:
				{
					AST __t167 = _t;
					l = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_let);
					_t = _t.getFirstChild();
					{
					int _cnt172=0;
					_loop172:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==VARIABLE_BINDING)) {
							AST __t169 = _t;
							letVarName = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
							match(_t,VARIABLE_BINDING);
							_t = _t.getFirstChild();
							
														ForLetClause clause= new ForLetClause();
														clause.ast = letVarName;
														clause.type = FLWORClause.ClauseType.LET;
														PathExpr inputSequence= new PathExpr(context);
													
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_as:
							{
								AST __t171 = _t;
								org.exist.xquery.parser.XQueryAST tmp10_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,LITERAL_as);
								_t = _t.getFirstChild();
								clause.sequenceType= new SequenceType();
								sequenceType(_t,clause.sequenceType);
								_t = _retTree;
								_t = __t171;
								_t = _t.getNextSibling();
								break;
							}
							case EOF:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							step=expr(_t,inputSequence);
							_t = _retTree;
							
														clause.varName= letVarName.getText();
														clause.inputSequence= inputSequence;
														clauses.add(clause);
													
							_t = __t169;
							_t = _t.getNextSibling();
						}
						else {
							if ( _cnt172>=1 ) { break _loop172; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt172++;
					} while (true);
					}
					_t = __t167;
					_t = _t.getNextSibling();
					break;
				}
				case GROUP_BY:
				{
					AST __t173 = _t;
					gb = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,GROUP_BY);
					_t = _t.getFirstChild();
					
					ForLetClause clause= new ForLetClause();
					clause.ast = gb;
					clause.type = FLWORClause.ClauseType.GROUPBY;
					clause.groupSpecs = new ArrayList<GroupSpec>(4);
					clauses.add(clause);
					
					{
					int _cnt178=0;
					_loop178:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==VARIABLE_BINDING)) {
							AST __t175 = _t;
							groupVarName = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
							match(_t,VARIABLE_BINDING);
							_t = _t.getFirstChild();
							PathExpr groupSpecExpr= null;
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case EOF:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								groupSpecExpr = new PathExpr(context);
								step=expr(_t,groupSpecExpr);
								_t = _retTree;
								break;
							}
							case 3:
							case LITERAL_collation:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							
							QName groupKeyVar = QName.parse(staticContext, groupVarName.getText(), null);
							
							GroupSpec groupSpec= new GroupSpec(context, groupSpecExpr, groupKeyVar);
							clause.groupSpecs.add(groupSpec);
							
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_collation:
							{
								org.exist.xquery.parser.XQueryAST tmp11_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,LITERAL_collation);
								_t = _t.getNextSibling();
								groupCollURI = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,STRING_LITERAL);
								_t = _t.getNextSibling();
								
								groupSpec.setCollator(groupCollURI.getText());
								
								break;
							}
							case 3:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							_t = __t175;
							_t = _t.getNextSibling();
						}
						else {
							if ( _cnt178>=1 ) { break _loop178; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt178++;
					} while (true);
					}
					_t = __t173;
					_t = _t.getNextSibling();
					break;
				}
				case ORDER_BY:
				{
					AST __t179 = _t;
					ob = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,ORDER_BY);
					_t = _t.getFirstChild();
					orderBy = new ArrayList(3);
					{
					int _cnt186=0;
					_loop186:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_tokenSet_0.member(_t.getType()))) {
							PathExpr orderSpecExpr= new PathExpr(context);
							step=expr(_t,orderSpecExpr);
							_t = _retTree;
							
							OrderSpec orderSpec= new OrderSpec(context, orderSpecExpr);
							int modifiers= 0;
							boolean orderDescending = false;
							orderBy.add(orderSpec);
							
							if (!context.orderEmptyGreatest()) {
							modifiers |= OrderSpec.EMPTY_LEAST;
							orderSpec.setModifiers(modifiers);
							}
							
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_ascending:
							case LITERAL_descending:
							{
								{
								if (_t==null) _t=ASTNULL;
								switch ( _t.getType()) {
								case LITERAL_ascending:
								{
									org.exist.xquery.parser.XQueryAST tmp12_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
									match(_t,LITERAL_ascending);
									_t = _t.getNextSibling();
									break;
								}
								case LITERAL_descending:
								{
									org.exist.xquery.parser.XQueryAST tmp13_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
									match(_t,LITERAL_descending);
									_t = _t.getNextSibling();
									
									modifiers |= OrderSpec.DESCENDING_ORDER;
									orderSpec.setModifiers(modifiers);
									orderDescending = true;
									
									break;
								}
								default:
								{
									throw new NoViableAltException(_t);
								}
								}
								}
								break;
							}
							case EOF:
							case 3:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_collation:
							case LITERAL_element:
							case LITERAL_empty:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_empty:
							{
								org.exist.xquery.parser.XQueryAST tmp14_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,LITERAL_empty);
								_t = _t.getNextSibling();
								{
								if (_t==null) _t=ASTNULL;
								switch ( _t.getType()) {
								case LITERAL_greatest:
								{
									org.exist.xquery.parser.XQueryAST tmp15_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
									match(_t,LITERAL_greatest);
									_t = _t.getNextSibling();
									
									if (!context.orderEmptyGreatest())
									modifiers &= OrderSpec.EMPTY_GREATEST;
									if (orderDescending)
									modifiers |= OrderSpec.DESCENDING_ORDER;
									orderSpec.setModifiers(modifiers);
									
									break;
								}
								case LITERAL_least:
								{
									org.exist.xquery.parser.XQueryAST tmp16_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
									match(_t,LITERAL_least);
									_t = _t.getNextSibling();
									
									modifiers |= OrderSpec.EMPTY_LEAST;
									orderSpec.setModifiers(modifiers);
									
									break;
								}
								default:
								{
									throw new NoViableAltException(_t);
								}
								}
								}
								break;
							}
							case EOF:
							case 3:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_collation:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
							{
							if (_t==null) _t=ASTNULL;
							switch ( _t.getType()) {
							case LITERAL_collation:
							{
								org.exist.xquery.parser.XQueryAST tmp17_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,LITERAL_collation);
								_t = _t.getNextSibling();
								collURI = (org.exist.xquery.parser.XQueryAST)_t;
								match(_t,STRING_LITERAL);
								_t = _t.getNextSibling();
								
								orderSpec.setCollation(collURI.getText());
								
								break;
							}
							case EOF:
							case 3:
							case EQNAME:
							case PARENTHESIZED:
							case ABSOLUTE_SLASH:
							case ABSOLUTE_DSLASH:
							case WILDCARD:
							case PREFIX_WILDCARD:
							case FUNCTION:
							case UNARY_MINUS:
							case UNARY_PLUS:
							case VARIABLE_REF:
							case ELEMENT:
							case TEXT:
							case INLINE_FUNCTION_DECL:
							case LOOKUP:
							case ARRAY:
							case ATTRIBUTE_TEST:
							case COMP_ELEM_CONSTRUCTOR:
							case COMP_ATTR_CONSTRUCTOR:
							case COMP_TEXT_CONSTRUCTOR:
							case COMP_COMMENT_CONSTRUCTOR:
							case COMP_PI_CONSTRUCTOR:
							case COMP_NS_CONSTRUCTOR:
							case COMP_DOC_CONSTRUCTOR:
							case PRAGMA:
							case GTEQ:
							case SEQUENCE:
							case NCNAME:
							case EQ:
							case STRING_LITERAL:
							case LITERAL_element:
							case COMMA:
							case LCURLY:
							case STAR:
							case PLUS:
							case LITERAL_map:
							case LITERAL_try:
							case LITERAL_some:
							case LITERAL_every:
							case LITERAL_if:
							case LITERAL_switch:
							case LITERAL_typeswitch:
							case LITERAL_update:
							case LITERAL_preceding:
							case LITERAL_following:
							case UNION:
							case LITERAL_return:
							case LITERAL_or:
							case LITERAL_and:
							case LITERAL_instance:
							case LITERAL_treat:
							case LITERAL_castable:
							case LITERAL_cast:
							case BEFORE:
							case AFTER:
							case LITERAL_eq:
							case LITERAL_ne:
							case LITERAL_lt:
							case LITERAL_le:
							case LITERAL_gt:
							case LITERAL_ge:
							case GT:
							case NEQ:
							case LT:
							case LTEQ:
							case LITERAL_is:
							case CONCAT:
							case LITERAL_to:
							case MINUS:
							case LITERAL_div:
							case LITERAL_idiv:
							case LITERAL_mod:
							case BANG:
							case LITERAL_intersect:
							case LITERAL_except:
							case SLASH:
							case DSLASH:
							case LITERAL_text:
							case LITERAL_node:
							case LITERAL_attribute:
							case LITERAL_comment:
							case 190:
							case 191:
							case HASH:
							case SELF:
							case XML_COMMENT:
							case XML_PI:
							case AT:
							case PARENT:
							case LITERAL_child:
							case LITERAL_self:
							case LITERAL_descendant:
							case 204:
							case 205:
							case LITERAL_parent:
							case LITERAL_ancestor:
							case 208:
							case 209:
							case INTEGER_LITERAL:
							case DOUBLE_LITERAL:
							case DECIMAL_LITERAL:
							case XML_CDATA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(_t);
							}
							}
							}
						}
						else {
							if ( _cnt186>=1 ) { break _loop186; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt186++;
					} while (true);
					}
					
					ForLetClause clause= new ForLetClause();
					clause.ast = ob;
					clause.type = FLWORClause.ClauseType.ORDERBY;
												clause.orderSpecs = orderBy;
					clauses.add(clause);
					
					_t = __t179;
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_where:
				{
					AST __t187 = _t;
					w = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_where);
					_t = _t.getFirstChild();
					
										whereExpr= new PathExpr(context);
									
					step=expr(_t,whereExpr);
					_t = _retTree;
					
										ForLetClause clause = new ForLetClause();
										clause.ast = w;
										clause.type = FLWORClause.ClauseType.WHERE;
										clause.inputSequence = whereExpr;
										clauses.add(clause);
									
					_t = __t187;
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					if ( _cnt188>=1 ) { break _loop188; } else {throw new NoViableAltException(_t);}
				}
				}
				_cnt188++;
			} while (true);
			}
			step=expr(_t,(PathExpr) action);
			_t = _retTree;
			
						for (int i= clauses.size() - 1; i >= 0; i--) {
							ForLetClause clause= (ForLetClause) clauses.get(i);
							FLWORClause expr;
							switch (clause.type) {
							    case LET:
							        expr= new LetExpr(context);
							        break;
							    case GROUPBY:
			expr = new GroupByClause(context);
			break;
			case ORDERBY:
			expr = new OrderByClause(context, clause.orderSpecs);
			break;
									case WHERE:
											expr = new WhereClause(context, new DebuggableExpression(clause.inputSequence));
											break;
			default:
			expr= new ForExpr(context, clause.allowEmpty);
			break;
							}
							expr.setASTNode(clause.ast);
							if (clause.type == FLWORClause.ClauseType.FOR || clause.type == FLWORClause.ClauseType.LET) {
							    final BindingExpression bind = (BindingExpression)expr;
			bind.setVariable(clause.varName);
			bind.setSequenceType(clause.sequenceType);
			bind.setInputSequence(clause.inputSequence);
			if (clause.type == FLWORClause.ClauseType.FOR) {
			((ForExpr) bind).setPositionalVariable(clause.posVar);
									 }
							} else if (clause.type == FLWORClause.ClauseType.GROUPBY ) {
							    if (clause.groupSpecs != null) {
			GroupSpec specs[]= new GroupSpec[clause.groupSpecs.size()];
			int k= 0;
			for (GroupSpec groupSpec : clause.groupSpecs) {
			specs[k++]= groupSpec;
			}
			((GroupByClause)expr).setGroupSpecs(specs);
			}
							}
			if (!(action instanceof FLWORClause))
			expr.setReturnExpression(new DebuggableExpression(action));
			else {
			expr.setReturnExpression(action);
			((FLWORClause)action).setPreviousClause(expr);
			}
			
							action= expr;
						}
			
						path.add(action);
						step = action;
					
			_t = __t157;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_instance:
		{
			AST __t189 = _t;
			org.exist.xquery.parser.XQueryAST tmp18_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_instance);
			_t = _t.getFirstChild();
			
						PathExpr expr = new PathExpr(context);
						SequenceType type= new SequenceType();
					
			step=expr(_t,expr);
			_t = _retTree;
			sequenceType(_t,type);
			_t = _retTree;
			
						step = new InstanceOfExpression(context, expr, type);
						path.add(step);
					
			_t = __t189;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_treat:
		{
			AST __t190 = _t;
			org.exist.xquery.parser.XQueryAST tmp19_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_treat);
			_t = _t.getFirstChild();
			
						PathExpr expr = new PathExpr(context);
						SequenceType type= new SequenceType();
					
			step=expr(_t,expr);
			_t = _retTree;
			sequenceType(_t,type);
			_t = _retTree;
			
						step = new TreatAsExpression(context, expr, type);
						path.add(step);
					
			_t = __t190;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_switch:
		{
			AST __t191 = _t;
			switchAST = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_switch);
			_t = _t.getFirstChild();
			
						PathExpr operand = new PathExpr(context);
					
			step=expr(_t,operand);
			_t = _retTree;
			
						SwitchExpression switchExpr = new SwitchExpression(context, operand);
			switchExpr.setASTNode(switchAST);
						path.add(switchExpr);
					
			{
			int _cnt197=0;
			_loop197:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LITERAL_case)) {
					
									List caseOperands = new ArrayList<Expression>(2);
									PathExpr returnExpr = new PathExpr(context);
								
					{
					{
					int _cnt195=0;
					_loop195:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==LITERAL_case)) {
							PathExpr caseOperand = new PathExpr(context);
							org.exist.xquery.parser.XQueryAST tmp20_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
							match(_t,LITERAL_case);
							_t = _t.getNextSibling();
							expr(_t,caseOperand);
							_t = _retTree;
							caseOperands.add(caseOperand);
						}
						else {
							if ( _cnt195>=1 ) { break _loop195; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt195++;
					} while (true);
					}
					AST __t196 = _t;
					org.exist.xquery.parser.XQueryAST tmp21_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_return);
					_t = _t.getFirstChild();
					step=expr(_t,returnExpr);
					_t = _retTree;
					switchExpr.addCase(caseOperands, returnExpr);
					_t = __t196;
					_t = _t.getNextSibling();
					}
				}
				else {
					if ( _cnt197>=1 ) { break _loop197; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt197++;
			} while (true);
			}
			{
			org.exist.xquery.parser.XQueryAST tmp22_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_default);
			_t = _t.getNextSibling();
			
							PathExpr returnExpr = new PathExpr(context);
						
			step=expr(_t,returnExpr);
			_t = _retTree;
			
							switchExpr.setDefault(returnExpr);
						
			}
			step = switchExpr;
			_t = __t191;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_typeswitch:
		{
			AST __t199 = _t;
			org.exist.xquery.parser.XQueryAST tmp23_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_typeswitch);
			_t = _t.getFirstChild();
			
						PathExpr operand = new PathExpr(context);
					
			step=expr(_t,operand);
			_t = _retTree;
			
						TypeswitchExpression tswitch = new TypeswitchExpression(context, operand);
						path.add(tswitch);
					
			{
			int _cnt206=0;
			_loop206:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LITERAL_case)) {
					
									PathExpr returnExpr = new PathExpr(context);
									QName qn = null;
									List<SequenceType> types = new ArrayList<SequenceType>(2);
									SequenceType type = new SequenceType();
								
					AST __t201 = _t;
					org.exist.xquery.parser.XQueryAST tmp24_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_case);
					_t = _t.getFirstChild();
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case VARIABLE_BINDING:
					{
						var = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,VARIABLE_BINDING);
						_t = _t.getNextSibling();
						qn = QName.parse(staticContext, var.getText());
						break;
					}
					case FUNCTION_TEST:
					case MAP_TEST:
					case ARRAY_TEST:
					case ATOMIC_TYPE:
					case ATTRIBUTE_TEST:
					case LITERAL_element:
					case LITERAL_empty:
					case 111:
					case LITERAL_item:
					case LITERAL_text:
					case LITERAL_node:
					case LITERAL_comment:
					case 190:
					case 191:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					{
					int _cnt204=0;
					_loop204:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_tokenSet_1.member(_t.getType()))) {
							sequenceType(_t,type);
							_t = _retTree;
							
							types.add(type);
							type = new SequenceType();
							
						}
						else {
							if ( _cnt204>=1 ) { break _loop204; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt204++;
					} while (true);
					}
					AST __t205 = _t;
					org.exist.xquery.parser.XQueryAST tmp25_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_return);
					_t = _t.getFirstChild();
					step=expr(_t,returnExpr);
					_t = _retTree;
					
					SequenceType[] atype = new SequenceType[types.size()];
					atype = types.toArray(atype);
					tswitch.addCase(atype, qn, returnExpr);
					
					_t = __t205;
					_t = _t.getNextSibling();
					_t = __t201;
					_t = _t.getNextSibling();
				}
				else {
					if ( _cnt206>=1 ) { break _loop206; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt206++;
			} while (true);
			}
			{
			org.exist.xquery.parser.XQueryAST tmp26_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_default);
			_t = _t.getNextSibling();
			
							PathExpr returnExpr = new PathExpr(context);
							QName qn = null;
						
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case VARIABLE_BINDING:
			{
				dvar = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,VARIABLE_BINDING);
				_t = _t.getNextSibling();
				qn = QName.parse(staticContext, dvar.getText());
				break;
			}
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			step=expr(_t,returnExpr);
			_t = _retTree;
			
							tswitch.setDefault(qn, returnExpr);
						
			}
			step = tswitch;
			_t = __t199;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_or:
		{
			AST __t209 = _t;
			org.exist.xquery.parser.XQueryAST tmp27_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_or);
			_t = _t.getFirstChild();
			PathExpr left= new PathExpr(context);	
			step=expr(_t,left);
			_t = _retTree;
			PathExpr right= new PathExpr(context);
			step=expr(_t,right);
			_t = _retTree;
			_t = __t209;
			_t = _t.getNextSibling();
			
					OpOr or= new OpOr(context);
					or.addPath(left);
					or.addPath(right);
					path.addPath(or);
					step = or;
				
			break;
		}
		case LITERAL_and:
		{
			AST __t210 = _t;
			org.exist.xquery.parser.XQueryAST tmp28_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_and);
			_t = _t.getFirstChild();
			
						PathExpr left= new PathExpr(context);
						PathExpr right= new PathExpr(context);
					
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t210;
			_t = _t.getNextSibling();
			
					OpAnd and= new OpAnd(context);
					and.addPath(left);
					and.addPath(right);
					path.addPath(and);
					step = and;
				
			break;
		}
		case UNION:
		{
			AST __t211 = _t;
			org.exist.xquery.parser.XQueryAST tmp29_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,UNION);
			_t = _t.getFirstChild();
			
						PathExpr left= new PathExpr(context);
						PathExpr right= new PathExpr(context);
					
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t211;
			_t = _t.getNextSibling();
			
					Union union= new Union(context, left, right);
					path.add(union);
					step = union;
				
			break;
		}
		case LITERAL_intersect:
		{
			AST __t212 = _t;
			org.exist.xquery.parser.XQueryAST tmp30_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_intersect);
			_t = _t.getFirstChild();
			
						PathExpr left = new PathExpr(context);
						PathExpr right = new PathExpr(context);
					
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t212;
			_t = _t.getNextSibling();
			
					Intersect intersect = new Intersect(context, left, right);
					path.add(intersect);
					step = intersect;
				
			break;
		}
		case LITERAL_except:
		{
			AST __t213 = _t;
			org.exist.xquery.parser.XQueryAST tmp31_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_except);
			_t = _t.getFirstChild();
			
						PathExpr left = new PathExpr(context);
						PathExpr right = new PathExpr(context);
					
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t213;
			_t = _t.getNextSibling();
			
					Except intersect = new Except(context, left, right);
					path.add(intersect);
					step = intersect;
				
			break;
		}
		case ABSOLUTE_SLASH:
		{
			AST __t214 = _t;
			org.exist.xquery.parser.XQueryAST tmp32_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ABSOLUTE_SLASH);
			_t = _t.getFirstChild();
			
						RootNode root= new RootNode(context);
						path.add(root);
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				step=expr(_t,path);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t214;
			_t = _t.getNextSibling();
			break;
		}
		case ABSOLUTE_DSLASH:
		{
			AST __t216 = _t;
			org.exist.xquery.parser.XQueryAST tmp33_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ABSOLUTE_DSLASH);
			_t = _t.getFirstChild();
			
						RootNode root= new RootNode(context);
						path.add(root);
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				step=expr(_t,path);
				_t = _retTree;
				
								if (step instanceof LocationStep) {
									LocationStep s= (LocationStep) step;
									if (s.getAxis() == Constants.ATTRIBUTE_AXIS ||
										(s.getTest().getType() == Type.ATTRIBUTE && s.getAxis() == Constants.CHILD_AXIS))
										// combines descendant-or-self::node()/attribute:*
										s.setAxis(Constants.DESCENDANT_ATTRIBUTE_AXIS);
									else {
										s.setAxis(Constants.DESCENDANT_SELF_AXIS);
										s.setAbbreviated(true);
									}
								} else
									step.setPrimaryAxis(Constants.DESCENDANT_SELF_AXIS);
							
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t216;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_to:
		{
			AST __t218 = _t;
			org.exist.xquery.parser.XQueryAST tmp34_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_to);
			_t = _t.getFirstChild();
			
						PathExpr start= new PathExpr(context);
						PathExpr end= new PathExpr(context);
						List args= new ArrayList(2);
						args.add(start);
						args.add(end);
					
			step=expr(_t,start);
			_t = _retTree;
			step=expr(_t,end);
			_t = _retTree;
			
						RangeExpression range= new RangeExpression(context);
						range.setArguments(args);
						path.addPath(range);
						step = range;
					
			_t = __t218;
			_t = _t.getNextSibling();
			break;
		}
		case GTEQ:
		case EQ:
		case GT:
		case NEQ:
		case LT:
		case LTEQ:
		{
			step=generalComp(_t,path);
			_t = _retTree;
			break;
		}
		case LITERAL_eq:
		case LITERAL_ne:
		case LITERAL_lt:
		case LITERAL_le:
		case LITERAL_gt:
		case LITERAL_ge:
		{
			step=valueComp(_t,path);
			_t = _retTree;
			break;
		}
		case BEFORE:
		case AFTER:
		case LITERAL_is:
		{
			step=nodeComp(_t,path);
			_t = _retTree;
			break;
		}
		case PARENTHESIZED:
		case FUNCTION:
		case VARIABLE_REF:
		case ELEMENT:
		case TEXT:
		case INLINE_FUNCTION_DECL:
		case LOOKUP:
		case ARRAY:
		case COMP_ELEM_CONSTRUCTOR:
		case COMP_ATTR_CONSTRUCTOR:
		case COMP_TEXT_CONSTRUCTOR:
		case COMP_COMMENT_CONSTRUCTOR:
		case COMP_PI_CONSTRUCTOR:
		case COMP_NS_CONSTRUCTOR:
		case COMP_DOC_CONSTRUCTOR:
		case STRING_LITERAL:
		case LCURLY:
		case LITERAL_map:
		case HASH:
		case XML_COMMENT:
		case XML_PI:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		case XML_CDATA:
		{
			step=primaryExpr(_t,path);
			_t = _retTree;
			break;
		}
		case EQNAME:
		case WILDCARD:
		case PREFIX_WILDCARD:
		case ATTRIBUTE_TEST:
		case NCNAME:
		case LITERAL_element:
		case LITERAL_preceding:
		case LITERAL_following:
		case BANG:
		case SLASH:
		case DSLASH:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_attribute:
		case LITERAL_comment:
		case 190:
		case 191:
		case SELF:
		case AT:
		case PARENT:
		case LITERAL_child:
		case LITERAL_self:
		case LITERAL_descendant:
		case 204:
		case 205:
		case LITERAL_parent:
		case LITERAL_ancestor:
		case 208:
		case 209:
		{
			step=pathExpr(_t,path);
			_t = _retTree;
			break;
		}
		case PRAGMA:
		{
			step=extensionExpr(_t,path);
			_t = _retTree;
			break;
		}
		case UNARY_MINUS:
		case UNARY_PLUS:
		case STAR:
		case PLUS:
		case MINUS:
		case LITERAL_div:
		case LITERAL_idiv:
		case LITERAL_mod:
		{
			step=numericExpr(_t,path);
			_t = _retTree;
			break;
		}
		case LITERAL_update:
		{
			step=updateExpr(_t,path);
			_t = _retTree;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final void xpath(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST xpath_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		context.setRootExpression(path);
		
		try {      // for error handling
			module(_t,path);
			_t = _retTree;
			
					context.resolveForwardReferences();
				
		}
		catch (RecognitionException e) {
			handleException(e);
		}
		catch (EXistException e) {
			handleException(e);
		}
		catch (PermissionDeniedException e) {
			handleException(e);
		}
		_retTree = _t;
	}
	
	public final void module(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		
		org.exist.xquery.parser.XQueryAST module_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST v = null;
		org.exist.xquery.parser.XQueryAST enc = null;
		Expression step = null;
		
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case VERSION_DECL:
		{
			AST __t7 = _t;
			v = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,VERSION_DECL);
			_t = _t.getFirstChild();
			
				final String version = v.getText();
				if (version.equals("3.1")) {
					context.setXQueryVersion(31);
				} else if (version.equals("3.0")) {
			context.setXQueryVersion(30);
			} else if (version.equals("1.0")) {
			context.setXQueryVersion(10);
			} else {
			throw new XPathException(v, "err:XQST0031: Wrong XQuery version: require 1.0 or 3.0");
			}
			
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STRING_LITERAL:
			{
				enc = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
			if (enc != null) {
			if (!XMLChar.isValidIANAEncoding(enc.getText())) {
			throw new XPathException(enc, "err:XQST0087: Unknown or wrong encoding not adhering to required XML 1.0 EncName.");
			}
			if (!enc.getText().equals("UTF-8")) {
			//util.serializer.encodings.CharacterSet
			//context.setEncoding(enc.getText());
			}
			}
			
			_t = __t7;
			_t = _t.getNextSibling();
			break;
		}
		case EOF:
		case EQNAME:
		case PARENTHESIZED:
		case ABSOLUTE_SLASH:
		case ABSOLUTE_DSLASH:
		case WILDCARD:
		case PREFIX_WILDCARD:
		case FUNCTION:
		case UNARY_MINUS:
		case UNARY_PLUS:
		case VARIABLE_REF:
		case ELEMENT:
		case TEXT:
		case NAMESPACE_DECL:
		case DEF_NAMESPACE_DECL:
		case DEF_COLLATION_DECL:
		case DEF_FUNCTION_NS_DECL:
		case GLOBAL_VAR:
		case FUNCTION_DECL:
		case INLINE_FUNCTION_DECL:
		case LOOKUP:
		case ARRAY:
		case OPTION:
		case MODULE_DECL:
		case MODULE_IMPORT:
		case SCHEMA_IMPORT:
		case ATTRIBUTE_TEST:
		case COMP_ELEM_CONSTRUCTOR:
		case COMP_ATTR_CONSTRUCTOR:
		case COMP_TEXT_CONSTRUCTOR:
		case COMP_COMMENT_CONSTRUCTOR:
		case COMP_PI_CONSTRUCTOR:
		case COMP_NS_CONSTRUCTOR:
		case COMP_DOC_CONSTRUCTOR:
		case PRAGMA:
		case GTEQ:
		case SEQUENCE:
		case NCNAME:
		case EQ:
		case STRING_LITERAL:
		case 77:
		case LITERAL_ordering:
		case LITERAL_construction:
		case 80:
		case 81:
		case LITERAL_element:
		case LITERAL_order:
		case COMMA:
		case LCURLY:
		case STAR:
		case PLUS:
		case LITERAL_map:
		case LITERAL_try:
		case LITERAL_some:
		case LITERAL_every:
		case LITERAL_if:
		case LITERAL_switch:
		case LITERAL_typeswitch:
		case LITERAL_update:
		case LITERAL_preceding:
		case LITERAL_following:
		case UNION:
		case LITERAL_return:
		case LITERAL_or:
		case LITERAL_and:
		case LITERAL_instance:
		case LITERAL_treat:
		case LITERAL_castable:
		case LITERAL_cast:
		case BEFORE:
		case AFTER:
		case LITERAL_eq:
		case LITERAL_ne:
		case LITERAL_lt:
		case LITERAL_le:
		case LITERAL_gt:
		case LITERAL_ge:
		case GT:
		case NEQ:
		case LT:
		case LTEQ:
		case LITERAL_is:
		case CONCAT:
		case LITERAL_to:
		case MINUS:
		case LITERAL_div:
		case LITERAL_idiv:
		case LITERAL_mod:
		case BANG:
		case LITERAL_intersect:
		case LITERAL_except:
		case SLASH:
		case DSLASH:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_attribute:
		case LITERAL_comment:
		case 190:
		case 191:
		case HASH:
		case SELF:
		case XML_COMMENT:
		case XML_PI:
		case AT:
		case PARENT:
		case LITERAL_child:
		case LITERAL_self:
		case LITERAL_descendant:
		case 204:
		case 205:
		case LITERAL_parent:
		case LITERAL_ancestor:
		case 208:
		case 209:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		case XML_CDATA:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case MODULE_DECL:
		{
			libraryModule(_t,path);
			_t = _retTree;
			break;
		}
		case EOF:
		case EQNAME:
		case PARENTHESIZED:
		case ABSOLUTE_SLASH:
		case ABSOLUTE_DSLASH:
		case WILDCARD:
		case PREFIX_WILDCARD:
		case FUNCTION:
		case UNARY_MINUS:
		case UNARY_PLUS:
		case VARIABLE_REF:
		case ELEMENT:
		case TEXT:
		case NAMESPACE_DECL:
		case DEF_NAMESPACE_DECL:
		case DEF_COLLATION_DECL:
		case DEF_FUNCTION_NS_DECL:
		case GLOBAL_VAR:
		case FUNCTION_DECL:
		case INLINE_FUNCTION_DECL:
		case LOOKUP:
		case ARRAY:
		case OPTION:
		case MODULE_IMPORT:
		case SCHEMA_IMPORT:
		case ATTRIBUTE_TEST:
		case COMP_ELEM_CONSTRUCTOR:
		case COMP_ATTR_CONSTRUCTOR:
		case COMP_TEXT_CONSTRUCTOR:
		case COMP_COMMENT_CONSTRUCTOR:
		case COMP_PI_CONSTRUCTOR:
		case COMP_NS_CONSTRUCTOR:
		case COMP_DOC_CONSTRUCTOR:
		case PRAGMA:
		case GTEQ:
		case SEQUENCE:
		case NCNAME:
		case EQ:
		case STRING_LITERAL:
		case 77:
		case LITERAL_ordering:
		case LITERAL_construction:
		case 80:
		case 81:
		case LITERAL_element:
		case LITERAL_order:
		case COMMA:
		case LCURLY:
		case STAR:
		case PLUS:
		case LITERAL_map:
		case LITERAL_try:
		case LITERAL_some:
		case LITERAL_every:
		case LITERAL_if:
		case LITERAL_switch:
		case LITERAL_typeswitch:
		case LITERAL_update:
		case LITERAL_preceding:
		case LITERAL_following:
		case UNION:
		case LITERAL_return:
		case LITERAL_or:
		case LITERAL_and:
		case LITERAL_instance:
		case LITERAL_treat:
		case LITERAL_castable:
		case LITERAL_cast:
		case BEFORE:
		case AFTER:
		case LITERAL_eq:
		case LITERAL_ne:
		case LITERAL_lt:
		case LITERAL_le:
		case LITERAL_gt:
		case LITERAL_ge:
		case GT:
		case NEQ:
		case LT:
		case LTEQ:
		case LITERAL_is:
		case CONCAT:
		case LITERAL_to:
		case MINUS:
		case LITERAL_div:
		case LITERAL_idiv:
		case LITERAL_mod:
		case BANG:
		case LITERAL_intersect:
		case LITERAL_except:
		case SLASH:
		case DSLASH:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_attribute:
		case LITERAL_comment:
		case 190:
		case 191:
		case HASH:
		case SELF:
		case XML_COMMENT:
		case XML_PI:
		case AT:
		case PARENT:
		case LITERAL_child:
		case LITERAL_self:
		case LITERAL_descendant:
		case 204:
		case 205:
		case LITERAL_parent:
		case LITERAL_ancestor:
		case 208:
		case 209:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		case XML_CDATA:
		{
			mainModule(_t,path);
			_t = _retTree;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		_retTree = _t;
	}
	
	public final void libraryModule(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		
		org.exist.xquery.parser.XQueryAST libraryModule_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST m = null;
		org.exist.xquery.parser.XQueryAST uri = null;
		Expression step = null;
		
		AST __t11 = _t;
		m = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,MODULE_DECL);
		_t = _t.getFirstChild();
		uri = (org.exist.xquery.parser.XQueryAST)_t;
		match(_t,STRING_LITERAL);
		_t = _t.getNextSibling();
		
		if (myModule == null)
		myModule = new ExternalModuleImpl(uri.getText(), m.getText());
		else {
		myModule.setNamespace(m.getText(), uri.getText());
		if (myModule.getContext() != null)
				((ModuleContext)myModule.getContext()).setModuleNamespace(m.getText(), uri.getText());
		}
		myModule.setDescription(m.getDoc());
		context.declareNamespace(m.getText(), uri.getText());
		staticContext.declareNamespace(m.getText(), uri.getText());
		
		_t = __t11;
		_t = _t.getNextSibling();
		prolog(_t,path);
		_t = _retTree;
		_retTree = _t;
	}
	
	public final void mainModule(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		
		org.exist.xquery.parser.XQueryAST mainModule_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		Expression step = null;
		
		prolog(_t,path);
		_t = _retTree;
		step=expr(_t,path);
		_t = _retTree;
		_retTree = _t;
	}
	
/**
 * Process the XQuery prolog.
 */
	public final void prolog(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		
		org.exist.xquery.parser.XQueryAST prolog_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST prefix = null;
		org.exist.xquery.parser.XQueryAST uri = null;
		org.exist.xquery.parser.XQueryAST base = null;
		org.exist.xquery.parser.XQueryAST defu = null;
		org.exist.xquery.parser.XQueryAST deff = null;
		org.exist.xquery.parser.XQueryAST defc = null;
		org.exist.xquery.parser.XQueryAST qname = null;
		org.exist.xquery.parser.XQueryAST e = null;
		org.exist.xquery.parser.XQueryAST qname2 = null;
		org.exist.xquery.parser.XQueryAST content = null;
		Expression step = null;
		boolean boundaryspace = false;
		boolean defaultcollation = false;
		boolean orderempty = false;
		boolean copynamespaces = false;
		boolean baseuri = false;
		boolean ordering = false;
		boolean construction = false;
		
		
		
		{
		_loop37:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NAMESPACE_DECL:
			{
				AST __t15 = _t;
				prefix = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NAMESPACE_DECL);
				_t = _t.getFirstChild();
				uri = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				
								if (declaredNamespaces.get(prefix.getText()) != null)
									throw new XPathException(prefix, "err:XQST0033: Prolog contains " +
										"multiple declarations for namespace prefix: " + prefix.getText());
								context.declareNamespace(prefix.getText(), uri.getText());
								staticContext.declareNamespace(prefix.getText(), uri.getText());
								declaredNamespaces.put(prefix.getText(), uri.getText());
							
				_t = __t15;
				_t = _t.getNextSibling();
				break;
			}
			case 77:
			{
				AST __t16 = _t;
				org.exist.xquery.parser.XQueryAST tmp35_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,77);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_preserve:
				{
					org.exist.xquery.parser.XQueryAST tmp36_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_preserve);
					_t = _t.getNextSibling();
					
					if (boundaryspace)
										throw new XPathException("err:XQST0068: Boundary-space already declared.");
					boundaryspace = true;
					context.setStripWhitespace(false);
					
					break;
				}
				case LITERAL_strip:
				{
					org.exist.xquery.parser.XQueryAST tmp37_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_strip);
					_t = _t.getNextSibling();
					
					if (boundaryspace)
										throw new XPathException("err:XQST0068: Boundary-space already declared.");
					boundaryspace = true;
					context.setStripWhitespace(true);
					
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t16;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_order:
			{
				AST __t18 = _t;
				org.exist.xquery.parser.XQueryAST tmp38_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_order);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_greatest:
				{
					org.exist.xquery.parser.XQueryAST tmp39_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_greatest);
					_t = _t.getNextSibling();
					
					context.setOrderEmptyGreatest(true);
					
					break;
				}
				case LITERAL_least:
				{
					org.exist.xquery.parser.XQueryAST tmp40_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_least);
					_t = _t.getNextSibling();
					
					context.setOrderEmptyGreatest(false);
					
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				
				if (orderempty)
				throw new XPathException("err:XQST0065: Ordering mode already declared.");
				orderempty = true;
				
				_t = __t18;
				_t = _t.getNextSibling();
				break;
			}
			case 81:
			{
				try {      // for error handling
					AST __t20 = _t;
					org.exist.xquery.parser.XQueryAST tmp41_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,81);
					_t = _t.getFirstChild();
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case LITERAL_preserve:
					{
						org.exist.xquery.parser.XQueryAST tmp42_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,LITERAL_preserve);
						_t = _t.getNextSibling();
						
						staticContext.setPreserveNamespaces(true);
						context.setPreserveNamespaces(true);
						
						break;
					}
					case 99:
					{
						org.exist.xquery.parser.XQueryAST tmp43_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,99);
						_t = _t.getNextSibling();
						
						staticContext.setPreserveNamespaces(false);
						context.setPreserveNamespaces(false);
						
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case LITERAL_inherit:
					{
						org.exist.xquery.parser.XQueryAST tmp44_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,LITERAL_inherit);
						_t = _t.getNextSibling();
						
						staticContext.setInheritNamespaces(true);
						context.setInheritNamespaces(true);
						
						break;
					}
					case 101:
					{
						org.exist.xquery.parser.XQueryAST tmp45_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,101);
						_t = _t.getNextSibling();
						
						staticContext.setInheritNamespaces(false);
						context.setInheritNamespaces(false);
						
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					
					if (copynamespaces)
					throw new XPathException("err:XQST0055: Copy-namespaces mode already declared.");
					copynamespaces = true;
					
					_t = __t20;
					_t = _t.getNextSibling();
				}
				catch (RecognitionException se) {
					throw new XPathException("err:XPST0003: XQuery syntax error.");
				}
				break;
			}
			case 80:
			{
				AST __t23 = _t;
				org.exist.xquery.parser.XQueryAST tmp46_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,80);
				_t = _t.getFirstChild();
				base = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				
				context.setBaseURI(new AnyURIValue(StringValue.expand(base.getText())), true);
				if (baseuri)
				throw new XPathException(base, "err:XQST0032: Base URI is already declared.");
				baseuri = true;
				
				_t = __t23;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_ordering:
			{
				AST __t24 = _t;
				org.exist.xquery.parser.XQueryAST tmp47_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_ordering);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_ordered:
				{
					org.exist.xquery.parser.XQueryAST tmp48_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_ordered);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_unordered:
				{
					org.exist.xquery.parser.XQueryAST tmp49_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_unordered);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				
				// ignored
				if (ordering)
				throw new XPathException("err:XQST0065: Ordering already declared.");
				ordering = true;
				
				_t = __t24;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_construction:
			{
				AST __t26 = _t;
				org.exist.xquery.parser.XQueryAST tmp50_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_construction);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_preserve:
				{
					org.exist.xquery.parser.XQueryAST tmp51_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_preserve);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_strip:
				{
					org.exist.xquery.parser.XQueryAST tmp52_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_strip);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				
				// ignored
				if (construction)
				throw new XPathException("err:XQST0069: Construction already declared.");
				construction = true;
				
				_t = __t26;
				_t = _t.getNextSibling();
				break;
			}
			case DEF_NAMESPACE_DECL:
			{
				AST __t28 = _t;
				org.exist.xquery.parser.XQueryAST tmp53_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,DEF_NAMESPACE_DECL);
				_t = _t.getFirstChild();
				defu = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				// Use setDefaultElementNamespace()
				context.declareNamespace("", defu.getText());
				staticContext.declareNamespace("",defu.getText());
				
				_t = __t28;
				_t = _t.getNextSibling();
				break;
			}
			case DEF_FUNCTION_NS_DECL:
			{
				AST __t29 = _t;
				org.exist.xquery.parser.XQueryAST tmp54_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,DEF_FUNCTION_NS_DECL);
				_t = _t.getFirstChild();
				deff = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				
				context.setDefaultFunctionNamespace(deff.getText());
				staticContext.setDefaultFunctionNamespace(deff.getText());
				
				_t = __t29;
				_t = _t.getNextSibling();
				break;
			}
			case DEF_COLLATION_DECL:
			{
				AST __t30 = _t;
				org.exist.xquery.parser.XQueryAST tmp55_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,DEF_COLLATION_DECL);
				_t = _t.getFirstChild();
				defc = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				
				if (defaultcollation)
				throw new XPathException("err:XQST0038: Default collation already declared.");
				defaultcollation = true;
				try {
				context.setDefaultCollation(defc.getText());
				} catch (XPathException xp) {
				throw new XPathException(defc, "err:XQST0038: the value specified by a default collation declaration is not present in statically known collations.");
				}
				
				_t = __t30;
				_t = _t.getNextSibling();
				break;
			}
			case GLOBAL_VAR:
			{
				AST __t31 = _t;
				qname = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,GLOBAL_VAR);
				_t = _t.getFirstChild();
				
								PathExpr enclosed= new PathExpr(context);
								SequenceType type= null;
								QName qn = QName.parse(staticContext, qname.getText());
								if (declaredGlobalVars.contains(qn))
									throw new XPathException(qname, "err:XQST0049: It is a " +
										"static error if more than one variable declared or " +
										"imported by a module has the same expanded QName. " +
										"Variable: " + qn.toString());
								declaredGlobalVars.add(qn);
							
				List annots = new ArrayList();
				{
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_2.member(_t.getType()))) {
					annotations(_t,annots);
					_t = _retTree;
				}
				else if ((_tokenSet_3.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_as:
				{
					AST __t34 = _t;
					org.exist.xquery.parser.XQueryAST tmp56_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_as);
					_t = _t.getFirstChild();
					type= new SequenceType();
					sequenceType(_t,type);
					_t = _retTree;
					_t = __t34;
					_t = _t.getNextSibling();
					break;
				}
				case EOF:
				case EQNAME:
				case PARENTHESIZED:
				case ABSOLUTE_SLASH:
				case ABSOLUTE_DSLASH:
				case WILDCARD:
				case PREFIX_WILDCARD:
				case FUNCTION:
				case UNARY_MINUS:
				case UNARY_PLUS:
				case VARIABLE_REF:
				case ELEMENT:
				case TEXT:
				case INLINE_FUNCTION_DECL:
				case LOOKUP:
				case ARRAY:
				case ATTRIBUTE_TEST:
				case COMP_ELEM_CONSTRUCTOR:
				case COMP_ATTR_CONSTRUCTOR:
				case COMP_TEXT_CONSTRUCTOR:
				case COMP_COMMENT_CONSTRUCTOR:
				case COMP_PI_CONSTRUCTOR:
				case COMP_NS_CONSTRUCTOR:
				case COMP_DOC_CONSTRUCTOR:
				case PRAGMA:
				case GTEQ:
				case SEQUENCE:
				case NCNAME:
				case EQ:
				case STRING_LITERAL:
				case LITERAL_element:
				case COMMA:
				case LCURLY:
				case LITERAL_external:
				case STAR:
				case PLUS:
				case LITERAL_map:
				case LITERAL_try:
				case LITERAL_some:
				case LITERAL_every:
				case LITERAL_if:
				case LITERAL_switch:
				case LITERAL_typeswitch:
				case LITERAL_update:
				case LITERAL_preceding:
				case LITERAL_following:
				case UNION:
				case LITERAL_return:
				case LITERAL_or:
				case LITERAL_and:
				case LITERAL_instance:
				case LITERAL_treat:
				case LITERAL_castable:
				case LITERAL_cast:
				case BEFORE:
				case AFTER:
				case LITERAL_eq:
				case LITERAL_ne:
				case LITERAL_lt:
				case LITERAL_le:
				case LITERAL_gt:
				case LITERAL_ge:
				case GT:
				case NEQ:
				case LT:
				case LTEQ:
				case LITERAL_is:
				case CONCAT:
				case LITERAL_to:
				case MINUS:
				case LITERAL_div:
				case LITERAL_idiv:
				case LITERAL_mod:
				case BANG:
				case LITERAL_intersect:
				case LITERAL_except:
				case SLASH:
				case DSLASH:
				case LITERAL_text:
				case LITERAL_node:
				case LITERAL_attribute:
				case LITERAL_comment:
				case 190:
				case 191:
				case HASH:
				case SELF:
				case XML_COMMENT:
				case XML_PI:
				case AT:
				case PARENT:
				case LITERAL_child:
				case LITERAL_self:
				case LITERAL_descendant:
				case 204:
				case 205:
				case LITERAL_parent:
				case LITERAL_ancestor:
				case 208:
				case 209:
				case INTEGER_LITERAL:
				case DOUBLE_LITERAL:
				case DECIMAL_LITERAL:
				case XML_CDATA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EOF:
				case EQNAME:
				case PARENTHESIZED:
				case ABSOLUTE_SLASH:
				case ABSOLUTE_DSLASH:
				case WILDCARD:
				case PREFIX_WILDCARD:
				case FUNCTION:
				case UNARY_MINUS:
				case UNARY_PLUS:
				case VARIABLE_REF:
				case ELEMENT:
				case TEXT:
				case INLINE_FUNCTION_DECL:
				case LOOKUP:
				case ARRAY:
				case ATTRIBUTE_TEST:
				case COMP_ELEM_CONSTRUCTOR:
				case COMP_ATTR_CONSTRUCTOR:
				case COMP_TEXT_CONSTRUCTOR:
				case COMP_COMMENT_CONSTRUCTOR:
				case COMP_PI_CONSTRUCTOR:
				case COMP_NS_CONSTRUCTOR:
				case COMP_DOC_CONSTRUCTOR:
				case PRAGMA:
				case GTEQ:
				case SEQUENCE:
				case NCNAME:
				case EQ:
				case STRING_LITERAL:
				case LITERAL_element:
				case COMMA:
				case LCURLY:
				case STAR:
				case PLUS:
				case LITERAL_map:
				case LITERAL_try:
				case LITERAL_some:
				case LITERAL_every:
				case LITERAL_if:
				case LITERAL_switch:
				case LITERAL_typeswitch:
				case LITERAL_update:
				case LITERAL_preceding:
				case LITERAL_following:
				case UNION:
				case LITERAL_return:
				case LITERAL_or:
				case LITERAL_and:
				case LITERAL_instance:
				case LITERAL_treat:
				case LITERAL_castable:
				case LITERAL_cast:
				case BEFORE:
				case AFTER:
				case LITERAL_eq:
				case LITERAL_ne:
				case LITERAL_lt:
				case LITERAL_le:
				case LITERAL_gt:
				case LITERAL_ge:
				case GT:
				case NEQ:
				case LT:
				case LTEQ:
				case LITERAL_is:
				case CONCAT:
				case LITERAL_to:
				case MINUS:
				case LITERAL_div:
				case LITERAL_idiv:
				case LITERAL_mod:
				case BANG:
				case LITERAL_intersect:
				case LITERAL_except:
				case SLASH:
				case DSLASH:
				case LITERAL_text:
				case LITERAL_node:
				case LITERAL_attribute:
				case LITERAL_comment:
				case 190:
				case 191:
				case HASH:
				case SELF:
				case XML_COMMENT:
				case XML_PI:
				case AT:
				case PARENT:
				case LITERAL_child:
				case LITERAL_self:
				case LITERAL_descendant:
				case 204:
				case 205:
				case LITERAL_parent:
				case LITERAL_ancestor:
				case 208:
				case 209:
				case INTEGER_LITERAL:
				case DOUBLE_LITERAL:
				case DECIMAL_LITERAL:
				case XML_CDATA:
				{
					e = _t==ASTNULL ? null : (org.exist.xquery.parser.XQueryAST)_t;
					step=expr(_t,enclosed);
					_t = _retTree;
					
										final VariableDeclaration decl= new VariableDeclaration(context, qn, enclosed);
										decl.setSequenceType(type);
										decl.setASTNode(e);
										path.add(decl);
										if(myModule != null) {
											myModule.declareVariable(qn, decl);
										}
									
					break;
				}
				case LITERAL_external:
				{
					org.exist.xquery.parser.XQueryAST tmp57_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_external);
					_t = _t.getNextSibling();
					
									    Variable decl = null;
										boolean isDeclared = false;
										try {
											decl = context.resolveVariable(qname.getText());
											isDeclared = (decl != null);
										} catch (XPathException ignoredException) {
										}
					
										if (!isDeclared)
					decl = context.declareVariable(qname.getText(), null);
					
					if (decl != null)
					decl.setSequenceType(type);
									
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t31;
				_t = _t.getNextSibling();
				break;
			}
			case OPTION:
			{
				AST __t36 = _t;
				qname2 = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,OPTION);
				_t = _t.getFirstChild();
				content = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				
								try {
									context.addOption(qname2.getText(), content.getText());
								} catch(XPathException e2) {
									e2.setLocation(qname2.getLine(), qname2.getColumn());
				throw e2;
								}
							
				_t = __t36;
				_t = _t.getNextSibling();
				break;
			}
			case FUNCTION_DECL:
			{
				functionDecl(_t,path);
				_t = _retTree;
				break;
			}
			case MODULE_IMPORT:
			case SCHEMA_IMPORT:
			{
				importDecl(_t,path);
				_t = _retTree;
				break;
			}
			default:
			{
				break _loop37;
			}
			}
		} while (true);
		}
		_retTree = _t;
	}
	
/** Parse a declared set of annotation associated to a function declaration or
 * a variable declaration
 * (distinction is made via one of the two parameters set to null)
 */
	public final void annotations(AST _t,
		List annots
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST annotations_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		
		List annotList = null;
		
		
		{
		_loop47:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==ANNOT_DECL)) {
				annotList = new ArrayList();
				annotation(_t,annotList);
				_t = _retTree;
				if (annotList.size() != 0)
				annots.add(annotList);
				
			}
			else {
				break _loop47;
			}
			
		} while (true);
		}
		_retTree = _t;
	}
	
/**
 * A sequence type declaration.
 */
	public final void sequenceType(AST _t,
		SequenceType type
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST sequenceType_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST t = null;
		org.exist.xquery.parser.XQueryAST lelement = null;
		org.exist.xquery.parser.XQueryAST eq1 = null;
		org.exist.xquery.parser.XQueryAST eq12 = null;
		org.exist.xquery.parser.XQueryAST lattr = null;
		org.exist.xquery.parser.XQueryAST eq2 = null;
		org.exist.xquery.parser.XQueryAST eq21 = null;
		org.exist.xquery.parser.XQueryAST nc = null;
		org.exist.xquery.parser.XQueryAST sl = null;
		org.exist.xquery.parser.XQueryAST lelement2 = null;
		org.exist.xquery.parser.XQueryAST dneq = null;
		org.exist.xquery.parser.XQueryAST dneq2 = null;
		
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ATOMIC_TYPE:
		{
			AST __t91 = _t;
			t = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ATOMIC_TYPE);
			_t = _t.getFirstChild();
			
							QName qn= QName.parse(staticContext, t.getText());
							int code= Type.getType(qn);
							if(!Type.subTypeOf(code, Type.ATOMIC))
								throw new XPathException(t, "Type " + qn.toString() + " is not an atomic type");
							type.setPrimaryType(code);
						
			_t = __t91;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_empty:
		{
			AST __t92 = _t;
			org.exist.xquery.parser.XQueryAST tmp58_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_empty);
			_t = _t.getFirstChild();
			
							type.setPrimaryType(Type.EMPTY);
							type.setCardinality(Cardinality.EMPTY);
						
			_t = __t92;
			_t = _t.getNextSibling();
			break;
		}
		case 111:
		{
			AST __t93 = _t;
			org.exist.xquery.parser.XQueryAST tmp59_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,111);
			_t = _t.getFirstChild();
			
							type.setPrimaryType(Type.EMPTY);
							type.setCardinality(Cardinality.EMPTY);
						
			_t = __t93;
			_t = _t.getNextSibling();
			break;
		}
		case FUNCTION_TEST:
		{
			AST __t94 = _t;
			org.exist.xquery.parser.XQueryAST tmp60_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,FUNCTION_TEST);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.FUNCTION_REFERENCE);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STAR:
			{
				org.exist.xquery.parser.XQueryAST tmp61_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STAR);
				_t = _t.getNextSibling();
				break;
			}
			case FUNCTION_TEST:
			case MAP_TEST:
			case ARRAY_TEST:
			case ATOMIC_TYPE:
			case ATTRIBUTE_TEST:
			case LITERAL_element:
			case LITERAL_empty:
			case LITERAL_as:
			case 111:
			case LITERAL_item:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_comment:
			case 190:
			case 191:
			{
				{
				List<SequenceType> paramTypes = new ArrayList<SequenceType>(5);
				{
				_loop98:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_1.member(_t.getType()))) {
						SequenceType paramType = new SequenceType();
						sequenceType(_t,paramType);
						_t = _retTree;
						paramTypes.add(paramType);
					}
					else {
						break _loop98;
					}
					
				} while (true);
				}
				SequenceType returnType = new SequenceType();
				org.exist.xquery.parser.XQueryAST tmp62_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_as);
				_t = _t.getNextSibling();
				sequenceType(_t,returnType);
				_t = _retTree;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t94;
			_t = _t.getNextSibling();
			break;
		}
		case MAP_TEST:
		{
			AST __t99 = _t;
			org.exist.xquery.parser.XQueryAST tmp63_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,MAP_TEST);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.MAP);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STAR:
			{
				org.exist.xquery.parser.XQueryAST tmp64_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STAR);
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			case FUNCTION_TEST:
			case MAP_TEST:
			case ARRAY_TEST:
			case ATOMIC_TYPE:
			case ATTRIBUTE_TEST:
			case LITERAL_element:
			case LITERAL_empty:
			case 111:
			case LITERAL_item:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_comment:
			case 190:
			case 191:
			{
				{
				List<SequenceType> paramTypes = new ArrayList<SequenceType>(5);
				{
				_loop103:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_1.member(_t.getType()))) {
						SequenceType paramType = new SequenceType();
						sequenceType(_t,paramType);
						_t = _retTree;
						paramTypes.add(paramType);
					}
					else {
						break _loop103;
					}
					
				} while (true);
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t99;
			_t = _t.getNextSibling();
			break;
		}
		case ARRAY_TEST:
		{
			AST __t104 = _t;
			org.exist.xquery.parser.XQueryAST tmp65_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ARRAY_TEST);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.ARRAY);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STAR:
			{
				org.exist.xquery.parser.XQueryAST tmp66_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STAR);
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			case FUNCTION_TEST:
			case MAP_TEST:
			case ARRAY_TEST:
			case ATOMIC_TYPE:
			case ATTRIBUTE_TEST:
			case LITERAL_element:
			case LITERAL_empty:
			case 111:
			case LITERAL_item:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_comment:
			case 190:
			case 191:
			{
				{
				List<SequenceType> paramTypes = new ArrayList<SequenceType>(5);
				{
				_loop108:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_1.member(_t.getType()))) {
						SequenceType paramType = new SequenceType();
						sequenceType(_t,paramType);
						_t = _retTree;
						paramTypes.add(paramType);
					}
					else {
						break _loop108;
					}
					
				} while (true);
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t104;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_item:
		{
			AST __t109 = _t;
			org.exist.xquery.parser.XQueryAST tmp67_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_item);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.ITEM);
			_t = __t109;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_node:
		{
			AST __t110 = _t;
			org.exist.xquery.parser.XQueryAST tmp68_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_node);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.NODE);
			_t = __t110;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_element:
		{
			AST __t111 = _t;
			lelement = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_element);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.ELEMENT);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case WILDCARD:
			{
				org.exist.xquery.parser.XQueryAST tmp69_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,WILDCARD);
				_t = _t.getNextSibling();
				break;
			}
			case EQNAME:
			{
				eq1 = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,EQNAME);
				_t = _t.getNextSibling();
				
									try {
										QName qname= QName.parse(staticContext, eq1.getText());
										type.setNodeName(qname);
									} catch (XPathException e) {
										e.setLocation(lelement.getLine(), lelement.getColumn());
					throw e;
									}
								
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EQNAME:
				{
					eq12 = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,EQNAME);
					_t = _t.getNextSibling();
					
											try {
						                        QName qname12= QName.parse(staticContext, eq12.getText());
						                        TypeTest test = new TypeTest(Type.getType(qname12));
						                    } catch (XPathException e) {
												e.setLocation(lelement.getLine(), lelement.getColumn());
						                    	throw e;
											}
										
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t111;
			_t = _t.getNextSibling();
			break;
		}
		case ATTRIBUTE_TEST:
		{
			AST __t114 = _t;
			lattr = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ATTRIBUTE_TEST);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.ATTRIBUTE);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQNAME:
			{
				eq2 = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,EQNAME);
				_t = _t.getNextSibling();
				
									try {
				QName qname = QName.parse(staticContext, eq2.getText(), XMLConstants.DEFAULT_NS_PREFIX);
				qname = new QName(qname, ElementValue.ATTRIBUTE);
										type.setNodeName(qname);
									} catch (XPathException e) {
										e.setLocation(lattr.getLine(), lattr.getColumn());
					throw e;
									}
								
				break;
			}
			case WILDCARD:
			{
				org.exist.xquery.parser.XQueryAST tmp70_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,WILDCARD);
				_t = _t.getNextSibling();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EQNAME:
				{
					eq21 = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,EQNAME);
					_t = _t.getNextSibling();
					
											try {
						                        QName qname21= QName.parse(staticContext, eq21.getText());
						                        TypeTest test = new TypeTest(Type.getType(qname21));
						                    } catch (XPathException e) {
							                    e.setLocation(lattr.getLine(), lattr.getColumn());
						                    	throw e;
						                    }
										
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t114;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_text:
		{
			AST __t117 = _t;
			org.exist.xquery.parser.XQueryAST tmp71_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_text);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.TEXT);
			_t = __t117;
			_t = _t.getNextSibling();
			break;
		}
		case 190:
		{
			AST __t118 = _t;
			org.exist.xquery.parser.XQueryAST tmp72_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,190);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.PROCESSING_INSTRUCTION);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NCNAME:
			{
				nc = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getNextSibling();
				break;
			}
			case STRING_LITERAL:
			{
				sl = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
			String value = "";
			if (nc != null)
			value = nc.getText();
			if (sl != null)
			value = sl.getText();
			QName qname= new QName.WildcardNamespaceURIQName(value);
			if (!"".equals(value))
			type.setNodeName(qname);
			
			_t = __t118;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_comment:
		{
			AST __t120 = _t;
			org.exist.xquery.parser.XQueryAST tmp73_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_comment);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.COMMENT);
			_t = __t120;
			_t = _t.getNextSibling();
			break;
		}
		case 191:
		{
			AST __t121 = _t;
			org.exist.xquery.parser.XQueryAST tmp74_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,191);
			_t = _t.getFirstChild();
			type.setPrimaryType(Type.DOCUMENT);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_element:
			{
				AST __t123 = _t;
				lelement2 = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_element);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EQNAME:
				{
					dneq = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,EQNAME);
					_t = _t.getNextSibling();
					
						try {
											    QName qname= QName.parse(staticContext, dneq.getText());
						                        type.setNodeName(qname);
						                        NameTest test= new NameTest(Type.DOCUMENT, qname);
						                    } catch(XPathException e) {
						                    	e.setLocation(lelement2.getLine(), lelement2.getColumn());
						                    	throw e;
						                    }
					
					break;
				}
				case WILDCARD:
				{
					org.exist.xquery.parser.XQueryAST tmp75_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,WILDCARD);
					_t = _t.getNextSibling();
					
					TypeTest test= new TypeTest(Type.DOCUMENT);
					
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case EQNAME:
					{
						dneq2 = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,EQNAME);
						_t = _t.getNextSibling();
						
							try {
								                            QName qname = QName.parse(staticContext, dneq2.getText());
								                            test = new TypeTest(Type.getType(qname));
								                        } catch(XPathException e) {
								                        	e.setLocation(lelement2.getLine(), lelement2.getColumn());
							                    			throw e;
								                        }
						
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t123;
				_t = _t.getNextSibling();
				break;
			}
			case 213:
			{
				AST __t126 = _t;
				org.exist.xquery.parser.XQueryAST tmp76_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,213);
				_t = _t.getFirstChild();
				org.exist.xquery.parser.XQueryAST tmp77_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,EQNAME);
				_t = _t.getNextSibling();
				_t = __t126;
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t121;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case STAR:
		{
			org.exist.xquery.parser.XQueryAST tmp78_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,STAR);
			_t = _t.getNextSibling();
			type.setCardinality(Cardinality.ZERO_OR_MORE);
			break;
		}
		case PLUS:
		{
			org.exist.xquery.parser.XQueryAST tmp79_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,PLUS);
			_t = _t.getNextSibling();
			type.setCardinality(Cardinality.ONE_OR_MORE);
			break;
		}
		case QUESTION:
		{
			org.exist.xquery.parser.XQueryAST tmp80_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,QUESTION);
			_t = _t.getNextSibling();
			type.setCardinality(Cardinality.ZERO_OR_ONE);
			break;
		}
		case 3:
		case FUNCTION_TEST:
		case MAP_TEST:
		case ARRAY_TEST:
		case ATOMIC_TYPE:
		case ATTRIBUTE_TEST:
		case LITERAL_element:
		case LITERAL_empty:
		case LITERAL_as:
		case 111:
		case LITERAL_item:
		case LITERAL_return:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_comment:
		case 190:
		case 191:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		_retTree = _t;
	}
	
/**
 * Parse a function declared in the prolog.
 */
	public final Expression  functionDecl(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST functionDecl_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST name = null;
		step = null;
		
		AST __t54 = _t;
		name = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,FUNCTION_DECL);
		_t = _t.getFirstChild();
		
					PathExpr body= new PathExpr(context);
				
		
					QName qn= null;
					try {
		qn = QName.parse(staticContext, name.getText(), staticContext.getDefaultFunctionNamespace());
					} catch(XPathException e) {
						// throw exception with correct source location
						e.setLocation(name.getLine(), name.getColumn());
						throw e;
					}
					FunctionSignature signature= new FunctionSignature(qn);
					signature.setDescription(name.getDoc());
					UserDefinedFunction func= new UserDefinedFunction(context, signature);
					func.setASTNode(name);
					List varList= new ArrayList(3);
				
		List annots = new ArrayList();
		{
		if (_t==null) _t=ASTNULL;
		if ((_tokenSet_4.member(_t.getType()))) {
			annotations(_t,annots);
			_t = _retTree;
			
			processAnnotations(annots, signature);
			
		}
		else if ((_tokenSet_5.member(_t.getType()))) {
		}
		else {
			throw new NoViableAltException(_t);
		}
		
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case VARIABLE_BINDING:
		{
			paramList(_t,varList);
			_t = _retTree;
			break;
		}
		case LCURLY:
		case LITERAL_external:
		case LITERAL_as:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		
					processParams(varList, func, signature);
				
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_as:
		{
			AST __t58 = _t;
			org.exist.xquery.parser.XQueryAST tmp81_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_as);
			_t = _t.getFirstChild();
			SequenceType type= new SequenceType();
			sequenceType(_t,type);
			_t = _retTree;
			signature.setReturnType(type);
			_t = __t58;
			_t = _t.getNextSibling();
			break;
		}
		case LCURLY:
		case LITERAL_external:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LCURLY:
		{
			AST __t60 = _t;
			org.exist.xquery.parser.XQueryAST tmp82_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LCURLY);
			_t = _t.getFirstChild();
			expr(_t,body);
			_t = _retTree;
			
								func.setFunctionBody(body);
			context.declareFunction(func);
			if(myModule != null)
			myModule.declareFunction(func);
							
			_t = __t60;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_external:
		{
			org.exist.xquery.parser.XQueryAST tmp83_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_external);
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		_t = __t54;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
	public final void importDecl(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		
		org.exist.xquery.parser.XQueryAST importDecl_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST i = null;
		org.exist.xquery.parser.XQueryAST pfx = null;
		org.exist.xquery.parser.XQueryAST moduleURI = null;
		org.exist.xquery.parser.XQueryAST s = null;
		org.exist.xquery.parser.XQueryAST pfx1 = null;
		org.exist.xquery.parser.XQueryAST targetURI = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case MODULE_IMPORT:
		{
			AST __t39 = _t;
			i = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,MODULE_IMPORT);
			_t = _t.getFirstChild();
			
						String modulePrefix = null;
						String location = null;
			List uriList= new ArrayList(2);
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NCNAME:
			{
				pfx = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getNextSibling();
				modulePrefix = pfx.getText();
				break;
			}
			case STRING_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			moduleURI = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,STRING_LITERAL);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STRING_LITERAL:
			{
				uriList(_t,uriList);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
						if (modulePrefix != null) {
							if (declaredNamespaces.get(modulePrefix) != null)
								throw new XPathException(i, "err:XQST0033: Prolog contains " +
									"multiple declarations for namespace prefix: " + modulePrefix);
							declaredNamespaces.put(modulePrefix, moduleURI.getText());
						}
			try {
			if (uriList.size() > 0) {
						    for (Iterator j= uriList.iterator(); j.hasNext();) {
			try {
			location= ((AnyURIValue) j.next()).getStringValue();
			context.importModule(moduleURI.getText(), modulePrefix, location);
			staticContext.declareNamespace(modulePrefix, moduleURI.getText());
			} catch(XPathException xpe) {
			if (!j.hasNext()) {
			throw xpe;
			}
			}
			}
			} else {
			context.importModule(moduleURI.getText(), modulePrefix, location);
			staticContext.declareNamespace(modulePrefix, moduleURI.getText());
			}
			} catch(XPathException xpe) {
			xpe.prependMessage("error found while loading module " + modulePrefix + ": ");
			throw xpe;
			}
					
			_t = __t39;
			_t = _t.getNextSibling();
			break;
		}
		case SCHEMA_IMPORT:
		{
			AST __t42 = _t;
			s = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,SCHEMA_IMPORT);
			_t = _t.getFirstChild();
			
						String nsPrefix = null;
						String location = null;
						boolean defaultElementNS = false;
			List uriList= new ArrayList(2);
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NCNAME:
			{
				pfx1 = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getNextSibling();
				nsPrefix = pfx1.getText();
				break;
			}
			case LITERAL_default:
			{
				org.exist.xquery.parser.XQueryAST tmp84_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_default);
				_t = _t.getNextSibling();
				org.exist.xquery.parser.XQueryAST tmp85_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_element);
				_t = _t.getNextSibling();
				org.exist.xquery.parser.XQueryAST tmp86_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_namespace);
				_t = _t.getNextSibling();
				defaultElementNS = true;
				break;
			}
			case STRING_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			targetURI = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,STRING_LITERAL);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STRING_LITERAL:
			{
				uriList(_t,uriList);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
			if ("".equals(targetURI.getText()) && nsPrefix != null) {
			throw new XPathException(s, "err:XQST0057: A schema without target namespace (zero-length string target namespace) may not bind a namespace prefix: " + nsPrefix);
			}
			if (nsPrefix != null) {
			if (declaredNamespaces.get(nsPrefix) != null)
			throw new XPathException(s, "err:XQST0033: Prolog contains " +
			"multiple declarations for namespace prefix: " + nsPrefix);
			declaredNamespaces.put(nsPrefix, targetURI.getText());
			}
			try {
			context.declareNamespace(nsPrefix, targetURI.getText());
			staticContext.declareNamespace(nsPrefix, targetURI.getText());
			// We currently do nothing with eventual location hints. /ljo
			} catch(XPathException xpe) {
			xpe.prependMessage("err:XQST0059: Error found while loading schema " + nsPrefix + ": ");
			throw xpe;
			}
			// We ought to do this for now until Dannes can say it works. /ljo
			//throw new XPathException(s, "err:XQST0009: the eXist XQuery implementation does not support the Schema Import Feature quite yet.");
					
			_t = __t42;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
	}
	
/**
 * Parse uris in schema and module declarations.
 */
	public final void uriList(AST _t,
		List uris
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST uriList_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		
		uri(_t,uris);
		_t = _retTree;
		{
		_loop77:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==STRING_LITERAL)) {
				uri(_t,uris);
				_t = _retTree;
			}
			else {
				break _loop77;
			}
			
		} while (true);
		}
		_retTree = _t;
	}
	
	public final void annotation(AST _t,
		List annotList
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST annotation_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST name = null;
		Expression le = null;
		
		AST __t49 = _t;
		name = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,ANNOT_DECL);
		_t = _t.getFirstChild();
		
		QName qn= null;
		try {
		qn = QName.parse(staticContext, name.getText(), staticContext.getDefaultFunctionNamespace());
		} catch(XPathException e) {
		// throw exception with correct source location
		e.setLocation(name.getLine(), name.getColumn());
		throw e;
		}
		
		String ns = qn.getNamespaceURI();
		
		if(!annotationValid(qn)) {
		XPathException e = new XPathException(ErrorCodes.XQST0045,
									"Annotation in a reserved namespace: " + qn.getNamespaceURI());
								e.setLocation(name.getLine(), name.getColumn());
								throw e;
		}
		
		annotList.add(qn);
		
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case STRING_LITERAL:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		{
			PathExpr annotPath = new PathExpr(context);
			le=literalExpr(_t,annotPath);
			_t = _retTree;
			annotPath.add(le);
			{
			_loop52:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_6.member(_t.getType()))) {
					le=literalExpr(_t,annotPath);
					_t = _retTree;
					annotPath.add(le);
				}
				else {
					break _loop52;
				}
				
			} while (true);
			}
			annotList.add(annotPath);
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		_t = __t49;
		_t = _t.getNextSibling();
		_retTree = _t;
	}
	
	public final Expression  literalExpr(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST literalExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST c = null;
		org.exist.xquery.parser.XQueryAST i = null;
		org.exist.xquery.parser.XQueryAST dec = null;
		org.exist.xquery.parser.XQueryAST dbl = null;
		step= null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case STRING_LITERAL:
		{
			c = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,STRING_LITERAL);
			_t = _t.getNextSibling();
			
					StringValue val = new StringValue(c.getText());
					val.expand();
			step= new LiteralValue(context, val);
			step.setASTNode(c);
			
			break;
		}
		case INTEGER_LITERAL:
		{
			i = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,INTEGER_LITERAL);
			_t = _t.getNextSibling();
			
			step= new LiteralValue(context, new IntegerValue(i.getText()));
			step.setASTNode(i);
			
			break;
		}
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		{
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case DECIMAL_LITERAL:
			{
				dec = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,DECIMAL_LITERAL);
				_t = _t.getNextSibling();
				
				step= new LiteralValue(context, new DecimalValue(dec.getText()));
				step.setASTNode(dec);
				
				break;
			}
			case DOUBLE_LITERAL:
			{
				dbl = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,DOUBLE_LITERAL);
				_t = _t.getNextSibling();
				
				step= new LiteralValue(context,
				new DoubleValue(Double.parseDouble(dbl.getText())));
				step.setASTNode(dbl);
				
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
/**
 * Parse params in function declaration.
 */
	public final void paramList(AST _t,
		List vars
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST paramList_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		
		param(_t,vars);
		_t = _retTree;
		{
		_loop70:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==VARIABLE_BINDING)) {
				param(_t,vars);
				_t = _retTree;
			}
			else {
				break _loop70;
			}
			
		} while (true);
		}
		_retTree = _t;
	}
	
/**
 * Parse an inline function declaration.
 */
	public final Expression  inlineFunctionDecl(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST inlineFunctionDecl_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST name = null;
		step = null;
		
		AST __t62 = _t;
		name = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,INLINE_FUNCTION_DECL);
		_t = _t.getFirstChild();
		
					PathExpr body= new PathExpr(context);
				
		
					FunctionSignature signature= new FunctionSignature(InlineFunction.INLINE_FUNCTION_QNAME);
					signature.setDescription(name.getDoc());
					UserDefinedFunction func= new UserDefinedFunction(context, signature);
					func.setASTNode(name);
					List varList= new ArrayList(3);
				
		List annots = new ArrayList();
		{
		if (_t==null) _t=ASTNULL;
		if ((_tokenSet_7.member(_t.getType()))) {
			annotations(_t,annots);
			_t = _retTree;
			
			processAnnotations(annots, signature);
			
		}
		else if ((_t.getType()==VARIABLE_BINDING||_t.getType()==LCURLY||_t.getType()==LITERAL_as)) {
		}
		else {
			throw new NoViableAltException(_t);
		}
		
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case VARIABLE_BINDING:
		{
			paramList(_t,varList);
			_t = _retTree;
			break;
		}
		case LCURLY:
		case LITERAL_as:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		
					processParams(varList, func, signature);
				
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_as:
		{
			AST __t66 = _t;
			org.exist.xquery.parser.XQueryAST tmp87_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_as);
			_t = _t.getFirstChild();
			SequenceType type= new SequenceType();
			sequenceType(_t,type);
			_t = _retTree;
			signature.setReturnType(type);
			_t = __t66;
			_t = _t.getNextSibling();
			break;
		}
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		AST __t67 = _t;
		org.exist.xquery.parser.XQueryAST tmp88_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
		match(_t,LCURLY);
		_t = _t.getFirstChild();
		expr(_t,body);
		_t = _retTree;
		
		func.setFunctionBody(body);
		// anonymous function
		step = new InlineFunction(context, func);
		
		_t = __t67;
		_t = _t.getNextSibling();
		_t = __t62;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
/**
 * Single function param.
 */
	public final void param(AST _t,
		List vars
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST param_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST varname = null;
		
		AST __t72 = _t;
		varname = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,VARIABLE_BINDING);
		_t = _t.getFirstChild();
		
					FunctionParameterSequenceType var = new FunctionParameterSequenceType(varname.getText());
					var.setCardinality(Cardinality.ZERO_OR_MORE);
					vars.add(var);
				
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_as:
		{
			AST __t74 = _t;
			org.exist.xquery.parser.XQueryAST tmp89_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_as);
			_t = _t.getFirstChild();
			var.setCardinality(Cardinality.EXACTLY_ONE);
			sequenceType(_t,var);
			_t = _retTree;
			_t = __t74;
			_t = _t.getNextSibling();
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		_t = __t72;
		_t = _t.getNextSibling();
		_retTree = _t;
	}
	
/**
 * Single uri.
 */
	public final void uri(AST _t,
		List uris
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST uri_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST uri = null;
		
		AST __t79 = _t;
		uri = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,STRING_LITERAL);
		_t = _t.getFirstChild();
		
					AnyURIValue any= new AnyURIValue(uri.getText());
					uris.add(any);
				
		_t = __t79;
		_t = _t.getNextSibling();
		_retTree = _t;
	}
	
/**
 * catchErrorList in try-catch.
 */
	public final void catchErrorList(AST _t,
		List<QName> catchErrors
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST catchErrorList_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		
		catchError(_t,catchErrors);
		_t = _retTree;
		{
		_loop82:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_8.member(_t.getType()))) {
				catchError(_t,catchErrors);
				_t = _retTree;
			}
			else {
				break _loop82;
			}
			
		} while (true);
		}
		_retTree = _t;
	}
	
/**
 * Single catchError.
 */
	public final void catchError(AST _t,
		List<QName> catchErrors
	) throws RecognitionException, XPathException {
		
		org.exist.xquery.parser.XQueryAST catchError_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST ncwc = null;
		org.exist.xquery.parser.XQueryAST wc = null;
		org.exist.xquery.parser.XQueryAST pwcnc = null;
		org.exist.xquery.parser.XQueryAST eq = null;
		
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case NCNAME:
		{
			AST __t85 = _t;
			ncwc = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,NCNAME);
			_t = _t.getFirstChild();
			org.exist.xquery.parser.XQueryAST tmp90_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,WILDCARD);
			_t = _t.getNextSibling();
			
			catchErrors.add(QName.WildcardLocalPartQName.parseFromPrefix(staticContext, ncwc.toString()));
			
			_t = __t85;
			_t = _t.getNextSibling();
			break;
		}
		case WILDCARD:
		{
			AST __t86 = _t;
			wc = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,WILDCARD);
			_t = _t.getFirstChild();
			
						catchErrors.add(QName.WildcardQName.getInstance());
					
			_t = __t86;
			_t = _t.getNextSibling();
			break;
		}
		case PREFIX_WILDCARD:
		{
			AST __t87 = _t;
			org.exist.xquery.parser.XQueryAST tmp91_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,PREFIX_WILDCARD);
			_t = _t.getFirstChild();
			pwcnc = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,NCNAME);
			_t = _t.getNextSibling();
			
			catchErrors.add(new QName.WildcardNamespaceURIQName(pwcnc.toString()));
			
			_t = __t87;
			_t = _t.getNextSibling();
			break;
		}
		case EQNAME:
		{
			AST __t88 = _t;
			eq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,EQNAME);
			_t = _t.getFirstChild();
			
						catchErrors.add(QName.parse(staticContext, eq.toString()));
					
			_t = __t88;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		_retTree = _t;
	}
	
	public final Expression  typeCastExpr(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST typeCastExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST castAST = null;
		org.exist.xquery.parser.XQueryAST t = null;
		org.exist.xquery.parser.XQueryAST castableAST = null;
		org.exist.xquery.parser.XQueryAST t2 = null;
		
			step= null;
			PathExpr expr= new PathExpr(context);
			int cardinality= Cardinality.EXACTLY_ONE;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_cast:
		{
			AST __t338 = _t;
			castAST = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_cast);
			_t = _t.getFirstChild();
			step=expr(_t,expr);
			_t = _retTree;
			t = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ATOMIC_TYPE);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case QUESTION:
			{
				org.exist.xquery.parser.XQueryAST tmp92_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,QUESTION);
				_t = _t.getNextSibling();
				cardinality= Cardinality.ZERO_OR_ONE;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
						QName qn= QName.parse(staticContext, t.getText());
						int code= Type.getType(qn);
						CastExpression castExpr= new CastExpression(context, expr, code, cardinality);
						castExpr.setASTNode(castAST);
						path.add(castExpr);
						step = castExpr;
					
			_t = __t338;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_castable:
		{
			AST __t340 = _t;
			castableAST = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_castable);
			_t = _t.getFirstChild();
			step=expr(_t,expr);
			_t = _retTree;
			t2 = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ATOMIC_TYPE);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case QUESTION:
			{
				org.exist.xquery.parser.XQueryAST tmp93_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,QUESTION);
				_t = _t.getNextSibling();
				cardinality= Cardinality.ZERO_OR_ONE;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
						QName qn= QName.parse(staticContext, t2.getText());
						int code= Type.getType(qn);
						CastableExpression castExpr= new CastableExpression(context, expr, code, cardinality);
						castExpr.setASTNode(castAST);
						path.add(castExpr);
						step = castExpr;
					
			_t = __t340;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  generalComp(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST generalComp_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST eq = null;
		org.exist.xquery.parser.XQueryAST neq = null;
		org.exist.xquery.parser.XQueryAST lt = null;
		org.exist.xquery.parser.XQueryAST lteq = null;
		org.exist.xquery.parser.XQueryAST gt = null;
		org.exist.xquery.parser.XQueryAST gteq = null;
		
			step= null;
			PathExpr left= new PathExpr(context);
			PathExpr right= new PathExpr(context);
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQ:
		{
			AST __t298 = _t;
			eq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,EQ);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new GeneralComparison(context, left, right, Comparison.EQ);
			step.setASTNode(eq);
						path.add(step);
					
			_t = __t298;
			_t = _t.getNextSibling();
			break;
		}
		case NEQ:
		{
			AST __t299 = _t;
			neq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,NEQ);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new GeneralComparison(context, left, right, Comparison.NEQ);
			step.setASTNode(neq);
						path.add(step);
					
			_t = __t299;
			_t = _t.getNextSibling();
			break;
		}
		case LT:
		{
			AST __t300 = _t;
			lt = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LT);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new GeneralComparison(context, left, right, Comparison.LT);
			step.setASTNode(lt);
						path.add(step);
					
			_t = __t300;
			_t = _t.getNextSibling();
			break;
		}
		case LTEQ:
		{
			AST __t301 = _t;
			lteq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LTEQ);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new GeneralComparison(context, left, right, Comparison.LTEQ);
			step.setASTNode(lteq);
						path.add(step);
					
			_t = __t301;
			_t = _t.getNextSibling();
			break;
		}
		case GT:
		{
			AST __t302 = _t;
			gt = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,GT);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new GeneralComparison(context, left, right, Comparison.GT);
			step.setASTNode(gt);
						path.add(step);
					
			_t = __t302;
			_t = _t.getNextSibling();
			break;
		}
		case GTEQ:
		{
			AST __t303 = _t;
			gteq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,GTEQ);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new GeneralComparison(context, left, right, Comparison.GTEQ);
			step.setASTNode(gteq);
						path.add(step);
					
			_t = __t303;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  valueComp(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST valueComp_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST eq = null;
		org.exist.xquery.parser.XQueryAST ne = null;
		org.exist.xquery.parser.XQueryAST lt = null;
		org.exist.xquery.parser.XQueryAST le = null;
		org.exist.xquery.parser.XQueryAST gt = null;
		org.exist.xquery.parser.XQueryAST ge = null;
		
			step= null;
			PathExpr left= new PathExpr(context);
			PathExpr right= new PathExpr(context);
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_eq:
		{
			AST __t291 = _t;
			eq = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_eq);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new ValueComparison(context, left, right, Comparison.EQ);
			step.setASTNode(eq);
						path.add(step);
					
			_t = __t291;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_ne:
		{
			AST __t292 = _t;
			ne = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_ne);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new ValueComparison(context, left, right, Comparison.NEQ);
			step.setASTNode(ne);
						path.add(step);
					
			_t = __t292;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_lt:
		{
			AST __t293 = _t;
			lt = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_lt);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new ValueComparison(context, left, right, Comparison.LT);
			step.setASTNode(lt);
						path.add(step);
					
			_t = __t293;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_le:
		{
			AST __t294 = _t;
			le = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_le);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new ValueComparison(context, left, right, Comparison.LTEQ);
			step.setASTNode(le);
						path.add(step);
					
			_t = __t294;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_gt:
		{
			AST __t295 = _t;
			gt = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_gt);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new ValueComparison(context, left, right, Comparison.GT);
			step.setASTNode(gt);
						path.add(step);
					
			_t = __t295;
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_ge:
		{
			AST __t296 = _t;
			ge = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_ge);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step= new ValueComparison(context, left, right, Comparison.GTEQ);
			step.setASTNode(ge);
						path.add(step);
					
			_t = __t296;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  nodeComp(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST nodeComp_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST is = null;
		org.exist.xquery.parser.XQueryAST before = null;
		org.exist.xquery.parser.XQueryAST after = null;
		
			step= null;
			PathExpr left= new PathExpr(context);
			PathExpr right= new PathExpr(context);
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_is:
		{
			AST __t305 = _t;
			is = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_is);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step = new NodeComparison(context, left, right, NodeComparisonOperator.IS);
			step.setASTNode(is);
						path.add(step);
					
			_t = __t305;
			_t = _t.getNextSibling();
			break;
		}
		case BEFORE:
		{
			AST __t306 = _t;
			before = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,BEFORE);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step = new NodeComparison(context, left, right, NodeComparisonOperator.BEFORE);
			step.setASTNode(before);
						path.add(step);
					
			_t = __t306;
			_t = _t.getNextSibling();
			break;
		}
		case AFTER:
		{
			AST __t307 = _t;
			after = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,AFTER);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			
						step = new NodeComparison(context, left, right, NodeComparisonOperator.AFTER);
			step.setASTNode(after);
						path.add(step);
					
			_t = __t307;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
/**
 * Process a primary expression like function calls,
 * variable references, value constructors etc.
 */
	public final Expression  primaryExpr(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST primaryExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST v = null;
		
			step = null;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ELEMENT:
		case TEXT:
		case COMP_ELEM_CONSTRUCTOR:
		case COMP_ATTR_CONSTRUCTOR:
		case COMP_TEXT_CONSTRUCTOR:
		case COMP_COMMENT_CONSTRUCTOR:
		case COMP_PI_CONSTRUCTOR:
		case COMP_NS_CONSTRUCTOR:
		case COMP_DOC_CONSTRUCTOR:
		case LCURLY:
		case XML_COMMENT:
		case XML_PI:
		case XML_CDATA:
		{
			step=constructor(_t,path);
			_t = _retTree;
			step=postfixExpr(_t,step);
			_t = _retTree;
			
					path.add(step);
				
			break;
		}
		case LITERAL_map:
		{
			step=mapExpr(_t,path);
			_t = _retTree;
			break;
		}
		case ARRAY:
		{
			step=arrayConstr(_t,path);
			_t = _retTree;
			step=postfixExpr(_t,step);
			_t = _retTree;
			path.add(step);
			break;
		}
		case PARENTHESIZED:
		{
			AST __t220 = _t;
			org.exist.xquery.parser.XQueryAST tmp94_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,PARENTHESIZED);
			_t = _t.getFirstChild();
			PathExpr pathExpr= new PathExpr(context);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				expr(_t,pathExpr);
				_t = _retTree;
				
								// simplify the expression
								switch (pathExpr.getSubExpressionCount()) {
									case 0:
										step = new EmptySequenceExpr(context);
										break;
									case 1:
										step = pathExpr.getSubExpression(0);
										break;
									default:
										step = pathExpr;
										break;
								}
							
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t220;
			_t = _t.getNextSibling();
			step=postfixExpr(_t,pathExpr);
			_t = _retTree;
			path.add(step);
			break;
		}
		case STRING_LITERAL:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		{
			step=literalExpr(_t,path);
			_t = _retTree;
			step=postfixExpr(_t,step);
			_t = _retTree;
			path.add(step);
			break;
		}
		case VARIABLE_REF:
		{
			v = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,VARIABLE_REF);
			_t = _t.getNextSibling();
			
				    final QName vrqn = QName.parse(staticContext, v.getText(), null);
			step= new VariableReference(context, vrqn);
			step.setASTNode(v);
			
			step=postfixExpr(_t,step);
			_t = _retTree;
			path.add(step);
			break;
		}
		case FUNCTION:
		{
			step=functionCall(_t,path);
			_t = _retTree;
			step=postfixExpr(_t,step);
			_t = _retTree;
			path.add(step);
			break;
		}
		case HASH:
		{
			step=functionReference(_t,path);
			_t = _retTree;
			path.add(step);
			break;
		}
		case INLINE_FUNCTION_DECL:
		{
			step=inlineFunctionDecl(_t,path);
			_t = _retTree;
			step=postfixExpr(_t,step);
			_t = _retTree;
			path.add(step);
			break;
		}
		case LOOKUP:
		{
			step=lookup(_t,null);
			_t = _retTree;
			step=postfixExpr(_t,step);
			_t = _retTree;
			path.add(step);
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  pathExpr(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST pathExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST eq = null;
		org.exist.xquery.parser.XQueryAST nc1 = null;
		org.exist.xquery.parser.XQueryAST nc = null;
		org.exist.xquery.parser.XQueryAST w = null;
		org.exist.xquery.parser.XQueryAST n = null;
		org.exist.xquery.parser.XQueryAST t = null;
		org.exist.xquery.parser.XQueryAST e = null;
		org.exist.xquery.parser.XQueryAST eq2 = null;
		org.exist.xquery.parser.XQueryAST eq21 = null;
		org.exist.xquery.parser.XQueryAST att = null;
		org.exist.xquery.parser.XQueryAST eq3 = null;
		org.exist.xquery.parser.XQueryAST eq31 = null;
		org.exist.xquery.parser.XQueryAST com = null;
		org.exist.xquery.parser.XQueryAST pi = null;
		org.exist.xquery.parser.XQueryAST ncpi = null;
		org.exist.xquery.parser.XQueryAST slpi = null;
		org.exist.xquery.parser.XQueryAST dn = null;
		org.exist.xquery.parser.XQueryAST dneq = null;
		org.exist.xquery.parser.XQueryAST dneq1 = null;
		org.exist.xquery.parser.XQueryAST at = null;
		org.exist.xquery.parser.XQueryAST attr = null;
		org.exist.xquery.parser.XQueryAST nc2 = null;
		org.exist.xquery.parser.XQueryAST nc3 = null;
		
			Expression rightStep= null;
			step= null;
			int axis= Constants.CHILD_AXIS;
			boolean axisGiven = false;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQNAME:
		case WILDCARD:
		case PREFIX_WILDCARD:
		case ATTRIBUTE_TEST:
		case NCNAME:
		case LITERAL_element:
		case LITERAL_preceding:
		case LITERAL_following:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_attribute:
		case LITERAL_comment:
		case 190:
		case 191:
		case LITERAL_child:
		case LITERAL_self:
		case LITERAL_descendant:
		case 204:
		case 205:
		case LITERAL_parent:
		case LITERAL_ancestor:
		case 208:
		case 209:
		{
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_preceding:
			case LITERAL_following:
			case LITERAL_attribute:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			{
				axis=forwardAxis(_t);
				_t = _retTree;
				axisGiven = true;
				break;
			}
			case EQNAME:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case ATTRIBUTE_TEST:
			case NCNAME:
			case LITERAL_element:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_comment:
			case 190:
			case 191:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					NodeTest test = null;
					XQueryAST ast = null;
				
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQNAME:
			{
				eq = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,EQNAME);
				_t = _t.getNextSibling();
				
							try {
								QName qname= QName.parse(staticContext, eq.getText());
								if (axis == Constants.ATTRIBUTE_AXIS) {
					                qname = new QName(qname, ElementValue.ATTRIBUTE);
					                test= new NameTest(Type.ATTRIBUTE, qname);
				
				} else {
				test= new NameTest(Type.ELEMENT, qname);
				}
								ast = eq;
							} catch(XPathException ex1) {
								ex1.setLocation(eq.getLine(), eq.getColumn());
								throw ex1;
							}
						
				break;
			}
			case PREFIX_WILDCARD:
			{
				AST __t225 = _t;
				org.exist.xquery.parser.XQueryAST tmp95_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,PREFIX_WILDCARD);
				_t = _t.getFirstChild();
				nc1 = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getNextSibling();
				_t = __t225;
				_t = _t.getNextSibling();
				
							try {
								QName qname= new QName.WildcardNamespaceURIQName(nc1.getText());
								test= new NameTest(Type.ELEMENT, qname);
								if (axis == Constants.ATTRIBUTE_AXIS)
									test.setType(Type.ATTRIBUTE);
								ast = nc1;
							} catch(XPathException ex2) {
								ex2.setLocation(nc1.getLine(), nc1.getColumn());
							}
						
				break;
			}
			case NCNAME:
			{
				AST __t226 = _t;
				nc = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getFirstChild();
				org.exist.xquery.parser.XQueryAST tmp96_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,WILDCARD);
				_t = _t.getNextSibling();
				_t = __t226;
				_t = _t.getNextSibling();
				
							try {
								String namespaceURI= staticContext.getURIForPrefix(nc.getText());
								QName qname= new QName.WildcardLocalPartQName(namespaceURI, nc.getText());
								test= new NameTest(Type.ELEMENT, qname);
								if (axis == Constants.ATTRIBUTE_AXIS)
									test.setType(Type.ATTRIBUTE);
								ast = nc;
							} catch(XPathException ex3) {
								ex3.setLocation(nc1.getLine(), nc1.getColumn());
							}
						
				break;
			}
			case WILDCARD:
			{
				w = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,WILDCARD);
				_t = _t.getNextSibling();
				
							if (axis == Constants.ATTRIBUTE_AXIS)
								test= new TypeTest(Type.ATTRIBUTE);
							else
								test= new TypeTest(Type.ELEMENT);
							ast = w;
						
				break;
			}
			case LITERAL_node:
			{
				n = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_node);
				_t = _t.getNextSibling();
				
							if (axis == Constants.ATTRIBUTE_AXIS) {
							//	throw new XPathException(n, "Cannot test for node() on the attribute axis");
							   test= new TypeTest(Type.ATTRIBUTE);
				} else {
							   test= new AnyNodeTest();
				}
							ast = n;
						
				break;
			}
			case LITERAL_text:
			{
				t = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_text);
				_t = _t.getNextSibling();
				
							if (axis == Constants.ATTRIBUTE_AXIS)
								throw new XPathException(t, "Cannot test for text() on the attribute axis");
							test= new TypeTest(Type.TEXT);
							ast = t;
						
				break;
			}
			case LITERAL_element:
			{
				AST __t227 = _t;
				e = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_element);
				_t = _t.getFirstChild();
				
								if (axis == Constants.ATTRIBUTE_AXIS)
									throw new XPathException(e, "Cannot test for element() on the attribute axis");
								test= new TypeTest(Type.ELEMENT);
								ast = e;
							
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EQNAME:
				{
					eq2 = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,EQNAME);
					_t = _t.getNextSibling();
					
										QName qname= QName.parse(staticContext, eq2.getText());
										test= new NameTest(Type.ELEMENT, qname);
									
					break;
				}
				case WILDCARD:
				{
					org.exist.xquery.parser.XQueryAST tmp97_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,WILDCARD);
					_t = _t.getNextSibling();
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case EQNAME:
					{
						eq21 = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,EQNAME);
						_t = _t.getNextSibling();
						
						QName qname= QName.parse(staticContext, eq21.getText());
						test = new TypeTest(Type.getType(qname));
											
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t227;
				_t = _t.getNextSibling();
				break;
			}
			case ATTRIBUTE_TEST:
			{
				AST __t230 = _t;
				att = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,ATTRIBUTE_TEST);
				_t = _t.getFirstChild();
				
								test= new TypeTest(Type.ATTRIBUTE);
								ast = att;
				
				if (!axisGiven) {
								    axis= Constants.ATTRIBUTE_AXIS;
				}
							
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EQNAME:
				{
					eq3 = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,EQNAME);
					_t = _t.getNextSibling();
					
										QName qname = QName.parse(staticContext, eq3.getText());
										qname = new QName(qname, ElementValue.ATTRIBUTE);
										test= new NameTest(Type.ATTRIBUTE, qname);
									
					break;
				}
				case WILDCARD:
				{
					org.exist.xquery.parser.XQueryAST tmp98_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,WILDCARD);
					_t = _t.getNextSibling();
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case EQNAME:
					{
						eq31 = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,EQNAME);
						_t = _t.getNextSibling();
						
						QName qname= QName.parse(staticContext, eq31.getText());
						test = new TypeTest(Type.getType(qname));
											
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t230;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_comment:
			{
				com = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,LITERAL_comment);
				_t = _t.getNextSibling();
				
							if (axis == Constants.ATTRIBUTE_AXIS)
								throw new XPathException(n, "Cannot test for comment() on the attribute axis");
							test= new TypeTest(Type.COMMENT);
							ast = com;
						
				break;
			}
			case 190:
			{
				AST __t233 = _t;
				pi = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,190);
				_t = _t.getFirstChild();
				
							if (axis == Constants.ATTRIBUTE_AXIS)
								throw new XPathException(n, "Cannot test for processing-instruction() on the attribute axis");
							test= new TypeTest(Type.PROCESSING_INSTRUCTION);
							ast = pi;
						
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case NCNAME:
				{
					ncpi = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,NCNAME);
					_t = _t.getNextSibling();
					
					QName qname;
					qname= new QName(ncpi.getText(), "", null);
					test= new NameTest(Type.PROCESSING_INSTRUCTION, qname);
					
					break;
				}
				case STRING_LITERAL:
				{
					slpi = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,STRING_LITERAL);
					_t = _t.getNextSibling();
					
					QName qname;
					qname= new QName(slpi.getText(), "", null);
					test= new NameTest(Type.PROCESSING_INSTRUCTION, qname);
					
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t233;
				_t = _t.getNextSibling();
				break;
			}
			case 191:
			{
				dn = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,191);
				_t = _t.getNextSibling();
				
							test= new TypeTest(Type.DOCUMENT);
							ast = dn;
						
				{
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LITERAL_element)) {
					AST __t236 = _t;
					org.exist.xquery.parser.XQueryAST tmp99_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,LITERAL_element);
					_t = _t.getFirstChild();
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case EQNAME:
					{
						dneq = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,EQNAME);
						_t = _t.getNextSibling();
						
						QName qname= QName.parse(staticContext, dneq.getText());
						test= new NameTest(Type.DOCUMENT, qname);
						
						break;
					}
					case WILDCARD:
					{
						org.exist.xquery.parser.XQueryAST tmp100_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
						match(_t,WILDCARD);
						_t = _t.getNextSibling();
						{
						if (_t==null) _t=ASTNULL;
						switch ( _t.getType()) {
						case EQNAME:
						{
							dneq1 = (org.exist.xquery.parser.XQueryAST)_t;
							match(_t,EQNAME);
							_t = _t.getNextSibling();
							
							QName qname= QName.parse(staticContext, dneq1.getText());
							test= new TypeTest(Type.getType(qname));
							
							break;
						}
						case 3:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(_t);
						}
						}
						}
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					_t = __t236;
					_t = _t.getNextSibling();
				}
				else if ((_t.getType()==213)) {
					AST __t239 = _t;
					org.exist.xquery.parser.XQueryAST tmp101_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,213);
					_t = _t.getFirstChild();
					org.exist.xquery.parser.XQueryAST tmp102_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,EQNAME);
					_t = _t.getNextSibling();
					_t = __t239;
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_9.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					step= new LocationStep(context, axis, test);
					path.add(step);
					if (ast != null)
						step.setASTNode(ast);
				
			{
			_loop241:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PREDICATE)) {
					predicate(_t,(LocationStep) step);
					_t = _retTree;
				}
				else {
					break _loop241;
				}
				
			} while (true);
			}
			break;
		}
		case AT:
		{
			at = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,AT);
			_t = _t.getNextSibling();
			QName qname= null;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQNAME:
			{
				attr = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,EQNAME);
				_t = _t.getNextSibling();
				
				qname= QName.parse(staticContext, attr.getText(), "");
				qname = new QName(qname, ElementValue.ATTRIBUTE);
				
				break;
			}
			case PREFIX_WILDCARD:
			{
				AST __t243 = _t;
				org.exist.xquery.parser.XQueryAST tmp103_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,PREFIX_WILDCARD);
				_t = _t.getFirstChild();
				nc2 = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getNextSibling();
				_t = __t243;
				_t = _t.getNextSibling();
				
				qname = new QName.WildcardNamespaceURIQName(nc2.getText(), ElementValue.ATTRIBUTE);
						
				break;
			}
			case NCNAME:
			{
				AST __t244 = _t;
				nc3 = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,NCNAME);
				_t = _t.getFirstChild();
				org.exist.xquery.parser.XQueryAST tmp104_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,WILDCARD);
				_t = _t.getNextSibling();
				_t = __t244;
				_t = _t.getNextSibling();
				
							String namespaceURI= staticContext.getURIForPrefix(nc3.getText());
							if (namespaceURI == null)
								throw new EXistException("No namespace defined for prefix " + nc3.getText());
							qname= new QName.WildcardLocalPartQName(namespaceURI, ElementValue.ATTRIBUTE);
						
				break;
			}
			case WILDCARD:
			{
				org.exist.xquery.parser.XQueryAST tmp105_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,WILDCARD);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					NodeTest test= qname == null ? new TypeTest(Type.ATTRIBUTE) : new NameTest(Type.ATTRIBUTE, qname);
					step= new LocationStep(context, Constants.ATTRIBUTE_AXIS, test);
					step.setASTNode(at);
					path.add(step);
				
			{
			_loop246:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PREDICATE)) {
					predicate(_t,(LocationStep) step);
					_t = _retTree;
				}
				else {
					break _loop246;
				}
				
			} while (true);
			}
			break;
		}
		case SELF:
		{
			org.exist.xquery.parser.XQueryAST tmp106_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,SELF);
			_t = _t.getNextSibling();
			
					step= new LocationStep(context, Constants.SELF_AXIS, new TypeTest(Type.NODE));
					path.add(step);
				
			{
			_loop248:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PREDICATE)) {
					predicate(_t,(LocationStep) step);
					_t = _retTree;
				}
				else {
					break _loop248;
				}
				
			} while (true);
			}
			break;
		}
		case PARENT:
		{
			org.exist.xquery.parser.XQueryAST tmp107_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,PARENT);
			_t = _t.getNextSibling();
			
					step= new LocationStep(context, Constants.PARENT_AXIS, new TypeTest(Type.NODE));
					path.add(step);
				
			{
			_loop250:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PREDICATE)) {
					predicate(_t,(LocationStep) step);
					_t = _retTree;
				}
				else {
					break _loop250;
				}
				
			} while (true);
			}
			break;
		}
		case BANG:
		{
			AST __t251 = _t;
			org.exist.xquery.parser.XQueryAST tmp108_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,BANG);
			_t = _t.getFirstChild();
			
						PathExpr left = new PathExpr(context);
						PathExpr right = new PathExpr(context);
					
			expr(_t,left);
			_t = _retTree;
			expr(_t,right);
			_t = _retTree;
			
						OpSimpleMap map = new OpSimpleMap(context, left, right);
						path.add(map);
						step = path;
					
			_t = __t251;
			_t = _t.getNextSibling();
			break;
		}
		case SLASH:
		{
			AST __t252 = _t;
			org.exist.xquery.parser.XQueryAST tmp109_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,SLASH);
			_t = _t.getFirstChild();
			step=expr(_t,path);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				rightStep=expr(_t,path);
				_t = _retTree;
				
								if (rightStep instanceof LocationStep) {
									if(((LocationStep) rightStep).getAxis() == Constants.UNKNOWN_AXIS)
										((LocationStep) rightStep).setAxis(Constants.CHILD_AXIS);
								} else {
									if (rightStep.getPrimaryAxis() == Constants.UNKNOWN_AXIS)
										rightStep.setPrimaryAxis(Constants.CHILD_AXIS);
									if(rightStep instanceof VariableReference) {
										rightStep = new SimpleStep(context, Constants.CHILD_AXIS, rightStep);
										path.replaceLastExpression(rightStep);
									}
								}
							
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t252;
			_t = _t.getNextSibling();
			
					if (step instanceof LocationStep && ((LocationStep) step).getAxis() == Constants.UNKNOWN_AXIS)
						 ((LocationStep) step).setAxis(Constants.CHILD_AXIS);
				
			break;
		}
		case DSLASH:
		{
			AST __t254 = _t;
			org.exist.xquery.parser.XQueryAST tmp110_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,DSLASH);
			_t = _t.getFirstChild();
			step=expr(_t,path);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				rightStep=expr(_t,path);
				_t = _retTree;
				
								if (rightStep instanceof LocationStep) {
									LocationStep rs= (LocationStep) rightStep;
									if (rs.getAxis() == Constants.ATTRIBUTE_AXIS ||
										(rs.getTest().getType() == Type.ATTRIBUTE && rs.getAxis() == Constants.CHILD_AXIS)) {
										rs.setAxis(Constants.DESCENDANT_ATTRIBUTE_AXIS);
									} else if (rs.getAxis() == Constants.CHILD_AXIS && rs.getTest().isWildcardTest()) {
										rs.setAxis(Constants.DESCENDANT_AXIS);
									} else if (rs.getAxis() == Constants.SELF_AXIS) {
										rs.setAxis(Constants.DESCENDANT_SELF_AXIS);
									} else {
										rs.setAxis(Constants.DESCENDANT_SELF_AXIS);
										rs.setAbbreviated(true);
									}
				
								} else {
									rightStep.setPrimaryAxis(Constants.DESCENDANT_SELF_AXIS);
									if(rightStep instanceof VariableReference) {
										rightStep = new SimpleStep(context, Constants.DESCENDANT_SELF_AXIS, rightStep);
										path.replaceLastExpression(rightStep);
									} else if (rightStep instanceof FilteredExpression)
										((FilteredExpression)rightStep).setAbbreviated(true);
				
								}
							
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t254;
			_t = _t.getNextSibling();
			
					if (step instanceof LocationStep && ((LocationStep) step).getAxis() == Constants.UNKNOWN_AXIS) {
						 ((LocationStep) step).setAxis(Constants.DESCENDANT_SELF_AXIS);
						 ((LocationStep) step).setAbbreviated(true);
					}
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  extensionExpr(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException,PermissionDeniedException,EXistException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST extensionExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST p = null;
		org.exist.xquery.parser.XQueryAST c = null;
		
			step = null;
			PathExpr pathExpr = new PathExpr(context);
			ExtensionExpression ext = null;
		
		
		{
		int _cnt346=0;
		_loop346:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==PRAGMA)) {
				AST __t344 = _t;
				p = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,PRAGMA);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case PRAGMA_END:
				{
					c = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,PRAGMA_END);
					_t = _t.getNextSibling();
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				
								Pragma pragma = context.getPragma(p.getText(), c.getText());
								if (pragma != null) {
									if (ext == null)
										ext = new ExtensionExpression(context);
									ext.addPragma(pragma);
								}
							
				_t = __t344;
				_t = _t.getNextSibling();
			}
			else {
				if ( _cnt346>=1 ) { break _loop346; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt346++;
		} while (true);
		}
		expr(_t,pathExpr);
		_t = _retTree;
		
				if (ext != null) {
					ext.setExpression(pathExpr);
					path.add(ext);
					step = ext;
				} else {
					path.add(pathExpr);
					step = pathExpr;
				}
			
		_retTree = _t;
		return step;
	}
	
	public final Expression  numericExpr(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST numericExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST plus = null;
		org.exist.xquery.parser.XQueryAST minus = null;
		org.exist.xquery.parser.XQueryAST uminus = null;
		org.exist.xquery.parser.XQueryAST uplus = null;
		org.exist.xquery.parser.XQueryAST div = null;
		org.exist.xquery.parser.XQueryAST idiv = null;
		org.exist.xquery.parser.XQueryAST mod = null;
		org.exist.xquery.parser.XQueryAST mult = null;
		
			step= null;
			PathExpr left= new PathExpr(context);
			PathExpr right= new PathExpr(context);
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case PLUS:
		{
			AST __t259 = _t;
			plus = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,PLUS);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t259;
			_t = _t.getNextSibling();
			
					OpNumeric op= new OpNumeric(context, left, right, ArithmeticOperator.ADDITION);
			op.setASTNode(plus);
					path.addPath(op);
					step= op;
				
			break;
		}
		case MINUS:
		{
			AST __t260 = _t;
			minus = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,MINUS);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t260;
			_t = _t.getNextSibling();
			
					OpNumeric op= new OpNumeric(context, left, right, ArithmeticOperator.SUBTRACTION);
			op.setASTNode(minus);
					path.addPath(op);
					step= op;
				
			break;
		}
		case UNARY_MINUS:
		{
			AST __t261 = _t;
			uminus = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,UNARY_MINUS);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			_t = __t261;
			_t = _t.getNextSibling();
			
					UnaryExpr unary= new UnaryExpr(context, ArithmeticOperator.SUBTRACTION);
			unary.setASTNode(uminus);
					unary.add(left);
					path.addPath(unary);
					step= unary;
				
			break;
		}
		case UNARY_PLUS:
		{
			AST __t262 = _t;
			uplus = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,UNARY_PLUS);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			_t = __t262;
			_t = _t.getNextSibling();
			
					UnaryExpr unary= new UnaryExpr(context, ArithmeticOperator.ADDITION);
			unary.setASTNode(uplus);
					unary.add(left);
					path.addPath(unary);
					step= unary;
				
			break;
		}
		case LITERAL_div:
		{
			AST __t263 = _t;
			div = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_div);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t263;
			_t = _t.getNextSibling();
			
					OpNumeric op= new OpNumeric(context, left, right, ArithmeticOperator.DIVISION);
			op.setASTNode(div);
					path.addPath(op);
					step= op;
				
			break;
		}
		case LITERAL_idiv:
		{
			AST __t264 = _t;
			idiv = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_idiv);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t264;
			_t = _t.getNextSibling();
			
					OpNumeric op= new OpNumeric(context, left, right, ArithmeticOperator.DIVISION_INTEGER);
			op.setASTNode(idiv);
					path.addPath(op);
					step= op;
				
			break;
		}
		case LITERAL_mod:
		{
			AST __t265 = _t;
			mod = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_mod);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t265;
			_t = _t.getNextSibling();
			
					OpNumeric op= new OpNumeric(context, left, right, ArithmeticOperator.MODULUS);
			op.setASTNode(mod);
					path.addPath(op);
					step= op;
				
			break;
		}
		case STAR:
		{
			AST __t266 = _t;
			mult = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,STAR);
			_t = _t.getFirstChild();
			step=expr(_t,left);
			_t = _retTree;
			step=expr(_t,right);
			_t = _retTree;
			_t = __t266;
			_t = _t.getNextSibling();
			
					OpNumeric op= new OpNumeric(context, left, right, ArithmeticOperator.MULTIPLICATION);
			op.setASTNode(mult);
					path.addPath(op);
					step= op;
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  updateExpr(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException,PermissionDeniedException,EXistException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST updateExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST updateAST = null;
		
		
		
		AST __t348 = _t;
		updateAST = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,LITERAL_update);
		_t = _t.getFirstChild();
		
					PathExpr p1 = new PathExpr(context);
					PathExpr p2 = new PathExpr(context);
					int type;
					int position = Insert.INSERT_APPEND;
				
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_replace:
		{
			org.exist.xquery.parser.XQueryAST tmp111_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_replace);
			_t = _t.getNextSibling();
			type = 0;
			break;
		}
		case LITERAL_value:
		{
			org.exist.xquery.parser.XQueryAST tmp112_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_value);
			_t = _t.getNextSibling();
			type = 1;
			break;
		}
		case LITERAL_insert:
		{
			org.exist.xquery.parser.XQueryAST tmp113_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_insert);
			_t = _t.getNextSibling();
			type = 2;
			break;
		}
		case LITERAL_delete:
		{
			org.exist.xquery.parser.XQueryAST tmp114_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_delete);
			_t = _t.getNextSibling();
			type = 3;
			break;
		}
		case LITERAL_rename:
		{
			org.exist.xquery.parser.XQueryAST tmp115_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_rename);
			_t = _t.getNextSibling();
			type = 4;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		step=expr(_t,p1);
		_t = _retTree;
		{
		if (_t==null) _t=ASTNULL;
		if ((_t.getType()==LITERAL_preceding)) {
			org.exist.xquery.parser.XQueryAST tmp116_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_preceding);
			_t = _t.getNextSibling();
			position = Insert.INSERT_BEFORE;
		}
		else if ((_t.getType()==LITERAL_following)) {
			org.exist.xquery.parser.XQueryAST tmp117_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_following);
			_t = _t.getNextSibling();
			position = Insert.INSERT_AFTER;
		}
		else if ((_t.getType()==LITERAL_into)) {
			org.exist.xquery.parser.XQueryAST tmp118_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_into);
			_t = _t.getNextSibling();
			position = Insert.INSERT_APPEND;
		}
		else if ((_tokenSet_10.member(_t.getType()))) {
		}
		else {
			throw new NoViableAltException(_t);
		}
		
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EOF:
		case EQNAME:
		case PARENTHESIZED:
		case ABSOLUTE_SLASH:
		case ABSOLUTE_DSLASH:
		case WILDCARD:
		case PREFIX_WILDCARD:
		case FUNCTION:
		case UNARY_MINUS:
		case UNARY_PLUS:
		case VARIABLE_REF:
		case ELEMENT:
		case TEXT:
		case INLINE_FUNCTION_DECL:
		case LOOKUP:
		case ARRAY:
		case ATTRIBUTE_TEST:
		case COMP_ELEM_CONSTRUCTOR:
		case COMP_ATTR_CONSTRUCTOR:
		case COMP_TEXT_CONSTRUCTOR:
		case COMP_COMMENT_CONSTRUCTOR:
		case COMP_PI_CONSTRUCTOR:
		case COMP_NS_CONSTRUCTOR:
		case COMP_DOC_CONSTRUCTOR:
		case PRAGMA:
		case GTEQ:
		case SEQUENCE:
		case NCNAME:
		case EQ:
		case STRING_LITERAL:
		case LITERAL_element:
		case COMMA:
		case LCURLY:
		case STAR:
		case PLUS:
		case LITERAL_map:
		case LITERAL_try:
		case LITERAL_some:
		case LITERAL_every:
		case LITERAL_if:
		case LITERAL_switch:
		case LITERAL_typeswitch:
		case LITERAL_update:
		case LITERAL_preceding:
		case LITERAL_following:
		case UNION:
		case LITERAL_return:
		case LITERAL_or:
		case LITERAL_and:
		case LITERAL_instance:
		case LITERAL_treat:
		case LITERAL_castable:
		case LITERAL_cast:
		case BEFORE:
		case AFTER:
		case LITERAL_eq:
		case LITERAL_ne:
		case LITERAL_lt:
		case LITERAL_le:
		case LITERAL_gt:
		case LITERAL_ge:
		case GT:
		case NEQ:
		case LT:
		case LTEQ:
		case LITERAL_is:
		case CONCAT:
		case LITERAL_to:
		case MINUS:
		case LITERAL_div:
		case LITERAL_idiv:
		case LITERAL_mod:
		case BANG:
		case LITERAL_intersect:
		case LITERAL_except:
		case SLASH:
		case DSLASH:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_attribute:
		case LITERAL_comment:
		case 190:
		case 191:
		case HASH:
		case SELF:
		case XML_COMMENT:
		case XML_PI:
		case AT:
		case PARENT:
		case LITERAL_child:
		case LITERAL_self:
		case LITERAL_descendant:
		case 204:
		case 205:
		case LITERAL_parent:
		case LITERAL_ancestor:
		case 208:
		case 209:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		case XML_CDATA:
		{
			step=expr(_t,p2);
			_t = _retTree;
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		
					Modification mod;
					if (type == 0)
						mod = new Replace(context, p1, p2);
					else if (type == 1)
						mod = new Update(context, p1, p2);
					else if (type == 2)
						mod = new Insert(context, p2, p1, position);
					else if (type == 3)
						mod = new Delete(context, p1);
					else
						mod = new Rename(context, p1, p2);
					mod.setASTNode(updateAST);
					path.add(mod);
					step = mod;
				
		_t = __t348;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
	public final Expression  constructor(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST constructor_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST qn = null;
		org.exist.xquery.parser.XQueryAST qns = null;
		org.exist.xquery.parser.XQueryAST attr = null;
		org.exist.xquery.parser.XQueryAST qna = null;
		org.exist.xquery.parser.XQueryAST pid = null;
		org.exist.xquery.parser.XQueryAST ex = null;
		org.exist.xquery.parser.XQueryAST e = null;
		org.exist.xquery.parser.XQueryAST attrName = null;
		org.exist.xquery.parser.XQueryAST attrVal = null;
		org.exist.xquery.parser.XQueryAST pcdata = null;
		org.exist.xquery.parser.XQueryAST t = null;
		org.exist.xquery.parser.XQueryAST tc = null;
		org.exist.xquery.parser.XQueryAST d = null;
		org.exist.xquery.parser.XQueryAST cdata = null;
		org.exist.xquery.parser.XQueryAST p = null;
		org.exist.xquery.parser.XQueryAST cdataSect = null;
		org.exist.xquery.parser.XQueryAST l = null;
		
			step= null;
			PathExpr elementContent= null;
			Expression contentExpr= null;
			Expression qnameExpr = null;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case COMP_ELEM_CONSTRUCTOR:
		{
			AST __t309 = _t;
			qn = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_ELEM_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						ElementConstructor c= new ElementConstructor(context);
						c.setASTNode(qn);
						step= c;
						SequenceConstructor construct = new SequenceConstructor(context);
						EnclosedExpr enclosed = new EnclosedExpr(context);
						enclosed.addPath(construct);
						c.setContent(enclosed);
						PathExpr qnamePathExpr = new PathExpr(context);
						c.setNameExpr(qnamePathExpr);
					
			qnameExpr=expr(_t,qnamePathExpr);
			_t = _retTree;
			{
			_loop311:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					elementContent = new PathExpr(context);
					contentExpr=expr(_t,elementContent);
					_t = _retTree;
					construct.addPath(elementContent);
				}
				else {
					break _loop311;
				}
				
			} while (true);
			}
			_t = __t309;
			_t = _t.getNextSibling();
			break;
		}
		case COMP_NS_CONSTRUCTOR:
		{
			AST __t312 = _t;
			qns = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_NS_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						NamespaceConstructor c = new NamespaceConstructor(context);
						c.setASTNode(qns);
						step = c;
						PathExpr qnamePathExpr = new PathExpr(context);
						c.setNameExpr(qnamePathExpr);
			elementContent = new PathExpr(context);
			c.setContentExpr(elementContent);
					
			qnameExpr=expr(_t,qnamePathExpr);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				contentExpr=expr(_t,elementContent);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t312;
			_t = _t.getNextSibling();
			break;
		}
		case COMP_ATTR_CONSTRUCTOR:
		{
			AST __t314 = _t;
			attr = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_ATTR_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						DynamicAttributeConstructor a= new DynamicAttributeConstructor(context);
			a.setASTNode(attr);
			step = a;
			PathExpr qnamePathExpr = new PathExpr(context);
			a.setNameExpr(qnamePathExpr);
			elementContent = new PathExpr(context);
			a.setContentExpr(elementContent);
					
			qna = _t==ASTNULL ? null : (org.exist.xquery.parser.XQueryAST)_t;
			qnameExpr=expr(_t,qnamePathExpr);
			_t = _retTree;
			
			QName qname = QName.parse(staticContext, qna.getText());
			if (Namespaces.XMLNS_NS.equals(qname.getNamespaceURI())
			|| ("".equals(qname.getNamespaceURI()) && qname.getLocalPart().equals(XMLConstants.XMLNS_ATTRIBUTE)))
			throw new XPathException("err:XQDY0044: the node-name property of the node constructed by a computed attribute constructor is in the namespace http://www.w3.org/2000/xmlns/ (corresponding to namespace prefix xmlns), or is in no namespace and has local name xmlns.");
			
			AST __t315 = _t;
			org.exist.xquery.parser.XQueryAST tmp119_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LCURLY);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				contentExpr=expr(_t,elementContent);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t315;
			_t = _t.getNextSibling();
			_t = __t314;
			_t = _t.getNextSibling();
			break;
		}
		case COMP_PI_CONSTRUCTOR:
		{
			AST __t317 = _t;
			pid = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_PI_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						DynamicPIConstructor pd= new DynamicPIConstructor(context);
			pd.setASTNode(pid);
			step = pd;
			PathExpr qnamePathExpr = new PathExpr(context);
			pd.setNameExpr(qnamePathExpr);
			elementContent = new PathExpr(context);
			pd.setContentExpr(elementContent);
					
			qnameExpr=expr(_t,qnamePathExpr);
			_t = _retTree;
			AST __t318 = _t;
			org.exist.xquery.parser.XQueryAST tmp120_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LCURLY);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EOF:
			case EQNAME:
			case PARENTHESIZED:
			case ABSOLUTE_SLASH:
			case ABSOLUTE_DSLASH:
			case WILDCARD:
			case PREFIX_WILDCARD:
			case FUNCTION:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case VARIABLE_REF:
			case ELEMENT:
			case TEXT:
			case INLINE_FUNCTION_DECL:
			case LOOKUP:
			case ARRAY:
			case ATTRIBUTE_TEST:
			case COMP_ELEM_CONSTRUCTOR:
			case COMP_ATTR_CONSTRUCTOR:
			case COMP_TEXT_CONSTRUCTOR:
			case COMP_COMMENT_CONSTRUCTOR:
			case COMP_PI_CONSTRUCTOR:
			case COMP_NS_CONSTRUCTOR:
			case COMP_DOC_CONSTRUCTOR:
			case PRAGMA:
			case GTEQ:
			case SEQUENCE:
			case NCNAME:
			case EQ:
			case STRING_LITERAL:
			case LITERAL_element:
			case COMMA:
			case LCURLY:
			case STAR:
			case PLUS:
			case LITERAL_map:
			case LITERAL_try:
			case LITERAL_some:
			case LITERAL_every:
			case LITERAL_if:
			case LITERAL_switch:
			case LITERAL_typeswitch:
			case LITERAL_update:
			case LITERAL_preceding:
			case LITERAL_following:
			case UNION:
			case LITERAL_return:
			case LITERAL_or:
			case LITERAL_and:
			case LITERAL_instance:
			case LITERAL_treat:
			case LITERAL_castable:
			case LITERAL_cast:
			case BEFORE:
			case AFTER:
			case LITERAL_eq:
			case LITERAL_ne:
			case LITERAL_lt:
			case LITERAL_le:
			case LITERAL_gt:
			case LITERAL_ge:
			case GT:
			case NEQ:
			case LT:
			case LTEQ:
			case LITERAL_is:
			case CONCAT:
			case LITERAL_to:
			case MINUS:
			case LITERAL_div:
			case LITERAL_idiv:
			case LITERAL_mod:
			case BANG:
			case LITERAL_intersect:
			case LITERAL_except:
			case SLASH:
			case DSLASH:
			case LITERAL_text:
			case LITERAL_node:
			case LITERAL_attribute:
			case LITERAL_comment:
			case 190:
			case 191:
			case HASH:
			case SELF:
			case XML_COMMENT:
			case XML_PI:
			case AT:
			case PARENT:
			case LITERAL_child:
			case LITERAL_self:
			case LITERAL_descendant:
			case 204:
			case 205:
			case LITERAL_parent:
			case LITERAL_ancestor:
			case 208:
			case 209:
			case INTEGER_LITERAL:
			case DOUBLE_LITERAL:
			case DECIMAL_LITERAL:
			case XML_CDATA:
			{
				ex = _t==ASTNULL ? null : (org.exist.xquery.parser.XQueryAST)_t;
				contentExpr=expr(_t,elementContent);
				_t = _retTree;
				
				if (ex.getText() != null && ex.getText().indexOf("?>") > Constants.STRING_NOT_FOUND)
				throw new XPathException("err:XQDY0026: content expression of a computed processing instruction constructor contains the string '?>' which is not allowed.");
				
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t318;
			_t = _t.getNextSibling();
			_t = __t317;
			_t = _t.getNextSibling();
			break;
		}
		case ELEMENT:
		{
			AST __t320 = _t;
			e = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,ELEMENT);
			_t = _t.getFirstChild();
			
						ElementConstructor c= new ElementConstructor(context, e.getText());
						c.setASTNode(e);
						step= c;
						staticContext.pushInScopeNamespaces();
					
			{
			_loop326:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==ATTRIBUTE)) {
					AST __t322 = _t;
					attrName = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
					match(_t,ATTRIBUTE);
					_t = _t.getFirstChild();
					
										AttributeConstructor attrib= new AttributeConstructor(context, attrName.getText());
										attrib.setASTNode(attrName);
									
					{
					_loop325:
					do {
						if (_t==null) _t=ASTNULL;
						switch ( _t.getType()) {
						case ATTRIBUTE_CONTENT:
						{
							attrVal = (org.exist.xquery.parser.XQueryAST)_t;
							match(_t,ATTRIBUTE_CONTENT);
							_t = _t.getNextSibling();
							
													attrib.addValue(StringValue.expand(attrVal.getText()));
												
							break;
						}
						case LCURLY:
						{
							AST __t324 = _t;
							org.exist.xquery.parser.XQueryAST tmp121_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
							match(_t,LCURLY);
							_t = _t.getFirstChild();
							PathExpr enclosed= new PathExpr(context);
							expr(_t,enclosed);
							_t = _retTree;
							attrib.addEnclosedExpr(enclosed);
							_t = __t324;
							_t = _t.getNextSibling();
							break;
						}
						default:
						{
							break _loop325;
						}
						}
					} while (true);
					}
					c.addAttribute(attrib);
					if (attrib.isNamespaceDeclaration()) {
					String nsPrefix = attrib.getQName().equals(XMLConstants.XMLNS_ATTRIBUTE) ?
					"" : QName.extractLocalName(attrib.getQName());
					staticContext.declareInScopeNamespace(nsPrefix,attrib.getLiteralValue());
					}
					
					
					_t = __t322;
					_t = _t.getNextSibling();
				}
				else {
					break _loop326;
				}
				
			} while (true);
			}
			{
			_loop328:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_11.member(_t.getType()))) {
					
									if (elementContent == null) {
										elementContent= new PathExpr(context);
										c.setContent(elementContent);
									}
								
					contentExpr=constructor(_t,elementContent);
					_t = _retTree;
					elementContent.add(contentExpr);
				}
				else {
					break _loop328;
				}
				
			} while (true);
			}
			
			staticContext.popInScopeNamespaces();
			
			_t = __t320;
			_t = _t.getNextSibling();
			break;
		}
		case TEXT:
		{
			AST __t329 = _t;
			pcdata = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,TEXT);
			_t = _t.getFirstChild();
			
						TextConstructor text= new TextConstructor(context, pcdata.getText());
			text.setASTNode(pcdata);
						step= text;
					
			_t = __t329;
			_t = _t.getNextSibling();
			break;
		}
		case COMP_TEXT_CONSTRUCTOR:
		{
			AST __t330 = _t;
			t = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_TEXT_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						elementContent = new PathExpr(context);
						DynamicTextConstructor text = new DynamicTextConstructor(context, elementContent);
						text.setASTNode(t);
						step= text;
					
			contentExpr=expr(_t,elementContent);
			_t = _retTree;
			_t = __t330;
			_t = _t.getNextSibling();
			break;
		}
		case COMP_COMMENT_CONSTRUCTOR:
		{
			AST __t331 = _t;
			tc = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_COMMENT_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						elementContent = new PathExpr(context);
						DynamicCommentConstructor comment = new DynamicCommentConstructor(context, elementContent);
						comment.setASTNode(t);
						step= comment;
					
			contentExpr=expr(_t,elementContent);
			_t = _retTree;
			_t = __t331;
			_t = _t.getNextSibling();
			break;
		}
		case COMP_DOC_CONSTRUCTOR:
		{
			AST __t332 = _t;
			d = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,COMP_DOC_CONSTRUCTOR);
			_t = _t.getFirstChild();
			
						elementContent = new PathExpr(context);
						DocumentConstructor doc = new DocumentConstructor(context, elementContent);
						doc.setASTNode(d);
						step= doc;
					
			contentExpr=expr(_t,elementContent);
			_t = _retTree;
			_t = __t332;
			_t = _t.getNextSibling();
			break;
		}
		case XML_COMMENT:
		{
			AST __t333 = _t;
			cdata = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,XML_COMMENT);
			_t = _t.getFirstChild();
			
						CommentConstructor comment= new CommentConstructor(context, cdata.getText());
			comment.setASTNode(cdata);
						step= comment;
					
			_t = __t333;
			_t = _t.getNextSibling();
			break;
		}
		case XML_PI:
		{
			AST __t334 = _t;
			p = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,XML_PI);
			_t = _t.getFirstChild();
			
						PIConstructor pi= new PIConstructor(context, p.getText());
			pi.setASTNode(p);
						step= pi;
					
			_t = __t334;
			_t = _t.getNextSibling();
			break;
		}
		case XML_CDATA:
		{
			AST __t335 = _t;
			cdataSect = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,XML_CDATA);
			_t = _t.getFirstChild();
			
						CDATAConstructor cd = new CDATAConstructor(context, cdataSect.getText());
						cd.setASTNode(cdataSect);
						step= cd;
					
			_t = __t335;
			_t = _t.getNextSibling();
			break;
		}
		case LCURLY:
		{
			AST __t336 = _t;
			l = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LCURLY);
			_t = _t.getFirstChild();
			
			EnclosedExpr subexpr= new EnclosedExpr(context);
			subexpr.setASTNode(l);
			
			step=expr(_t,subexpr);
			_t = _retTree;
			step= subexpr;
			_t = __t336;
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return step;
	}
	
/**
 * Handles predicates and dynamic function calls:
 * PostfixExpr	   ::=   	PrimaryExpr (Predicate | ArgumentList)*
 */
	public final Expression  postfixExpr(AST _t,
		Expression expression
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST postfixExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST fn = null;
		
			step= expression;
		
		
		{
		_loop274:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PREDICATE:
			{
				AST __t269 = _t;
				org.exist.xquery.parser.XQueryAST tmp122_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,PREDICATE);
				_t = _t.getFirstChild();
				
								FilteredExpression filter = new FilteredExpression(context, step);
								step= filter;
								Predicate predicateExpr= new Predicate(context);
							
				expr(_t,predicateExpr);
				_t = _retTree;
				
								filter.addPredicate(predicateExpr);
							
				_t = __t269;
				_t = _t.getNextSibling();
				break;
			}
			case DYNAMIC_FCALL:
			{
				AST __t270 = _t;
				fn = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
				match(_t,DYNAMIC_FCALL);
				_t = _t.getFirstChild();
				
								List<Expression> params= new ArrayList<Expression>(5);
								boolean isPartial = false;
							
				{
				_loop273:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_12.member(_t.getType()))) {
						{
						if (_t==null) _t=ASTNULL;
						switch ( _t.getType()) {
						case QUESTION:
						{
							org.exist.xquery.parser.XQueryAST tmp123_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
							match(_t,QUESTION);
							_t = _t.getNextSibling();
							
													params.add(new Function.Placeholder(context));
													isPartial = true;
												
							break;
						}
						case EOF:
						case EQNAME:
						case PARENTHESIZED:
						case ABSOLUTE_SLASH:
						case ABSOLUTE_DSLASH:
						case WILDCARD:
						case PREFIX_WILDCARD:
						case FUNCTION:
						case UNARY_MINUS:
						case UNARY_PLUS:
						case VARIABLE_REF:
						case ELEMENT:
						case TEXT:
						case INLINE_FUNCTION_DECL:
						case LOOKUP:
						case ARRAY:
						case ATTRIBUTE_TEST:
						case COMP_ELEM_CONSTRUCTOR:
						case COMP_ATTR_CONSTRUCTOR:
						case COMP_TEXT_CONSTRUCTOR:
						case COMP_COMMENT_CONSTRUCTOR:
						case COMP_PI_CONSTRUCTOR:
						case COMP_NS_CONSTRUCTOR:
						case COMP_DOC_CONSTRUCTOR:
						case PRAGMA:
						case GTEQ:
						case SEQUENCE:
						case NCNAME:
						case EQ:
						case STRING_LITERAL:
						case LITERAL_element:
						case COMMA:
						case LCURLY:
						case STAR:
						case PLUS:
						case LITERAL_map:
						case LITERAL_try:
						case LITERAL_some:
						case LITERAL_every:
						case LITERAL_if:
						case LITERAL_switch:
						case LITERAL_typeswitch:
						case LITERAL_update:
						case LITERAL_preceding:
						case LITERAL_following:
						case UNION:
						case LITERAL_return:
						case LITERAL_or:
						case LITERAL_and:
						case LITERAL_instance:
						case LITERAL_treat:
						case LITERAL_castable:
						case LITERAL_cast:
						case BEFORE:
						case AFTER:
						case LITERAL_eq:
						case LITERAL_ne:
						case LITERAL_lt:
						case LITERAL_le:
						case LITERAL_gt:
						case LITERAL_ge:
						case GT:
						case NEQ:
						case LT:
						case LTEQ:
						case LITERAL_is:
						case CONCAT:
						case LITERAL_to:
						case MINUS:
						case LITERAL_div:
						case LITERAL_idiv:
						case LITERAL_mod:
						case BANG:
						case LITERAL_intersect:
						case LITERAL_except:
						case SLASH:
						case DSLASH:
						case LITERAL_text:
						case LITERAL_node:
						case LITERAL_attribute:
						case LITERAL_comment:
						case 190:
						case 191:
						case HASH:
						case SELF:
						case XML_COMMENT:
						case XML_PI:
						case AT:
						case PARENT:
						case LITERAL_child:
						case LITERAL_self:
						case LITERAL_descendant:
						case 204:
						case 205:
						case LITERAL_parent:
						case LITERAL_ancestor:
						case 208:
						case 209:
						case INTEGER_LITERAL:
						case DOUBLE_LITERAL:
						case DECIMAL_LITERAL:
						case XML_CDATA:
						{
							PathExpr pathExpr = new PathExpr(context);
							expr(_t,pathExpr);
							_t = _retTree;
							params.add(pathExpr);
							break;
						}
						default:
						{
							throw new NoViableAltException(_t);
						}
						}
						}
					}
					else {
						break _loop273;
					}
					
				} while (true);
				}
				
								step = new DynamicFunctionCall(context, step, params, isPartial);
							
				_t = __t270;
				_t = _t.getNextSibling();
				break;
			}
			default:
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LOOKUP)) {
					step=lookup(_t,step);
					_t = _retTree;
				}
			else {
				break _loop274;
			}
			}
		} while (true);
		}
		_retTree = _t;
		return step;
	}
	
	public final Expression  mapExpr(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException,PermissionDeniedException,EXistException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST mapExpr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		
		
		
		AST __t353 = _t;
		org.exist.xquery.parser.XQueryAST tmp124_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
		match(_t,LITERAL_map);
		_t = _t.getFirstChild();
		
					MapExpr expr = new MapExpr(context);
					path.add(expr);
					step = expr;
				
		{
		_loop356:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==COLON)) {
				AST __t355 = _t;
				org.exist.xquery.parser.XQueryAST tmp125_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
				match(_t,COLON);
				_t = _t.getFirstChild();
				
									PathExpr key = new PathExpr(context);
									PathExpr value = new PathExpr(context);
								
				step=expr(_t,key);
				_t = _retTree;
				step=expr(_t,value);
				_t = _retTree;
				expr.map(key, value);
				_t = __t355;
				_t = _t.getNextSibling();
			}
			else {
				break _loop356;
			}
			
		} while (true);
		}
		_t = __t353;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
	public final Expression  arrayConstr(AST _t,
		PathExpr path
	) throws RecognitionException, XPathException,PermissionDeniedException,EXistException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST arrayConstr_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST t = null;
		
		
		
		AST __t358 = _t;
		t = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,ARRAY);
		_t = _t.getFirstChild();
		
				    String type = t.getText();
				    ArrayConstructor array;
				    if (type.equals("[")) {
				        array = new ArrayConstructor(context, ArrayConstructor.ConstructorType.SQUARE_ARRAY);
				    } else {
				        array = new ArrayConstructor(context, ArrayConstructor.ConstructorType.CURLY_ARRAY);
				    }
				    step = array;
				
		{
		_loop360:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_0.member(_t.getType()))) {
				PathExpr arg = new PathExpr(context);
				expr(_t,arg);
				_t = _retTree;
				array.addArgument(arg);
			}
			else {
				break _loop360;
			}
			
		} while (true);
		}
		_t = __t358;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
	public final Expression  functionCall(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST functionCall_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST fn = null;
		
			PathExpr pathExpr;
			step= null;
			boolean isPartial = false;
		
		
		AST __t283 = _t;
		fn = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,FUNCTION);
		_t = _t.getFirstChild();
		List params= new ArrayList(2);
		{
		_loop286:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_12.member(_t.getType()))) {
				pathExpr= new PathExpr(context);
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case QUESTION:
				{
					org.exist.xquery.parser.XQueryAST tmp126_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
					match(_t,QUESTION);
					_t = _t.getNextSibling();
					
										params.add(new Function.Placeholder(context));
										isPartial = true;
									
					break;
				}
				case EOF:
				case EQNAME:
				case PARENTHESIZED:
				case ABSOLUTE_SLASH:
				case ABSOLUTE_DSLASH:
				case WILDCARD:
				case PREFIX_WILDCARD:
				case FUNCTION:
				case UNARY_MINUS:
				case UNARY_PLUS:
				case VARIABLE_REF:
				case ELEMENT:
				case TEXT:
				case INLINE_FUNCTION_DECL:
				case LOOKUP:
				case ARRAY:
				case ATTRIBUTE_TEST:
				case COMP_ELEM_CONSTRUCTOR:
				case COMP_ATTR_CONSTRUCTOR:
				case COMP_TEXT_CONSTRUCTOR:
				case COMP_COMMENT_CONSTRUCTOR:
				case COMP_PI_CONSTRUCTOR:
				case COMP_NS_CONSTRUCTOR:
				case COMP_DOC_CONSTRUCTOR:
				case PRAGMA:
				case GTEQ:
				case SEQUENCE:
				case NCNAME:
				case EQ:
				case STRING_LITERAL:
				case LITERAL_element:
				case COMMA:
				case LCURLY:
				case STAR:
				case PLUS:
				case LITERAL_map:
				case LITERAL_try:
				case LITERAL_some:
				case LITERAL_every:
				case LITERAL_if:
				case LITERAL_switch:
				case LITERAL_typeswitch:
				case LITERAL_update:
				case LITERAL_preceding:
				case LITERAL_following:
				case UNION:
				case LITERAL_return:
				case LITERAL_or:
				case LITERAL_and:
				case LITERAL_instance:
				case LITERAL_treat:
				case LITERAL_castable:
				case LITERAL_cast:
				case BEFORE:
				case AFTER:
				case LITERAL_eq:
				case LITERAL_ne:
				case LITERAL_lt:
				case LITERAL_le:
				case LITERAL_gt:
				case LITERAL_ge:
				case GT:
				case NEQ:
				case LT:
				case LTEQ:
				case LITERAL_is:
				case CONCAT:
				case LITERAL_to:
				case MINUS:
				case LITERAL_div:
				case LITERAL_idiv:
				case LITERAL_mod:
				case BANG:
				case LITERAL_intersect:
				case LITERAL_except:
				case SLASH:
				case DSLASH:
				case LITERAL_text:
				case LITERAL_node:
				case LITERAL_attribute:
				case LITERAL_comment:
				case 190:
				case 191:
				case HASH:
				case SELF:
				case XML_COMMENT:
				case XML_PI:
				case AT:
				case PARENT:
				case LITERAL_child:
				case LITERAL_self:
				case LITERAL_descendant:
				case 204:
				case 205:
				case LITERAL_parent:
				case LITERAL_ancestor:
				case 208:
				case 209:
				case INTEGER_LITERAL:
				case DOUBLE_LITERAL:
				case DECIMAL_LITERAL:
				case XML_CDATA:
				{
					expr(_t,pathExpr);
					_t = _retTree;
					params.add(pathExpr);
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
			}
			else {
				break _loop286;
			}
			
		} while (true);
		}
		_t = __t283;
		_t = _t.getNextSibling();
		
				step = FunctionFactory.createFunction(context, fn, path, params);
				if (isPartial) {
					if (!(step instanceof FunctionCall))
						step = FunctionFactory.wrap(context, (Function)step);
					step = new PartialFunctionApplication(context, (FunctionCall) step);
				}
			
		_retTree = _t;
		return step;
	}
	
	public final Expression  functionReference(AST _t,
		PathExpr path
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST functionReference_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST name = null;
		org.exist.xquery.parser.XQueryAST arity = null;
		
			step = null;
		
		
		AST __t288 = _t;
		name = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,HASH);
		_t = _t.getFirstChild();
		arity = (org.exist.xquery.parser.XQueryAST)_t;
		match(_t,INTEGER_LITERAL);
		_t = _t.getNextSibling();
		
					QName qname;
					try {
						qname = QName.parse(staticContext, name.getText(), staticContext.getDefaultFunctionNamespace());
					} catch(XPathException e) {
						// throw exception with correct source location
						e.setLocation(name.getLine(), name.getColumn());
						throw e;
					}
					NamedFunctionReference ref = new NamedFunctionReference(context, qname, Integer.parseInt(arity.getText()));
					step = ref;
				
		_t = __t288;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
	public final Expression  lookup(AST _t,
		Expression leftExpr
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		Expression step;
		
		org.exist.xquery.parser.XQueryAST lookup_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		org.exist.xquery.parser.XQueryAST lookup = null;
		org.exist.xquery.parser.XQueryAST pos = null;
		
		AST __t278 = _t;
		lookup = _t==ASTNULL ? null :(org.exist.xquery.parser.XQueryAST)_t;
		match(_t,LOOKUP);
		_t = _t.getFirstChild();
		
		PathExpr lookupExpr = new PathExpr(context);
		int position = 0;
		
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case INTEGER_VALUE:
		{
			pos = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,INTEGER_VALUE);
			_t = _t.getNextSibling();
			position = Integer.parseInt(pos.getText());
			break;
		}
		case EOF:
		case EQNAME:
		case PARENTHESIZED:
		case ABSOLUTE_SLASH:
		case ABSOLUTE_DSLASH:
		case WILDCARD:
		case PREFIX_WILDCARD:
		case FUNCTION:
		case UNARY_MINUS:
		case UNARY_PLUS:
		case VARIABLE_REF:
		case ELEMENT:
		case TEXT:
		case INLINE_FUNCTION_DECL:
		case LOOKUP:
		case ARRAY:
		case ATTRIBUTE_TEST:
		case COMP_ELEM_CONSTRUCTOR:
		case COMP_ATTR_CONSTRUCTOR:
		case COMP_TEXT_CONSTRUCTOR:
		case COMP_COMMENT_CONSTRUCTOR:
		case COMP_PI_CONSTRUCTOR:
		case COMP_NS_CONSTRUCTOR:
		case COMP_DOC_CONSTRUCTOR:
		case PRAGMA:
		case GTEQ:
		case SEQUENCE:
		case NCNAME:
		case EQ:
		case STRING_LITERAL:
		case LITERAL_element:
		case COMMA:
		case LCURLY:
		case STAR:
		case PLUS:
		case LITERAL_map:
		case LITERAL_try:
		case LITERAL_some:
		case LITERAL_every:
		case LITERAL_if:
		case LITERAL_switch:
		case LITERAL_typeswitch:
		case LITERAL_update:
		case LITERAL_preceding:
		case LITERAL_following:
		case UNION:
		case LITERAL_return:
		case LITERAL_or:
		case LITERAL_and:
		case LITERAL_instance:
		case LITERAL_treat:
		case LITERAL_castable:
		case LITERAL_cast:
		case BEFORE:
		case AFTER:
		case LITERAL_eq:
		case LITERAL_ne:
		case LITERAL_lt:
		case LITERAL_le:
		case LITERAL_gt:
		case LITERAL_ge:
		case GT:
		case NEQ:
		case LT:
		case LTEQ:
		case LITERAL_is:
		case CONCAT:
		case LITERAL_to:
		case MINUS:
		case LITERAL_div:
		case LITERAL_idiv:
		case LITERAL_mod:
		case BANG:
		case LITERAL_intersect:
		case LITERAL_except:
		case SLASH:
		case DSLASH:
		case LITERAL_text:
		case LITERAL_node:
		case LITERAL_attribute:
		case LITERAL_comment:
		case 190:
		case 191:
		case HASH:
		case SELF:
		case XML_COMMENT:
		case XML_PI:
		case AT:
		case PARENT:
		case LITERAL_child:
		case LITERAL_self:
		case LITERAL_descendant:
		case 204:
		case 205:
		case LITERAL_parent:
		case LITERAL_ancestor:
		case 208:
		case 209:
		case INTEGER_LITERAL:
		case DOUBLE_LITERAL:
		case DECIMAL_LITERAL:
		case XML_CDATA:
		{
			{
			int _cnt281=0;
			_loop281:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					expr(_t,lookupExpr);
					_t = _retTree;
				}
				else {
					if ( _cnt281>=1 ) { break _loop281; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt281++;
			} while (true);
			}
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		
		if (lookupExpr.getLength() == 0) {
			if (lookup.getText().equals("?*")) {
				step = new Lookup(context, leftExpr);
			} else if (position == 0) {
			step = new Lookup(context, leftExpr, lookup.getText());
		} else {
							step = new Lookup(context, leftExpr, position);
		}
		} else {
		step = new Lookup(context, leftExpr, lookupExpr);
		}
		step.setASTNode(lookup);
		
		_t = __t278;
		_t = _t.getNextSibling();
		_retTree = _t;
		return step;
	}
	
	public final int  forwardAxis(AST _t) throws RecognitionException, PermissionDeniedException,EXistException {
		int axis;
		
		org.exist.xquery.parser.XQueryAST forwardAxis_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		axis= Constants.UNKNOWN_AXIS;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_child:
		{
			org.exist.xquery.parser.XQueryAST tmp127_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_child);
			_t = _t.getNextSibling();
			axis= Constants.CHILD_AXIS;
			break;
		}
		case LITERAL_attribute:
		{
			org.exist.xquery.parser.XQueryAST tmp128_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_attribute);
			_t = _t.getNextSibling();
			axis= Constants.ATTRIBUTE_AXIS;
			break;
		}
		case LITERAL_self:
		{
			org.exist.xquery.parser.XQueryAST tmp129_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_self);
			_t = _t.getNextSibling();
			axis= Constants.SELF_AXIS;
			break;
		}
		case LITERAL_parent:
		{
			org.exist.xquery.parser.XQueryAST tmp130_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_parent);
			_t = _t.getNextSibling();
			axis= Constants.PARENT_AXIS;
			break;
		}
		case LITERAL_descendant:
		{
			org.exist.xquery.parser.XQueryAST tmp131_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_descendant);
			_t = _t.getNextSibling();
			axis= Constants.DESCENDANT_AXIS;
			break;
		}
		case 204:
		{
			org.exist.xquery.parser.XQueryAST tmp132_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,204);
			_t = _t.getNextSibling();
			axis= Constants.DESCENDANT_SELF_AXIS;
			break;
		}
		case 205:
		{
			org.exist.xquery.parser.XQueryAST tmp133_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,205);
			_t = _t.getNextSibling();
			axis= Constants.FOLLOWING_SIBLING_AXIS;
			break;
		}
		case LITERAL_following:
		{
			org.exist.xquery.parser.XQueryAST tmp134_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_following);
			_t = _t.getNextSibling();
			axis= Constants.FOLLOWING_AXIS;
			break;
		}
		case 209:
		{
			org.exist.xquery.parser.XQueryAST tmp135_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,209);
			_t = _t.getNextSibling();
			axis= Constants.PRECEDING_SIBLING_AXIS;
			break;
		}
		case LITERAL_preceding:
		{
			org.exist.xquery.parser.XQueryAST tmp136_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_preceding);
			_t = _t.getNextSibling();
			axis= Constants.PRECEDING_AXIS;
			break;
		}
		case LITERAL_ancestor:
		{
			org.exist.xquery.parser.XQueryAST tmp137_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,LITERAL_ancestor);
			_t = _t.getNextSibling();
			axis= Constants.ANCESTOR_AXIS;
			break;
		}
		case 208:
		{
			org.exist.xquery.parser.XQueryAST tmp138_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
			match(_t,208);
			_t = _t.getNextSibling();
			axis= Constants.ANCESTOR_SELF_AXIS;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		_retTree = _t;
		return axis;
	}
	
	public final void predicate(AST _t,
		LocationStep step
	) throws RecognitionException, PermissionDeniedException,EXistException,XPathException {
		
		org.exist.xquery.parser.XQueryAST predicate_AST_in = (_t == ASTNULL) ? null : (org.exist.xquery.parser.XQueryAST)_t;
		
		AST __t276 = _t;
		org.exist.xquery.parser.XQueryAST tmp139_AST_in = (org.exist.xquery.parser.XQueryAST)_t;
		match(_t,PREDICATE);
		_t = _t.getFirstChild();
		PathExpr path = new PathExpr(context);
		expr(_t,path);
		_t = _retTree;
		
				    Predicate predicateExpr= new Predicate(context);
				    predicateExpr.addPath(path);
				    step.addPredicate(predicateExpr);
		
		_t = __t276;
		_t = _t.getNextSibling();
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"QNAME",
		"EQNAME",
		"PREDICATE",
		"FLWOR",
		"PARENTHESIZED",
		"ABSOLUTE_SLASH",
		"ABSOLUTE_DSLASH",
		"WILDCARD",
		"PREFIX_WILDCARD",
		"FUNCTION",
		"DYNAMIC_FCALL",
		"UNARY_MINUS",
		"UNARY_PLUS",
		"XPOINTER",
		"XPOINTER_ID",
		"VARIABLE_REF",
		"VARIABLE_BINDING",
		"ELEMENT",
		"ATTRIBUTE",
		"ATTRIBUTE_CONTENT",
		"TEXT",
		"VERSION_DECL",
		"NAMESPACE_DECL",
		"DEF_NAMESPACE_DECL",
		"DEF_COLLATION_DECL",
		"DEF_FUNCTION_NS_DECL",
		"ANNOT_DECL",
		"GLOBAL_VAR",
		"FUNCTION_DECL",
		"INLINE_FUNCTION_DECL",
		"FUNCTION_INLINE",
		"FUNCTION_TEST",
		"MAP_TEST",
		"LOOKUP",
		"ARRAY",
		"ARRAY_TEST",
		"PROLOG",
		"OPTION",
		"ATOMIC_TYPE",
		"MODULE",
		"ORDER_BY",
		"GROUP_BY",
		"POSITIONAL_VAR",
		"CATCH_ERROR_CODE",
		"CATCH_ERROR_DESC",
		"CATCH_ERROR_VAL",
		"MODULE_DECL",
		"MODULE_IMPORT",
		"SCHEMA_IMPORT",
		"ATTRIBUTE_TEST",
		"COMP_ELEM_CONSTRUCTOR",
		"COMP_ATTR_CONSTRUCTOR",
		"COMP_TEXT_CONSTRUCTOR",
		"COMP_COMMENT_CONSTRUCTOR",
		"COMP_PI_CONSTRUCTOR",
		"COMP_NS_CONSTRUCTOR",
		"COMP_DOC_CONSTRUCTOR",
		"PRAGMA",
		"GTEQ",
		"SEQUENCE",
		"\"xpointer\"",
		"opening parenthesis '('",
		"closing parenthesis ')'",
		"name",
		"\"xquery\"",
		"\"version\"",
		"semicolon ';'",
		"\"module\"",
		"\"namespace\"",
		"=",
		"string literal",
		"\"declare\"",
		"\"default\"",
		"\"boundary-space\"",
		"\"ordering\"",
		"\"construction\"",
		"\"base-uri\"",
		"\"copy-namespaces\"",
		"\"option\"",
		"\"function\"",
		"\"variable\"",
		"MOD",
		"\"import\"",
		"\"encoding\"",
		"\"collation\"",
		"\"element\"",
		"\"order\"",
		"\"empty\"",
		"\"greatest\"",
		"\"least\"",
		"\"preserve\"",
		"\"strip\"",
		"\"ordered\"",
		"\"unordered\"",
		"COMMA",
		"\"no-preserve\"",
		"\"inherit\"",
		"\"no-inherit\"",
		"dollar sign '$'",
		"opening curly brace '{'",
		"closing curly brace '}'",
		"COLON",
		"\"external\"",
		"\"schema\"",
		"braced uri literal",
		"\"as\"",
		"\"at\"",
		"\"empty-sequence\"",
		"question mark '?'",
		"wildcard '*'",
		"+",
		"\"item\"",
		"\"map\"",
		"\"array\"",
		"\"for\"",
		"\"let\"",
		"\"try\"",
		"\"some\"",
		"\"every\"",
		"\"if\"",
		"\"switch\"",
		"\"typeswitch\"",
		"\"update\"",
		"\"replace\"",
		"\"value\"",
		"\"insert\"",
		"\"delete\"",
		"\"rename\"",
		"\"with\"",
		"\"into\"",
		"\"preceding\"",
		"\"following\"",
		"\"catch\"",
		"union",
		"\"return\"",
		"\"where\"",
		"\"in\"",
		"\"allowing\"",
		"\"by\"",
		"\"stable\"",
		"\"ascending\"",
		"\"descending\"",
		"\"group\"",
		"\"satisfies\"",
		"\"case\"",
		"\"then\"",
		"\"else\"",
		"\"or\"",
		"\"and\"",
		"\"instance\"",
		"\"of\"",
		"\"treat\"",
		"\"castable\"",
		"\"cast\"",
		"BEFORE",
		"AFTER",
		"\"eq\"",
		"\"ne\"",
		"\"lt\"",
		"\"le\"",
		"\"gt\"",
		"\"ge\"",
		">",
		"!=",
		"<",
		"<=",
		"\"is\"",
		"\"isnot\"",
		"||",
		"\"to\"",
		"-",
		"\"div\"",
		"\"idiv\"",
		"\"mod\"",
		"BANG",
		"PRAGMA_START",
		"pragma expression",
		"\"union\"",
		"\"intersect\"",
		"\"except\"",
		"single slash '/'",
		"double slash '//'",
		"\"text\"",
		"\"node\"",
		"\"attribute\"",
		"\"comment\"",
		"\"processing-instruction\"",
		"\"document-node\"",
		"\"document\"",
		"HASH",
		".",
		"XML comment",
		"processing instruction",
		"opening brace '['",
		"closing brace ']'",
		"@ char",
		"..",
		"\"child\"",
		"\"self\"",
		"\"descendant\"",
		"\"descendant-or-self\"",
		"\"following-sibling\"",
		"\"parent\"",
		"\"ancestor\"",
		"\"ancestor-or-self\"",
		"\"preceding-sibling\"",
		"INTEGER_LITERAL",
		"DOUBLE_LITERAL",
		"DECIMAL_LITERAL",
		"\"schema-element\"",
		"XML end tag",
		"double quote '\\\"'",
		"single quote '",
		"QUOT_ATTRIBUTE_CONTENT",
		"ESCAPE_QUOT",
		"APOS_ATTRIBUTE_CONTENT",
		"ESCAPE_APOS",
		"ELEMENT_CONTENT",
		"end of XML comment",
		"end of processing instruction",
		"CDATA section",
		"\"collection\"",
		"\"validate\"",
		"start of processing instruction",
		"CDATA section start",
		"end of CDATA section",
		"LETTER",
		"DIGITS",
		"HEX_DIGITS",
		"NMSTART",
		"NMCHAR",
		"WS",
		"XQuery XQDoc comment",
		"XQuery comment",
		"PREDEFINED_ENTITY_REF",
		"CHAR_REF",
		"S",
		"NEXT_TOKEN",
		"CHAR",
		"BASECHAR",
		"IDEOGRAPHIC",
		"COMBINING_CHAR",
		"DIGIT",
		"EXTENDER",
		"INTEGER_VALUE"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[8];
		data[0]=-9006778328432862L;
		data[1]=9157507459273721352L;
		data[2]=-15771394864314688L;
		data[3]=4297064350L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 9012250136281088L, 2392537469812736L, -1441151880758558720L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[8];
		data[0]=-9006777254691038L;
		data[1]=9157547041692321288L;
		data[2]=-15771394864314688L;
		data[3]=4297064350L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[8];
		data[0]=-9006778328432862L;
		data[1]=9157547041692321288L;
		data[2]=-15771394864314688L;
		data[3]=4297064350L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 1074790400L, 40132174413824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 1048576L, 40132174413824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[8];
		data[1]=1024L;
		data[3]=1835008L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 1074790400L, 35734127902720L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 6176L, 8L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[8];
		data[0]=-9001727446892694L;
		data[1]=9160181471703467528L;
		data[2]=-15771394863069216L;
		data[3]=4297064350L;
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0]=-9006778328432854L;
		data[1]=9157507459273721352L;
		data[2]=-15771394864314688L;
		data[3]=4297064350L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[8];
		data[0]=2287828610723086336L;
		data[1]=549755813888L;
		data[3]=4294967320L;
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[8];
		data[0]=-9006778328432862L;
		data[1]=9157788934250432008L;
		data[2]=-15771394864314688L;
		data[3]=4297064350L;
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	}
	
