/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
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
package org.exist.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.exist.Indexer;
import org.exist.cluster.ClusterComunication;
import org.exist.cluster.journal.JournalManager;
import org.exist.indexing.IndexManager;
import org.exist.memtree.SAXAdapter;
import org.exist.protocolhandler.eXistURLStreamHandlerFactory;
import org.exist.scheduler.Scheduler;
import org.exist.security.User;
import org.exist.security.XMLSecurityManager;
import org.exist.security.xacml.XACMLConstants;
import org.exist.storage.BrokerFactory;
import org.exist.storage.BrokerPool;
import org.exist.storage.CollectionCacheManager;
import org.exist.storage.DBBroker;
import org.exist.storage.DefaultCacheManager;
import org.exist.storage.IndexSpec;
import org.exist.storage.NativeBroker;
import org.exist.storage.NativeValueIndex;
import org.exist.storage.TextSearchEngine;
import org.exist.storage.XQueryPool;
import org.exist.storage.journal.Journal;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionManager;
import org.exist.validation.GrammarPool;
import org.exist.validation.resolver.eXistXMLCatalogResolver;
import org.exist.xquery.FunctionFactory;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.XQueryWatchDog;
import org.exist.xslt.TransformerFactoryAllocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class Configuration implements ErrorHandler {
    private final static Logger LOG = Logger.getLogger(Configuration.class); //Logger
    protected String configFilePath = null;
    protected File existHome = null;
    
    protected DocumentBuilder builder = null;
    protected HashMap config = new HashMap(); //Configuration
    
    //TODO : extract this
    public static final class SystemTaskConfig {
    	
    	public static final String CONFIGURATION_ELEMENT_NAME = "system-task";
        protected String className;
        protected long period = -1;
        protected String cronExpr = null;
        protected Properties params = new Properties();
        
        public SystemTaskConfig(String className) {
            this.className = className;
        }
        
        public String getClassName() {
            return className;
        }
        
        public void setPeriod(long period) {
            this.period = period;
        }
        
        public long getPeriod() {
            return period;
        }
        
        public String getCronExpr() {
            return cronExpr;
        }
        
        public void setCronExpr(String cronExpr) {
            this.cronExpr = cronExpr;
        }
        
        public Properties getProperties() {
            return params;
        }
    }
    
    public static final class IndexModuleConfig {
        protected String id;
        protected String className;
        protected Element config;
        
        public IndexModuleConfig(String id, String className, Element config) {
            this.id = id;
            this.className = className;
            this.config = config;
        }
        
        public String getClassName() {
            return className;
        }
        
        public Element getConfig() {
            return config;
        }
        
        public String getId() {
            return id;
        }
    }
    
    public Configuration() throws DatabaseConfigurationException {
        this("conf.xml", null);
    }
    
    public Configuration(String configFilename) throws DatabaseConfigurationException {
        this(configFilename, null);
    }
    
    public Configuration(String configFilename, String existHomeDirname) throws DatabaseConfigurationException {
        try {
            InputStream is = null;
            
            if (configFilename == null) {
                // Default file name
                configFilename = "conf.xml";
            }
            
            // firstly, try to read the configuration from a file within the
            // classpath
            try {
                is = Configuration.class.getClassLoader().getResourceAsStream(configFilename);
                if (is != null) LOG.info("Reading configuration from classloader");
            } catch (Exception e) {
                // EB: ignore and go forward, e.g. in case there is an absolute
                // file name for configFileName
                LOG.debug(e);
            }
            
            // otherwise, secondly try to read configuration from file. Guess the
            // location if necessary
            if (is == null) {
                existHome = (existHomeDirname != null) ? new File(existHomeDirname) : ConfigurationHelper.getExistHome();
                if (existHome == null) {
                    // EB: try to create existHome based on location of config file
                    // when config file points to absolute file location
                    File absoluteConfigFile = new File(configFilename);
                    if (absoluteConfigFile.isAbsolute() &&
                        absoluteConfigFile.exists() && absoluteConfigFile.canRead()) {
                        existHome = absoluteConfigFile.getParentFile();
                        configFilename = absoluteConfigFile.getName();
                    }
                }
                File configFile = new File(configFilename);
                if (!configFile.isAbsolute() && existHome != null)
                    // try the passed or constructed existHome first
                    configFile = new File(existHome, configFilename);
                if (configFile == null)
                    configFile = ConfigurationHelper.lookup(configFilename);
                if (!configFile.exists() || !configFile.canRead())
                    throw new DatabaseConfigurationException("Unable to read configuration file at " + config);
                configFilePath = configFile.getAbsolutePath();
                is = new FileInputStream(configFile);
                // set dbHome to parent of the conf file found, to resolve relative
                // path from conf file
                existHomeDirname = configFile.getParentFile().getCanonicalPath();
                LOG.info("Reading configuration from file " + configFile);
            }
            
            
            // initialize xml parser
            // we use eXist's in-memory DOM implementation to work
            // around a bug in Xerces
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
//            factory.setFeature("http://apache.org/xml/features/validation/schema", true);
//            factory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            InputSource src = new InputSource(is);
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            SAXAdapter adapter = new SAXAdapter();
            reader.setContentHandler(adapter);
            reader.parse(src);
            
            Document doc = adapter.getDocument();
            
            //indexer settings
            NodeList indexers = doc.getElementsByTagName(Indexer.CONFIGURATION_ELEMENT_NAME);
            if (indexers.getLength() > 0) {
                configureIndexer(existHomeDirname, doc, (Element) indexers.item(0));
            }
            
            //scheduler settings
            NodeList schedulers = doc.getElementsByTagName(Scheduler.CONFIGURATION_ELEMENT_NAME);
            if(schedulers.getLength() > 0) {
                configureScheduler((Element) schedulers.item(0));
            }
            
            //db connection settings
            NodeList dbcon = doc.getElementsByTagName(BrokerPool.CONFIGURATION_CONNECTION_ELEMENT_NAME);
            if (dbcon.getLength() > 0) {
                configureBackend(existHomeDirname, (Element) dbcon.item(0));
            }
            
            //transformer settings
            NodeList transformers = doc.getElementsByTagName(TransformerFactoryAllocator.CONFIGURATION_ELEMENT_NAME);
            if(transformers.getLength() > 0) {
                configureTransformer((Element) transformers.item(0));
            }
            
            //serializer settings
            NodeList serializers = doc.getElementsByTagName(Serializer.CONFIGURATION_ELEMENT_NAME);
            if (serializers.getLength() > 0) {
                configureSerializer((Element) serializers.item(0));
            }
            
            //XUpdate settings
            NodeList xupdates = doc.getElementsByTagName(DBBroker.CONFIGURATION_ELEMENT_NAME);
            if (xupdates.getLength() > 0) {
                configureXUpdate((Element) xupdates.item(0));
            }
            
            //XQuery settings
            NodeList xquery = doc.getElementsByTagName(XQueryContext.CONFIGURATION_ELEMENT_NAME);
            if (xquery.getLength() > 0) {
                configureXQuery((Element) xquery.item(0));
            }
            
            //XACML settings
            NodeList xacml = doc.getElementsByTagName(XACMLConstants.CONFIGURATION_ELEMENT_NAME);
            if (xacml.getLength() > 0) {
                configureXACML((Element)xacml.item(0));
            }
            
            //Cluster configuration
            NodeList clusters = doc.getElementsByTagName(ClusterComunication.CONFIGURATION_ELEMENT_NAME);
            if(clusters.getLength() > 0) {
                configureCluster((Element)clusters.item(0));
            }
            
            //Validation
            NodeList validations = doc.getElementsByTagName(XMLReaderObjectFactory.CONFIGURATION_ELEMENT_NAME);
            if (validations.getLength() > 0) {
                configureValidation(existHomeDirname, doc, (Element) validations.item(0));
            }
            
        } catch (SAXException e) {
            LOG.warn("error while reading config file: " + configFilename, e);
            throw new DatabaseConfigurationException(e.getMessage());
        } catch (ParserConfigurationException cfg) {
            LOG.warn("error while reading config file: " + configFilename, cfg);
            throw new DatabaseConfigurationException(cfg.getMessage());
        } catch (IOException io) {
            LOG.warn("error while reading config file: " + configFilename, io);
            throw new DatabaseConfigurationException(io.getMessage());
        }
    }
    
    private void configureCluster(Element cluster) {
        String protocol = cluster.getAttribute("protocol");
        if(protocol != null) {
            config.put(ClusterComunication.PROPERTY_CLUSTER_PROTOCOL, protocol);
            LOG.debug(ClusterComunication.PROPERTY_CLUSTER_PROTOCOL + ": " + config.get(ClusterComunication.PROPERTY_CLUSTER_PROTOCOL));
        }
        String user = cluster.getAttribute("dbaUser");
        if(user != null) {
            config.put(ClusterComunication.PROPERTY_CLUSTER_USER, user);
            LOG.debug(ClusterComunication.PROPERTY_CLUSTER_USER + ": " + config.get(ClusterComunication.PROPERTY_CLUSTER_USER));
        }
        String pwd = cluster.getAttribute("dbaPassword");
        if(pwd != null) {
            config.put(ClusterComunication.PROPERTY_CLUSTER_PWD, pwd);
            LOG.debug(ClusterComunication.PROPERTY_CLUSTER_PWD + ": " + config.get(ClusterComunication.PROPERTY_CLUSTER_PWD));
        }
        String dir = cluster.getAttribute("journalDir");
        if(dir != null) {
            config.put(JournalManager.PROPERTY_JOURNAL_DIR, dir);
            LOG.debug(JournalManager.PROPERTY_JOURNAL_DIR + ": " + config.get(JournalManager.PROPERTY_JOURNAL_DIR));
        }
        String excludedColl = cluster.getAttribute("exclude");
        ArrayList list = new ArrayList();
        if(excludedColl != null) {
            String[] excl = excludedColl.split(",");
            for(int i=0;i<excl.length;i++){
                list.add(excl[i]);
                
            }
        }
        if(!list.contains(NativeBroker.TEMP_COLLECTION))
            list.add(NativeBroker.TEMP_COLLECTION);
        config.put(ClusterComunication.PROPERTY_CLUSTER_EXCLUDE, list);
        LOG.debug(ClusterComunication.PROPERTY_CLUSTER_EXCLUDE + ": " + config.get(ClusterComunication.PROPERTY_CLUSTER_EXCLUDE));
        
        /*Cluster parameters for test*/
        String maxStore = cluster.getAttribute("journalMaxItem");
        if(maxStore == null || maxStore.trim().length()==0) {
            maxStore = "65000";
        }
        config.put(JournalManager.PROPERTY_CLUSTER_JOURNAL_MAXSTORE, Integer.valueOf(maxStore));
        LOG.debug(JournalManager.PROPERTY_CLUSTER_JOURNAL_MAXSTORE + ": " + config.get(JournalManager.PROPERTY_CLUSTER_JOURNAL_MAXSTORE));
        
        String shift = cluster.getAttribute("journalIndexShift");
        if(shift == null || shift.trim().length()==0 ) {
            shift = "100";
        }
        config.put(JournalManager.PROPERTY_CLUSTER_JOURNAL_SHIFT, Integer.valueOf(shift));
        LOG.debug(JournalManager.PROPERTY_CLUSTER_JOURNAL_SHIFT + ": " + config.get(JournalManager.PROPERTY_CLUSTER_JOURNAL_SHIFT));
    }
    
    private void configureXQuery(Element xquery) throws DatabaseConfigurationException {
        
        //java binding
        String javabinding = xquery.getAttribute("enable-java-binding");
        if(javabinding != null) {
            config.put(FunctionFactory.PROPERTY_ENABLE_JAVA_BINDING, javabinding);
            LOG.debug(FunctionFactory.PROPERTY_ENABLE_JAVA_BINDING + ": " + config.get(FunctionFactory.PROPERTY_ENABLE_JAVA_BINDING));
        }
        
        String optimize = xquery.getAttribute("enable-query-rewriting");
        if (optimize != null && optimize.length() > 0) {
            config.put(XQueryContext.PROPERTY_ENABLE_QUERY_REWRITING, optimize);
            LOG.debug(XQueryContext.PROPERTY_ENABLE_QUERY_REWRITING + ": " + config.get(XQueryContext.PROPERTY_ENABLE_QUERY_REWRITING));
        }
        
        String backwardCompatible = xquery.getAttribute("backwardCompatible");
        if (backwardCompatible != null && backwardCompatible.length() > 0) {
            config.put(XQueryContext.PROPERTY_XQUERY_BACKWARD_COMPATIBLE, backwardCompatible);
            LOG.debug(XQueryContext.PROPERTY_XQUERY_BACKWARD_COMPATIBLE + ": " + config.get(XQueryContext.PROPERTY_XQUERY_BACKWARD_COMPATIBLE));
        }
        
        //built-in-modules
        NodeList builtins = xquery.getElementsByTagName(XQueryContext.CONFIGURATION_MODULES_ELEMENT_NAME);
        if (builtins.getLength() > 0) {
            Element elem = (Element) builtins.item(0);
            NodeList modules = elem.getElementsByTagName(XQueryContext.CONFIGURATION_MODULE_ELEMENT_NAME);
            String moduleList[][] = new String[modules.getLength()][2];
            for (int i = 0; i < modules.getLength(); i++) {
                elem = (Element) modules.item(i);
                String uri = elem.getAttribute("uri");
                String clazz = elem.getAttribute("class");
                if (uri == null)
                    throw new DatabaseConfigurationException("element 'module' requires an attribute 'uri'");
                if (clazz == null)
                    throw new DatabaseConfigurationException("element 'module' requires an attribute 'class'");
                moduleList[i][0] = uri;
                moduleList[i][1] = clazz;
                LOG.debug("Configured module '" + uri + "' implemented in '" + clazz + "'");
            }
            config.put(XQueryContext.PROPERTY_BUILT_IN_MODULES, moduleList);
        }
    }
    
    private void configureXACML(Element xacml) {
        String enable = xacml.getAttribute(XACMLConstants.ENABLE_XACML_ATTRIBUTE);
        config.put(XACMLConstants.ENABLE_XACML_PROPERTY, parseBoolean(enable, false));
        LOG.debug(XACMLConstants.ENABLE_XACML_PROPERTY + ": " + config.get(XACMLConstants.ENABLE_XACML_PROPERTY));
        
        String loadDefaults = xacml.getAttribute(XACMLConstants.LOAD_DEFAULT_POLICIES_ATTRIBUTE);
        config.put(XACMLConstants.LOAD_DEFAULT_POLICIES_PROPERTY, parseBoolean(loadDefaults, true));
        LOG.debug(XACMLConstants.LOAD_DEFAULT_POLICIES_PROPERTY + ": " + config.get(XACMLConstants.LOAD_DEFAULT_POLICIES_PROPERTY));
    }
    
    /**
     * @param xupdates
     * @throws NumberFormatException
     */
    private void configureXUpdate(Element xupdate) throws NumberFormatException {
        String growth = xupdate.getAttribute(DBBroker.XUPDATE_GROWTH_FACTOR_ATTRIBUTE);
        if (growth != null) {
            config.put(DBBroker.PROPERTY_XUPDATE_GROWTH_FACTOR, new Integer(growth));
            LOG.debug(DBBroker.PROPERTY_XUPDATE_GROWTH_FACTOR + ": "
                + config.get(DBBroker.PROPERTY_XUPDATE_GROWTH_FACTOR));
        }
        
        String fragmentation = xupdate.getAttribute(DBBroker.XUPDATE_FRAGMENTATION_FACTOR_ATTRIBUTE);
        if (fragmentation != null) {
            config.put(DBBroker.PROPERTY_XUPDATE_FRAGMENTATION_FACTOR, new Integer(fragmentation));
            LOG.debug(DBBroker.PROPERTY_XUPDATE_FRAGMENTATION_FACTOR + ": "
                + config.get(DBBroker.PROPERTY_XUPDATE_FRAGMENTATION_FACTOR));
        }
        
        String consistencyCheck = xupdate.getAttribute(DBBroker.XUPDATE_CONSISTENCY_CHECKS_ATTRIBUTE);
        if (consistencyCheck != null) {
            config.put(DBBroker.PROPERTY_XUPDATE_CONSISTENCY_CHECKS, Boolean.valueOf(consistencyCheck.equals("yes")));
            LOG.debug(DBBroker.PROPERTY_XUPDATE_CONSISTENCY_CHECKS + ": "
                + config.get(DBBroker.PROPERTY_XUPDATE_CONSISTENCY_CHECKS));
        }
    }
    
    private void configureTransformer(Element transformer) {
        String className = transformer.getAttribute(TransformerFactoryAllocator.TRANSFORMER_CLASS_ATTRIBUTE);
        if (className != null) {
            config.put(TransformerFactoryAllocator.PROPERTY_TRANSFORMER_CLASS, className);
            LOG.debug(TransformerFactoryAllocator.PROPERTY_TRANSFORMER_CLASS + ": " + 
            		config.get(TransformerFactoryAllocator.PROPERTY_TRANSFORMER_CLASS));
        }
    }
    
    /**
     * @param serializers
     */
    private void configureSerializer(Element serializer) {
        String xinclude = serializer.getAttribute(Serializer.ENABLE_XINCLUDE_ATTRIBUTE);
        if (xinclude != null) {
            config.put(Serializer.PROPERTY_ENABLE_XINCLUDE, xinclude);
            LOG.debug(Serializer.PROPERTY_ENABLE_XINCLUDE + ": " + config.get(Serializer.PROPERTY_ENABLE_XINCLUDE));
        }
        
        String xsl = serializer.getAttribute(Serializer.ENABLE_XSL_ATTRIBUTE);
        if (xsl != null) {
            config.put(Serializer.PROPERTY_ENABLE_XSL, xsl);
            LOG.debug(Serializer.PROPERTY_ENABLE_XSL + ": " + config.get(Serializer.PROPERTY_ENABLE_XSL));
        }
        
        String indent = serializer.getAttribute(Serializer.INDENT_ATTRIBUTE);
        if (indent != null) {
            config.put(Serializer.PROPERTY_INDENT, indent);
            LOG.debug(Serializer.PROPERTY_INDENT + ": " + config.get(Serializer.PROPERTY_INDENT));
        }
        
        String compress = serializer.getAttribute(Serializer.COMPRESS_OUTPUT_ATTRIBUTE);
        if (compress != null) {
            config.put(Serializer.PROPERTY_COMPRESS_OUTPUT, compress);
	        LOG.debug(Serializer.PROPERTY_COMPRESS_OUTPUT + ": " + config.get(Serializer.PROPERTY_COMPRESS_OUTPUT));
	    }
        
        String internalId = serializer.getAttribute(Serializer.ADD_EXIST_ID_ATTRIBUTE);
        if (internalId != null) {
            config.put(Serializer.PROPERTY_ADD_EXIST_ID, internalId);
            LOG.debug(Serializer.PROPERTY_ADD_EXIST_ID + ": " + config.get(Serializer.PROPERTY_ADD_EXIST_ID));
        }
        
        String tagElementMatches = serializer.getAttribute(Serializer.TAG_MATCHING_ELEMENTS_ATTRIBUTE);
        if (tagElementMatches != null) {
            config.put(Serializer.PROPERTY_TAG_MATCHING_ELEMENTS, tagElementMatches);
            LOG.debug(Serializer.PROPERTY_TAG_MATCHING_ELEMENTS + ": " + config.get(Serializer.PROPERTY_TAG_MATCHING_ELEMENTS));
        }
        
        String tagAttributeMatches = serializer.getAttribute(Serializer.TAG_MATCHING_ATTRIBUTES_ATTRIBUTE);
        if (tagAttributeMatches != null) {
            config.put(Serializer.PROPERTY_TAG_MATCHING_ATTRIBUTES, tagAttributeMatches);
            LOG.debug(Serializer.PROPERTY_TAG_MATCHING_ATTRIBUTES + ": " + config.get(Serializer.PROPERTY_TAG_MATCHING_ATTRIBUTES));
        }
    }
    
    /**
     * Reads the scheduler configuration
     */
    private void configureScheduler(Element scheduler) {
        NodeList nlJobs = scheduler.getElementsByTagName(Scheduler.CONFIGURATION_JOB_ELEMENT_NAME);
        
        if(nlJobs == null)
            return;
        
        String jobList[][] = new String[nlJobs.getLength()][2];
        String jobResource = null;
        String jobSchedule = null;
        
        for(int i = 0; i < nlJobs.getLength(); i++) {
            Element job = (Element)nlJobs.item(i);
            
            //get the job resource
            jobResource = job.getAttribute("class");
            if(jobResource == null)
                jobResource = job.getAttribute("xquery");
            
            //get the job schedule
            jobSchedule = job.getAttribute("cron-trigger");
            if(jobSchedule == null)
                jobSchedule = job.getAttribute("period");
            
            //check we have both a resource and a schedule
            if(jobResource == null | jobSchedule == null)
                return;
            
            jobList[i][0] = jobResource;
            jobList[i][1] = jobSchedule;
            LOG.debug("Configured scheduled job '" + jobResource + "' with trigger '" + jobSchedule + "'");
        }
        config.put(Scheduler.PROPERTY_SCHEDULER_JOBS, jobList);
    }
    
    /**
     * @param dbHome
     * @param dbcon
     * @throws DatabaseConfigurationException
     */
    private void configureBackend(String dbHome, Element con) throws DatabaseConfigurationException {
        String mysql = con.getAttribute(BrokerFactory.PROPERTY_DATABASE);
        if (mysql != null) {
            config.put(BrokerFactory.PROPERTY_DATABASE, mysql);
            LOG.debug(BrokerFactory.PROPERTY_DATABASE + ": " + config.get(BrokerFactory.PROPERTY_DATABASE));
        }
        
        // directory for database files
        String dataFiles = con.getAttribute(BrokerPool.DATA_DIR_ATTRIBUTE);
        if (dataFiles != null) {
            File df = ConfigurationHelper.lookup(dataFiles, dbHome);
            if (!df.canRead())
                throw new DatabaseConfigurationException(
                    "cannot read data directory: "
                    + df.getAbsolutePath());
            config.put(BrokerPool.PROPERTY_DATA_DIR, df.getAbsolutePath());
            LOG.debug(BrokerPool.PROPERTY_DATA_DIR + ": " + config.get(BrokerPool.PROPERTY_DATA_DIR));
        }
        
        String cacheMem = con.getAttribute(DefaultCacheManager.CACHE_SIZE_ATTRIBUTE);
        if (cacheMem != null) {
            if (cacheMem.endsWith("M") || cacheMem.endsWith("m"))
                cacheMem = cacheMem.substring(0, cacheMem.length() - 1);
            try {
                config.put(DefaultCacheManager.PROPERTY_CACHE_SIZE, new Integer(cacheMem));
                LOG.debug(DefaultCacheManager.PROPERTY_CACHE_SIZE + ": " + config.get(DefaultCacheManager.PROPERTY_CACHE_SIZE) + "m");
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        String collectionCache = con.getAttribute(CollectionCacheManager.CACHE_SIZE_ATTRIBUTE);
        if (collectionCache != null) {
            if (collectionCache.endsWith("M") || collectionCache.endsWith("m"))
                collectionCache = collectionCache.substring(0, collectionCache.length() - 1);
            try {
                config.put(CollectionCacheManager.PROPERTY_CACHE_SIZE, new Integer(collectionCache));
                LOG.debug(CollectionCacheManager.PROPERTY_CACHE_SIZE + ": " + config.get(CollectionCacheManager.PROPERTY_CACHE_SIZE) + "m");
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        String pageSize = con.getAttribute(NativeBroker.PAGE_SIZE_ATTRIBUTE);
        if (pageSize != null) {
            try {
                config.put(NativeBroker.PROPERTY_PAGE_SIZE, new Integer(pageSize));
                LOG.debug(NativeBroker.PROPERTY_PAGE_SIZE + ": " + config.get(NativeBroker.PROPERTY_PAGE_SIZE));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        String freeMem = con.getAttribute(NativeBroker.MIN_FREE_MEMORY_ATTRIBUTE);
        if (freeMem != null) {
            try {
                config.put(NativeBroker.PROPERTY_MIN_FREE_MEMORY, new Integer(freeMem));
                LOG.debug(NativeBroker.PROPERTY_MIN_FREE_MEMORY + ": " + config.get(NativeBroker.PROPERTY_MIN_FREE_MEMORY));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        //Not clear : rather looks like a buffers count
        String collCacheSize = con.getAttribute(BrokerPool.COLLECTION_CACHE_SIZE_ATTRIBUTE);
        if (collCacheSize != null) {
            try {
                config.put(BrokerPool.PROPERTY_COLLECTION_CACHE_SIZE, new Integer(collCacheSize));
                LOG.debug(BrokerPool.PROPERTY_COLLECTION_CACHE_SIZE + ": " + config.get(BrokerPool.PROPERTY_COLLECTION_CACHE_SIZE));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        //Unused !
        String buffers = con.getAttribute("buffers");
        if (buffers != null) {
            try {
                config.put("db-connection.buffers", new Integer(buffers));
                LOG.debug("db-connection.buffers: " + config.get("db-connection.buffers"));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        //Unused !
        String collBuffers = con.getAttribute("collection_buffers");
        if (collBuffers != null) {
            try {
                config.put("db-connection.collections.buffers", new Integer(collBuffers));
                LOG.debug("db-connection.collections.buffers: " + config.get("db-connection.collections.buffers"));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        
        //Unused !
        String wordBuffers = con.getAttribute("words_buffers");
        if (wordBuffers != null)
            try {
                config.put("db-connection.words.buffers", new Integer(wordBuffers));
                LOG.debug("db-connection.words.buffers: " + config.get("db-connection.words.buffers"));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        
        //Unused !
        String elementBuffers = con.getAttribute("elements_buffers");
        if (elementBuffers != null) {
            try {
                config.put("db-connection.elements.buffers", new Integer(elementBuffers));
                LOG.debug("db-connection.elements.buffers: " + config.get("db-connection.elements.buffers"));
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }

        NodeList securityConf = con.getElementsByTagName(BrokerPool.CONFIGURATION_SECURITY_ELEMENT_NAME);
        String securityManagerClassName = BrokerPool.DEFAULT_SECURITY_CLASS;
        if (securityConf.getLength()>0) {
            Element security = (Element)securityConf.item(0);
            securityManagerClassName = security.getAttribute("class");
            String encoding = security.getAttribute("password-encoding");
            //Unused
            config.put("db-connection.security.password-encoding",encoding);
            if (encoding!=null) {
                LOG.info("db-connection.security.password-encoding: " + config.get("db-connection.security.password-encoding"));
                User.setPasswordEncoding(encoding);
            } else {
                LOG.info("No password encoding set, defaulting.");
            }
            String realm = security.getAttribute("password-realm");
            //Unused
            config.put("db-connection.security.password-realm",realm);
            if (realm!=null) {
                LOG.info("db-connection.security.password-realm: " + config.get("db-connection.security.password-realm"));
                User.setPasswordRealm(realm);
            } else {
                LOG.info("No password realm set, defaulting.");
            }
        }
        
        try {
            config.put(BrokerPool.PROPERTY_SECURITY_CLASS, Class.forName(securityManagerClassName));
            LOG.debug(BrokerPool.PROPERTY_SECURITY_CLASS + ": " + config.get(BrokerPool.PROPERTY_SECURITY_CLASS));
        } catch (Throwable ex) {
            if (ex instanceof ClassNotFoundException) {
                throw new DatabaseConfigurationException("Cannot find security manager class "+securityManagerClassName);
            } else {
                throw new DatabaseConfigurationException("Cannot load security manager class "+securityManagerClassName+" due to "+ex.getMessage());
            }
        }
        
        NodeList poolConf = con.getElementsByTagName(BrokerPool.CONFIGURATION_POOL_ELEMENT_NAME);
        if (poolConf.getLength() > 0) {
            configurePool((Element) poolConf.item(0));
        }
        NodeList queryPoolConf = con.getElementsByTagName(XQueryPool.CONFIGURATION_ELEMENT_NAME);
        if (queryPoolConf.getLength() > 0) {
            configureXQueryPool((Element) queryPoolConf.item(0));
        }
        NodeList watchConf = con.getElementsByTagName(XQueryWatchDog.CONFIGURATION_ELEMENT_NAME);
        if (watchConf.getLength() > 0) {
            configureWatchdog((Element) watchConf.item(0));
        }
        NodeList sysTasks = con.getElementsByTagName(SystemTaskConfig.CONFIGURATION_ELEMENT_NAME);
        if (sysTasks.getLength() > 0) {
            configureSystemTasks(sysTasks);
        }
        NodeList recoveries = con.getElementsByTagName(BrokerPool.CONFIGURATION_RECOVERY_ELEMENT_NAME);
        if (recoveries.getLength() > 0) {
            configureRecovery((Element)recoveries.item(0));
        }
        NodeList defaultPermissions = con.getElementsByTagName("default-permissions"); 
        if (defaultPermissions.getLength() > 0) {
            configurePermissions((Element)defaultPermissions.item(0));
        }
    }
    
    private void configureRecovery(Element recovery) throws DatabaseConfigurationException {
        String option = recovery.getAttribute(BrokerPool.RECOVERY_ENABLED_ATTRIBUTE);
        boolean value = true;
        if (option != null) {
            value = option.equals("yes");
        }
        setProperty(BrokerPool.PROPERTY_RECOVERY_ENABLED, new Boolean(value));
        LOG.debug(BrokerPool.PROPERTY_RECOVERY_ENABLED + ": " + config.get(BrokerPool.PROPERTY_RECOVERY_ENABLED));
        
        option = recovery.getAttribute(Journal.RECOVERY_SYNC_ON_COMMIT_ATTRIBUTE);
        value = true;
        if (option != null) {
            value = option.equals("yes");
        }
        setProperty(Journal.PROPERTY_RECOVERY_SYNC_ON_COMMIT, new Boolean(value));
        LOG.debug(Journal.PROPERTY_RECOVERY_SYNC_ON_COMMIT + ": " + config.get(Journal.PROPERTY_RECOVERY_SYNC_ON_COMMIT));
        
        option = recovery.getAttribute(TransactionManager.RECOVERY_GROUP_COMMIT_ATTRIBUTE);
        value = false;
        if (option != null) {
            value = option.equals("yes");
        }
        setProperty(TransactionManager.PROPERTY_RECOVERY_GROUP_COMMIT, new Boolean(value));
        LOG.debug(TransactionManager.PROPERTY_RECOVERY_GROUP_COMMIT + ": " + config.get(TransactionManager.PROPERTY_RECOVERY_GROUP_COMMIT));
        
        option = recovery.getAttribute(Journal.RECOVERY_JOURNAL_DIR_ATTRIBUTE);
        if (option != null) {
            setProperty(Journal.PROPERTY_RECOVERY_JOURNAL_DIR, option);
            LOG.debug(Journal.PROPERTY_RECOVERY_JOURNAL_DIR + ": " + config.get(Journal.PROPERTY_RECOVERY_JOURNAL_DIR));
        }
        
        option = recovery.getAttribute(Journal.RECOVERY_SIZE_LIMIT_ATTRIBUTE);
        if (option != null) {
            if (option.endsWith("M") || option.endsWith("m"))
                option = option.substring(0, option.length() - 1);
            try {
                Integer size = new Integer(option);
                setProperty(Journal.PROPERTY_RECOVERY_SIZE_LIMIT, size);
                LOG.debug(Journal.PROPERTY_RECOVERY_SIZE_LIMIT + ": " + config.get(Journal.PROPERTY_RECOVERY_SIZE_LIMIT) + "m");
            } catch (NumberFormatException e) {
                throw new DatabaseConfigurationException("size attribute in recovery section needs to be a number");
            }
        }
    }
    
    private void configurePermissions(Element defautPermission) throws DatabaseConfigurationException {
        String option = defautPermission.getAttribute("collection");
        if (option != null && option.length() > 0) {
            try {
                Integer perms = new Integer(Integer.parseInt(option, 8));
                setProperty(XMLSecurityManager.PROPERTY_PERMISSIONS_COLLECTIONS, perms);
                LOG.debug(XMLSecurityManager.PROPERTY_PERMISSIONS_COLLECTIONS + ": " + config.get(XMLSecurityManager.PROPERTY_PERMISSIONS_COLLECTIONS));
            } catch (NumberFormatException e) {
                throw new DatabaseConfigurationException("collection attribute in default-permissions section needs " +
                    "to be an octal number");
            }
        }
        option = defautPermission.getAttribute("resource");
        if (option != null && option.length() > 0) {
            try {
                Integer perms = new Integer(Integer.parseInt(option, 8));
                setProperty(XMLSecurityManager.PROPERTY_PERMISSIONS_RESOURCES, perms);
                LOG.debug(XMLSecurityManager.PROPERTY_PERMISSIONS_RESOURCES + ": " + config.get(XMLSecurityManager.PROPERTY_PERMISSIONS_RESOURCES));
            } catch (NumberFormatException e) {
                throw new DatabaseConfigurationException("resource attribute in default-permissions section needs " +
                    "to be an octal number");
            }
        }
    }
    
    /**
     * @param sysTasks
     */
    private void configureSystemTasks(NodeList sysTasks) throws DatabaseConfigurationException {
        SystemTaskConfig taskList[] = new SystemTaskConfig[sysTasks.getLength()];
        for (int i = 0; i < sysTasks.getLength(); i++) {
            Element taskDef = (Element) sysTasks.item(i);
            String classAttr = taskDef.getAttribute("class");
            if (classAttr == null || classAttr.length() == 0)
                throw new DatabaseConfigurationException("No class specified for system-task");
            SystemTaskConfig sysTask = new SystemTaskConfig(classAttr);
            String cronAttr = taskDef.getAttribute("cron-trigger");
            String periodAttr = taskDef.getAttribute("period");
            if (cronAttr != null && cronAttr.length() > 0) {
                sysTask.setCronExpr(cronAttr);
            } else {
                if (periodAttr == null || periodAttr.length() == 0)
                    throw new DatabaseConfigurationException("No period or cron-trigger specified for system-task");
                long period;
                try {
                    period = Long.parseLong(periodAttr);
                } catch (NumberFormatException e) {
                    throw new DatabaseConfigurationException("Attribute period is not a number: " + e.getMessage());
                }
                sysTask.setPeriod(period);
            }
            NodeList params = taskDef.getElementsByTagName("parameter");
            for (int j = 0; j < params.getLength(); j++) {
                Element param = (Element) params.item(j);
                String name = param.getAttribute("name");
                String value = param.getAttribute("value");
                if (name == null || name.length() == 0)
                    throw new DatabaseConfigurationException("No name specified for parameter");
                sysTask.params.setProperty(name, value);
            }
            taskList[i] = sysTask;
        }
        config.put(BrokerPool.PROPERTY_SYSTEM_TASK_CONFIG, taskList);
        LOG.debug(BrokerPool.PROPERTY_SYSTEM_TASK_CONFIG+ ": " + config.get(BrokerPool.PROPERTY_SYSTEM_TASK_CONFIG));
    }
    
    /**
     * @param watchConf
     */
    private void configureWatchdog(Element watchDog) {
        String timeout = watchDog.getAttribute("query-timeout");
        if (timeout != null) {
            try {
                config.put(XQueryWatchDog.PROPERTY_QUERY_TIMEOUT, new Long(timeout));
                LOG.debug(XQueryWatchDog.PROPERTY_QUERY_TIMEOUT + ": " + config.get(XQueryWatchDog.PROPERTY_QUERY_TIMEOUT));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String maxOutput = watchDog.getAttribute("output-size-limit");
        if (maxOutput != null) {
            try {
                config.put(XQueryWatchDog.PROPERTY_OUTPUT_SIZE_LIMIT, new Integer(maxOutput));
                LOG.debug(XQueryWatchDog.PROPERTY_OUTPUT_SIZE_LIMIT + ": " + config.get(XQueryWatchDog.PROPERTY_OUTPUT_SIZE_LIMIT));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
    }
    
    /**
     * @param queryPoolConf
     */
    private void configureXQueryPool(Element queryPool) {
        String maxStackSize = queryPool.getAttribute("max-stack-size");
        if (maxStackSize != null) {
            try {
                config.put(XQueryPool.PROPERTY_MAX_STACK_SIZE, new Integer(maxStackSize));
                LOG.debug(XQueryPool.PROPERTY_MAX_STACK_SIZE + ": " + config.get(XQueryPool.PROPERTY_MAX_STACK_SIZE));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String maxPoolSize = queryPool.getAttribute("size");
        if (maxPoolSize != null) {
            try {
                config.put(XQueryPool.PROPERTY_POOL_SIZE, new Integer(maxPoolSize));
                LOG.debug(XQueryPool.PROPERTY_POOL_SIZE + ": " + config.get(XQueryPool.PROPERTY_POOL_SIZE));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String timeout = queryPool.getAttribute("timeout");
        if (timeout != null) {
            try {
                config.put(XQueryPool.PROPERTY_TIMEOUT, new Long(timeout));
                LOG.debug(XQueryPool.PROPERTY_TIMEOUT + ": " + config.get(XQueryPool.PROPERTY_TIMEOUT));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String timeoutCheckInterval = queryPool.getAttribute("timeout-check-interval");
        if (timeoutCheckInterval != null) {
            try {
                config.put(XQueryPool.PROPERTY_TIMEOUT_CHECK_INTERVAL, new Long(timeoutCheckInterval));
                LOG.debug(XQueryPool.PROPERTY_TIMEOUT_CHECK_INTERVAL + ": " + config.get(XQueryPool.PROPERTY_TIMEOUT_CHECK_INTERVAL));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
    }
    
    /**
     * @param poolConf
     */
    private void configurePool(Element pool) {
        String min = pool.getAttribute("min");
        if (min != null) {
            try {
                config.put(BrokerPool.PROPERTY_MIN_CONNECTIONS, new Integer(min));
                LOG.debug(BrokerPool.PROPERTY_MIN_CONNECTIONS + ": " + config.get(BrokerPool.PROPERTY_MIN_CONNECTIONS));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String max = pool.getAttribute("max");
        if (max != null) {
            try {
                config.put(BrokerPool.PROPERTY_MAX_CONNECTIONS, new Integer(max));
                LOG.debug(BrokerPool.PROPERTY_MAX_CONNECTIONS + ": " + config.get(BrokerPool.PROPERTY_MAX_CONNECTIONS));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String sync = pool.getAttribute("sync-period");
        if (sync != null) {
            try {
                config.put(BrokerPool.PROPERTY_SYNC_PERIOD, new Long(sync));
                LOG.debug(BrokerPool.PROPERTY_SYNC_PERIOD + ": " + config.get(BrokerPool.PROPERTY_SYNC_PERIOD));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String maxShutdownWait = pool.getAttribute("wait-before-shutdown");
        if (maxShutdownWait != null) {
            try {
                config.put(BrokerPool.PROPERTY_SHUTDOWN_DELAY, new Long(maxShutdownWait));
                LOG.debug(BrokerPool.PROPERTY_SHUTDOWN_DELAY + ": " + config.get(BrokerPool.PROPERTY_SHUTDOWN_DELAY));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
    }
    
    /**
     * @param dbHome
     * @param doc
     * @param indexer
     * @throws DatabaseConfigurationException
     * @throws MalformedURLException
     * @throws IOException
     */
    private void configureIndexer(String dbHome, Document doc, Element indexer) throws DatabaseConfigurationException, MalformedURLException {
        String parseNum = indexer.getAttribute("parseNumbers");
        if (parseNum != null) {
            config.put(TextSearchEngine.PROPERTY_INDEX_NUMBERS, Boolean.valueOf(parseNum.equals("yes")));
            LOG.debug(TextSearchEngine.PROPERTY_INDEX_NUMBERS + ": " + config.get(TextSearchEngine.PROPERTY_INDEX_NUMBERS));
        }
        
        String stemming = indexer.getAttribute("stemming");
        if (stemming != null) {
            config.put(TextSearchEngine.PROPERTY_STEM, Boolean.valueOf(stemming.equals("yes")));
            LOG.debug(TextSearchEngine.PROPERTY_STEM + ": " + config.get(TextSearchEngine.PROPERTY_STEM));
        }
        
        String termFreq = indexer.getAttribute("track-term-freq");
        if (termFreq != null) {
            config.put(TextSearchEngine.PROPERTY_STORE_TERM_FREQUENCY, Boolean.valueOf(termFreq.equals("yes")));
            LOG.debug(TextSearchEngine.PROPERTY_STORE_TERM_FREQUENCY + ": " + config.get(TextSearchEngine.PROPERTY_STORE_TERM_FREQUENCY));
        }
        
        String caseSensitive = indexer.getAttribute("caseSensitive");
        if (caseSensitive != null) {
            config.put(NativeValueIndex.PROPERTY_INDEX_CASE_SENSITIVE, Boolean.valueOf(caseSensitive.equals("yes")));
            LOG.debug(NativeValueIndex.PROPERTY_INDEX_CASE_SENSITIVE + ": " + config.get(NativeValueIndex.PROPERTY_INDEX_CASE_SENSITIVE));
        }
        
        String suppressWS = indexer.getAttribute("suppress-whitespace");
        if (suppressWS != null) {
            config.put(Indexer.PROPERTY_SUPPRESS_WHITESPACE, suppressWS);
            LOG.debug(Indexer.PROPERTY_SUPPRESS_WHITESPACE + ": " + config.get(Indexer.PROPERTY_SUPPRESS_WHITESPACE));
        }
        
        String tokenizer = indexer.getAttribute("tokenizer");
        if (tokenizer != null) {
            config.put(TextSearchEngine.PROPERTY_TOKENIZER, tokenizer);
            LOG.debug(TextSearchEngine.PROPERTY_TOKENIZER + ": " + config.get(TextSearchEngine.PROPERTY_TOKENIZER));
        }
        int depth = 3;
        String indexDepth = indexer.getAttribute("index-depth");
        if (indexDepth != null) {
            try {
                depth = Integer.parseInt(indexDepth);
                if (depth < 3) {
                    LOG.warn("parameter index-depth should be >= 3 or you will experience a severe " +
                        "performance loss for node updates (XUpdate or XQuery update extensions)");
                    depth = 3;
                }
                config.put(NativeBroker.PROPERTY_INDEX_DEPTH, new Integer(depth));
                LOG.debug(NativeBroker.PROPERTY_INDEX_DEPTH + ": " + config.get(NativeBroker.PROPERTY_INDEX_DEPTH));
            } catch (NumberFormatException e) {
                LOG.warn(e);
            }
        }
        
        String suppressWSmixed = indexer.getAttribute("preserve-whitespace-mixed-content");
        if (suppressWSmixed != null) {
            config.put(Indexer.PROPERTY_PRESERVE_WS_MIXED_CONTENT, Boolean.valueOf(suppressWSmixed.equals("yes")));
            LOG.debug(Indexer.PROPERTY_PRESERVE_WS_MIXED_CONTENT + ": " + config.get(Indexer.PROPERTY_PRESERVE_WS_MIXED_CONTENT));
        }
        
        // index settings
        NodeList cl = doc.getElementsByTagName(Indexer.CONFIGURATION_INDEX_ELEMENT_NAME);
        if (cl.getLength() > 0) {
            Element elem = (Element) cl.item(0);
            IndexSpec spec = new IndexSpec(null, elem);
            config.put(Indexer.PROPERTY_INDEXER_CONFIG, spec);
            //LOG.debug(Indexer.PROPERTY_INDEXER_CONFIG + ": " + config.get(Indexer.PROPERTY_INDEXER_CONFIG));
        }
        
        // stopwords
        NodeList stopwords = indexer.getElementsByTagName(Indexer.CONFIGURATION_STOPWORDS_ELEMENT_NAME);
        if (stopwords.getLength() > 0) {
            String stopwordFile = ((Element) stopwords.item(0)).getAttribute("file");
            File sf = ConfigurationHelper.lookup(stopwordFile, dbHome);
            if (sf.canRead()) {
                config.put(TextSearchEngine.PROPERTY_STOPWORD_FILE, stopwordFile);
                LOG.debug(TextSearchEngine.PROPERTY_STOPWORD_FILE + ": " + config.get(TextSearchEngine.PROPERTY_STOPWORD_FILE));
            }
        }
        
        // index modules
        NodeList modules = indexer.getElementsByTagName(IndexManager.CONFIGURATION_ELEMENT_NAME);
        if (modules.getLength() > 0) {
            modules = ((Element) modules.item(0)).getElementsByTagName(IndexManager.CONFIGURATION_MODULE_ELEMENT_NAME);
            IndexModuleConfig modConfig[] = new IndexModuleConfig[modules.getLength()];
            for (int i = 0; i < modules.getLength(); i++) {
                Element elem = (Element) modules.item(i);
                String className = elem.getAttribute("class");
                String id = elem.getAttribute("id");
                if (className == null || className.length() == 0)
                    throw new DatabaseConfigurationException("Required attribute class is missing for module");
                if (id == null || id.length() == 0)
                    throw new DatabaseConfigurationException("Required attribute id is missing for module");
                modConfig[i] = new IndexModuleConfig(id, className, elem);
            }
            config.put(IndexManager.PROPERTY_INDEXER_MODULES, modConfig);
        }
    }
    
    private void configureValidation(String dbHome, Document doc, Element validation) 
                                        throws DatabaseConfigurationException {
        
        // Register custom protocol URL
        // TODO DWES move to different location?
        eXistURLStreamHandlerFactory.init();
        
        // Determine validation mode
        String mode = validation.getAttribute("mode");
        if (mode != null) {
            config.put(XMLReaderObjectFactory.PROPERTY_VALIDATION, mode);
            LOG.debug(XMLReaderObjectFactory.PROPERTY_VALIDATION + ": " 
                + config.get(XMLReaderObjectFactory.PROPERTY_VALIDATION));
        }
        
        
        // Extract catalogs
        LOG.debug("Creating eXist catalog resolver");
        eXistXMLCatalogResolver resolver = new eXistXMLCatalogResolver();
        
        NodeList entityResolver = validation.getElementsByTagName(XMLReaderObjectFactory.CONFIGURATION_ENTITY_RESOLVER_ELEMENT_NAME);
        if (entityResolver.getLength() > 0) {
            Element r = (Element) entityResolver.item(0);
            NodeList catalogs = r.getElementsByTagName(XMLReaderObjectFactory.CONFIGURATION_CATALOG_ELEMENT_NAME);
            
            LOG.debug("Found "+catalogs.getLength()+" catalog uri entries.");
            LOG.debug("Using dbHome="+dbHome);
            
            // Determine webapps directory. SingleInstanceConfiguration cannot
            // be used at this phase. Trick is to check wether dbHOME is
            // pointing to a WEB-INF directory, meaning inside war file)
            File webappHome=null;
            if(dbHome==null){  /// DWES Why? let's make jUnit happy
                webappHome=new File("webapp").getAbsoluteFile();
            } else if(dbHome.endsWith("WEB-INF")){
                webappHome = new File(dbHome).getParentFile().getAbsoluteFile();
            } else {
                webappHome = new File(dbHome, "webapp").getAbsoluteFile();
            }
            LOG.debug("using webappHome="+webappHome.toURI().toString());
            
            // Get and store all URIs
            List allURIs= new ArrayList();
            for (int i = 0; i < catalogs.getLength(); i++) {
                String uri = ((Element) catalogs.item(i)).getAttribute("uri");
                
                if(uri!=null){ // when uri attribute is filled in
                    
                    // Substitute string, creating an uri from a local file
                    if(uri.indexOf("${WEBAPP_HOME}")!=-1){
                        uri=uri.replaceAll("\\$\\{WEBAPP_HOME\\}", webappHome.toURI().toString() );
                    }
                    
                    // Add uri to confiuration
                    LOG.info("Add catalog uri "+uri+"");
                    allURIs.add(uri);
                }
                
            }
            resolver.setCatalogs(allURIs);
            
            // Store all configured URIs
            config.put(XMLReaderObjectFactory.CATALOG_URIS, allURIs);
            
        }
        
        // Store resolver
        config.put(XMLReaderObjectFactory.CATALOG_RESOLVER, resolver);
        
        // cache
        GrammarPool gp = new GrammarPool();
        config.put(XMLReaderObjectFactory.GRAMMER_POOL, gp);
        
    }
    
    
    public String getConfigFilePath() {
        return configFilePath;
    }
    
    public File getExistHome() {
        return existHome;
    }
    
    public Object getProperty(String name) {
        return config.get(name);
    }
    
    public boolean hasProperty(String name) {
        return config.containsKey(name);
    }
    
    public void setProperty(String name, Object obj) {
        config.put(name, obj);
    }
    
    /**
     * Takes the passed string and converts it to a non-null
     * <code>Boolean</code> object.  If value is null, the specified
     * default value is used.  Otherwise, Boolean.TRUE is returned if
     * and only if the passed string equals &quot;yes&quot; or
     * &quot;true&quot;, ignoring case.
     *
     * @param value The string to parse
     * @param defaultValue The default if the string is null
     * @return The parsed <code>Boolean</code>
     */
    private Boolean parseBoolean(String value, boolean defaultValue) {
        if(value == null)
            return Boolean.valueOf(defaultValue);
        value = value.toLowerCase();
        return Boolean.valueOf(value.equals("yes") || value.equals("true"));
    }
    
    public int getInteger(String name) {
        Object obj = getProperty(name);
        
        if ((obj == null) || !(obj instanceof Integer))
            return -1;
        
        return ((Integer) obj).intValue();
    }
    
    /**
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        System.err.println("error occured while reading configuration file "
            + "[line: " + exception.getLineNumber() + "]:"
            + exception.getMessage());
    }
    
    /**
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        System.err.println("error occured while reading configuration file "
            + "[line: " + exception.getLineNumber() + "]:"
            + exception.getMessage());
    }
    
    /**
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException {
        System.err.println("error occured while reading configuration file "
            + "[line: " + exception.getLineNumber() + "]:"
            + exception.getMessage());
    }
    
}
