package cloud.alchemy.fabut.model;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Type that should be inherited by all types in testutil's tests.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public abstract class Type {
    
    // Thread-safe cache for toString results
    private static final ConcurrentHashMap<Object, String> toStringCache = new ConcurrentHashMap<>();
    
    // ThreadLocal to handle circular references during toString
    private static final ThreadLocal<Map<Object, Boolean>> inProgress = 
        ThreadLocal.withInitial(() -> new IdentityHashMap<>());
    
    // Indentation constants
    protected static final String INDENT = "  ";
    protected static final int MAX_COLLECTION_SIZE = 10;
    
    /**
     * Returns a string representation of this Type instance.
     * Handles circular references and caches results for better performance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        // Check for circular references
        if (isCircularReference(this)) {
            return getClass().getSimpleName() + "{...circular reference...}";
        }
        
        // Check cache for existing representation
        String cached = getCachedToString(this);
        if (cached != null) {
            return cached;
        }
        
        try {
            startRendering(this);
            String result = getClass().getSimpleName() + "{}";
            cacheToString(this, result);
            return result;
        } finally {
            finishRendering(this);
        }
    }
    
    /**
     * Clears the toString cache. Useful for tests or when object state changes.
     */
    public static void clearToStringCache() {
        toStringCache.clear();
    }
    
    /**
     * Format a string value for toString representation
     * @param value The string value to format
     * @return Formatted string with quotes
     */
    protected static String formatString(String value) {
        return value == null ? "'null'" : "'" + value + "'";
    }
    
    /**
     * Format a collection for toString representation, handling large collections
     * @param collection The collection to format
     * @param size The size of the collection
     * @return Formatted string representation of the collection
     */
    protected static String formatCollection(String collection, int size) {
        if (size > MAX_COLLECTION_SIZE) {
            return collection.substring(0, collection.indexOf(',')*3) + ", ... (" + (size - 3) + " more)]";
        }
        return collection;
    }
    
    /**
     * Returns a string representation optimized for test assertions.
     * This can be overridden by subclasses to provide test-specific formats.
     *
     * @return a string representation optimized for test assertions
     */
    public String toTestString() {
        return toString();
    }
    
    /**
     * Checks if an object is currently being rendered (circular reference detection)
     * @param obj The object to check
     * @return true if a circular reference is detected
     */
    protected static boolean isCircularReference(Object obj) {
        return inProgress.get().containsKey(obj);
    }
    
    /**
     * Gets a cached toString result for an object
     * @param obj The object to get the cached result for
     * @return The cached string or null if not cached
     */
    protected static String getCachedToString(Object obj) {
        return toStringCache.get(obj);
    }
    
    /**
     * Marks an object as currently being rendered
     * @param obj The object being rendered
     */
    protected static void startRendering(Object obj) {
        inProgress.get().put(obj, Boolean.TRUE);
    }
    
    /**
     * Removes an object from the currently rendering set
     * @param obj The object that finished rendering
     */
    protected static void finishRendering(Object obj) {
        inProgress.get().remove(obj);
    }
    
    /**
     * Caches a toString result for an object
     * @param obj The object to cache the result for
     * @param result The string result to cache
     */
    protected static void cacheToString(Object obj, String result) {
        toStringCache.put(obj, result);
    }
}
