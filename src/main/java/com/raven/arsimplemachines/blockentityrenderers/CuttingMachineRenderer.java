package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.CuttingMachineControllerBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CuttingMachineRenderer implements BlockEntityRenderer<CuttingMachineControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/cuttingmachine.png");

    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID, "models/block/obj/cuttingmachine_multiblock.obj"
            );
            model = new WavefrontObject(objPath);

        } catch (ModelFormatException e) {
            System.out.println("✗ FAILED to load cuttingmachine_multiblock.obj");
            throw new RuntimeException(e);

        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR loading cuttingmachine_multiblock.obj");
            throw new RuntimeException(e);
        }
    }

    public CuttingMachineRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CuttingMachineControllerBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();

        boolean isFormed = state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!isFormed) return;

        boolean anim = be.renderData.running;
        float saw = anim ? be.renderData.sawRotation : 0f;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();


// Base lift
        poseStack.translate(0.0, 1.0, 0.0);

// Facing rotation
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
        }

// Per‑facing translation correction
        switch (facing) {
            case NORTH -> poseStack.translate(0, -1.0, -2.0);          // perfect
            case SOUTH -> poseStack.translate(-1.0, -1.0, -1.0);        // +1X, +1Z
            case WEST  -> poseStack.translate(0.0, -1.0, -1.0);        // +1X
            case EAST  -> poseStack.translate(-1.0, -1.0, -2.0);        // +1Z
        }

        // Hull always renders
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);

        // Saw blade rotation
        poseStack.pushPose();

// Move to true saw center
        poseStack.translate(1.0000, 1.0000, 1.5171);

// Rotate based on facing
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(saw));
            case SOUTH -> poseStack.mulPose(Axis.XN.rotationDegrees(saw));
            case EAST  -> poseStack.mulPose(Axis.XP.rotationDegrees(saw));
            case WEST  -> poseStack.mulPose(Axis.XN.rotationDegrees(saw));
        }

// Move back
        poseStack.translate(-1.0000, -1.0000, -1.5171);

        model.renderPart("Saw_Mesh", poseStack, vc, light, overlay);

        poseStack.popPose();



        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(CuttingMachineControllerBlockEntity be) {
        return true;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(CuttingMachineControllerBlockEntity be) {
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
}
