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

/**
 * Created by lukas on 15.04.15.
 */
public interface MorphingMazeComponent<C> extends MazeComponent<C> {

    void add(MazeComponent<C> component);

    void set(MazeComponent<C> component);

    MorphingMazeComponent<C> copy();

    /**
     * Creates a snapshot that can be restored later using restoreFromSnapshot
     */
    MazeSnapshot<C> createSnapshot();

    /**
     * Efficiently restores the maze to a previous snapshot state
     * without full copying by applying inverse operations
     */
    void restoreFromSnapshot(MazeSnapshot<C> snapshot);

    /**
     * Adds a component and returns a diff that can be used to undo the operation
     */
    MazeChangeDiff<C> addWithDiff(MazeComponent<C> component);

    /**
     * Applies the inverse of a diff to undo changes
     */
    void applyInverseDiff(MazeChangeDiff<C> diff);
}
