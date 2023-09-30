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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import org.lwjgl.util.vector.Vector3f;

import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.ivtoolkit.raytracing.IvRaytraceableAxisAlignedBox;

public class IvTileEntityRotatable extends TileEntity {

    public int direction;

    @Override
    public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readFromNBT(par1nbtTagCompound);

        direction = par1nbtTagCompound.getInteger("direction");
    }

    @Override
    public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
        super.writeToNBT(par1nbtTagCompound);

        par1nbtTagCompound.setInteger("direction", direction);
    }

    @Override
    public Packet getDescriptionPacket() {
        return IvTileEntityHelper.getStandardDescriptionPacket(this);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    public IvRaytraceableAxisAlignedBox getInterpolatedRotatedBox(Object userInfo, double x, double y, double z,
        double width, double height, double depth, double x1, double y1, double z1, double width1, double height1,
        double depth1, float fraction) {
        double xI = IvMathHelper.mix(x, x1, fraction);
        double yI = IvMathHelper.mix(y, y1, fraction);
        double zI = IvMathHelper.mix(z, z1, fraction);

        double wI = IvMathHelper.mix(width, width1, fraction);
        double hI = IvMathHelper.mix(height, height1, fraction);
        double dI = IvMathHelper.mix(depth, depth1, fraction);

        return getRotatedBox(userInfo, xI, yI, zI, wI, hI, dI);
    }

    public IvRaytraceableAxisAlignedBox getRotatedBox(Object userInfo, double x, double y, double z, double width,
        double height, double depth) {
        return IvMultiBlockHelper.getRotatedBox(
            userInfo,
            x,
            y,
            z,
            width,
            height,
            depth,
            getDirection(),
            new double[] { xCoord + 0.5, yCoord + 0.5, zCoord + 0.5 });
    }

    public AxisAlignedBB getRotatedBB(double x, double y, double z, double width, double height, double depth) {
        return IvMultiBlockHelper.getRotatedBB(
            x,
            y,
            z,
            width,
            height,
            depth,
            getDirection(),
            new double[] { xCoord + 0.5, yCoord + 0.5, zCoord + 0.5 });
    }

    public Vector3f getRotatedVector(Vector3f vector3f) {
        return IvMultiBlockHelper.getRotatedVector(vector3f, getDirection());
    }

    public Vec3 getRotatedVector(Vec3 vec3) {
        return IvMultiBlockHelper.getRotatedVector(vec3, getDirection());
    }

    public int getDirection() {
        return direction;
    }
}
