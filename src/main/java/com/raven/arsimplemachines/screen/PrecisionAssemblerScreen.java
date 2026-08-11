package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.PrecisionAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PrecisionAssemblerScreen extends AbstractContainerScreen<PrecisionAssemblerMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/generic_menu.png");

    public PrecisionAssemblerScreen(PrecisionAssemblerMenu menu, Inventory inv, Component title) {
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
        int maxEnergy = menu.getMaxPower();

        var be = menu.getBlockEntity();
        boolean noInput = (be == null || !be.getClientHasInputItems());

        boolean noEnergy = false;
        if (be != null) {
            if (be.recipeRunning && be.currentRecipe != null) {
                noEnergy = (energy < be.currentRecipe.getEnergyPerTick());
            } else if (be.getClientHasInputItems()) {
                noEnergy = (energy < 80);
            }
        }

        // -------------------------
        // POWER BAR
        // -------------------------
        gfx.drawString(this.font, "P", leftPos + 12, topPos + 5, 0x404040, false);

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
        // PROGRESS BAR
        // -------------------------
        int barX = leftPos + 40;
        int barY = topPos + 60;
        int barW = 100;
        int barH = 8;

        int frameColor = 0xFF555555;
        int fillColor = 0xFF55FF55;

        if (noInput) fillColor = 0xFF777777;
        else if (noEnergy) fillColor = 0xFFFF5555;

        gfx.fill(barX, barY, barX + barW, barY + 1, frameColor);
        gfx.fill(barX, barY + barH - 1, barX + barW, barY + barH, frameColor);
        gfx.fill(barX, barY, barX + 1, barY + barH, frameColor);
        gfx.fill(barX + barW - 1, barY, barX + barW, barY + barH, frameColor);

        int innerW = barW - 2;
        int progress = menu.getProgressScaled(innerW);

        if (be == null || !be.recipeRunning || noEnergy) {
            progress = 0;
        }

        gfx.fill(
                barX + 1,
                barY + 1,
                barX + 1 + progress,
                barY + barH - 1,
                fillColor
        );

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg;
        int color;

        if (be == null) {
            msg = "Idle";
            color = 0x404040;
        } else if (be.recipeRunning) {
            msg = "Assembling...";
            color = 0xFFAA00;
        } else {
            if (noInput) {
                msg = "Idle";
                color = 0x404040;
            } else if (noEnergy) {
                msg = "Not enough energy";
                color = 0xFF5555;
            } else {
                msg = "No matching recipe";
                color = 0xAA0000;
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
