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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * A memory-efficient alternative to HashMultimap for maze reachability tracking.
 * Uses HashMap<MazePassage, List<MazePassage>> instead of HashMultimap's 
 * HashMap<MazePassage, Set<MazePassage>> approach to reduce memory overhead
 * and improve cache locality for large maze structures.
 */
public class CompactReachabilityMap implements Multimap<MazePassage, MazePassage> {
    
    private final Map<MazePassage, List<MazePassage>> map;
    private int size = 0;
    
    public CompactReachabilityMap() {
        this.map = new HashMap<>();
    }
    
    public CompactReachabilityMap(int expectedKeys) {
        this.map = new HashMap<>(expectedKeys);
    }
    
    public CompactReachabilityMap(Multimap<MazePassage, MazePassage> other) {
        this.map = new HashMap<>(other.keySet().size());
        putAll(other);
    }
    
    @Override
    public boolean put(MazePassage key, MazePassage value) {
        if (key == null || value == null) {
            return false;
        }
        
        List<MazePassage> values = map.get(key);
        if (values == null) {
            values = new ArrayList<>(4); // Small initial capacity
            map.put(key, values);
        }
        
        // Check for duplicates (ArrayList.contains is O(n) but lists are typically small)
        if (!values.contains(value)) {
            values.add(value);
            size++;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean remove(Object key, Object value) {
        if (!(key instanceof MazePassage) || !(value instanceof MazePassage)) {
            return false;
        }
        
        List<MazePassage> values = map.get(key);
        if (values == null) {
            return false;
        }
        
        boolean removed = values.remove(value);
        if (removed) {
            size--;
            if (values.isEmpty()) {
                map.remove(key);
            }
        }
        return removed;
    }
    
    @Override
    public Collection<MazePassage> get(MazePassage key) {
        List<MazePassage> values = map.get(key);
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        for (List<MazePassage> values : map.values()) {
            if (values.contains(value)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean containsEntry(Object key, Object value) {
        List<MazePassage> values = map.get(key);
        return values != null && values.contains(value);
    }
    
    @Override
    public Collection<MazePassage> removeAll(Object key) {
        List<MazePassage> values = map.remove(key);
        if (values != null) {
            size -= values.size();
            return new ArrayList<>(values);
        }
        return Collections.emptyList();
    }
    
    @Override
    public Collection<MazePassage> replaceValues(MazePassage key, Iterable<? extends MazePassage> values) {
        Collection<MazePassage> oldValues = removeAll(key);
        for (MazePassage value : values) {
            put(key, value);
        }
        return oldValues;
    }
    
    @Override
    public Set<MazePassage> keySet() {
        return map.keySet();
    }
    
    @Override
    public Collection<MazePassage> values() {
        List<MazePassage> result = new ArrayList<>(size);
        for (List<MazePassage> values : map.values()) {
            result.addAll(values);
        }
        return result;
    }
    
    @Override
    public Collection<Map.Entry<MazePassage, MazePassage>> entries() {
        List<Map.Entry<MazePassage, MazePassage>> result = new ArrayList<>(size);
        for (Map.Entry<MazePassage, List<MazePassage>> entry : map.entrySet()) {
            MazePassage key = entry.getKey();
            for (MazePassage value : entry.getValue()) {
                result.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
            }
        }
        return result;
    }
    
    @Override
    public Map<MazePassage, Collection<MazePassage>> asMap() {
        Map<MazePassage, Collection<MazePassage>> result = new HashMap<>(map.size());
        for (Map.Entry<MazePassage, List<MazePassage>> entry : map.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public boolean putAll(MazePassage key, Iterable<? extends MazePassage> values) {
        boolean changed = false;
        for (MazePassage value : values) {
            if (put(key, value)) {
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public boolean putAll(Multimap<? extends MazePassage, ? extends MazePassage> multimap) {
        boolean changed = false;
        for (Map.Entry<? extends MazePassage, ? extends MazePassage> entry : multimap.entries()) {
            if (put(entry.getKey(), entry.getValue())) {
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public Multiset<MazePassage> keys() {
        // This is rarely used in maze code, so we can provide a simple implementation
        List<MazePassage> keyList = new ArrayList<>(size);
        for (Map.Entry<MazePassage, List<MazePassage>> entry : map.entrySet()) {
            MazePassage key = entry.getKey();
            int count = entry.getValue().size();
            for (int i = 0; i < count; i++) {
                keyList.add(key);
            }
        }
        return HashMultiset.create(keyList);
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public void clear() {
        map.clear();
        size = 0;
    }
    
    /**
     * Create a new CompactReachabilityMap instance.
     * Factory method for consistency with HashMultimap.create()
     */
    public static CompactReachabilityMap create() {
        return new CompactReachabilityMap();
    }
    
    /**
     * Create a new CompactReachabilityMap with expected capacity.
     */
    public static CompactReachabilityMap create(int expectedKeys) {
        return new CompactReachabilityMap(expectedKeys);
    }
    
    /**
     * Create a copy of the given multimap using CompactReachabilityMap.
     */
    public static CompactReachabilityMap create(Multimap<MazePassage, MazePassage> multimap) {
        return new CompactReachabilityMap(multimap);
    }
}