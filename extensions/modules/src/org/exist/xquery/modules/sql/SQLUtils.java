package org.exist.xquery.modules.sql;

import java.sql.Types;

import org.exist.xquery.value.Type;

/**
 * Utility class for converting to/from SQL types and escaping XML text and attributes.
 * 
 * @author Adam Retter <adam@exist-db.org>
 * @author Robert Walpole <robert.walpole@metoffice.gov.uk>
 * @serial 2010-07-23
 * @version 1.0
 * 
 */
public final class SQLUtils {
	
	public static int sqlTypeFromString( String sqlType )
    {
        sqlType = sqlType.toUpperCase();

        if( sqlType.equals( "ARRAY" ) ) {
            return( Types.ARRAY );
        } else if( sqlType.equals( "BIGINT" ) ) {
            return( Types.BIGINT );
        } else if( sqlType.equals( "BINARY" ) ) {
            return( Types.BINARY );
        } else if( sqlType.equals( "BIT" ) ) {
            return( Types.BIT );
        } else if( sqlType.equals( "BLOB" ) ) {
            return( Types.BLOB );
        } else if( sqlType.equals( "BOOLEAN" ) ) {
            return( Types.BOOLEAN );
        } else if( sqlType.equals( "CHAR" ) ) {
            return( Types.CHAR );
        } else if( sqlType.equals( "CLOB" ) ) {
            return( Types.CLOB );
        } else if( sqlType.equals( "DECIMAL" ) ) {
            return( Types.DECIMAL );
        } else if( sqlType.equals( "DOUBLE" ) ) {
            return( Types.DOUBLE );
        } else if( sqlType.equals( "FLOAT" ) ) {
            return( Types.FLOAT );
        } else if( sqlType.equals( "LONGVARCHAR" ) ) {
            return( Types.LONGVARCHAR );
        } else if( sqlType.equals( "NUMERIC" ) ) {
            return( Types.NUMERIC );
        } else if( sqlType.equals( "SMALLINT" ) ) {
            return( Types.SMALLINT );
        } else if( sqlType.equals( "TINYINT" ) ) {
            return( Types.TINYINT );
        } else if( sqlType.equals( "INTEGER" ) ) {
            return( Types.INTEGER );
        } else if( sqlType.equals( "VARCHAR" ) ) {
            return( Types.VARCHAR );
        } else if(sqlType.equals("SQLXML")) {
            return Types.SQLXML;
        } else if(sqlType.equals("TIMESTAMP")) {
            return Types.TIMESTAMP;
        } else {
            return( Types.VARCHAR ); //default
        }
    }

    /**
     * Converts a SQL data type to an XML data type.
     *
     * @param   sqlType  The SQL data type as specified by JDBC
     *
     * @return  The XML Type as specified by eXist
     */
    public static int sqlTypeToXMLType( int sqlType )
    {
        switch( sqlType ) {

            case Types.ARRAY: {
                return( Type.NODE );
            }

            case Types.BIGINT: {
                return( Type.INT );
            }

            case Types.BINARY: {
                return( Type.BASE64_BINARY );
            }

            case Types.BIT: {
                return( Type.INT );
            }

            case Types.BLOB: {
                return( Type.BASE64_BINARY );
            }

            case Types.BOOLEAN: {
                return( Type.BOOLEAN );
            }

            case Types.CHAR: {
                return( Type.STRING );
            }

            case Types.CLOB: {
                return( Type.STRING );
            }

            case Types.DECIMAL: {
                return( Type.DECIMAL );
            }

            case Types.DOUBLE: {
                return( Type.DOUBLE );
            }

            case Types.FLOAT: {
                return( Type.FLOAT );
            }

            case Types.LONGVARCHAR: {
                return( Type.STRING );
            }

            case Types.NUMERIC: {
                return( Type.NUMBER );
            }

            case Types.SMALLINT: {
                return( Type.INT );
            }

            case Types.TINYINT: {
                return( Type.INT );
            }

            case Types.INTEGER: {
                return( Type.INTEGER );
            }

            case Types.VARCHAR: {
                return( Type.STRING );
            }

            case Types.SQLXML: {
                return(Type.NODE);
            }
                
            case Types.TIMESTAMP: {
                return Type.DATE_TIME;
            }

            default: {
                return( Type.ANY_TYPE );
            }
        }
    }

    public static String escapeXmlText( String text )
    {
        String work = null;

        if( text != null ) {
            work = text.replaceAll( "\\&", "\\&amp;" );
            work = work.replaceAll( "<", "\\&lt;" );
            work = work.replaceAll( ">", "\\&gt;" );
        }

        return( work );
    }

    public static String escapeXmlAttr( String attr )
    {
        String work = null;

        if( attr != null ) {
            work = escapeXmlText( attr );
            work = work.replaceAll( "'", "\\&apos;" );
            work = work.replaceAll( "\"", "\\&quot;" );
        }

        return( work );
    }

}