/*
 * Notice: This is a modified version of a libgdx file. See https://github.com/libgdx/libgdx for the original work.
 * Copyright 2011 See libgdx AUTHORS file.
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

package ivorius.ivtoolkit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend this class to implement a material attribute. Register the attribute type by statically calling the
 * {@link #register(String)} method, whose return value should be used to instantiate the attribute. A class can
 * implement
 * multiple types
 *
 * @author Xoppa
 */
public abstract class Attribute {

    /**
     * The registered type aliases
     */
    private final static List<String> types = new ArrayList<>();

    /**
     * @return The ID of the specified attribute type, or zero if not available
     */
    public final static long getAttributeType(final String alias) {
        for (int i = 0; i < types.size(); i++) if (types.get(i)
            .compareTo(alias) == 0) return 1L << i;
        return 0;
    }

    /**
     * @return The alias of the specified attribute type, or null if not available.
     */
    public final static String getAttributeAlias(final long type) {
        int idx = -1;
        while (type != 0 && ++idx < 63 && (((type >> idx) & 1) == 0));
        return (idx >= 0 && idx < types.size()) ? types.get(idx) : null;
    }

    /**
     * Use {@link ivorius.ivtoolkit.models.Attribute#register(String)} instead
     */
    protected final static long register(final String alias) {
        long result = getAttributeType(alias);
        if (result > 0) return result;
        types.add(alias);
        return 1L << (types.size() - 1);
    }

    /**
     * The type of this attribute
     */
    public final long type;

    private final int typeBit;

    protected Attribute(final long type) {
        this.type = type;
        this.typeBit = Long.numberOfTrailingZeros(type);
    }

    /**
     * @return An exact copy of this attribute
     */
    public abstract Attribute copy();

    protected boolean equals(Attribute other) {
        return other.hashCode() == hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Attribute)) return false;
        final Attribute other = (Attribute) obj;
        if (this.type != other.type) return false;
        return equals(other);
    }

    @Override
    public String toString() {
        return getAttributeAlias(type);
    }

    @Override
    public int hashCode() {
        return 7489 * typeBit;
    }
}
