/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2012 The eXist Project
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
package org.exist.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.config.Configurable;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.config.Configurator;
import org.exist.config.Startable;
import org.exist.config.annotation.ConfigurationClass;
import org.exist.config.annotation.ConfigurationFieldAsElement;
import org.exist.config.annotation.ConfigurationFieldClassMask;
import org.exist.config.annotation.ConfigurationReferenceBy;
import org.exist.plugin.Plug;
import org.exist.plugin.PluginsManager;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
@ConfigurationClass("scheduler")
public class SchedulerManager implements Plug, Configurable, Startable {

	/* /db/system/scheduler */
	private final static XmldbURI COLLECTION_URI = XmldbURI.SYSTEM.append("scheduler");
	private final static XmldbURI CONFIG_FILE_URI = XmldbURI.create("scheduler.xml");

    private final static Logger LOG = Logger.getLogger(SchedulerManager.class);
    
    @ConfigurationFieldAsElement("job")
    @ConfigurationReferenceBy("id")
    @ConfigurationFieldClassMask("org.exist.scheduler.Job")
    private List<Job> jobs = new ArrayList<Job>();

    private Collection collection = null;

	private Configuration configuration = null;
	
	protected final org.quartz.Scheduler scheduler;
	protected Database db;
	
	public SchedulerManager(PluginsManager pm) {
		db = pm.getDatabase();
		scheduler = db.getScheduler().getScheduler();
	}

	@Override
	public void startUp(DBBroker broker) throws EXistException {

        TransactionManager transaction = broker.getDatabase().getTransactionManager();
        Txn txn = null;
		
        Collection systemCollection = null;
		try {
			systemCollection = broker.getCollection(XmldbURI.SYSTEM);
	        if(systemCollection == null)
	        	throw new EXistException("/db/system collecton does not exist!");
		} catch (PermissionDeniedException e) {
			throw new EXistException(e);
		}

        try {
            collection = broker.getCollection(COLLECTION_URI);
            if (collection == null) {
                txn = transaction.beginTransaction();
                collection = broker.getOrCreateCollection(txn, COLLECTION_URI);
                if (collection == null){
                    return;
                }
                //if db corrupted it can lead to unrunnable issue
                //throw new ConfigurationException("Collection '/db/system/scheduler' can't be created.");

                collection.setPermissions(0770);
                broker.saveCollection(txn, collection);

                transaction.commit(txn);
            } 
        } catch (Exception e) {
            transaction.abort(txn);
            e.printStackTrace();
            LOG.debug("loading configuration failed: " + e.getMessage(), e);
        }

        Configuration _config_ = Configurator.parse(this, broker, collection, CONFIG_FILE_URI);
        configuration = Configurator.configure(this, _config_);
	}
	
	public void addJob(Configuration config) throws ConfigurationException {
		jobs.add( new Job(this, config) );
	}

	@Override
	public void sync() {
	}

	public void stop() {
//		scheduler.shutdown();
	}

	@Override
	public boolean isConfigured() {
		return configuration != null;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}
}
