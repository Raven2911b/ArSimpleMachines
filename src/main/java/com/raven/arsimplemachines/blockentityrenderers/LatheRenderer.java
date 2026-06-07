package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.LatheControllerBlockEntity;
import com.raven.arsimplemachines.multiblock.MultiblockControllerBlock;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LatheRenderer implements BlockEntityRenderer<LatheControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/lathe.png");

    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID, "models/block/obj/lathe_multiblock.obj"
            );
            model = new WavefrontObject(objPath);

        } catch (ModelFormatException e) {
            System.out.println("✗ FAILED to load lathe_multiblock.obj");
            throw new RuntimeException(e);

        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR loading lathe_multiblock.obj");
            throw new RuntimeException(e);
        }
    }

    public LatheRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(LatheControllerBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();
        boolean isFormed = be.isFormed();

        if (!isFormed) return;

        boolean anim = be.renderData.running;

        float shaft = anim ? be.renderData.shaftRotation : 0f;
        float tool  = anim ? be.renderData.toolOffset    : 0f;
        float rod   = anim ? be.renderData.rodOffset     : 0f;


        Direction facing = state.getValue(MultiblockControllerBlock.FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.0, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.translate(-0.5, -0.875, -2.5);

        // Hull always renders
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);

        // Shaft
        poseStack.pushPose();
        poseStack.translate(0.35, 0.95, 0.0);
        poseStack.translate(0.0, 0.0, 0.75);
        poseStack.mulPose(Axis.ZP.rotationDegrees(shaft));
        poseStack.translate(0.0, 0.0, -0.75);
        model.renderPart("Shaft_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // Tool
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, -tool);
        model.renderPart("Tool_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // Rod
        poseStack.pushPose();
        poseStack.translate(0.35, 1.175, 0.0);
        poseStack.translate(0.0, 0.0, 0.75);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-rod));
        poseStack.translate(0.0, 0.0, -0.75);
        model.renderPart("Rod_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        poseStack.popPose();
    }


    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(LatheControllerBlockEntity be) {
        return true;
    }
}
