package org.exist.indexing.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import org.exist.collections.CollectionConfiguration;
import org.exist.util.DatabaseConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class AnalyzerConfig {

    /*

     Supported configurations

     <analyzer class="org.apache.lucene.analysis.standard.StandardAnalyzer"/>

     <analyzer id="ws" class="org.apache.lucene.analysis.WhitespaceAnalyzer"/>

     <analyzer id="stdstops" class="org.apache.lucene.analysis.standard.StandardAnalyzer">
     .<param name="stopwords" type="java.io.File" value="/tmp/stop.txt"/>
     </analyzer>

     <analyzer id="stdstops" class="org.apache.lucene.analysis.standard.StandardAnalyzer">
     .<param name="stopwords" type="java.util.Set">
     ..<value>the</value>
     ..<value>this</value>
     ..<value>and</value>
     ..<value>that</value>
     .</param>
     </analyzer>

     <analyzer id="sbstops" class="org.apache.lucene.analysis.snowball.SnowballAnalyzer">
     .<param name="name" value="English"/>
     .<param name="stopwords" type="java.util.Set">
     ..<value>the</value>
     ..<value>this</value>
     ..<value>and</value>
     ..<value>that</value>
     .</param>
     </analyzer>

     */

    private static final Logger LOG = Logger.getLogger(AnalyzerConfig.class);
	
    private static final String ID_ATTRIBUTE = "id";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String PARAM_VALUE_ENTRY = "value";
    private static final String PARAM_ELEMENT_NAME = "param";
   

    private Map<String, Analyzer> analyzers = new TreeMap<String, Analyzer>();
    private Analyzer defaultAnalyzer = null;

    public Analyzer getAnalyzerById(String id) {
        return analyzers.get(id);
    }

    public Analyzer getDefaultAnalyzer() {
        return defaultAnalyzer;
    }

    /**
     * Parse <analyzer/> element and register configured analyzer.
     *
     * @param config The analyzer element from .xconf file.
     *
     * @throws DatabaseConfigurationException Something unexpected happened.
     */
    public void addAnalyzer(Element config) throws DatabaseConfigurationException {

        // Configure lucene analuzer with configuration
        final Analyzer analyzer = configureAnalyzer(config);   
        if(analyzer == null) {
            return;
        }

        // Get (optional) id-attribute of analyzer
        final String id = config.getAttribute(ID_ATTRIBUTE);

        // If no ID is provided, register as default analyzer
        // else register analyzer
        if (StringUtils.isBlank(id)) {  
            setDefaultAnalyzer(analyzer);
        } else {
            analyzers.put(id, analyzer);
        }
    }

    /**
     * Set default the analyzer.
     *
     * @param analyzer Lucene analyzer
     */
    public void setDefaultAnalyzer(Analyzer analyzer) {
        defaultAnalyzer = analyzer;
    }

    /**
     * Parse <analyzer/> element from xconf and initialize an analyzer with the parameters.
     *
     * @param config The analyzer element
     * @return Initialized Analyzer object
     *
     * @throws DatabaseConfigurationException Something unexpected happened.
     */
    protected static Analyzer configureAnalyzer(Element config) throws DatabaseConfigurationException {

        // Get classname from attribute
        final String className = config.getAttribute(CLASS_ATTRIBUTE);

        if (StringUtils.isBlank(className)) {
            // No classname is defined.
            LOG.error("Missing class attribute or attribute is empty.");

            // DW: throw exception?

        } else {
            // Classname is defined.
            try {
                Class<?> clazz = Class.forName(className);
                if (!Analyzer.class.isAssignableFrom(clazz)) {
                    LOG.warn("Lucene index: analyzer class has to be a subclass of " + Analyzer.class.getName());
                    return null;
                }

                // Get list of parameters
                final List<KeyTypedValue> cParams = getAllConstructorParameters(config);

                // Iterate over all parameters, convert data to two arrays
                // that can be used in the reflection code
                final Class<?> cParamClasses[] = new Class<?>[cParams.size()];
                final Object cParamValues[] = new Object[cParams.size()];
                for(int i = 0; i < cParams.size(); i++) {
                    KeyTypedValue ktv = cParams.get(i);
                    cParamClasses[i] = ktv.getValueClass();
                    cParamValues[i] = ktv.getValue();
                }
                
                //try and get a matching constructor
                try {
                    final Constructor<?> cstr = clazz.getDeclaredConstructor(cParamClasses);
                    cstr.setAccessible(true);                
                    return (Analyzer)cstr.newInstance(cParamValues);
                    
                } catch (NoSuchMethodException nsme) {

                    // We could not find a constructor that had a complete match
                    // This makes sense because because a lucene Version class is requires most of the time
            
                    //LOG.warn("Could not find matching analyzer class constructor" + className + ": " + nsme.getMessage()
                    //        + " now looking for similar constructor with Version parameter", nsme);
                    
                    //couldnt find a matching constructor,
                    //if a version parameter wasnt already specified
                    //see if there is one with a Version parameter 
                    if (cParamClasses.length == 0 || (cParamClasses.length > 0 && cParamClasses[0] != Version.class)) {

                        // extend arrays, add to front
                        Class<?>[] vcParamClasses = addVersionToClasses(cParamClasses);
                        Object[] vcParamValues = adVersionValueToValues(cParamValues);

                        // Finally invoke again
                        try {
                            final Constructor<?> cstr = clazz.getDeclaredConstructor(vcParamClasses);
                            cstr.setAccessible(true);        
                            LOG.warn("Using analyzer " + clazz.getName());
                            return (Analyzer)cstr.newInstance(vcParamValues);
                            
                        } catch (NoSuchMethodException vnsme) {
                            LOG.error("Could not find matching analyzer class constructor" + className + ": " + vnsme.getMessage(), vnsme);
                        }
                    }
                }
                
            } catch (ClassNotFoundException e) {
                LOG.error("Lucene index: analyzer class " + className + " not found.");
            } catch (IllegalAccessException e) {
                LOG.error("Exception while instantiating analyzer class " + className + ": " + e.getMessage(), e);
            } catch (InstantiationException e) {
                LOG.error("Exception while instantiating analyzer class " + className + ": " + e.getMessage(), e);
            } catch (InvocationTargetException ite) {
                LOG.error("Exception while instantiating analyzer class " + className + ": " + ite.getMessage(), ite);
            } catch (ParameterException pe) {
                LOG.error("Exception while instantiating analyzer class " + className + ": " + pe.getMessage(), pe);
            }
        }
        return null;
    }

    /**
     * Extend list of values, add version-value to front
     */
    private static Object[] adVersionValueToValues(final Object[] cParamValues) {
        final Object vcParamValues[] = new Object[cParamValues.length + 1];
        vcParamValues[0] = LuceneIndex.LUCENE_VERSION_IN_USE;
        System.arraycopy(cParamValues, 0, vcParamValues, 1, cParamValues.length);
        return vcParamValues;
    }

    /**
     * Extend list of classes, add version-class to front
     */
    private static Class<?>[] addVersionToClasses(final Class<?>[] cParamClasses) {
        final Class<?> vcParamClasses[] = new Class<?>[cParamClasses.length + 1];
        vcParamClasses[0] = Version.class;
        System.arraycopy(cParamClasses, 0, vcParamClasses, 1, cParamClasses.length);
        return vcParamClasses;
    }

    /**
     * Retrieve parameter info from all <param/> elements.
     *
     * @param config The <analyzer/> element from the provided configuration
     * @return List of triples key-value-valueType
     * @throws org.exist.indexing.lucene.AnalyzerConfig.ParameterException
     */
    private static List<KeyTypedValue> getAllConstructorParameters(Element config) throws ParameterException {
        final List<KeyTypedValue> parameters = new ArrayList<KeyTypedValue>();
        final NodeList params = config.getElementsByTagNameNS(CollectionConfiguration.NAMESPACE, PARAM_ELEMENT_NAME);

        // iterate over all <param/> elements
        for(int i = 0; i < params.getLength(); i++) {
            parameters.add(getConstructorParameter((Element)params.item(i)));
        }
        
        return parameters;
    }


    /**
     * Retrieve configuration information from one <param/> element. Type information is used to construct actual data
     * containing objects.
     *
     * @param param Element that represents <param/>
     * @return Triple key-value-value-type
     * @throws org.exist.indexing.lucene.AnalyzerConfig.ParameterException
     */
    private static KeyTypedValue getConstructorParameter(Element param) throws ParameterException {

        // Get attributes
        final NamedNodeMap attrs = param.getAttributes();

        // Get name of parameter
        final String name = attrs.getNamedItem(NAME_ATTRIBUTE).getNodeValue();

        // Get value type information of parameter
        final String type;
        if (attrs.getNamedItem(TYPE_ATTRIBUTE) != null) {
            type = attrs.getNamedItem(TYPE_ATTRIBUTE).getNodeValue();
        } else {
            type = null;
        }

        // Get actual value from.... attribute?
        final String value;
        if(attrs.getNamedItem(PARAM_VALUE_ENTRY) != null) {
            value = attrs.getNamedItem(PARAM_VALUE_ENTRY).getNodeValue();
        } else {
            // This is dangerous
            value = null;
        }

        // Place holder return value
        KeyTypedValue parameter=null;

        if (type != null && type.equals("java.lang.reflect.Field")) {

            if (value == null) {
                throw new ParameterException("The 'value' attribute must exist and must contain a full classname.");
            }

            // Use reflection
            // - retrieve classname from the value field
            // - retrieve fieldname from the value field
            final String clazzName = value.substring(0, value.lastIndexOf("."));
            final String fieldName = value.substring(value.lastIndexOf(".") + 1);

            try {
                // Retrieve value from Field
                final Class fieldClazz = Class.forName(clazzName);
                final Field field = fieldClazz.getField(fieldName);
                field.setAccessible(true);
                final Object fValue = field.get(fieldClazz.newInstance());
                parameter = new KeyTypedValue(name, fValue);
                
            } catch(NoSuchFieldException nsfe) {
                throw new ParameterException(nsfe.getMessage(), nsfe);
            } catch(ClassNotFoundException nsfe) {
                throw new ParameterException(nsfe.getMessage(), nsfe);
            } catch(InstantiationException nsfe) {
                throw new ParameterException(nsfe.getMessage(), nsfe);
            } catch(IllegalAccessException nsfe) {
                throw new ParameterException(nsfe.getMessage(), nsfe);
            }
            
        } else if (type != null && type.equals("java.io.File")) {
            // This is actually decrecated now, "Reader" must be used
            final File f = new File(value);
            parameter = new KeyTypedValue(name, f, File.class);
            
        } else if (type != null && type.equals("java.io.FileReader")) {
            // DW: Experimental
            final File f = new File(value);
            try {     
                final Reader r = new FileReader(f);
                parameter = new KeyTypedValue(name, r, Reader.class);
            } catch (FileNotFoundException ex) {
                LOG.error(String.format("File %s could not be found.", f.getAbsolutePath()), ex);
            }       
            
        } else if (type != null && type.equals("java.util.Set")) {
            // This is actually deprecated now, Lucene4 requires CharArraySet
            final Set s = getConstructorParameterSetValues(param);
            parameter = new KeyTypedValue(name, s, Set.class);

        } else if (type != null && type.equals("org.apache.lucene.analysis.util.CharArraySet")) {
            // This is mandatory since Lucene4
            final CharArraySet s = getConstructorParameterCharArraySetValues(param);
            parameter = new KeyTypedValue(name, s, CharArraySet.class);

        } else if (type != null && (type.equals("java.lang.Integer") || type.equals("int"))) {
            try {
                final Integer n = Integer.parseInt(value);
                parameter = new KeyTypedValue(name, n);
            } catch (NumberFormatException ex) {
                LOG.error(String.format("Value %s could not be converted to an integer.", value));
            }
            
        } else if (type != null && (type.equals("java.lang.Boolean") || type.equals("boolean"))) {
            // Straight forward
            final boolean b = Boolean.parseBoolean(value);
            parameter = new KeyTypedValue(name, b);
            
        } else {
            // FallBack type = null
            try {
                //if the type is an Enum then use valueOf()
                final Class clazz = Class.forName(type);
                if(clazz.isEnum()) {
                    parameter = new KeyTypedValue(name, Enum.valueOf(clazz, value), clazz);
                } else {       
                    //default, assume java.lang.String
                    parameter = new KeyTypedValue(name, value);
                }

            } catch(ClassNotFoundException cnfe) {
                throw new ParameterException(String.format("Class for type: %s not found. %s", type, cnfe.getMessage()), cnfe);
            }
        }
        
        return parameter;
    }



    /**
     * Get parameter configuration data as standard Java (Hash)Set.
     *
     * @param param The parameter-configuration element.
     * @return Set of parameter values
     */
    private static Set<String> getConstructorParameterSetValues(Element param) {
        final Set<String> set = new HashSet<String>();
        final NodeList values = param.getElementsByTagNameNS(CollectionConfiguration.NAMESPACE, PARAM_VALUE_ENTRY);
        for(int i = 0; i < values.getLength(); i++) {
            final Element value = (Element)values.item(i);
            
            //TODO getNodeValue() on org.exist.dom.ElementImpl should return null according to W3C spec!
            if(value instanceof org.exist.dom.ElementImpl) {
               set.add(value.getNodeValue());
            } else {
                set.add(value.getTextContent());
            }
        }
        
        return set;
    }

    /**
     * Get parameter configuration data as a Lucene CharArraySet.
     *
     * @param param The parameter-configuration element.
     * @return Parameter data as Lucene CharArraySet
     */
    private static CharArraySet getConstructorParameterCharArraySetValues(Element param) {
        final Set<String> set = getConstructorParameterSetValues(param);
        return CharArraySet.copy(LuceneIndex.LUCENE_VERSION_IN_USE, set);
    }

    /**
     * CLass for containing the Triple : key (name), corresponding value and class type of value.
     */
    private static class KeyTypedValue {
        private final String key;
        private final Object value;
        private final Class<?> valueClass;
        
        public KeyTypedValue(String key, Object value) {
            this(key, value, value.getClass());
        }
        
        public KeyTypedValue(String key, Object value, Class<?> valueClass) {
            this.key = key;
            this.value = value;
            this.valueClass = valueClass;
        }
        
        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
        
        public Class<?> getValueClass() {
            return valueClass;
        }
    }

    /**
     * Exception class to for reporting problems with the parameters.
     */
    private static class ParameterException extends Exception {

        public ParameterException(String message) {
            super(message);
        }
        public ParameterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
