package cloud.alchemy.fabut.tracking;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single object being tracked for field-level usage analysis.
 * Records which getter methods are called on the object during a test's tracked segment.
 */
public class TrackedObject {

    private final int identityHash;
    private final Class<?> objectClass;
    private final Set<String> allFields;
    private final Set<String> accessedFields = ConcurrentHashMap.newKeySet();
    private final Object objectRef;

    public TrackedObject(int identityHash, Class<?> objectClass, Set<String> allFields) {
        this(identityHash, objectClass, allFields, null);
    }

    public TrackedObject(int identityHash, Class<?> objectClass, Set<String> allFields, Object objectRef) {
        this.identityHash = identityHash;
        this.objectClass = Objects.requireNonNull(objectClass);
        this.allFields = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(allFields)));
        this.objectRef = objectRef;
    }

    public int getIdentityHash() {
        return identityHash;
    }

    public Class<?> getObjectClass() {
        return objectClass;
    }

    public Set<String> getAllFields() {
        return allFields;
    }

    public Set<String> getAccessedFields() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(accessedFields));
    }

    public void recordAccess(String fieldName) {
        if (allFields.contains(fieldName)) {
            accessedFields.add(fieldName);
        }
    }

    public Set<String> getUnusedFields() {
        var unused = new LinkedHashSet<>(allFields);
        unused.removeAll(accessedFields);
        return Collections.unmodifiableSet(unused);
    }

    public double getUsagePercentage() {
        if (allFields.isEmpty()) {
            return 100.0;
        }
        return (accessedFields.size() * 100.0) / allFields.size();
    }

    public boolean isFullyUsed() {
        return accessedFields.containsAll(allFields);
    }

    public boolean isNeverAccessed() {
        return accessedFields.isEmpty() && !allFields.isEmpty();
    }

    public int getAccessedCount() {
        return accessedFields.size();
    }

    public int getTotalFieldCount() {
        return allFields.size();
    }

    public Object getObjectRef() {
        return objectRef;
    }
}
