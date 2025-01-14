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

package ivorius.ivtoolkit.math;

/**
 * Created by lukas on 09.06.14.
 */
public class IvShapeHelper {

    public static boolean isPointInSpheroid(double[] point, double[] spheroidOrigin, double[] spheroidRadius) {
        double totalDist = 0.0;

        for (int i = 0; i < point.length; i++) {
            double dist = (point[i] - spheroidOrigin[i]) / spheroidRadius[i];
            totalDist += dist * dist;
        }

        return totalDist < 1.0;
    }
}
