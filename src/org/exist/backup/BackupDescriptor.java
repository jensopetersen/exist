package org.exist.backup;

import org.exist.util.EXistInputSource;

import java.util.Properties;
import java.io.IOException;
import java.io.File;

public interface BackupDescriptor {
	public final static String COLLECTION_DESCRIPTOR="__contents__.xml";

    public final static String BACKUP_PROPERTIES = "backup.properties";

	public EXistInputSource getInputSource();
	
	public EXistInputSource getInputSource(String describedItem);
	
	public BackupDescriptor getChildBackupDescriptor(String describedItem);
	
	public String getSymbolicPath();
	
	public String getSymbolicPath(String describedItem,boolean isChildDescriptor);

    /**
     * Returns general properties of the backup, normally including the creation date
     * or if it is an incremental backup.
     *
     * @return a Properties object or null if no properties were found
     * @throws IOException if there was an error in the properties file
     */
    public Properties getProperties() throws IOException;

    public File getParentDir();
}
