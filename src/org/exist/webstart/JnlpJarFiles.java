/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 The eXist Project
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
 * $Id$
 */

package org.exist.webstart;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exist.start.LatestFileResolver;

/**
 *  Class for managing webstart jar files.
 *
 * @author Dannes Wessels
 */
public class JnlpJarFiles {
    
    private static Logger logger = Logger.getLogger(JnlpJarFiles.class);
    
    private Map<String, File> allFiles = new HashMap<String, File>();
    
    // Names of core jar files sans ".jar" extension.
    // Use %latest% token in place of a version string.
    private String allJarNames[] = new String[]{
        "xmldb",
        "xmlrpc-common-%latest%",
        "xmlrpc-client-%latest%",
        "ws-commons-util-%latest%",
        "commons-pool-%latest%",
        "excalibur-cli-%latest%",
        "jEdit-syntax",
        "jline-%latest%",
        "log4j-%latest%",
        "sunxacml-%latest%"
    };
    
    // Resolves jar file patterns from jars[].
    private LatestFileResolver jarFileResolver = new LatestFileResolver();
    
    /**
     * Get jar file specified by file pattern.
     * @param folder  Directory containing the jars.
     * @param jarFileBaseName  Name of jar file, including %latest% token if
     * necessary sans .jar file extension.
     * @return File object of jar file, null if not found.
     */
    private File getJarFromLocation(File folder, String jarFileBaseName){
        String fileToFind = folder.getAbsolutePath() + File.separatorChar
                + jarFileBaseName + ".jar";
        String resolvedFile = jarFileResolver.getResolvedFileName(
                fileToFind
                );
        File jar = new File(resolvedFile);
        if (jar.exists()) {
            logger.debug(
                    "Found match: " + resolvedFile
                    + " for file pattern: " + fileToFind
                    );
            return jar;
        } else {
            logger.warn("Could not resolve file pattern: " + fileToFind);
            return null;
        }
    }


    // Copy jars from map to list
    private void addToJars(File jar) {
        if (jar != null && jar.getName().endsWith(".jar")) {
            allFiles.put(jar.getName(), jar);
        }
    }
    
    /**
     * Creates a new instance of JnlpJarFiles
     *
     * @param jnlpHelper
     */
    public JnlpJarFiles(JnlpHelper jnlpHelper) {
        logger.info("Initializing jar files Webstart");

        logger.debug("Number of webstart jars="+allJarNames.length);
        
        // Setup CORE jars
        for(String jarname : allJarNames){
            File jar = getJarFromLocation(jnlpHelper.getCoreJarsFolder(), jarname);
            addToJars(jar);
         }
        
        // Setup exist.jar
        File mainJar=new File(jnlpHelper.getExistJarFolder(), "exist.jar");
        addToJars(mainJar);
    }
    

    /**
     *  Get All jar file as list.
     *
     * @return list of jar files.
     */
    public List<File> getAllWebstartJars(){
        List<File> corejars = new ArrayList<File>();

        for(File file: allFiles.values()){
                corejars.add(file);
        }

        return corejars;
    }
    

    /**
     * Get file reference for JAR file.
     * @param key
     * @return
     */
    public File getJarFile(String key){
        File retVal = allFiles.get(key);
        return retVal;
    }
    
}
