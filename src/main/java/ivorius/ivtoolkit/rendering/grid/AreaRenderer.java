/*
 * Copyright 2015 Lukas Tenbrink
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ivorius.ivtoolkit.rendering.grid;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 09.02.15.
 */
@SideOnly(Side.CLIENT)
public class AreaRenderer
{
    public static void renderAreaLined(BlockArea area, float sizeP)
    {
        renderArea(area, true, false, sizeP);
    }

    public static void renderArea(BlockArea area, boolean lined, boolean insides, float sizeP)
    {
        drawCuboid(area.getLowerCorner(), area.getHigherCorner().add(1, 1, 1), lined, insides, sizeP);
    }

    @SideOnly(Side.CLIENT)
    private static void drawCuboid(BlockCoord min, BlockCoord max, boolean lined, boolean insides, float sizeP)
    {
        float width2 = ((float) max.x - (float) min.x) * 0.5f;
        float height2 = ((float) max.y - (float) min.y) * 0.5f;
        float length2 = ((float) max.z - (float) min.z) * 0.5f;

        double centerX = min.x + width2;
        double centerY = min.y + height2;
        double centerZ = min.z + length2;

        int sizeCE = insides ? -1 : 1;

        GL11.glPushMatrix();
        GL11.glTranslated(centerX, centerY, centerZ);
        if (lined)
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawLineCuboid(Tessellator.instance, width2 + sizeP, height2 + sizeP, length2 + sizeP, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        else
            drawCuboid(Tessellator.instance, width2 * sizeCE + sizeP, height2 * sizeCE + sizeP, length2 * sizeCE + sizeP, 1);
        GL11.glPopMatrix();
    }

    public static void drawCuboid(Tessellator tessellator, float sizeX, float sizeY, float sizeZ, float in)
    {
        tessellator.startDrawingQuads();

        tessellator.addVertexWithUV(-sizeX * in, -sizeY * in, -sizeZ, 0, 0);
        tessellator.addVertexWithUV(-sizeX * in, sizeY * in, -sizeZ, 0, 1);
        tessellator.addVertexWithUV(sizeX * in, sizeY * in, -sizeZ, 1, 1);
        tessellator.addVertexWithUV(sizeX * in, -sizeY * in, -sizeZ, 1, 0);

        tessellator.addVertexWithUV(-sizeX * in, -sizeY * in, sizeZ, 0, 0);
        tessellator.addVertexWithUV(sizeX * in, -sizeY * in, sizeZ, 1, 0);
        tessellator.addVertexWithUV(sizeX * in, sizeY * in, sizeZ, 1, 1);
        tessellator.addVertexWithUV(-sizeX * in, sizeY * in, sizeZ, 0, 1);

        tessellator.addVertexWithUV(-sizeX, -sizeY * in, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(-sizeX, -sizeY * in, sizeZ * in, 0, 1);
        tessellator.addVertexWithUV(-sizeX, sizeY * in, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(-sizeX, sizeY * in, -sizeZ * in, 1, 0);

        tessellator.addVertexWithUV(sizeX, -sizeY * in, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(sizeX, sizeY * in, -sizeZ * in, 0, 1);
        tessellator.addVertexWithUV(sizeX, sizeY * in, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(sizeX, -sizeY * in, sizeZ * in, 1, 0);

        tessellator.addVertexWithUV(-sizeX * in, sizeY, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(-sizeX * in, sizeY, sizeZ * in, 0, 1);
        tessellator.addVertexWithUV(sizeX * in, sizeY, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(sizeX * in, sizeY, -sizeZ * in, 1, 0);

        tessellator.addVertexWithUV(-sizeX * in, -sizeY, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(sizeX * in, -sizeY, -sizeZ * in, 1, 0);
        tessellator.addVertexWithUV(sizeX * in, -sizeY, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(-sizeX * in, -sizeY, sizeZ * in, 0, 1);

        tessellator.draw();
    }

    public static void drawLineCuboid(Tessellator tessellator, float sizeX, float sizeY, float sizeZ, float in)
    {
        tessellator.startDrawing(GL11.GL_LINE_STRIP);

        tessellator.addVertex(-sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, -sizeY * in, -sizeZ);

        tessellator.addVertex(-sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(-sizeX * in, -sizeY * in, sizeZ);

        tessellator.draw();

        tessellator.startDrawing(GL11.GL_LINES);

        tessellator.addVertex(-sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, sizeZ);

        tessellator.addVertex(sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, sizeZ);

        tessellator.addVertex(sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, sizeZ);

        tessellator.draw();
    }
}
