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

package ivorius.ivtoolkit.network;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by lukas on 01.07.14.
 */
public class PacketTileEntityClientEvent implements IMessage {

    private int dimension;
    private int x, y, z;
    private String context;
    private ByteBuf payload;

    public PacketTileEntityClientEvent() {}

    public PacketTileEntityClientEvent(int dimension, int x, int y, int z, String context, ByteBuf payload) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.context = context;
        this.payload = payload;
    }

    public static <ETileEntity extends TileEntity & ClientEventHandler> PacketTileEntityClientEvent packetEntityData(
        ETileEntity entity, String context, Object... params) {
        ByteBuf buf = Unpooled.buffer();
        entity.assembleClientEvent(buf, context, params);
        return new PacketTileEntityClientEvent(
            entity.getWorldObj().provider.dimensionId,
            entity.xCoord,
            entity.yCoord,
            entity.zCoord,
            context,
            buf);
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        context = ByteBufUtils.readUTF8String(buf);
        payload = IvPacketHelper.readByteBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, context);
        IvPacketHelper.writeByteBuffer(buf, payload);
    }
}
