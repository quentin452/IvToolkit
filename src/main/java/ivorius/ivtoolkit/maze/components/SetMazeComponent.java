/*
 * Copyright 2015 Lukas Tenbrink
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ivorius.ivtoolkit.maze.components;

import java.util.*;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Created by lukas on 15.04.15.
 */
public class SetMazeComponent<C> implements MorphingMazeComponent<C> {

    public final Set<MazeRoom> rooms;
    public final Map<MazePassage, C> exits;
    public final Multimap<MazePassage, MazePassage> reachability;

    public SetMazeComponent() {
        this.rooms = new HashSet<>();
        this.exits = new HashMap<>();
        this.reachability = CompactReachabilityMap.create();
    }

    @Deprecated
    public SetMazeComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits) {
        this(rooms, exits, CompactReachabilityMap.create());
        connectAll(exits.keySet(), reachability);
    }

    public static void connectAll(Set<MazePassage> exits, Multimap<MazePassage, MazePassage> reachability) {
        // Transitive, so one in both direction is enough
        List<MazePassage> connections = Lists.newArrayList(exits);
        for (int i = 1; i < connections.size(); i++) {
            MazePassage left = connections.get(i - 1);
            MazePassage right = connections.get(i);
            reachability.put(left, right);
            reachability.put(right, left);
        }
    }

    public static void connectAll(Set<MazePassage> exits,
        ImmutableMultimap.Builder<MazePassage, MazePassage> reachability) {
        // Transitive, so one in both direction is enough
        List<MazePassage> connections = Lists.newArrayList(exits);
        for (int i = 1; i < connections.size(); i++) {
            MazePassage left = connections.get(i - 1);
            MazePassage right = connections.get(i);
            reachability.put(left, right);
            reachability.put(right, left);
        }
    }

    public SetMazeComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits,
        Multimap<MazePassage, MazePassage> reachability) {
        // Initialize with optimized capacity to reduce rehashing
        int roomsCapacity = Math.max(16, rooms.size() * 4 / 3 + 1);
        int exitsCapacity = Math.max(16, exits.size() * 4 / 3 + 1);
        
        this.rooms = new HashSet<>(roomsCapacity);
        this.exits = new HashMap<>(exitsCapacity);
        this.reachability = CompactReachabilityMap.create();
        
        this.rooms.addAll(rooms);
        this.exits.putAll(exits);
        this.reachability.putAll(reachability);
    }

    @Override
    public Set<MazeRoom> rooms() {
        return rooms;
    }

    @Override
    public Map<MazePassage, C> exits() {
        return exits;
    }

    @Override
    public Multimap<MazePassage, MazePassage> reachability() {
        return reachability;
    }

    @Override
    public void add(MazeComponent<C> component) {
        rooms.addAll(component.rooms());

        // Remove all solved connections, and add the ones still open from the other component
        component.exits()
            .entrySet()
            .stream()
            .filter(entry -> exits.remove(entry.getKey()) == null)
            .forEach(entry -> exits.put(entry.getKey(), entry.getValue()));

        reachability.putAll(component.reachability());
    }

    @Override
    public void set(MazeComponent<C> component) {
        rooms.clear();
        rooms.addAll(component.rooms());

        exits.clear();
        exits.putAll(component.exits());

        reachability.clear();
        reachability.putAll(component.reachability());
    }

    @Override
    public MorphingMazeComponent<C> copy() {
        return new SetMazeComponent<>(rooms, exits, reachability);
    }

    @Override
    public MazeSnapshot<C> createSnapshot() {
        // Create shallow copies for the snapshot
        return new MazeSnapshot<>(
            new HashSet<>(rooms),
            new HashMap<>(exits),
            CompactReachabilityMap.create(reachability)
        );
    }

    @Override
    public void restoreFromSnapshot(MazeSnapshot<C> snapshot) {
        restoreFromSnapshotWithDiff(snapshot);
    }

    /**
     * Restores from snapshot and returns the diff representing the restoration.
     * This is useful for cache invalidation.
     */
    public MazeChangeDiff<C> restoreFromSnapshotWithDiff(MazeSnapshot<C> snapshot) {
        List<MazeChangeDiff<C>> changes = snapshot.getChangesFromSnapshot();
        
        // Combine all changes into a single diff for cache invalidation
        Set<MazeRoom> allAddedRooms = new HashSet<>();
        Set<MazeRoom> allRemovedRooms = new HashSet<>();
        Map<MazePassage, C> allAddedExits = new HashMap<>();
        Map<MazePassage, C> allRemovedExits = new HashMap<>();
        
        // Apply all inverse changes in reverse order
        for (int i = changes.size() - 1; i >= 0; i--) {
            MazeChangeDiff<C> change = changes.get(i);
            applyInverseDiff(change);
            
            // Track the inverse operations for cache invalidation
            allRemovedRooms.addAll(change.addedRooms);
            allAddedRooms.addAll(change.removedRooms);
            allRemovedExits.putAll(change.addedExits);
            allAddedExits.putAll(change.removedExits);
        }
        
        changes.clear();
        
        return new MazeChangeDiff<>(allAddedRooms, allRemovedRooms, 
                                   allAddedExits, allRemovedExits,
                                   CompactReachabilityMap.create(), CompactReachabilityMap.create());
    }

    @Override
    public MazeChangeDiff<C> addWithDiff(MazeComponent<C> component) {
        Set<MazeRoom> addedRooms = new HashSet<>();
        Map<MazePassage, C> addedExits = new HashMap<>();
        Map<MazePassage, C> removedExits = new HashMap<>();
        Multimap<MazePassage, MazePassage> addedReachability = CompactReachabilityMap.create();

        // Track rooms that will be added
        for (MazeRoom room : component.rooms()) {
            if (!rooms.contains(room)) {
                addedRooms.add(room);
            }
        }
        rooms.addAll(component.rooms());

        // Track exit changes (some may be removed due to connections)
        for (Map.Entry<MazePassage, C> entry : component.exits().entrySet()) {
            C existing = exits.remove(entry.getKey());
            if (existing == null) {
                addedExits.put(entry.getKey(), entry.getValue());
                exits.put(entry.getKey(), entry.getValue());
            } else {
                removedExits.put(entry.getKey(), existing);
            }
        }

        // Track reachability additions
        for (Map.Entry<MazePassage, MazePassage> entry : component.reachability().entries()) {
            if (!reachability.containsEntry(entry.getKey(), entry.getValue())) {
                addedReachability.put(entry.getKey(), entry.getValue());
            }
        }
        reachability.putAll(component.reachability());

        return new MazeChangeDiff<>(
            addedRooms, new HashSet<>(),
            addedExits, removedExits,
            addedReachability, CompactReachabilityMap.create()
        );
    }

    @Override
    public void applyInverseDiff(MazeChangeDiff<C> diff) {
        // Remove added rooms
        rooms.removeAll(diff.addedRooms);
        
        // Remove added exits and restore removed exits
        for (MazePassage passage : diff.addedExits.keySet()) {
            exits.remove(passage);
        }
        exits.putAll(diff.removedExits);
        
        // Remove added reachability
        for (Map.Entry<MazePassage, MazePassage> entry : diff.addedReachability.entries()) {
            reachability.remove(entry.getKey(), entry.getValue());
        }
    }
}
