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

    public void put(String newId, SLType newType) {
        scope.peek().add(newId);
        id.put(newId, newType);
    }

    public void putAll(HashMap<String, SLType> newIds) {
        scope.peek().addAll(newIds.keySet());
        id.putAll(newIds);
    }
}
