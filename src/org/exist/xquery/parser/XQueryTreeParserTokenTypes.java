// $ANTLR 2.7.4: "XQueryTree.g" -> "XQueryTreeParser.java"$

	package org.exist.xquery.parser;

	import antlr.debug.misc.*;
	import java.io.StringReader;
	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Iterator;
	import java.util.Map;
	import java.util.HashMap;
	import java.util.Stack;
	import org.exist.storage.BrokerPool;
	import org.exist.storage.DBBroker;
	import org.exist.storage.analysis.Tokenizer;
	import org.exist.EXistException;
	import org.exist.dom.DocumentSet;
	import org.exist.dom.DocumentImpl;
	import org.exist.dom.QName;
	import org.exist.security.PermissionDeniedException;
	import org.exist.security.User;
	import org.exist.xquery.*;
	import org.exist.xquery.value.*;
	import org.exist.xquery.functions.*;
	import org.exist.xquery.update.*;

public interface XQueryTreeParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int QNAME = 4;
	int PREDICATE = 5;
	int FLWOR = 6;
	int PARENTHESIZED = 7;
	int ABSOLUTE_SLASH = 8;
	int ABSOLUTE_DSLASH = 9;
	int WILDCARD = 10;
	int PREFIX_WILDCARD = 11;
	int FUNCTION = 12;
	int UNARY_MINUS = 13;
	int UNARY_PLUS = 14;
	int XPOINTER = 15;
	int XPOINTER_ID = 16;
	int VARIABLE_REF = 17;
	int VARIABLE_BINDING = 18;
	int ELEMENT = 19;
	int ATTRIBUTE = 20;
	int ATTRIBUTE_CONTENT = 21;
	int TEXT = 22;
	int VERSION_DECL = 23;
	int NAMESPACE_DECL = 24;
	int DEF_NAMESPACE_DECL = 25;
	int DEF_COLLATION_DECL = 26;
	int DEF_FUNCTION_NS_DECL = 27;
	int GLOBAL_VAR = 28;
	int FUNCTION_DECL = 29;
	int PROLOG = 30;
	int OPTION = 31;
	int ATOMIC_TYPE = 32;
	int MODULE = 33;
	int ORDER_BY = 34;
	int GROUP_BY = 35;
	int POSITIONAL_VAR = 36;
	int BEFORE = 37;
	int AFTER = 38;
	int MODULE_DECL = 39;
	int MODULE_IMPORT = 40;
	int SCHEMA_IMPORT = 41;
	int ATTRIBUTE_TEST = 42;
	int COMP_ELEM_CONSTRUCTOR = 43;
	int COMP_ATTR_CONSTRUCTOR = 44;
	int COMP_TEXT_CONSTRUCTOR = 45;
	int COMP_COMMENT_CONSTRUCTOR = 46;
	int COMP_PI_CONSTRUCTOR = 47;
	int COMP_NS_CONSTRUCTOR = 48;
	int COMP_DOC_CONSTRUCTOR = 49;
	int PRAGMA = 50;
	int LITERAL_xpointer = 51;
	int LPAREN = 52;
	int RPAREN = 53;
	int NCNAME = 54;
	int LITERAL_xquery = 55;
	int LITERAL_version = 56;
	int SEMICOLON = 57;
	int LITERAL_module = 58;
	int LITERAL_namespace = 59;
	int EQ = 60;
	int STRING_LITERAL = 61;
	int LITERAL_declare = 62;
	int LITERAL_default = 63;
	// "boundary-space" = 64
	int LITERAL_ordering = 65;
	int LITERAL_construction = 66;
	// "base-uri" = 67
	// "copy-namespaces" = 68
	int LITERAL_option = 69;
	int LITERAL_function = 70;
	int LITERAL_variable = 71;
	int LITERAL_import = 72;
	int LITERAL_encoding = 73;
	int LITERAL_collation = 74;
	int LITERAL_element = 75;
	int LITERAL_order = 76;
	int LITERAL_empty = 77;
	int LITERAL_greatest = 78;
	int LITERAL_least = 79;
	int LITERAL_preserve = 80;
	int LITERAL_strip = 81;
	int LITERAL_ordered = 82;
	int LITERAL_unordered = 83;
	int COMMA = 84;
	// "no-preserve" = 85
	int LITERAL_inherit = 86;
	// "no-inherit" = 87
	int DOLLAR = 88;
	int LCURLY = 89;
	int RCURLY = 90;
	int COLON = 91;
	int LITERAL_external = 92;
	int LITERAL_at = 93;
	int LITERAL_schema = 94;
	int LITERAL_as = 95;
	// "empty-sequence" = 96
	int QUESTION = 97;
	int STAR = 98;
	int PLUS = 99;
	int LITERAL_item = 100;
	int LITERAL_for = 101;
	int LITERAL_let = 102;
	int LITERAL_some = 103;
	int LITERAL_every = 104;
	int LITERAL_if = 105;
	int LITERAL_typeswitch = 106;
	int LITERAL_update = 107;
	int LITERAL_replace = 108;
	int LITERAL_value = 109;
	int LITERAL_insert = 110;
	int LITERAL_delete = 111;
	int LITERAL_rename = 112;
	int LITERAL_with = 113;
	int LITERAL_into = 114;
	int LITERAL_preceding = 115;
	int LITERAL_following = 116;
	int LITERAL_where = 117;
	int LITERAL_return = 118;
	int LITERAL_in = 119;
	int LITERAL_by = 120;
	int LITERAL_ascending = 121;
	int LITERAL_descending = 122;
	int LITERAL_group = 123;
	int LITERAL_satisfies = 124;
	int LITERAL_case = 125;
	int LITERAL_then = 126;
	int LITERAL_else = 127;
	int LITERAL_or = 128;
	int LITERAL_and = 129;
	int LITERAL_instance = 130;
	int LITERAL_of = 131;
	int LITERAL_treat = 132;
	int LITERAL_castable = 133;
	int LITERAL_cast = 134;
	int LT = 135;
	int GT = 136;
	int LITERAL_eq = 137;
	int LITERAL_ne = 138;
	int LITERAL_lt = 139;
	int LITERAL_le = 140;
	int LITERAL_gt = 141;
	int LITERAL_ge = 142;
	int NEQ = 143;
	int GTEQ = 144;
	int LTEQ = 145;
	int LITERAL_is = 146;
	int LITERAL_isnot = 147;
	int ANDEQ = 148;
	int OREQ = 149;
	int LITERAL_to = 150;
	int MINUS = 151;
	int LITERAL_div = 152;
	int LITERAL_idiv = 153;
	int LITERAL_mod = 154;
	int PRAGMA_START = 155;
	int PRAGMA_END = 156;
	int LITERAL_union = 157;
	int UNION = 158;
	int LITERAL_intersect = 159;
	int LITERAL_except = 160;
	int SLASH = 161;
	int DSLASH = 162;
	int LITERAL_text = 163;
	int LITERAL_node = 164;
	int LITERAL_attribute = 165;
	int LITERAL_comment = 166;
	// "processing-instruction" = 167
	// "document-node" = 168
	int LITERAL_document = 169;
	int SELF = 170;
	int XML_COMMENT = 171;
	int XML_PI = 172;
	int LPPAREN = 173;
	int RPPAREN = 174;
	int AT = 175;
	int PARENT = 176;
	int LITERAL_child = 177;
	int LITERAL_self = 178;
	int LITERAL_descendant = 179;
	// "descendant-or-self" = 180
	// "following-sibling" = 181
	int LITERAL_parent = 182;
	int LITERAL_ancestor = 183;
	// "ancestor-or-self" = 184
	// "preceding-sibling" = 185
	int DOUBLE_LITERAL = 186;
	int DECIMAL_LITERAL = 187;
	int INTEGER_LITERAL = 188;
	// "schema-element" = 189
	int END_TAG_START = 190;
	int QUOT = 191;
	int APOS = 192;
	int QUOT_ATTRIBUTE_CONTENT = 193;
	int ESCAPE_QUOT = 194;
	int APOS_ATTRIBUTE_CONTENT = 195;
	int ESCAPE_APOS = 196;
	int ELEMENT_CONTENT = 197;
	int XML_COMMENT_END = 198;
	int XML_PI_END = 199;
	int XML_CDATA = 200;
	int LITERAL_collection = 201;
	int LITERAL_validate = 202;
	int XML_PI_START = 203;
	int XML_CDATA_START = 204;
	int XML_CDATA_END = 205;
	int LETTER = 206;
	int DIGITS = 207;
	int HEX_DIGITS = 208;
	int NMSTART = 209;
	int NMCHAR = 210;
	int WS = 211;
	int EXPR_COMMENT = 212;
	int PREDEFINED_ENTITY_REF = 213;
	int CHAR_REF = 214;
	int S = 215;
	int NEXT_TOKEN = 216;
	int CHAR = 217;
	int BASECHAR = 218;
	int IDEOGRAPHIC = 219;
	int COMBINING_CHAR = 220;
	int DIGIT = 221;
	int EXTENDER = 222;
}
