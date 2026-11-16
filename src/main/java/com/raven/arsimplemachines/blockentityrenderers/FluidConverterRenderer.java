package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.FluidConverterBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FluidConverterRenderer implements BlockEntityRenderer<FluidConverterBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/lathe.png");

    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID, "models/block/obj/fluid_converter.obj"
            );
            model = new WavefrontObject(objPath);

        } catch (ModelFormatException e) {
            System.out.println("✗ FAILED to load fluid_converter.obj");
            throw new RuntimeException(e);

        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR loading fluid_converter.obj");
            throw new RuntimeException(e);
        }
    }

    public FluidConverterRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(FluidConverterBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();

        // ARLib-native formed check
        boolean isFormed = state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!isFormed) return;


        // ARLib-native facing property
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();

        switch (facing) {
            case NORTH -> {
                poseStack.translate(3.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.5, -1.0, -2.5);
            }
            case EAST -> {
                poseStack.translate(-0.5, 0.0, 3.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(0));
                poseStack.translate(0.5, -1.0, -2.5);
            }
            case SOUTH -> {
                poseStack.translate(-0.5, 0.0, -1.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(1.5, -1.0, -0.5);
            }
            case WEST -> {
                poseStack.translate(-0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-1.5, -1.0, 0.5);
            }
        }

        model.renderPart("Cube_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Power_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Control_Mesh", poseStack, vc, light, overlay);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(FluidConverterBlockEntity be) {
        return true;
    }
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(FluidConverterBlockEntity be) {
        var pos = be.getBlockPos();

        // Covers your entire 4×2×? lathe footprint
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
