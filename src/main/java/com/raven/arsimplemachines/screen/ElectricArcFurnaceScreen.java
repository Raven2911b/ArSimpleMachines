package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.ElectricArcFurnaceMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectricArcFurnaceScreen extends AbstractContainerScreen<ElectricArcFurnaceMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/generic_menu.png");

    private static final ResourceLocation ARC_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/progressbars.png"
            );

    public ElectricArcFurnaceScreen(ElectricArcFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;

        // Hide vanilla labels
        this.inventoryLabelY = 9999;
        this.titleLabelY = 9999;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int energy = menu.getPowerStored();
        //int maxEnergy = menu.getMaxPower();

        var be = menu.getBlockEntity();
       // boolean hasInput = (be != null && be.hasAnyInputItems());
        boolean noInput = (be == null || !be.getClientHasInputItems());


        boolean noEnergy = false;
        if (be != null) {
            if (be.recipeRunning && be.currentRecipe != null) {
                // While running, compare against recipe’s per‑tick cost
                noEnergy = (energy < be.currentRecipe.getEnergyPerTick());
            }else if (be.getClientHasInputItems()) {
                noEnergy = (energy < 80);
            }

        }

        int maxEnergy = menu.getMaxPower();

        boolean lowEnergy = (maxEnergy > 0 && energy < (maxEnergy * 0.15));

        gfx.drawString(this.font, "ARC FURNACE", leftPos + 65, topPos + 5, 0x404040, false);

        // -------------------------
        // POWER BAR
        // -------------------------
        gfx.drawString(this.font, "ᴘᴡʀ", leftPos + 8, topPos + 6, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 11, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 11, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 11, topPos + 55, 176, 57, 8, 1);

        int scaledPower = menu.getPowerScaled(38);
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 12,
                topPos + 16 + (38 - scaledPower) + 1,
                0, 171,
                6, scaledPower
        );

        // -------------------------
        // PROGRESS BAR (animated)
        // -------------------------
        // Background frame (static)
        gfx.blit(ARC_PROGRESS_TEXTURE,
                leftPos + 70, topPos + 25,
                0, 66,
                42, 42
        );

        // Animated fill (bottom → top)
        int progress = menu.getProgressScaled(42);

        if (be == null || !be.recipeRunning || noEnergy) {
            progress = 0;
        }

        // How far up the fill should start
        int fillY = topPos + 25 + (42 - progress);

        // Which part of the PNG to sample
        int uvY = 66 + (42 - progress);

        gfx.blit(ARC_PROGRESS_TEXTURE,
                leftPos + 70, fillY,   // shifted upward
                42, uvY,               // UV shifted upward
                42, progress           // height grows upward
        );

        // -------------------------
        // LOW ENERGY PULSE EFFECT (POWER BAR)
        // -------------------------
        if (lowEnergy) {
            long tick = Minecraft.getInstance().level.getGameTime() % 20;

            // Pulse every 10 ticks (half-second)
            if (tick < 10) {
                gfx.fill(
                        leftPos + 12, topPos + 16,        // top-left of power bar
                        leftPos + 18, topPos + 56,        // bottom-right of power bar
                        0x55FF0000                        // translucent red overlay
                );
            }
        }

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg;
        int color;
        if (be == null) {
            msg = "Idle";
            color = 0x606060;        // neutral gray
        } else if (be.recipeRunning) {
            msg = "Arc active...";
            color = 0x228B22;        // forest green
        } else {
            if (noInput) {
                msg = "Idle";
                color = 0x606060;    // neutral gray
            } else if (noEnergy) {
                msg = "Not enough energy";
                color = 0xFF2222;    // strong red
            } else {
                msg = "No matching recipe";
                color = 0xCC4444;    // muted red
            }
        }

        gfx.drawString(this.font, msg, leftPos + 8, topPos + 75, color, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // -------------------------
        // POWER BAR TOOLTIP
        // -------------------------
        int px = leftPos + 11;
        int py = topPos + 16;
        int pw = 8;
        int ph = 40;

        if (mouseX >= px && mouseX < px + pw &&
                mouseY >= py && mouseY < py + ph) {

            int stored = menu.getPowerStored();
            int max = menu.getMaxPower();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Energy: " + stored + " / " + max + " FE"),
                    mouseX,
                    mouseY
            );
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // No labels
    }
}
