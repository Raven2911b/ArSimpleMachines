package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.ModelFormatException;
import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.ChemicalReactorControllerBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ChemicalReactorRenderer implements BlockEntityRenderer<ChemicalReactorControllerBlockEntity> {

    private static WavefrontObject model;

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/block/chemicalreactor.png"
            );

    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "models/block/obj/chemical_reactor.obj"
            );
            model = new WavefrontObject(objPath);

        } catch (ModelFormatException e) {
            System.out.println("✗ FAILED to load chemical_reactor.obj");
            throw new RuntimeException(e);

        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR loading chemical_reactor.obj");
            throw new RuntimeException(e);
        }
    }

    public ChemicalReactorRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ChemicalReactorControllerBlockEntity be, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int light, int overlay) {

        if (be == null || be.getLevel() == null) return;

        BlockState state = be.getBlockState();
        boolean isFormed = state.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!isFormed) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VertexConsumer vc = buffer.getBuffer(Static.ENTITY_SOLID_TRIANGLES.apply(TEX));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(270));
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.translate(1.5, -1.00, -0.5);

        // Only existing part
        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalReactorControllerBlockEntity be) {
        return true;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ChemicalReactorControllerBlockEntity be) {
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
