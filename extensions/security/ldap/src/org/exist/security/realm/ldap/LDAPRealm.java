/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2010 The eXist Project
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
package org.exist.security.realm.ldap;

import java.util.Collection;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.config.Configurable;
import org.exist.config.Configuration;
import org.exist.config.Configurator;
import org.exist.config.annotation.*;
import org.exist.security.AuthenticationException;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.User;
import org.exist.security.UserImpl;
import org.exist.security.realm.Realm;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
@ConfigurationClass("LDAP")
public class LDAPRealm implements Realm, Configurable {

	private final static Logger LOG = Logger.getLogger(LDAPRealm.class);

	protected LdapContextFactory ldapContextFactory = null;

	protected Configuration configuration = null;

	public LDAPRealm(Configuration config) {
		configuration = Configurator.configure(this, config);
	}

	protected LdapContextFactory ensureContextFactory() {
		if (this.ldapContextFactory == null) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("No LdapContextFactory specified - creating a default instance.");
			}

			LdapContextFactory factory = new LdapContextFactory(configuration);

			this.ldapContextFactory = factory;
		}
		return this.ldapContextFactory;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getAccount(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<User> getAccounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAccount(String accountName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateAccount(User account) throws PermissionDeniedException, EXistException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Group> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Group getGroup(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasGroup(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User getAccount(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Group getGroup(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUp(DBBroker broker) throws EXistException {
		// TODO Auto-generated method stub

	}

	public User authenticate(String username, Object credentials) throws AuthenticationException {
		// Binds using the username and password provided by the user.
		LdapContext ctx = null;
		try {
			ctx = ensureContextFactory().getLdapContext(username, String.valueOf(credentials));

		} catch (NamingException e) {
			throw new AuthenticationException(AuthenticationException.UNNOWN_EXCEPTION, e.getMessage());

		} finally {
			LdapUtils.closeContext(ctx);
		}

		return new UserImpl(this, username);
	}

	// configurable methods
	@Override
	public boolean isConfigured() {
		return (configuration != null);
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public User addAccount(User account) throws PermissionDeniedException, EXistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAccount(User account) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAccount(User account) throws PermissionDeniedException, EXistException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Group addGroup(Group role) throws PermissionDeniedException, EXistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasGroup(Group role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateGroup(Group role) throws PermissionDeniedException, EXistException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteGroup(Group role) throws PermissionDeniedException, EXistException {
		// TODO Auto-generated method stub
		return false;
	}

}
