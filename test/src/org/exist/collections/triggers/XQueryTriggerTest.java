/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-2010 The eXist Project
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
 *  $Id$
 */
package org.exist.collections.triggers;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.xml.transform.OutputKeys;

import org.exist.TestUtils;
import org.exist.storage.DBBroker;
import org.exist.util.Base64Decoder;
import org.exist.xmldb.DatabaseInstanceManager;
import org.exist.xmldb.EXistResource;
import org.exist.xmldb.IndexQueryService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

/** class under test : {@link XQueryTrigger}
 * @author Pierrick Brihaye <pierrick.brihaye@free.fr>
 */
public class XQueryTriggerTest {
	
	private final static String URI = "xmldb:exist://" + DBBroker.ROOT_COLLECTION;
	private final static String TEST_COLLECTION = "testXQueryTrigger";

    private final String COLLECTION_CONFIG =
    	"<exist:collection xmlns:exist='http://exist-db.org/collection-config/1.0'>" +
	    "  <exist:triggers>" +
		"     <exist:trigger event='create-document'" +
		"                    class='org.exist.collections.triggers.XQueryTrigger'>" +
		"	     <exist:parameter name='query' " +
		"			value=\"import module namespace log = 'log' at '" +
						URI +  "/" + TEST_COLLECTION + "/" + MODULE_NAME + "';" +
						"log:log('trigger1')\" />" +
		"	     <exist:parameter name='bindingPrefix' value='log'/>" +
		"        />" +
		"     </exist:trigger>" +
		
		"     <exist:trigger event='update-document'" +
		"                    class='org.exist.collections.triggers.XQueryTrigger'>" +
		"	     <exist:parameter name='query' " +
		"			value=\"import module namespace log = 'log' at '" +
						URI +  "/" + TEST_COLLECTION + "/" + MODULE_NAME + "';" +
						"log:log('trigger2')\" />" +
		"	     <exist:parameter name=\"bindingPrefix\" value=\"log\"/>" +
		"        />" +
		"     </exist:trigger>" +

		"     <exist:trigger event='delete-document'" +
		"                    class='org.exist.collections.triggers.XQueryTrigger'>" +
		"	     <exist:parameter name=\"query\" value=\"import module namespace log = 'log' at '" + 
					URI +  "/" + TEST_COLLECTION + "/" + MODULE_NAME + "';" +
							"log:log('trigger3')\"/>" +
		"	     <exist:parameter name='bindingPrefix' value='log' />" +
		"        />" +
		"     </exist:trigger>" +
		
		"  </exist:triggers>" +
        "</exist:collection>";    

    private final String COLLECTION_CONFIG_FOR_COLLECTIONS_EVENTS =
    	"<exist:collection xmlns:exist='http://exist-db.org/collection-config/1.0'>" +
	    "  <exist:triggers>" +
		"     <exist:trigger event='create-collection'" +
		"                    class='org.exist.collections.triggers.XQueryTrigger'>" +
		"	     <exist:parameter name='query' " +
		"			value=\"import module namespace log = 'log' at '" +
						URI +  "/" + TEST_COLLECTION + "/" + MODULE_COLLECTION_NAME + "';" +
						"log:log('trigger4')\" />" +
		"	     <exist:parameter name='bindingPrefix' value='log'/>" +
		"        />" +
		"     </exist:trigger>" +
		
		"     <exist:trigger event='rename-collection'" +
		"                    class='org.exist.collections.triggers.XQueryTrigger'>" +
		"	     <exist:parameter name='query' " +
		"			value=\"import module namespace log = 'log' at '" +
						URI +  "/" + TEST_COLLECTION + "/" + MODULE_COLLECTION_NAME + "';" +
						"log:log('trigger5')\" />" +
		"	     <exist:parameter name=\"bindingPrefix\" value=\"log\"/>" +
		"        />" +
		"     </exist:trigger>" +

