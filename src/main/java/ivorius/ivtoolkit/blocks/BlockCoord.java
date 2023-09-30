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

package ivorius.ivtoolkit.blocks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import io.netty.buffer.ByteBuf;

/**
 * Created by lukas on 08.06.14.
 */
public class BlockCoord implements Cloneable {

    public static final BlockCoord ZERO = new BlockCoord(0, 0, 0);

    public final int x;
    public final int y;
    public final int z;

    public BlockCoord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockCoord(TileEntity tileEntity) {
        this(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    }

    public BlockCoord(NBTTagCompound compound, String keyBase) {
        this.x = compound.getInteger(keyBase + "_x");
        this.y = compound.getInteger(keyBase + "_y");
        this.z = compound.getInteger(keyBase + "_z");
    }

    public BlockCoord(ByteBuf buffer) {
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
    }

    public static void writeCoordToNBT(String keyBase, BlockCoord coord, NBTTagCompound compound) {
        if (coord != null) {
            coord.writeToNBT(compound, keyBase);
        }
    }

    public static BlockCoord readCoordFromNBT(String keyBase, NBTTagCompound compound) {
        if (compound.hasKey(keyBase + "_x") && compound.hasKey(keyBase + "_y") && compound.hasKey(keyBase + "_z")) {
            return new BlockCoord(compound, keyBase);
        }

        return null;
    }

    public static void writeCoordToBuffer(BlockCoord coord, ByteBuf buffer) {
        buffer.writeBoolean(coord != null);

        if (coord != null) {
            coord.writeToBuffer(buffer);
        }
    }

    public static BlockCoord readCoordFromBuffer(ByteBuf buffer) {
        if (buffer.readBoolean()) {
            return new BlockCoord(buffer);
        }

        return null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockCoord add(int x, int y, int z) {
        return new BlockCoord(this.x + x, this.y + y, this.z + z);
    }

    public BlockCoord add(BlockCoord coord) {
        return new BlockCoord(x + coord.x, y + coord.y, z + coord.z);
    }

    public BlockCoord subtract(int x, int y, int z) {
        return new BlockCoord(this.x - x, this.y - y, this.z - z);
    }

    public BlockCoord subtract(BlockCoord coord) {
        return new BlockCoord(x - coord.x, y - coord.y, z - coord.z);
    }

    public BlockCoord getLowerCorner(BlockCoord coord) {
        return new BlockCoord(Math.min(x, coord.x), Math.min(y, coord.y), Math.min(z, coord.z));
    }

    public BlockCoord getHigherCorner(BlockCoord coord) {
        return new BlockCoord(Math.max(x, coord.x), Math.max(y, coord.y), Math.max(z, coord.z));
    }

    public void writeToNBT(NBTTagCompound compound, String keyBase) {
        compound.setInteger(keyBase + "_x", x);
        compound.setInteger(keyBase + "_y", y);
        compound.setInteger(keyBase + "_z", z);
    }

    public void writeToBuffer(ByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
    }

    @Override
    public String toString() {
        return "BlockCoord{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlockCoord that = (BlockCoord) o;

        if (x != that.x) {
            return false;
        }
        if (y != that.y) {
            return false;
        }
        if (z != that.z) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public BlockCoord invert() {
        return new BlockCoord(-x, -y, -z);
    }

    public Block getBlock(World world) {
        return world.getBlock(x, y, z);
    }

    public int getMetadata(World world) {
        return world.getBlockMetadata(x, y, z);
    }

    public TileEntity getTileEntity(World world) {
        return world.getTileEntity(x, y, z);
    }
}
