package org.exist.xmldb;

import java.io.IOException;
import java.util.Iterator;

import org.exist.EXistException;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.DocumentImpl;
import org.exist.security.Group;
import org.exist.security.Permission;
import org.exist.security.PermissionDeniedException;
import org.exist.security.Subject;
import org.exist.security.Account;
import org.exist.security.User;
import org.exist.security.internal.aider.UserAider;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.LockException;
import org.exist.util.SyntaxException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;

/*************************************************
 * Modified by {Marco.Tampucci, Massimo.Martinelli} @isti.cnr.it
**************************************/
public class LocalUserManagementService implements UserManagementService {
	private LocalCollection collection;

	private BrokerPool pool;
	private Subject user;

	public LocalUserManagementService(
		Subject user,
		BrokerPool pool,
		LocalCollection collection) {
		this.pool = pool;
		this.collection = collection;
		this.user = user;
	}

    @Override
	public void addAccount(Account u) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		if (!manager.hasAdminPrivileges(user))
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				" you are not allowed to change this user");
		if (manager.hasAccount(u.getName()))
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				"user " + u.getName() + " exists");
		
		DBBroker broker = null;
		try {
	        broker = pool.get(user);

	        manager.addAccount(u);

		} catch (PermissionDeniedException e) {
			throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					e.getMessage(),
					e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLDBException(
					ErrorCodes.UNKNOWN_ERROR,
					e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public void addGroup(Group group) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		
		if (!manager.hasAdminPrivileges(user))
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				" you are not allowed to add role");
		
		if (manager.hasGroup(group.getName()))
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				"group '" + group.getName() + "' exists");
		
		DBBroker broker = null;
		try {
	        broker = pool.get(user);

        	manager.addGroup(group);
	        
		} catch (PermissionDeniedException e) {
			throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					e.getMessage(),
					e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLDBException(
					ErrorCodes.UNKNOWN_ERROR,
					e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public void setPermissions(Resource resource, Permission perm) throws XMLDBException {
		
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		DocumentImpl document = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			document = ((AbstractEXistResource) resource).openDocument(broker, Lock.WRITE_LOCK);
			if (!document.getPermissions().validate(user, Permission.WRITE) && !manager.hasAdminPrivileges(user))
				throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					"you are not the owner of this resource; owner = "
						+ document.getPermissions().getOwner());

			document.setPermissions(perm);
            if (!manager.hasGroup(perm.getOwnerGroup()))
                manager.addGroup(perm.getOwnerGroup());
            broker.storeXMLResource(transaction, document);
        
            transact.commit(transaction);
		
		} catch (PermissionDeniedException e) {
			throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					e.getMessage(),
					e);
		} catch (Exception e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
		    ((AbstractEXistResource)resource).closeDocument(document, Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void setPermissions(Collection child, Permission perm)
		throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		org.exist.collections.Collection coll = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			coll = broker.openCollection(collection.getPathURI(), Lock.WRITE_LOCK);
			if(coll == null)
				throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, "Collection " + collection.getPath() + 
						" not found");
			if (!coll.getPermissions().validate(user, Permission.WRITE) && !manager.hasAdminPrivileges(user)) {
                transact.abort(transaction);
				throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					"you are not the owner of this collection");
            }
            if (!manager.hasGroup(perm.getOwnerGroup()))
                manager.addGroup(perm.getOwnerGroup());
			coll.setPermissions(perm);
			broker.saveCollection(transaction, coll);
            transact.commit(transaction);
			broker.flush();
		} catch (IOException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (PermissionDeniedException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				e.getMessage(),
				e);
		} catch (LockException e) {
            transact.abort(transaction);
			throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					"Failed to acquire lock on collections.dbx",
					e);
		} catch (Exception e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			if(coll != null)
				coll.release(Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void chmod(String modeStr) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		org.exist.collections.Collection coll = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			coll = broker.openCollection(collection.getPathURI(), Lock.WRITE_LOCK);
			if(coll == null)
				throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, "Collection " + collection.getPath() + 
						" not found");
            if (!coll.getPermissions().validate(user, Permission.WRITE) && !manager.hasAdminPrivileges(user)) {
                transact.abort(transaction);
				   throw new XMLDBException(
					   ErrorCodes.PERMISSION_DENIED,
					   "you are not the owner of this collection");
            }
			coll.setPermissions(modeStr);
			broker.saveCollection(transaction, coll);
            transact.commit(transaction);
			broker.flush();
		} catch (SyntaxException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (LockException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				"Failed to acquire lock on collections.dbx",
				e);
		} catch (IOException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (PermissionDeniedException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				e.getMessage(),
				e);
		} catch (TriggerException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			if(coll != null)
				coll.release(Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void chmod(Resource resource, int mode) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		DocumentImpl document = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			document = ((AbstractEXistResource) resource).openDocument(broker, Lock.WRITE_LOCK);
			if (!document.getPermissions().validate(user, Permission.WRITE) && !manager.hasAdminPrivileges(user)) {
                transact.abort(transaction);
				throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					"you are not the owner of this resource");
            }
			document.setPermissions(mode);
			broker.storeXMLResource(transaction, document);
            transact.commit(transaction);
		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
		    ((AbstractEXistResource) resource).closeDocument(document, Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void chmod(int mode) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		org.exist.collections.Collection coll = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			coll = broker.openCollection(collection.getPathURI(), Lock.WRITE_LOCK);
			if(coll == null)
				throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, "Collection " + collection.getPath() + 
						" not found");
			if (!coll.getPermissions().validate(user, Permission.WRITE) && !manager.hasAdminPrivileges(user)) {
                transact.abort(transaction);
				throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					"you are not the owner of this collection");
            }
			coll.setPermissions(mode);
			broker.saveCollection(transaction, coll);
            transact.commit(transaction);
			broker.flush();
		} catch (IOException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (PermissionDeniedException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				e.getMessage(),
				e);
		} catch (LockException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				"Failed to acquire lock on collections.dbx",
				e);
		} catch (TriggerException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				"Failed to acquire lock on collections.dbx",
				e);
		} finally {
			if(coll != null)
				coll.release(Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void chmod(Resource resource, String modeStr)
		throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		DocumentImpl document = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			document = ((AbstractEXistResource) resource).openDocument(broker, Lock.WRITE_LOCK);
			if (!document.getPermissions().validate(user, Permission.WRITE) && !manager.hasAdminPrivileges(user)) {
                transact.abort(transaction);
				throw new XMLDBException(
					ErrorCodes.PERMISSION_DENIED,
					"you are not the owner of this resource");
            }
			document.setPermissions(modeStr);
			broker.storeXMLResource(transaction, document);
		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} catch (SyntaxException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			((AbstractEXistResource) resource).closeDocument(document, Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void chown(Account u, String group) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		if (!manager.hasAccount(u.getName()))
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, "Unknown account '"+u.getName()+"'.");
		if (!manager.hasAdminPrivileges(user))
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				"need admin privileges for chown");
		org.exist.collections.Collection coll = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			coll = broker.openCollection(collection.getPathURI(), Lock.WRITE_LOCK);
			coll.getPermissions().setOwner(u);
            if (!manager.hasGroup(group))
                manager.addGroup(group);
			coll.getPermissions().setGroup(group);
			broker.saveCollection(transaction, coll);
            transact.commit(transaction);
			broker.flush();
			//broker.sync();
		} catch (PermissionDeniedException e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				e.getMessage(),
				e);
		} catch (Exception e) {
            transact.abort(transaction);
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			if(coll != null)
				coll.release(Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public void chown(Resource res, Account u, String group)
		throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		if (!manager.hasAccount(u.getName()))
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, "Unknown account '"+u.getName()+"'");
		if (!manager.hasAdminPrivileges(user))
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				"need admin privileges for chown");
		DocumentImpl document = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			document = ((AbstractEXistResource) res).openDocument(broker, Lock.WRITE_LOCK);
			Permission perm = document.getPermissions();
			perm.setOwner(u);
            if (!manager.hasGroup(group))
				try {
					manager.addGroup(group);
				} catch (PermissionDeniedException e) {
					throw new XMLDBException(
							ErrorCodes.PERMISSION_DENIED,
							e.getMessage());
				}
			perm.setGroup(group);
            broker.storeXMLResource(transaction, document);
            transact.commit(transaction);
		} catch (Exception e) {
            transact.abort(transaction);
			throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					e.getMessage(),
					e);
		} finally {
			((AbstractEXistResource) res).closeDocument(document, Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}

	/* (non-Javadoc)
	 * @see org.exist.xmldb.UserManagementService#hasUserLock(org.xmldb.api.base.Resource)
	 */
    @Override
	public String hasUserLock(Resource res) throws XMLDBException {
		DocumentImpl doc = null;
		DBBroker broker = null;
		try {
		    broker = pool.get(user);
		    doc = ((AbstractEXistResource) res).openDocument(broker, Lock.READ_LOCK);
			Account lockOwner = doc.getUserLock();
			return lockOwner == null ? null : lockOwner.getName();
		} catch (EXistException e) {
		    throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					e.getMessage(),
					e);
        } finally {
        	((AbstractEXistResource) res).closeDocument(doc, Lock.READ_LOCK);
		    pool.release(broker);
		}
	}
	
    @Override
	public void lockResource(Resource res, Account u) throws XMLDBException {
		DocumentImpl doc = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			doc = ((AbstractEXistResource) res).openDocument(broker, Lock.WRITE_LOCK);
			if (!doc.getPermissions().validate(user, Permission.UPDATE))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, 
						"User is not allowed to lock resource " + res.getId());
			org.exist.security.SecurityManager manager = pool.getSecurityManager();
			if(!(user.equals(u) || manager.hasAdminPrivileges(user))) {
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
						"User " + user.getName() + " is not allowed to lock resource for " +
						"user " + u.getName());
			}
			Account lockOwner = doc.getUserLock();
			if(lockOwner != null) {
				if(lockOwner.equals(u))
					return;
				else if(!manager.hasAdminPrivileges(user))
					throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
							"Resource is already locked by user " + lockOwner.getName());
			}
			doc.setUserLock(u);
            broker.storeXMLResource(transaction, doc);
            transact.commit(transaction);
		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					e.getMessage(), e);
		} finally {
			((AbstractEXistResource) res).closeDocument(doc, Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}
	
    @Override
	public void unlockResource(Resource res) throws XMLDBException {
		DocumentImpl doc = null;
		DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			doc = ((AbstractEXistResource) res).openDocument(broker, Lock.WRITE_LOCK);
			if (!doc.getPermissions().validate(user, Permission.UPDATE))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, 
						"User is not allowed to lock resource " + res.getId());
			org.exist.security.SecurityManager manager = pool.getSecurityManager();
			Account lockOwner = doc.getUserLock();
			if(lockOwner != null && !(lockOwner.equals(user) || manager.hasAdminPrivileges(user))) {
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
						"Resource is already locked by user " + lockOwner.getName());
			}
			doc.setUserLock(null);
            broker.storeXMLResource(transaction, doc);
            transact.commit(transaction);
		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					e.getMessage(), e);
		} finally {
			((AbstractEXistResource) res).closeDocument(doc, Lock.WRITE_LOCK);
			pool.release(broker);
		}
	}
	
    @Override
	public String getName() {
		return "UserManagementService";
	}

    @Override
	public Permission getPermissions(Collection coll) throws XMLDBException {
		if (coll instanceof LocalCollection)
			return ((LocalCollection) coll).getCollection().getPermissions();
		return null;
	}

    @Override
	public Permission getPermissions(Resource resource) throws XMLDBException {
	    DBBroker broker = null;
	    DocumentImpl doc = null;
	    try {
	        broker = pool.get(user);
	        doc = ((AbstractEXistResource) resource).openDocument(broker, Lock.READ_LOCK);
	        return doc.getPermissions();
	    } catch (EXistException e) {
	        throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					e.getMessage(),
					e);
        } finally {
        	((AbstractEXistResource) resource).closeDocument(doc, Lock.READ_LOCK);
	        pool.release(broker);
	    }
	}

    @Override
	public Permission[] listResourcePermissions() throws XMLDBException {
		DBBroker broker = null;
		org.exist.collections.Collection c = null;
		try {
			broker = pool.get(user);
			c = broker.openCollection(collection.getPathURI(), Lock.READ_LOCK);
			if (!c	.getPermissions().validate(user, Permission.READ))
				return new Permission[0];
			Permission perms[] =
				new Permission[c.getDocumentCount()];
			int j = 0;
			DocumentImpl doc;
			for (Iterator<DocumentImpl> i = c.iterator(broker); i.hasNext(); j++) {
				doc = i.next();
				perms[j] = doc.getPermissions();
			}
			return perms;
		} catch (EXistException e) {
		    throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					e.getMessage(),
					e);
        } finally {
        	if(c != null)
        		c.release(Lock.READ_LOCK);
		    pool.release(broker);
		}
	}

    @Override
	public Permission[] listCollectionPermissions() throws XMLDBException {
		DBBroker broker = null;
		org.exist.collections.Collection c = null;
		try {
			broker = pool.get(user);
			c = broker.openCollection(collection.getPathURI(), Lock.READ_LOCK);
			if (!c.getPermissions().validate(user, Permission.READ))
				return new Permission[0];
			Permission perms[] =
				new Permission[c.getChildCollectionCount()];
			XmldbURI child;
			org.exist.collections.Collection childColl;
			int j = 0;
			for (Iterator<XmldbURI> i = c.collectionIterator(); i.hasNext(); j++) {
				child = i.next();
 				childColl =
					broker.openCollection(collection.getPathURI().append(child), Lock.READ_LOCK);
				if(childColl != null) {
					try {
						perms[j] = childColl.getPermissions();
					} finally {
						childColl.release(Lock.READ_LOCK);
					}
				}
			}
			return perms;
		} catch (EXistException e) {
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			if(c != null)
				c.release(Lock.READ_LOCK);
			pool.release(broker);
		}
	}

    @Override
	public String getProperty(String property) throws XMLDBException {
		return null;
	}

    @Override
	public Account getAccount(String name) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		
		DBBroker broker = null;
		try {
	        broker = pool.get(user);

	        return manager.getAccount(user, name);

		} catch (EXistException e) {
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public Account[] getAccounts() throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();

		DBBroker broker = null;
		try {
	        broker = pool.get(user);
	        
	        java.util.Collection<Account> users = manager.getUsers();
	        return users.toArray(new Account[users.size()]);
	        
		} catch (EXistException e) {
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public Group getGroup(String name) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();

		DBBroker broker = null;
		try {
	        broker = pool.get(user);
	        
	        return manager.getGroup(user, name);

		} catch (EXistException e) {
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public String[] getGroups() throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();

		DBBroker broker = null;
		try {
	        broker = pool.get(user);
	        
			java.util.Collection<Group> roles = manager.getGroups();
			String[] res = new String[roles.size()];
			int i = 0;
			for (Group role : roles) {
				res[i] = role.getName();
				i++;
			}
			return res;

		} catch (EXistException e) {
			throw new XMLDBException(
				ErrorCodes.VENDOR_ERROR,
				e.getMessage(),
				e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public String getVersion() {
		return "1.0";
	}

    @Override
	public void removeAccount(Account u) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		if (!manager.hasAdminPrivileges(user))
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				"you are not allowed to remove users");

		DBBroker broker = null;
		try {
	        broker = pool.get(user);
	        
			manager.deleteAccount(user, u);
		} catch (PermissionDeniedException e) {
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				"unable to remove user " + u.getName(),
				e);
		} catch (Exception e) {
			throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public void removeGroup(Group role) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		if (!manager.hasAdminPrivileges(user))
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				"you are not allowed to remove users");

		DBBroker broker = null;
		try {
	        broker = pool.get(user);
	        
			manager.deleteGroup(user, role.getName());
		} catch (PermissionDeniedException e) {
			throw new XMLDBException(
				ErrorCodes.PERMISSION_DENIED,
				"unable to remove role " + role.getName(),
				e);
		} catch (EXistException e) {
			throw new XMLDBException(
					ErrorCodes.VENDOR_ERROR,
					e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}

    @Override
	public void setCollection(Collection collection) throws XMLDBException {
		this.collection = (LocalCollection) collection;
	}

    @Override
	public void setProperty(String property, String value)
		throws XMLDBException {
	}

    @Override
	public void updateAccount(Account u) throws XMLDBException {
		org.exist.security.SecurityManager manager = pool.getSecurityManager();
		
		DBBroker broker = null;
		try {
			broker = pool.get(user);
		
			manager.updateAccount(user, u);
		} catch (PermissionDeniedException e) {
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, e.getMessage());
		} catch (Exception e) {
			new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getMessage());
		} finally {
			pool.release(broker);
		}
	}
	
    @Override
	public void addUserGroup(Account user) throws XMLDBException {
		
	}
	
    @Override
	public void removeGroup(Account user, String rmgroup) throws XMLDBException {
		
	}

	@Override
	public void addUser(User user) throws XMLDBException {
		Account account = new UserAider(user.getName());
		addAccount(account);
	}

	@Override
	public void updateUser(User user) throws XMLDBException {
		Account account = new UserAider(user.getName());
		account.setPassword(user.getPassword());
		//TODO: groups
		updateAccount(account);
	}

	@Override
	public User getUser(String name) throws XMLDBException {
		return getAccount(name);
	}

	@Override
	public User[] getUsers() throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeUser(User user) throws XMLDBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lockResource(Resource res, User u) throws XMLDBException {
		Account account = new UserAider(u.getName());
		lockResource(res, account);
	}
}

