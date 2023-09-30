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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.MCRegistry;

/**
 * Created by lukas on 11.02.14.
 */
public class IvBlockMapper {

    private List<Block> mapping;

    public IvBlockMapper() {
        mapping = new ArrayList<>();
    }

    public IvBlockMapper(NBTTagCompound compound, String key, MCRegistry registry) {
        this(compound.getTagList(key, Constants.NBT.TAG_STRING), registry);
    }

    public IvBlockMapper(NBTTagList list, MCRegistry registry) {
        mapping = new ArrayList<>(list.tagCount());

        for (int i = 0; i < list.tagCount(); i++) {
            mapping.add(registry.blockFromID(list.getStringTagAt(i)));
        }
    }

    public void addMapping(Block block) {
        if (!mapping.contains(block)) {
            mapping.add(block);
        }
    }

    public void addMapping(Block[] blocks) {
        for (Block block : blocks) {
            addMapping(block);
        }
    }

    public int getMapping(Block block) {
        return mapping.indexOf(block);
    }

    public Block getBlock(int mapping) {
        return this.mapping.get(mapping);
    }

    public int getMapSize() {
        return mapping.size();
    }

    public NBTTagList createTagList() {
        NBTTagList list = new NBTTagList();

        for (Block block : mapping) {
            String name = Block.blockRegistry.getNameForObject(block);

            if (name != null) list.appendTag(new NBTTagString(name));
            else {
                list.appendTag(new NBTTagString("minecraft:stone"));
                System.out.println("Did not find name for block '" + block + "' to map");
            }
        }

        return list;
    }

    public NBTTagCompound createNBTForBlocks(Block[] blocks) {
        NBTTagCompound compound = new NBTTagCompound();

        int[] vals = new int[blocks.length];
        for (int i = 0; i < blocks.length; i++) vals[i] = getMapping(blocks[i]);
        NBTTagCompound compressed = new NBTTagCompound();
        IvNBTHelper.writeCompressed("data", vals, getMapSize() - 1, compressed);
        compound.setTag("blocksCompressed", compressed);

        // if (getMapSize() <= Byte.MAX_VALUE)
        // {
        // byte[] byteArray = new byte[blocks.length];
        //
        // for (int i = 0; i < blocks.length; i++)
        // {
        // byteArray[i] = (byte) getMapping(blocks[i]);
        // }
        //
        // compound.setByteArray("blockBytes", byteArray);
        // }
        // else
        // {
        // int[] intArray = new int[blocks.length];
        //
        // for (int i = 0; i < blocks.length; i++)
        // {
        // intArray[i] = getMapping(blocks[i]);
        // }
        //
        // compound.setIntArray("blockInts", intArray);
        // }

        return compound;
    }

    public Block[] createBlocksFromNBT(NBTTagCompound compound) {
        Block[] blocks;

        if (compound.hasKey("blocksCompressed")) {
            NBTTagCompound compressed = compound.getCompoundTag("blocksCompressed");
            int[] vals = IvNBTHelper.readCompressed("data", compressed);

            blocks = new Block[vals.length];

            for (int i = 0; i < vals.length; i++) blocks[i] = getBlock(vals[i]);
        } else if (compound.hasKey("blockBytes")) {
            byte[] byteArray = compound.getByteArray("blockBytes");
            blocks = new Block[byteArray.length];

            for (int i = 0; i < byteArray.length; i++) blocks[i] = getBlock(byteArray[i]);
        } else if (compound.hasKey("blockInts")) {
            int[] intArray = compound.getIntArray("blockInts");
            blocks = new Block[intArray.length];

            for (int i = 0; i < intArray.length; i++) blocks[i] = getBlock(intArray[i]);
        } else {
            throw new RuntimeException("Unrecognized block collection type " + compound);
        }

        return blocks;
    }
}