		"     <exist:trigger event='delete-collection'" +
		"                    class='org.exist.collections.triggers.XQueryTrigger'>" +
		"	     <exist:parameter name=\"query\" value=\"import module namespace log = 'log' at '" + 
					URI +  "/" + TEST_COLLECTION + "/" + MODULE_COLLECTION_NAME + "';" +
							"log:log('trigger6')\"/>" +
		"	     <exist:parameter name='bindingPrefix' value='log' />" +
		"        />" +
		"     </exist:trigger>" +
		
		"  </exist:triggers>" +
        "</exist:collection>";    

    private final String EMPTY_COLLECTION_CONFIG =
    	"<exist:collection xmlns:exist='http://exist-db.org/collection-config/1.0'>" +
        "</exist:collection>";    
    
    private final static String DOCUMENT_NAME = "test.xml";
    
    private final static String DOCUMENT_CONTENT = 
		  "<test>"
		+ "<item id='1'><price>5.6</price><stock>22</stock></item>"
		+ "<item id='2'><price>7.4</price><stock>43</stock></item>"
		+ "<item id='3'><price>18.4</price><stock>5</stock></item>"
		+ "<item id='4'><price>65.54</price><stock>16</stock></item>"
		+ "</test>";    

    /** XUpdate document update specification */
    private final static String DOCUMENT_UPDATE =
        "<xu:modifications xmlns:xu='http://www.xmldb.org/xupdate' version='1.0'>" +
        "<!-- special offer -->" +
        "<xu:update select='/test/item[@id = \"3\"]/price'>" +       
        	"15.2"+
        "</xu:update>" +
      "</xu:modifications>";
    
    private final static String MODIFIED_DOCUMENT_CONTENT = 
    	DOCUMENT_CONTENT.replaceAll("<price>18.4</price>", "<price>15.2</price>");
   
    private final static String BINARY_DOCUMENT_NAME = "1x1.gif";
    private final static String BINARY_DOCUMENT_CONTENT = "R0lGODlhAQABAIABAAD/AP///yH+EUNyZWF0ZWQgd2l0aCBHSU1QACwAAAAAAQABAAACAkQBADs=";    
    
    /** "log" document that will be updated by the trigger */
    private final static String LOG_NAME = "XQueryTriggerLog.xml";
    
    /** initial content of the "log" document */
    private final static String EMPTY_LOG = "<events/>";
    
    /** XQuery module implementing the trigger under test */
    private final static String MODULE_NAME = "XQueryTriggerLogger.xqm";

    private final static String MODULE_COLLECTION_NAME = "XQueryCollectionTriggerLogger.xqm";
    
    /** XQuery module implementing the trigger under test; 
     * the log() XQuery function will add an <event> element inside <events> element */
    private final static String MODULE =
    	"module namespace log='log'; " +
    	"import module namespace xmldb='http://exist-db.org/xquery/xmldb'; " +
    	"import module namespace util='http://exist-db.org/xquery/util'; " +
    	"declare variable $log:type external;" +
    	"declare variable $log:collection external;" +    	
    	"declare variable $log:uri external;" +
    	"declare variable $log:event external;" +
    	"declare function log:log($id as xs:string?) {" +
    	"let $isLoggedIn := xmldb:login('" + URI + "/" + TEST_COLLECTION + "', 'admin', '') return " +
    	  "xmldb:update(" +
    	    "'" + URI + "/" + TEST_COLLECTION + "', " +
            "<xu:modifications xmlns:xu='http://www.xmldb.org/xupdate' version='1.0'>" +
              "<xu:append select='/events'>" +
                "<xu:element name='event'>" +
                  "<xu:attribute name='id'>{$id}</xu:attribute>" +
                  "<xu:attribute name='time'>{current-dateTime()}</xu:attribute>" +
                  "<xu:attribute name='type'>{$log:type}</xu:attribute>" +
                  "<xu:element name='collection'>{$log:collection}</xu:element>" +
                  "<xu:element name='uri'>{$log:uri}</xu:element>" +
                  "<xu:element name='event'>{$log:event}</xu:element>" +                 
                  "<xu:element name='document'>{if (util:is-binary-doc($log:uri)) then util:binary-doc($log:uri) else doc($log:uri)}</xu:element>" +   
                "</xu:element>" +            
              "</xu:append>" +
            "</xu:modifications>" +
          ")" +
        "};";
    
