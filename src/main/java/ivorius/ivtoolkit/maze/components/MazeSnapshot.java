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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a snapshot of a maze state that can be efficiently restored
 * by tracking incremental changes.
 */
public class MazeSnapshot<C> {
    private final Set<MazeRoom> rooms;
    private final Map<MazePassage, C> exits;
    private final Multimap<MazePassage, MazePassage> reachability;
    private final List<MazeChangeDiff<C>> changesFromSnapshot;

    public MazeSnapshot(Set<MazeRoom> rooms, Map<MazePassage, C> exits,
                       Multimap<MazePassage, MazePassage> reachability) {
        this.rooms = rooms;
        this.exits = exits;
        this.reachability = reachability;
        this.changesFromSnapshot = new ArrayList<>();
    }

    public Set<MazeRoom> getRooms() {
        return rooms;
    }

    public Map<MazePassage, C> getExits() {
        return exits;
    }

    public Multimap<MazePassage, MazePassage> getReachability() {
        return reachability;
    }

    public List<MazeChangeDiff<C>> getChangesFromSnapshot() {
        return changesFromSnapshot;
    }

    public void addChange(MazeChangeDiff<C> change) {
        if (!change.isEmpty()) {
            changesFromSnapshot.add(change);
        }
    }
}