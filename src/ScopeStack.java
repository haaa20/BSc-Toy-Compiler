import SimpleLang.SLType;

import java.util.*;

public class ScopeStack {
    // A class to keep track of which idfrs are currently in scope
    // I'll do this with a map of strings to types
    // plus a stack of lists of strings at different layers
    private HashMap<String, SLType> id;
    private Stack<Collection<String>> scope;
    private Collection<String> global; // This will ALWAYS be the base of the stack, and should be the only
    // layer on the stack other than the top which we can access individually

    // TO KEEP THINGS CLEAN, I AM NOT GOING TO THROW ANY EXCEPTIONS FROM THIS CLASS

    public Collection<String> getGlobal() {
        return global;
    }

    public ScopeStack() {
        id = new HashMap<>();
        scope = new Stack<>();
        global = new HashSet<>();

        scope.push(global);
    }

    // Pushes a new scope stack layer with some new values
    public void pushScope(HashMap<String, SLType> newIds) {
        pushScope();
        putAll(newIds);
    }

    // Pushes a new, empty collection of Strings to the top of the scope stack
    // New ID's will be added to this
    public void pushScope() {
        Set<String> scopeId = new HashSet<>();
        scope.push(scopeId);
    }

    // Pops off the top of the stack, removing said ids from the map
    public Collection<String> popScope() {
        Collection<String> popped = scope.pop();
        for (String s : popped) {
            id.remove(s);
        }
        return popped;
    }

    // Global layer interface
    // ...
    // ...
    public void globalPut(String newId, SLType newType) {
        // Put the k-v pair directly onto the id map without putting the id at the current scope layer
        global.add(newId);
        id.put(newId, newType);
    }

    public void globalRemove(String keyId) {
        if (global.remove(keyId)) {
            id.remove(keyId);
        }
    }

    public boolean globalContains(String keyId) {
        return global.contains(keyId);
    }

    // Simple Map implementation below
    // ...
    // ...
    public SLType get(String keyId) {
        return id.get(keyId);
    }

    public void put(String newId, SLType newType) {
        // Something to think about: what if you're overriding a variable from an outer scope?
        // No need to worry about it for this coursework, but worth keeping in mind
        scope.peek().add(newId);
        id.put(newId, newType);
    }

    public void remove(String keyId) {
        // Only works on the top layer
        if (scope.peek().remove(keyId)) {
            id.remove(keyId);
        }
    }

    public void putAll(HashMap<String, SLType> newIds) {
        scope.peek().addAll(newIds.keySet());
        id.putAll(newIds);
    }

    public boolean containsKey(String keyId) {
        return (id.containsKey(keyId));
    }
}
