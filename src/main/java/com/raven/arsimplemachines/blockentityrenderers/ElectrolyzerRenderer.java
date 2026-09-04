package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.ElectrolyzerControllerBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

public class ElectrolyzerRenderer implements BlockEntityRenderer<ElectrolyzerControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/block/electrolyzer.png"
            );

    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "models/block/obj/electrolyzer_multiblock.obj"
            );
            model = new WavefrontObject(objPath);

        } catch (ModelFormatException e) {
            System.out.println("✗ FAILED to load electrolyzer.obj");
            throw new RuntimeException(e);

        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR loading electrolyzer.obj");
            throw new RuntimeException(e);
        }
    }

    public ElectrolyzerRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ElectrolyzerControllerBlockEntity be, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();
        boolean isFormed = state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!isFormed) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();

        // Same transform as Chemical Reactor
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        poseStack.translate(-0.5, 0.0, -0.5);

        // Only one mesh for now
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);
        if (be.renderData.running) {
            renderLightning(poseStack, buffer);
        }
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(ElectrolyzerControllerBlockEntity be) {
        return true;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ElectrolyzerControllerBlockEntity be) {
        var pos = be.getBlockPos();

        return new net.minecraft.world.phys.AABB(
                pos.getX() - 2,
                pos.getY() - 1,
                pos.getZ() - 2,
                pos.getX() + 3,
                pos.getY() + 3,
                pos.getZ() + 3
        );
    }
    private static final RenderType LIGHTNING_TYPE = RenderType.lightning();

    private void renderLightning(PoseStack poseStack, MultiBufferSource buffer) {

        float time = (System.currentTimeMillis() % 200) / 200f;

        poseStack.pushPose();

        // Position above the electrolyzer top
        poseStack.translate(1.5, 1.6, 0.5);

        // Slow rotation for visual interest
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 360));

        VertexConsumer vc = buffer.getBuffer(LIGHTNING_TYPE);
        Matrix4f mat = poseStack.last().pose();

        float x = 0;
        float y = 0;
        float z = 0;

        for (int i = 0; i < 6; i++) {

            float nx = x + (randomOffset() * 0.18f);
            float ny = y + 0.07f;
            float nz = z + (randomOffset() * 0.18f);

            float t = 0.06f; // thickness
            float ox = (randomOffset() * t);
            float oz = (randomOffset() * t);

            // TRIANGLE 1
            vc.addVertex(mat, x + ox, y, z + oz)
                    .setColor(1f, 1f, 1f, 0.9f)
                    .setUv(0, 0)
                    .setLight(0xF000F0)
                    .setOverlay(0);

            vc.addVertex(mat, nx + ox, ny, nz + oz)
                    .setColor(1f, 1f, 1f, 0.9f)
                    .setUv(0, 0)
                    .setLight(0xF000F0)
                    .setOverlay(0);

            vc.addVertex(mat, nx - ox, ny, nz - oz)
                    .setColor(1f, 1f, 1f, 0.9f)
                    .setUv(0, 0)
                    .setLight(0xF000F0)
                    .setOverlay(0);

            // TRIANGLE 2
            vc.addVertex(mat, x + ox, y, z + oz)
                    .setColor(1f, 1f, 1f, 0.9f)
                    .setUv(0, 0)
                    .setLight(0xF000F0)
                    .setOverlay(0);

            vc.addVertex(mat, nx - ox, ny, nz - oz)
                    .setColor(1f, 1f, 1f, 0.9f)
                    .setUv(0, 0)
                    .setLight(0xF000F0)
                    .setOverlay(0);

            vc.addVertex(mat, x - ox, y, z - oz)
                    .setColor(1f, 1f, 1f, 0.9f)
                    .setUv(0, 0)
                    .setLight(0xF000F0)
                    .setOverlay(0);

            x = nx;
            y = ny;
            z = nz;
        }

        poseStack.popPose();
    }

    private float randomOffset() {
        return (float)(Math.random() * 2 - 1);
    }

}
