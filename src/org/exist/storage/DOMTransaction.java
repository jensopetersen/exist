
package org.exist.storage;

import org.exist.util.Lock;
import org.exist.util.LockException;
import org.exist.util.ReadOnlyException;


/**
 * DOMTransaction controls access to the DOM file
 * 
 * This implements a wrapper around the code passed in
 * method start(). The class acquires a lock on the
 * file, enters the locked code block and calls start.
 * 
 * @author wolf
 *
 */
public abstract class DOMTransaction {

    private Object ownerObject;
    private DOMFile file;
    private int mode = Lock.READ_LOCK;

    public DOMTransaction(Object owner, DOMFile f) {
    	ownerObject = owner;
    	file = f;
    }

	public DOMTransaction(Object owner, DOMFile f, int mode) {
		this(owner, f);
		this.mode = mode;
	}
	
    public abstract Object start() throws ReadOnlyException;

    public Object run() {
    	Lock lock = file.getLock();
        try {
            // try to acquire a lock on the file
            try {
                lock.acquire( mode );
            } catch( LockException e ) {
                // timed out
                e.printStackTrace();
                return null;
            }
    	    file.setOwnerObject(ownerObject);
            return start();
    	} catch( ReadOnlyException e ) {
        } finally {
			lock.release();
        }
        return null;
    }
}
