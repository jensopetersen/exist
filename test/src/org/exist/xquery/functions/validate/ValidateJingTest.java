package org.exist.xquery.functions.validate;


import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import static org.junit.Assert.*;

import org.exist.storage.DBBroker;
import org.exist.xmldb.DatabaseInstanceManager;
import org.exist.xquery.XPathException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Ignore;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;

/**
 *
 * @author jim.fuller@webcomposite.com
 */
public class ValidateJingTest {

    private static Class cl = null;
    private static XPathQueryService service;
    private static Collection root = null;
    private static Database database = null;

    public ValidateJingTest() {
    }

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();

        // initialize driver
        cl = Class.forName("org.exist.xmldb.DatabaseImpl");
        database = (Database) cl.newInstance();
        database.setProperty("create-database", "true");
        DatabaseManager.registerDatabase(database);
        root = DatabaseManager.getCollection("xmldb:exist://" + DBBroker.ROOT_COLLECTION, "admin", null);
        service = (XPathQueryService) root.getService("XQueryService", "1.0");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DatabaseManager.deregisterDatabase(database);
        DatabaseInstanceManager dim = (DatabaseInstanceManager) root.getService("DatabaseInstanceManager", "1.0");
        dim.shutdown();
    }

    @Test
    public void testValidateXSDwithJing() throws XPathException {

        ResourceSet result = null;
        String r = "";
        try {
            String query = "let $v := <doc>\n" +
                    "\t<title>Title</title>\n" +
                    "\t<p>Some paragraph.</p>\n" +
                    "      </doc>\n" +
                    "let $schema := <xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "\t\t elementFormDefault=\"qualified\">\n" +
                    "\t<xs:element name=\"doc\">\n" +
                    "\t  <xs:complexType>\n" +
                    "\t    <xs:sequence>\n" +
                    "\t      <xs:element minOccurs=\"0\" ref=\"title\"/>\n" +
                    "\t      <xs:element minOccurs=\"0\" maxOccurs=\"unbounded\" ref=\"p\"/>\n" +
                    "\t    </xs:sequence>\n" +
                    "\t  </xs:complexType>\n" +
                    "\t</xs:element>\n" +
                    "\t<xs:element name=\"title\" type=\"xs:string\"/>\n" +
                    "\t<xs:element name=\"p\" type=\"xs:string\"/>\n" +
                    "      </xs:schema>\n" +
                    "return\n" +
                    "\n" +
                    "\tvalidation:jing($v,$schema)";

            result = service.query(query);
            r = (String) result.getResource(0).getContent();
            assertEquals("true", r);
        } catch (XMLDBException e) {
            System.out.println("testValidateXSDwithJing(): " + e);
            fail(e.getMessage());
        }

    }

    @Test
    public void testValidateXSDwithJing_invalid() throws XPathException {

        ResourceSet result = null;
        String r = "";
        try {
            String query = "let $v := <doc>\n" +
                    "\t<title1>Title</title1>\n" +
                    "\t<p>Some paragraph.</p>\n" +
                    "      </doc>\n" +
                    "let $schema := <xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "\t\t elementFormDefault=\"qualified\">\n" +
                    "\t<xs:element name=\"doc\">\n" +
                    "\t  <xs:complexType>\n" +
                    "\t    <xs:sequence>\n" +
                    "\t      <xs:element minOccurs=\"0\" ref=\"title\"/>\n" +
                    "\t      <xs:element minOccurs=\"0\" maxOccurs=\"unbounded\" ref=\"p\"/>\n" +
                    "\t    </xs:sequence>\n" +
                    "\t  </xs:complexType>\n" +
                    "\t</xs:element>\n" +
                    "\t<xs:element name=\"title\" type=\"xs:string\"/>\n" +
                    "\t<xs:element name=\"p\" type=\"xs:string\"/>\n" +
                    "      </xs:schema>\n" +
                    "return\n" +
                    "\n" +
                    "\tvalidation:jing($v,$schema)";

            result = service.query(query);
            r = (String) result.getResource(0).getContent();
            assertEquals("false", r);
        } catch (XMLDBException e) {
            System.out.println("testValidateXSDwithJing(): " + e);
            fail(e.getMessage());
        }

    }

    @Test
    public void testValidateRNGwithJing() throws XPathException {

        ResourceSet result = null;
        String r = "";
        try {
            String query = "let $v := <doc>\n" +
                    "\t<title>Title</title>\n" +
                    "\t<p>Some paragraph.</p>\n" +
                    "      </doc>\n" +
                    "let $schema := <grammar xmlns=\"http://relaxng.org/ns/structure/1.0\">\n" +
                    "  <start>\n" +
                    "    <ref name=\"doc\"/>\n" +
                    "  </start>\n" +
                    "  <define name=\"doc\">\n" +
                    "    <element name=\"doc\">\n" +
                    "      <optional>\n" +
                    "        <ref name=\"title\"/>\n" +
                    "      </optional>\n" +
                    "      <zeroOrMore>\n" +
                    "        <ref name=\"p\"/>\n" +
                    "      </zeroOrMore>\n" +
                    "    </element>\n" +
                    "  </define>\n" +
                    "  <define name=\"title\">\n" +
                    "    <element name=\"title\">\n" +
                    "      <text/>\n" +
                    "    </element>\n" +
                    "  </define>\n" +
                    "  <define name=\"p\">\n" +
                    "    <element name=\"p\">\n" +
                    "      <text/>\n" +
                    "    </element>\n" +
                    "  </define>\n" +
                    "</grammar>\n" +

                    "return\n" +
                    "\n" +
                    "\tvalidation:jing($v,$schema)";

            result = service.query(query);
            r = (String) result.getResource(0).getContent();
            assertEquals("true", r);
        } catch (XMLDBException e) {
            System.out.println("testValidateRNGwithJing(): " + e);
            fail(e.getMessage());
        }

    }

    @Test
    public void testValidateRNGwithJing_invalid() throws XPathException {

        ResourceSet result = null;
        String r = "";
        try {
            String query = "let $v := <doc>\n" +
                    "\t<title1>Title</title1>\n" +
                    "\t<p>Some paragraph.</p>\n" +
                    "      </doc>\n" +
                    "let $schema := <grammar xmlns=\"http://relaxng.org/ns/structure/1.0\">\n" +
                    "  <start>\n" +
                    "    <ref name=\"doc\"/>\n" +
                    "  </start>\n" +
                    "  <define name=\"doc\">\n" +
                    "    <element name=\"doc\">\n" +
                    "      <optional>\n" +
                    "        <ref name=\"title\"/>\n" +
                    "      </optional>\n" +
                    "      <zeroOrMore>\n" +
                    "        <ref name=\"p\"/>\n" +
                    "      </zeroOrMore>\n" +
                    "    </element>\n" +
                    "  </define>\n" +
                    "  <define name=\"title\">\n" +
                    "    <element name=\"title\">\n" +
                    "      <text/>\n" +
                    "    </element>\n" +
                    "  </define>\n" +
                    "  <define name=\"p\">\n" +
                    "    <element name=\"p\">\n" +
                    "      <text/>\n" +
                    "    </element>\n" +
                    "  </define>\n" +
                    "</grammar>\n" +

                    "return\n" +
                    "\n" +
                    "\tvalidation:jing($v,$schema)";

            result = service.query(query);
            r = (String) result.getResource(0).getContent();
            assertEquals("false", r);
        } catch (XMLDBException e) {
            System.out.println("testValidateRNGwithJing(): " + e);
            fail(e.getMessage());
        }

    }

    /* Hint:
     * 14013 [main] DEBUG org.exist.xquery.XQuery  - Compilation took 31 ms
     * 14013 [main] DEBUG org.exist.xquery.functions.validation.Shared  - Streaming element or document node
     * 14029 [main] ERROR org.exist.xquery.PathExpr  - org.exist.storage.io.ExistIOException: A problem ocurred while serializing the node set: null
     * 14029 [main] DEBUG org.exist.xquery.XQuery  - Execution took 16 ms
     * 14029 [main] DEBUG org.exist.xquery.XQuery  - mostRecentDocumentTime: 0
     */
    @Test @Ignore("Fails")
    public void repeatTests(){
        for(int i=0 ; i<100 ; i++){
            try {
                testValidateRNGwithJing();
                testValidateRNGwithJing_invalid();
                testValidateXSDwithJing();
                testValidateXSDwithJing_invalid();

            } catch (Exception ex) {
                fail(ex.getMessage());
                ex.printStackTrace();
            }

        }
    }
}
