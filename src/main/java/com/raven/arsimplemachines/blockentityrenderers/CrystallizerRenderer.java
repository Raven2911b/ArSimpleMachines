package com.raven.arsimplemachines.blockentityrenderers;

import ARLib.obj.Static;
import ARLib.obj.WavefrontObject;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.CrystallizerControllerBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.function.Function;

public class CrystallizerRenderer implements BlockEntityRenderer<CrystallizerControllerBlockEntity> {

    private static WavefrontObject model;
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/crystallizer_multiblock.png");

    private static final ResourceLocation LIQUID_TEX =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/block/liquidmesh.png");

    public static final RenderType LIQUID_TINTED = RenderType.create(
            "liquid_tinted",
            DefaultVertexFormat.BLOCK,  // Use BLOCK format instead of NEW_ENTITY
            VertexFormat.Mode.TRIANGLES,
            1536,
            true,  // needsSort
            true,  // translucent
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LIQUID_TEX, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true)
    );



    static {
        try {
            ResourceLocation objPath = ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "models/block/obj/crystallizer_multiblock.obj"
            );

            model = new WavefrontObject(objPath);

        } catch (Exception e) {
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
        // MACHINE BODY
        // ============================================================
        poseStack.pushPose();

        poseStack.translate(2.0, 0.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        switch (facing) {
            case SOUTH -> { poseStack.mulPose(Axis.YP.rotationDegrees(270)); poseStack.translate(-1.0, 0.0, -3.0); }
            case WEST  -> { poseStack.mulPose(Axis.YP.rotationDegrees(180)); poseStack.translate(-2.0, 0.0, -1.0); }
            case EAST  -> { poseStack.mulPose(Axis.YP.rotationDegrees(0));   poseStack.translate(1.0, 0.0, -2.0); }
            case NORTH -> { poseStack.mulPose(Axis.YP.rotationDegrees(90)); }
        }

        model.renderPart("Hull_Mesh", poseStack, vc, light, overlay);
        model.renderPart("Rail_Mesh", poseStack, vc, light, overlay);

        poseStack.popPose();

        // ============================================================
        // LIQUID MESH + ITEM ICON
        // ============================================================
        poseStack.pushPose();
        VertexConsumer vc1 = buffer.getBuffer(LIQUID_TINTED);

        poseStack.translate(2.0, 0.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        switch (facing) {
            case SOUTH -> { poseStack.mulPose(Axis.YP.rotationDegrees(270)); poseStack.translate(-1.0, 0.0, -3.0); }
            case WEST  -> { poseStack.mulPose(Axis.YP.rotationDegrees(180)); poseStack.translate(-2.0, 0.0, -1.0); }
            case EAST  -> { poseStack.mulPose(Axis.YP.rotationDegrees(0));   poseStack.translate(1.0, 0.0, -2.0); }
            case NORTH -> { poseStack.mulPose(Axis.YP.rotationDegrees(90)); }
        }

        if (!be.renderData.running) {
            poseStack.translate(0.0, -1.0f, 0.0);
        } else {
            float progress = be.renderData.recipeProgressClient;

            float minY = -0.7f;
            float maxY = 0.0f;

            float bob;

            if (progress <= 0.15f) {
                // Phase 1: Rise (0% → 15%)
                float t = progress / 0.15f;   // 0 → 1
                bob = minY + t * (maxY - minY);

            } else if (progress <= 0.85f) {
                // Phase 2: Hold (15% → 85%)
                bob = maxY;

            } else {
                // Phase 3: Fall (85% → 100%)
                float t = (progress - 0.85f) / 0.15f;  // 0 → 1
                bob = maxY - t * (maxY - minY);
            }

            poseStack.translate(0.0, bob, 0.0);

        }

        model.renderPart("Liquid_Mesh", poseStack, vc1, light, overlay);

        // ============================================================
// FLOATING ITEM ICONS (3 copies of the same item)
// ============================================================

        ItemStack output = be.renderData.outputItem;
        if (!output.isEmpty()) {

            // Shared animation values
            float time = be.getLevel().getGameTime() + partialTicks;
            float bob = (float)Math.sin(time / 8.0f) * 0.05f;

            // ---------- ITEM 1 (left) ----------
            poseStack.pushPose();

            poseStack.translate(1.0f, 1.4f, 0.7f); // left position
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            poseStack.translate(0.0, bob, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.5f)); // slow spin

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    output,
                    ItemDisplayContext.GUI,
                    light,
                    overlay,
                    poseStack,
                    buffer,
                    be.getLevel(),
                    0
            );

            poseStack.popPose();


            // ---------- ITEM 2 (center) ----------
            poseStack.pushPose();

            poseStack.translate(1.0f, 1.4f, 1.5f); // center position
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            poseStack.translate(0.0, bob, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0f)); // your original speed

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    output,
                    ItemDisplayContext.GUI,
                    light,
                    overlay,
                    poseStack,
                    buffer,
                    be.getLevel(),
                    0
            );

            poseStack.popPose();


            // ---------- ITEM 3 (right) ----------
            poseStack.pushPose();

            poseStack.translate(1.0f, 1.4f, 2.3f); // right position
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            poseStack.translate(0.0, bob, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.5f)); // slightly faster

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    output,
                    ItemDisplayContext.GUI,
                    light,
                    overlay,
                    poseStack,
                    buffer,
                    be.getLevel(),
                    0
            );

            poseStack.popPose();
        }


        poseStack.popPose(); // CLOSE LIQUID TRANSFORM


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
