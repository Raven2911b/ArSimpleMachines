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

        // -----------------------------
        // ANIMATED TRAY
        // -----------------------------
        poseStack.pushPose();

        float progress = 0f;
        if (be.recipeRunning && be.recipeMaxProgress > 0) {
            progress = (float) be.recipeProgress / (float) be.recipeMaxProgress;
        }
        float travel = 2.5f;

        poseStack.translate(0.0f, 0.0f, progress * travel);
        model.renderPart("Tray_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // -----------------------------
        // PROCESS A — STAMP ANIMATION
        // -----------------------------
        poseStack.pushPose();

        // When tray reaches the stamp zone (40% → 60%)
        float stamp = 0f;

        if (progress >= 0.18 && progress <= 0.40f) {
            // Normalize to 0 → 1 inside the window
            float t = (progress - 0.18f) / 0.15f;

            // Down then up using a sine curve
            stamp = (float) Math.sin(t * Math.PI);
        }

        // Stamp travel distance (Y axis)
        float stampTravel = -0.30f;   // negative = downward

        poseStack.translate(0.0f, stamp * stampTravel, 0.0f);

        model.renderPart("ProcessA_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();


        // -----------------------------
        // PROCESS B — PEN ANIMATION
        // -----------------------------
        poseStack.pushPose();

        float penY = 0f;     // vertical movement
        float penX = 0f;     // horizontal movement (draw X)
        float penZ = 0f;     // horizontal movement (draw X)

        // Same travel distance as ProcessA
        float penTravel = -0.30f;

        // -----------------------------
        // Phase 1 — Move down (0.45 → 0.55)
        // -----------------------------
        if (progress >= 0.45f && progress <= 0.55f) {
            float t = (progress - 0.45f) / 0.10f;
            penY = (float) Math.sin(t * Math.PI) * penTravel;
        }

        // -----------------------------
        // Phase 2 — Stay down + draw X (0.55 → 0.70)
        // -----------------------------
        if (progress >= 0.55f && progress <= 0.70f) {
            penY = penTravel;   // stay fully down

            float t = (progress - 0.55f) / 0.15f;

            // Draw an X: diagonal motion
            penX = (float) Math.sin(t * Math.PI * 2.0f) * 0.10f;  // left-right
            penZ = (float) Math.sin(t * Math.PI * 2.0f + Math.PI) * 0.10f; // front-back
        }

        // -----------------------------
        // Phase 3 — Move up (0.70 → 0.80)
        // -----------------------------
        if (progress >= 0.70f && progress <= 0.80f) {
            float t = (progress - 0.70f) / 0.10f;
            penY = penTravel * (1.0f - (float) Math.sin(t * Math.PI));
        }

        // Apply transforms
        poseStack.translate(penX, penY, penZ);

        model.renderPart("ProcessB_Mesh", poseStack, vc, light, overlay);
        poseStack.popPose();

        // -----------------------------
        // PROCESS C — GAVEL ARM SWING
        // -----------------------------
        poseStack.pushPose();

        float angle = 0f;
        float maxAngle = -90f;

        // Start swing only once
        if (!be.processCSwingStarted) {
            if (progress >= 0.80f && progress <= 0.86f) {
                be.processCSwingStarted = true;
            }
        }

        if (be.processCSwingStarted && !be.processCSwingFinished) {

            // Down (full 0.06 window)
            if (progress >= 0.80f && progress <= 0.86f) {
                float t = (progress - 0.80f) / 0.06f;
                angle = (float) Math.sin(t * Math.PI) * maxAngle;
            }

            // Hold
            else if (progress > 0.86f && progress <= 0.90f) {
                angle = maxAngle;
            }

            // Up (full 0.06 window)
            else if (progress > 0.90f && progress <= 0.96f) {
                float t = (progress - 0.90f) / 0.06f;
                angle = maxAngle * (1.0f - (float) Math.sin(t * Math.PI));
            }

            // Finished
            else if (progress > 0.96f) {
                be.processCSwingFinished = true;
                angle = 0f;
            }
        }

        // REAL hinge pivot from OBJ
        float px = 1.504f;
        float py = 1.431f;
        float pz = 2.91f;

        // Rotate around hinge
        poseStack.translate(px, py, pz);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-angle));
        poseStack.translate(-px, -py, -pz);

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
