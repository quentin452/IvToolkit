/*
 * Copyright 2014 Lukas Tenbrink
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

package ivorius.ivtoolkit.rendering;

/**
 * Created by lukas on 21.02.14.
 */
public interface Iv2DScreenEffect {

    public abstract boolean shouldApply(float ticks);

    public abstract void apply(int screenWidth, int screenHeight, float ticks, IvOpenGLTexturePingPong pingPong);

    public abstract void destruct();
}
