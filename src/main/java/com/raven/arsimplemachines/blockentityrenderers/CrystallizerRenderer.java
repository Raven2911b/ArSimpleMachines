package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.CrystallizerControllerBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class CrystallizerRenderer implements BlockEntityRenderer<CrystallizerControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/crystallizer_multiblock.png");

    static {
        try {
            System.out.println("CRYSTALLIZER: Attempting to load OBJ...");

            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "models/block/obj/crystallizer_multiblock.obj"
            );

            System.out.println("CRYSTALLIZER: OBJ path = " + objPath);

            model = new WavefrontObject(objPath);

            if (model == null) {
                System.out.println("CRYSTALLIZER: OBJ LOAD FAILED — model is null");
            } else {
                System.out.println("CRYSTALLIZER: OBJ LOADED SUCCESSFULLY");
                System.out.println("CRYSTALLIZER: GROUPS FOUND = " + model.groupObjects.keySet());
            }

        } catch (Exception e) {
            System.out.println("CRYSTALLIZER: EXCEPTION DURING OBJ LOAD:");
            e.printStackTrace();
            model = null;
        }
    }

    public CrystallizerRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CrystallizerControllerBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();
        if (!state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        // ============================================================
        //  MACHINE BODY (Hull + Rails)
        // ============================================================
        poseStack.pushPose();

        poseStack.translate(2.0, 0.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        switch (facing) {
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(-1.0, 0.0, -3.0);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-2.0, 0.0, -1.0);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(0));
                poseStack.translate(1.0, 0.0, -2.0);
            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                // no extra translate
            }
        }

        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Rail_Mesh", poseStack, vc, light, overlay);

        poseStack.popPose();


        // ============================================================
        //  LIQUID MESH (SAME BASE TRANSFORM AS HULL + Y ANIMATION)
        // ============================================================
        poseStack.pushPose();

        // same base transform as hull
        poseStack.translate(2.0, 0.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        switch (facing) {
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(-1.0, 0.0, -3.0);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-2.0, 0.0, -1.0);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(0));
                poseStack.translate(1.0, 0.0, -2.0);
            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                // no extra translate
            }
        }

        // now only move it vertically + hide/animate
        if (!be.renderData.running) {
            poseStack.translate(0.0, -1.0f, 0.0);
        } else {
            float progress = be.renderData.recipeProgressClient; // 0 → 1

            float minY = -0.7f;
            float maxY = 0.0f;

            float bob;
            if (progress <= 0.5f) {
                float t = progress / 0.5f;
                bob = minY + t * (maxY - minY);
            } else {
                float t = (progress - 0.5f) / 0.5f;
                bob = maxY - t * (maxY - minY);
            }

            poseStack.translate(0.0, bob, 0.0);
        }

        model.renderPart("Liquid_Mesh", poseStack, vc, light, overlay);

        poseStack.popPose();
    }


    @Override
    public boolean shouldRenderOffScreen(CrystallizerControllerBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(CrystallizerControllerBlockEntity be) {
        var pos = be.getBlockPos();
        return new AABB(
                pos.getX() - 2,
                pos.getY() - 1,
                pos.getZ() - 2,
                pos.getX() + 3,
                pos.getY() + 3,
                pos.getZ() + 3
        );
    }
}
