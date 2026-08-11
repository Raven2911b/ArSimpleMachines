package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.PrecisionAssemblerControllerBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class PrecisionAssemblerRenderer implements BlockEntityRenderer<PrecisionAssemblerControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/precision_assembler.png");

    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID, "models/block/obj/precision_assembler.obj"
            );
            model = new WavefrontObject(objPath);

        } catch (ModelFormatException e) {
            System.out.println("✗ FAILED to load precision_assembler.obj");
            throw new RuntimeException(e);

        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR loading precision_assembler.obj");
            throw new RuntimeException(e);
        }
    }

    public PrecisionAssemblerRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(PrecisionAssemblerControllerBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();

        boolean isFormed = state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!isFormed) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();

        // Base lift (same as Cutting Machine)
        poseStack.translate(0.0, 1.0, 0.0);

        // Facing rotation
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
        }

        // Per-facing translation correction (you will adjust these)
        switch (facing) {
            case NORTH -> poseStack.translate(0, -1.0, -1.0);
            case SOUTH -> poseStack.translate(-1.0, -1.0, 0.0);
            case WEST  -> poseStack.translate(0.0, -1.0, 0.0);
            case EAST  -> poseStack.translate(-1.0, -1.0, -1.0);
        }

        // -----------------------------
        // STATIC MESHES
        // -----------------------------
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Tray_Mesh", poseStack, vc, light, overlay);

        // -----------------------------
        // ANIMATED MESHES
        // -----------------------------
        // float progress = be.renderData.progress; // 0 → 100 or 0 → 1 depending on your BE

        // Process A
        poseStack.pushPose();
        poseStack.translate(0, 0, 0);
        model.renderPart("ProcessA_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // Process B
        poseStack.pushPose();
        poseStack.translate(0, 0, 0);
        model.renderPart("ProcessB_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // Process C
        poseStack.pushPose();
        poseStack.translate(0, 0, 0);
        model.renderPart("ProcessC_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(PrecisionAssemblerControllerBlockEntity be) {
        return true;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(PrecisionAssemblerControllerBlockEntity be) {
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