    /** XQuery module implementing the trigger under test; 
     * the log() XQuery function will add an <event> element inside <events> element */
    private final static String MODULE_COLLECTION =
    	"module namespace log='log'; " +
    	"import module namespace xmldb='http://exist-db.org/xquery/xmldb'; " +
    	"declare variable $log:eventType external;" +
    	"declare variable $log:collectionName external;" +    	
    	"declare variable $log:triggerEvent external;" +
    	"declare function log:log($id as xs:string?) {" +
    	"let $isLoggedIn := xmldb:login('" + URI + "/" + TEST_COLLECTION + "', 'admin', '') return " +
    	  "xmldb:update(" +
    	    "'" + URI + "/" + TEST_COLLECTION + "', " +
            "<xu:modifications xmlns:xu='http://www.xmldb.org/xupdate' version='1.0'>" +
              "<xu:append select='/events'>" +
                "<xu:element name='event'>" +
                  "<xu:attribute name='id'>{$id}</xu:attribute>" +
                  "<xu:attribute name='time'>{current-dateTime()}</xu:attribute>" +
                  "<xu:attribute name='type'>{$log:eventType}</xu:attribute>" +
                  "<xu:element name='collectionName'>{$log:collectionName}</xu:element>" +
                  "<xu:element name='triggerEvent'>{$log:triggerEvent}</xu:element>" +                 
                "</xu:element>" +            
              "</xu:append>" +
            "</xu:modifications>" +
          ")" +
        "};";

    private static Collection root;
    private static Collection testCollection;

      /** XQuery module implementing the invalid trigger under test */
    private final static String INVALID_MODULE =
    	"module namespace log='log'; " +
    	"import module namespace xmldb='http://exist-db.org/xquery/xmldb'; " +
    	"declare variable $log:type external;" +
    	"declare variable $log:collection external;" +
    	"declare variable $log:uri external;" +
    	"declare variable $log:event external;" +
    	"declare function log:log($id as xs:string?) {" +
    	"   undeclared-function-causes-trigger-error()" +
        "};";

    /** just start the DB and create the test collection */
    @BeforeClass
    public static void startDB() {
        try {
            // initialize driver
            Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            Database database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");
            DatabaseManager.registerDatabase(database);

            root = DatabaseManager.getCollection(URI, "admin", "");
            CollectionManagementService service = (CollectionManagementService) root
                    .getService("CollectionManagementService", "1.0");
            testCollection = service.createCollection(TEST_COLLECTION);
            assertNotNull(testCollection);
        } catch (ClassNotFoundException e) {
        	fail(e.getMessage());
        } catch (InstantiationException e) {
        	fail(e.getMessage());
        } catch (IllegalAccessException e) {
        	fail(e.getMessage());
        } catch (XMLDBException e) {
            e.printStackTrace();
        	fail(e.getMessage());
        }
    }

