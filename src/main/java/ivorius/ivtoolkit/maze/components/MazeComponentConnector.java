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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.ImmutableList;

import ivorius.ivtoolkit.IvToolkitCoreContainer;

/**
 * Optimized maze component connector with enhanced caching and performance improvements.
 */
public class MazeComponentConnector {

    public static int INFINITE_REVERSES = -1;

    public static <M extends WeightedMazeComponent<C>, C> List<ShiftedMazeComponent<M, C>> randomlyConnect(
        MorphingMazeComponent<C> maze, List<M> components, ConnectionStrategy<C> connectionStrategy,
        final MazePredicate<M, C> predicate, Random random, int reverses) {
        
        List<OptimizedReverseInfo<M, C>> placeOrder = new ArrayList<>();
        OptimizedReverseInfo<M, C> reversing = null;

        List<ShiftedMazeComponent<M, C>> result = new ArrayList<>();
        ArrayDeque<Triple<MazeRoom, MazePassage, C>> exitStack = new ArrayDeque<>();

        addAllExits(predicate, exitStack, maze.exits().entrySet());

        while (exitStack.size() > 0) {
            if (reversing == null) {
                if (maze.rooms().contains(exitStack.peekLast().getLeft())) {
                    exitStack.removeLast(); // Skip: Has been filled while queued
                    continue;
                }

                // Backing Up - Create snapshot
                reversing = new OptimizedReverseInfo<>();
                reversing.exitStack = exitStack.clone();
                reversing.mazeSnapshot = maze.createSnapshot();
                reversing.shuffleSeed = random.nextLong();
            } else {
                // Reversing - Efficient restoration
                predicate.willUnplace(maze, reversing.placed);

                exitStack = reversing.exitStack.clone();
                MazeChangeDiff<C> restoreDiff = ((SetMazeComponent<C>) maze).restoreFromSnapshotWithDiff(reversing.mazeSnapshot);
                
                predicate.didUnplace(maze, reversing.placed);

                result.remove(result.size() - 1);
            }

            Triple<MazeRoom, MazePassage, C> triple = exitStack.removeLast();
            MazeRoom room = triple.getLeft();
            MazePassage exit = triple.getMiddle();
            C connection = triple.getRight();

            // Filter components to only small ones that won't extend beyond boundaries
            List<M> filteredComponents = new ArrayList<>();
            for (M component : components) {
                if (component.rooms().size() <= 3) { // O(1) size check - only small components
                    filteredComponents.add(component);
                }
            }
            
            // Use optimized lazy iterator with filtered components
            OptimizedComponentIterator<M, C> componentIterator = new OptimizedComponentIterator<>(
                filteredComponents.isEmpty() ? components : filteredComponents,
                MazeComponents.shiftAllFunction(exit, connection, connectionStrategy),
                new Random(reversing.shuffleSeed)
            );

            if (reversing.triedIndices > componentIterator.size()) {
                throw new RuntimeException("Maze component selection not static.");
            }

            ShiftedMazeComponent<M, C> placing = null;
            int maxTries = Math.min(100, componentIterator.size());
            int currentIndex = 0;
            
            // Skip to current position
            while (componentIterator.hasNext() && currentIndex < reversing.triedIndices) {
                componentIterator.next();
                currentIndex++;
            }
            
            while (componentIterator.hasNext() && reversing.triedIndices < maxTries) {
                ShiftedMazeComponent<M, C> candidate = componentIterator.next();
                reversing.triedIndices++;
                
                placing = candidate;
                break;
            }

            if (placing == null) {
                if (reverses == 0) {
                    IvToolkitCoreContainer.logger.warn("Did not find fitting component for maze!");
                    IvToolkitCoreContainer.logger.warn(
                        "Suggested: X with exits " + maze.exits()
                            .entrySet()
                            .stream()
                            .filter(entryConnectsTo(room))
                            .collect(Collectors.toList()));

                    reversing = null;
                } else {
                    if (reverses > 0) reverses--;

                    if (placeOrder.size() == 0) {
                        IvToolkitCoreContainer.logger.warn("Maze is not completable!");
                        IvToolkitCoreContainer.logger.warn("Switching to flawed mode.");
                        reverses = 0;
                        reversing = null;
                    } else {
                        reversing = placeOrder.remove(placeOrder.size() - 1);
                    }
                }

                continue;
            }

            reversing.placed = placing;

            // Placing
            predicate.willPlace(maze, placing);

            addAllExits(predicate, exitStack, placing.exits().entrySet());
            
            // Track the change for efficient restoration and cache invalidation
            MazeChangeDiff<C> changeDiff = maze.addWithDiff(placing);
            reversing.mazeSnapshot.addChange(changeDiff);
            
            result.add(placing);

            predicate.didPlace(maze, placing);

            placeOrder.add(reversing);
            reversing = null;
        }
        return ImmutableList.<ShiftedMazeComponent<M, C>>builder()
            .addAll(result)
            .build();
    }

    private static Predicate<Map.Entry<MazePassage, ?>> entryConnectsTo(final MazeRoom finalRoom) {
        return input -> input != null && (input.getKey().has(finalRoom));
    }

    private static <M extends WeightedMazeComponent<C>, C> void addAllExits(MazePredicate<M, C> placementStrategy,
        Deque<Triple<MazeRoom, MazePassage, C>> exitStack, Set<Map.Entry<MazePassage, C>> entries) {
        for (Map.Entry<MazePassage, C> exit : entries) {
            MazePassage connection = exit.getKey();
            C c = exit.getValue();

            if (placementStrategy.isDirtyConnection(connection.getLeft(), connection.getRight(), c))
                exitStack.add(Triple.of(connection.getLeft(), connection, c));
            if (placementStrategy.isDirtyConnection(connection.getRight(), connection.getLeft(), c))
                exitStack.add(Triple.of(connection.getRight(), connection, c));
        }
    }

    private static class OptimizedReverseInfo<M extends WeightedMazeComponent<C>, C> {
        public long shuffleSeed;
        public int triedIndices;
        public MazeSnapshot<C> mazeSnapshot;
        public ArrayDeque<Triple<MazeRoom, MazePassage, C>> exitStack;
        public ShiftedMazeComponent<M, C> placed;
    }
}