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

        poseStack.translate(1.0, 0.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        switch (facing) {
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
            poseStack.translate(-1.0, 0.0, -1.0);}
            case WEST  -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-1.0, 0.0, 0.0);
            }
            case EAST  -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(0));
                poseStack.translate(0.0, 0.0, -1.0);}
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        // --- STATIC PARTS ---
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Coil_Mesh", poseStack, vc, light, overlay);

        // --- PLATE OUTPUT ---
        poseStack.pushPose();

// Move plate OUT of the rollers along Z axis
        poseStack.translate(0.0, 0.0, be.renderData.plateOffset);

// Only render plate once ingot has entered the rollers
        if (be.renderData.ingotOffset > 0f) {
            model.renderPart("Plate_Mesh", poseStack, vc, light, overlay);
        }

        poseStack.popPose();


        // --- INGOT FEED ---
        poseStack.pushPose();

// Move ingot along X axis
        poseStack.translate(0.0, 0.0, be.renderData.ingotOffset);

        if (be.renderData.ingotOffset < 2.4f) {
            model.renderPart("Ingot_Mesh", poseStack, vc, light, overlay);
        }

        poseStack.popPose();


        // --- ROLLERS (spin in place) ---
        poseStack.pushPose();

// Roller 1
        poseStack.pushPose();
        poseStack.translate(2.278259, 0.113660, 2.436339);
        poseStack.mulPose(Axis.XP.rotationDegrees(-roller));
        poseStack.translate(-2.278259, -0.113660, -2.436339);
        model.renderPart("Roller1_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

// Roller 2
        poseStack.pushPose();
        poseStack.translate(2.327889, -0.639297, 2.291945);
        poseStack.mulPose(Axis.XP.rotationDegrees(roller));
        poseStack.translate(-2.327889, 0.639297, -2.291945);
        model.renderPart("Roller2_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

// Roller 3
        poseStack.pushPose();
        poseStack.translate(2.87, -0.61, 2.96);
        poseStack.mulPose(Axis.XP.rotationDegrees(-roller));
        poseStack.translate(-2.87, 0.61, -2.96);
        model.renderPart("Roller3_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        poseStack.popPose();


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
