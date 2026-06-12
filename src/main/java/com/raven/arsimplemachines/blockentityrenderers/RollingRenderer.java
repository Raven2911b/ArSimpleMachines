package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.RollingControllerBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class RollingRenderer implements BlockEntityRenderer<RollingControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/rollingmachine.png");

    static {
        try {
            System.out.println("ROLLING: Attempting to load OBJ...");

            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "models/block/obj/rollingmachine_multiblock.obj"
            );

            System.out.println("ROLLING: OBJ path = " + objPath);

            model = new WavefrontObject(objPath);

            if (model == null) {
                System.out.println("ROLLING: OBJ LOAD FAILED — model is null");
            } else {
                System.out.println("ROLLING: OBJ LOADED SUCCESSFULLY");
                System.out.println("ROLLING: GROUPS FOUND = " + model.groupObjects.keySet());
            }

        } catch (Exception e) {
            System.out.println("ROLLING: EXCEPTION DURING OBJ LOAD:");
            e.printStackTrace();
            model = null;
        }
    }



    public RollingRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(RollingControllerBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();
        if (!state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) return;

        float roller = be.renderData.rollerSpin;
        float press  = be.renderData.pressOffset;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.0, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.translate(-0.5, 0.0, -2.5);

        // --- STATIC PARTS ---
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Plate_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Coil_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Ingot_Mesh", poseStack, vc, light, overlay);

        // --- ROLLERS (spin together) ---
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(roller));
        model.renderPart("Roller1_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Roller2_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Roller3_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // --- PRESS (if exists) ---
//        poseStack.pushPose();
//        poseStack.translate(0, -press, 0);
//        model.renderPart("Press_Mesh", poseStack, vc, light, overlay); // remove if not in OBJ
//        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RollingControllerBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(RollingControllerBlockEntity be) {
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
