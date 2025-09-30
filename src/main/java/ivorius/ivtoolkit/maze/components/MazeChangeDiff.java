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

import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Set;

/**
 * Represents incremental changes to a maze component to enable efficient restoration
 * without full copying.
 */
public class MazeChangeDiff<C> {
    public final Set<MazeRoom> addedRooms;
    public final Set<MazeRoom> removedRooms;
    public final Map<MazePassage, C> addedExits;
    public final Map<MazePassage, C> removedExits;
    public final Multimap<MazePassage, MazePassage> addedReachability;
    public final Multimap<MazePassage, MazePassage> removedReachability;

    public MazeChangeDiff(Set<MazeRoom> addedRooms, Set<MazeRoom> removedRooms,
                         Map<MazePassage, C> addedExits, Map<MazePassage, C> removedExits,
                         Multimap<MazePassage, MazePassage> addedReachability,
                         Multimap<MazePassage, MazePassage> removedReachability) {
        this.addedRooms = addedRooms;
        this.removedRooms = removedRooms;
        this.addedExits = addedExits;
        this.removedExits = removedExits;
        this.addedReachability = addedReachability;
        this.removedReachability = removedReachability;
    }

    public Set<MazeRoom> getAddedRooms() {
        return addedRooms;
    }

    public Set<MazeRoom> getRemovedRooms() {
        return removedRooms;
    }
    
    public Map<MazePassage, C> getAddedExits() {
        return addedExits;
    }

    public Map<MazePassage, C> getRemovedExits() {
        return removedExits;
    }

    /**
     * Creates the inverse diff that would undo this change
     */
    public MazeChangeDiff<C> inverse() {
        return new MazeChangeDiff<>(removedRooms, addedRooms,
                                   removedExits, addedExits,
                                   removedReachability, addedReachability);
    }

    /**
     * Check if this diff represents no changes
     */
    public boolean isEmpty() {
        return addedRooms.isEmpty() && removedRooms.isEmpty() &&
               addedExits.isEmpty() && removedExits.isEmpty() &&
               addedReachability.isEmpty() && removedReachability.isEmpty();
    }
}