    @AfterClass
    public static void shutdownDB() {
        TestUtils.cleanupDB();
        try {
            Collection root = DatabaseManager.getCollection("xmldb:exist://" + DBBroker.ROOT_COLLECTION, "admin", "");
            DatabaseInstanceManager mgr = (DatabaseInstanceManager) root.getService("DatabaseInstanceManager", "1.0");
            mgr.shutdown();
        } catch (XMLDBException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        testCollection = null;
    }

    /** create "log" document that will be updated by the trigger,
     * and store the XQuery module implementing the trigger under test */
    @Before
    public void storePreliminaryDocuments() {
    	try {
			XMLResource doc = (XMLResource) testCollection.createResource(LOG_NAME, "XMLResource" );
			doc.setContent(EMPTY_LOG);
			testCollection.storeResource(doc);
	
			BinaryResource module = (BinaryResource) testCollection.createResource(MODULE_NAME, "BinaryResource" );
			((EXistResource)module).setMimeType("application/xquery");
			module.setContent(MODULE.getBytes());
			testCollection.storeResource(module);
			
			module = (BinaryResource) testCollection.createResource(MODULE_COLLECTION_NAME, "BinaryResource" );
			((EXistResource)module).setMimeType("application/xquery");
			module.setContent(MODULE_COLLECTION.getBytes());
			testCollection.storeResource(module);

    	} catch (XMLDBException e) {
    		fail(e.getMessage());
        }    	
    }

    /** test a trigger fired by storing a Document  */
    @Test
    public void storeDocument() {
    	
    	ResourceSet result;
    	
    	try {    		
    		// configure the Collection with the trigger under test
			IndexQueryService idxConf = (IndexQueryService)
			testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG);
			
			// this will fire the trigger
			XMLResource doc =
				(XMLResource) testCollection.createResource(DOCUMENT_NAME, "XMLResource" );
			doc.setContent(DOCUMENT_CONTENT);
			testCollection.storeResource(doc);
			
    		// remove the trigger for the Collection under test
			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);			

	        XPathQueryService service = (XPathQueryService) testCollection.getService("XPathQueryService", "1.0");
	        //TODO : understand why it is necessary !
	        service.setProperty(OutputKeys.INDENT, "no");
	        	        
	        result = service.query("/events/event[@id = 'trigger1']");
	        assertEquals(2, result.getSize());
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger1'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "']");
	        assertEquals(2, result.getSize());	        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger1'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + DOCUMENT_NAME + "']");
	        assertEquals(2, result.getSize());	        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger1'][event = 'CREATE-DOCUMENT']");
	        assertEquals(2, result.getSize());	        
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger1']/document/test");
	        assertEquals(1, result.getSize());	        	        
	        assertXMLEqual(DOCUMENT_CONTENT, ((XMLResource)result.getResource(0)).getContent().toString());
	        
    	} catch (Exception e) {
            e.printStackTrace();
    		fail(e.getMessage());    		
    	}
			
    }

    /** test a trigger fired by a Document Update */
    @Test
    public void updateDocument() {
    	
    	ResourceSet result;
    	
    	try {
			IndexQueryService idxConf = (IndexQueryService)
			testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG);
	       
			//TODO : trigger UPDATE events !
	        XUpdateQueryService update = (XUpdateQueryService) testCollection.getService("XUpdateQueryService", "1.0");
	        update.updateResource(DOCUMENT_NAME, DOCUMENT_UPDATE);
	        
	        idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);

	        XPathQueryService service = (XPathQueryService) testCollection
    		.getService("XPathQueryService", "1.0");	        
	        // this is necessary to compare with MODIFIED_DOCUMENT_CONTENT ; TODO better compare with XML diff tool
	        service.setProperty(OutputKeys.INDENT, "no");

	        result = service.query("/events/event[@id = 'trigger2']");
	        assertEquals(2, result.getSize());
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger2'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "']");
	        assertEquals(2, result.getSize());	        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger2'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + DOCUMENT_NAME + "']");
	        assertEquals(2, result.getSize());	        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger2'][event = 'UPDATE-DOCUMENT']");
	        assertEquals(2, result.getSize());	        
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger2']/document/test");
	        assertEquals(2, result.getSize());	        
	        assertXMLEqual(DOCUMENT_CONTENT, result.getResource(0).getContent().toString());	        
	        assertXMLEqual(MODIFIED_DOCUMENT_CONTENT, result.getResource(1).getContent().toString());

    	} catch (Exception e) {
    		fail(e.getMessage());    		
    	}	
        
    }

    /** test a trigger fired by a Document Delete */
    @Test
    public void deleteDocument() {
    	
    	ResourceSet result;
    	
    	try {
			IndexQueryService idxConf = (IndexQueryService)
			testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG);
	
			testCollection.removeResource(testCollection.getResource(DOCUMENT_NAME));

			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);
			
	        XPathQueryService service = (XPathQueryService) testCollection
	        	.getService("XPathQueryService", "1.0");

	        service.setProperty(OutputKeys.INDENT, "no");        

	        result = service.query("/events/event[@id = 'trigger3']");
	        assertEquals(2, result.getSize());
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger3'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "']");
	        assertEquals(2, result.getSize());	        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger3'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + DOCUMENT_NAME + "']");
	        assertEquals(2, result.getSize());	        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger3'][event = 'DELETE-DOCUMENT']");
	        assertEquals(2, result.getSize());	        
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger3']/document/test");
	        assertEquals(1, result.getSize());
	        assertXMLEqual(MODIFIED_DOCUMENT_CONTENT, result.getResource(0).getContent().toString());        
			
    	} catch (Exception e) {
            e.printStackTrace();
    		fail(e.getMessage());    		
    	}
    }
    
	/** test a trigger fired by storing a Binary Document  */
    @Test
    public void storeBinaryDocument() {
    	
    	ResourceSet result;
    	
    	try {    		
    		// configure the Collection with the trigger under test
			IndexQueryService idxConf = (IndexQueryService)
			testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG);
			
			// this will fire the trigger
			Resource res = testCollection.createResource(BINARY_DOCUMENT_NAME, "BinaryResource");
			Base64Decoder dec = new Base64Decoder();
			dec.translate(BINARY_DOCUMENT_CONTENT);
			res.setContent(dec.getByteArray());
			testCollection.storeResource(res);
			
    		// remove the trigger for the Collection under test
			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);			

	        XPathQueryService service = (XPathQueryService) testCollection.getService("XPathQueryService", "1.0");
	        //TODO : understand why it is necessary !
	        service.setProperty(OutputKeys.INDENT, "no");

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger1'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + BINARY_DOCUMENT_NAME + "'][event = 'CREATE-DOCUMENT']");
	        assertEquals(2, result.getSize());
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger1'][@type = 'finish'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + BINARY_DOCUMENT_NAME + "'][event = 'CREATE-DOCUMENT']/document");
	        assertEquals(1, result.getSize());
	        assertEquals("<document>" + BINARY_DOCUMENT_CONTENT + "</document>", result.getResource(0).getContent().toString());
	        
    	}
    	catch(Exception e)
    	{
            e.printStackTrace();
    		fail(e.getMessage());    		
    	}
    }

    /** test a trigger fired by a Binary Document Delete */
    @Test
    public void deleteBinaryDocument() {
    	
    	ResourceSet result;
    	
    	try {
			IndexQueryService idxConf = (IndexQueryService)
			testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG);
	
			testCollection.removeResource(testCollection.getResource(BINARY_DOCUMENT_NAME));

			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);
			
	        XPathQueryService service = (XPathQueryService) testCollection
	        	.getService("XPathQueryService", "1.0");

	        service.setProperty(OutputKeys.INDENT, "no");        

	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger3'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + BINARY_DOCUMENT_NAME + "'][event = 'DELETE-DOCUMENT']");
	        assertEquals(2, result.getSize());	        
	        
	        //TODO : consistent URI !	        
	        result = service.query("/events/event[@id = 'trigger3'][@type = 'prepare'][collection = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "'][uri = '" + DBBroker.ROOT_COLLECTION +  "/" + TEST_COLLECTION + "/" + BINARY_DOCUMENT_NAME + "'][event = 'DELETE-DOCUMENT']/document");
	        assertEquals(1, result.getSize());
	        assertEquals("<document>" + BINARY_DOCUMENT_CONTENT + "</document>", result.getResource(0).getContent().toString());        
			
    	} catch (Exception e) {
            e.printStackTrace();
    		fail(e.getMessage());    		
    	}
    }
    
    /** test a trigger fired by a Collection manipulations */
    @Test
    public void createCollection() {
		IndexQueryService idxConf;
		try {
			idxConf = (IndexQueryService) root.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG_FOR_COLLECTIONS_EVENTS);
			
            CollectionManagementService service = (CollectionManagementService) testCollection.getService("CollectionManagementService", "1.0");
            Collection collection = service.createCollection("test");
            assertNotNull(collection);

    		// remove the trigger for the Collection under test
			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);			
            
	        XPathQueryService query = (XPathQueryService) root.getService("XPathQueryService", "1.0");
	        	        
	        ResourceSet result = query.query("/events/event[@id = 'trigger4']");
	        assertEquals(2, result.getSize());

            
		} catch (XMLDBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }

    /** test a trigger fired by a Collection manipulations */
    @Test @Ignore //TODO: code
    public void renameCollection() {
		IndexQueryService idxConf;
		try {
			idxConf = (IndexQueryService) root.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG_FOR_COLLECTIONS_EVENTS);
			
            CollectionManagementService service = (CollectionManagementService) testCollection.getService("CollectionManagementService", "1.0");
            Collection collection = service.createCollection("test");
            assertNotNull(collection);

    		// remove the trigger for the Collection under test
			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);			
            
	        XPathQueryService query = (XPathQueryService) root.getService("XPathQueryService", "1.0");
	        	        
	        ResourceSet result = query.query("/events/event[@id = 'trigger4']");
	        assertEquals(2, result.getSize());

            
		} catch (XMLDBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
    /** test a trigger fired by a Collection manipulations */
    @Test
    public void removeCollection() {
		IndexQueryService idxConf;
		try {
			idxConf = (IndexQueryService) testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG_FOR_COLLECTIONS_EVENTS);
			
            CollectionManagementService service = (CollectionManagementService) testCollection.getService("CollectionManagementService", "1.0");
            Collection collection = service.createCollection("test");
            assertNotNull(collection);

            service.removeCollection("test");

    		// remove the trigger for the Collection under test
			idxConf.configureCollection(EMPTY_COLLECTION_CONFIG);			
            
	        XPathQueryService query = (XPathQueryService) root.getService("XPathQueryService", "1.0");
	        	        
	        ResourceSet result = query.query("/events/event[@id = 'trigger6']");
	        assertEquals(2, result.getSize());

            
		} catch (XMLDBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }


    @Test
    public void storeDocument_invalidTriggerForPrepare()
    {
        //replace the valid trigger with the invalid trigger
        try
        {
            BinaryResource invalidModule = (BinaryResource) testCollection.createResource(MODULE_NAME, "BinaryResource" );
            ((EXistResource)invalidModule).setMimeType("application/xquery");
            invalidModule.setContent(INVALID_MODULE.getBytes());
            testCollection.storeResource(invalidModule);

            // configure the Collection with the trigger under test
            IndexQueryService idxConf = null;

            idxConf = (IndexQueryService)testCollection.getService("IndexQueryService", "1.0");
			idxConf.configureCollection(COLLECTION_CONFIG);
        }
        catch(XMLDBException xdbe)
        {
            fail(xdbe.getMessage());
        }

        
        final int max_store_attempts = 10;
        int count_prepare_exceptions = 0;
        for(int i = 0; i < max_store_attempts; i++)
        {
            try
            {
                // this will fire the trigger
                XMLResource doc = (XMLResource) testCollection.createResource(DOCUMENT_NAME, "XMLResource");
                doc.setContent(DOCUMENT_CONTENT);
                testCollection.storeResource(doc);
            }
            catch(XMLDBException xdbe)
            {
               if(xdbe.getCause() instanceof TriggerException)
               {
                   if(xdbe.getCause().getMessage().equals(XQueryTrigger.PEPARE_EXCEIPTION_MESSAGE))
                   {
                        count_prepare_exceptions++;
                   }
               }
            }
        }

        assertEquals(max_store_attempts, count_prepare_exceptions);
    }
}