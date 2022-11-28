package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.relationship.RemoveEvent;

import java.util.*;

public class ChangeTracker {

    private static final ThreadLocal<ChangeTracker> CHANGE_TRACKER_THREAD_LOCAL = ThreadLocal.withInitial(ChangeTracker::new);

    private final Map<Object, List<Object>> changes;

    private ChangeTracker() {
        this.changes = new HashMap<>();
    }

    public static ChangeTracker getInstance() {
        return CHANGE_TRACKER_THREAD_LOCAL.get();
    }

    public void addChange(Object parent, Object change) {
        this.addChange(parent, Collections.singletonList(change));
    }

    public void addChange(Object parent, List<Object> changes) {
        if (!this.changes.containsKey(parent)) {
            this.changes.put(parent, new ArrayList<>());
        }
        this.changes.get(parent).addAll(changes);
    }

    public void handleRemoval(Object key) {
        List<Object> removed = this.changes.get(key);
        if (removed != null && !removed.isEmpty()) {
            for (Object obj : removed) {
                // TODO
            }
        }
    }
}
