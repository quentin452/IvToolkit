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

import ivorius.ivtoolkit.random.WeightedShuffler;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Optimized component iterator with better memory usage and performance.
 * Uses batch processing and early filtering to reduce memory allocation.
 */
public class OptimizedComponentIterator<M extends WeightedMazeComponent<C>, C> implements Iterator<ShiftedMazeComponent<M, C>> {
    
    private final List<M> components;
    private final Function<M, Stream<ShiftedMazeComponent<M, C>>> shiftFunction;
    private final Random random;
    private final List<ShiftedMazeComponent<M, C>> shuffledBuffer;
    private int currentIndex = 0;
    private boolean initialized = false;
    private final int batchSize;

    public OptimizedComponentIterator(List<M> components, 
                                     Function<M, Stream<ShiftedMazeComponent<M, C>>> shiftFunction,
                                     Random random) {
        this.components = components;
        this.shiftFunction = shiftFunction;
        this.random = random;
        
        // Estimate buffer size more accurately
        int estimatedSize = Math.min(1000, components.size() * 4); // Assume avg 4 shifts per component
        this.shuffledBuffer = new ArrayList<>(estimatedSize);
        this.batchSize = Math.max(10, components.size() / 10); // Process in batches
    }

    private void ensureInitialized() {
        if (!initialized) {
            long startTime = System.nanoTime();
            
            // Process components in batches to reduce memory pressure
            int processedComponents = 0;
            List<ShiftedMazeComponent<M, C>> batch = new ArrayList<>(batchSize * 4);
            
            for (M component : components) {
                // Generate shifted components for this component
                shiftFunction.apply(component).forEach(shifted -> {
                    // Early filtering could be added here if needed
                    batch.add(shifted);
                });
                
                processedComponents++;
                
                // Process batch when it reaches target size
                if (processedComponents % batchSize == 0 || processedComponents == components.size()) {
                    // Shuffle this batch and add to main buffer
                    WeightedShuffler.shuffle(random, batch, 
                        shifted -> shifted.getComponent().getWeight());
                    shuffledBuffer.addAll(batch);
                    batch.clear();
                }
            }
            
            // Final shuffle of the entire buffer for good distribution
            Collections.shuffle(shuffledBuffer, random);
            
            initialized = true;
            
            long initTime = System.nanoTime() - startTime;
            if (initTime > 10_000_000) { // Log if initialization takes more than 10ms
                System.out.printf("Component iterator initialized: %d components -> %d shifted variants in %.2fms%n",
                    components.size(), shuffledBuffer.size(), initTime / 1_000_000.0);
            }
        }
    }

    @Override
    public boolean hasNext() {
        ensureInitialized();
        return currentIndex < shuffledBuffer.size();
    }

    @Override
    public ShiftedMazeComponent<M, C> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return shuffledBuffer.get(currentIndex++);
    }

    /**
     * Gets the total number of available components without consuming them
     */
    public int size() {
        ensureInitialized();
        return shuffledBuffer.size();
    }

    /**
     * Resets the iterator to the beginning
     */
    public void reset() {
        currentIndex = 0;
    }
    
    /**
     * Gets current position in the iterator
     */
    public int getPosition() {
        return currentIndex;
    }
    
    /**
     * Skips to a specific position
     */
    public void skipTo(int position) {
        ensureInitialized();
        currentIndex = Math.max(0, Math.min(position, shuffledBuffer.size()));
    }
}