package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.CuttingMachineMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CuttingMachineScreen extends AbstractContainerScreen<CuttingMachineMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/generic_menu.png"
            );

    public CuttingMachineScreen(CuttingMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = 9999; // hide inventory label
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // Suppress default labels
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int labelY = topPos + 5;

        // BACKGROUND
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // POWER BAR
        // -------------------------
        gfx.drawString(this.font, "ᴘᴡʀ", leftPos + 8, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 11,  topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 11,  topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 11,  topPos + 55, 176, 57, 8, 1);

        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();
        int scaled = menu.getPowerScaled(38);

        gfx.blit(GUI_TEXTURE,
                leftPos + 11 + 1,
                topPos + 16 + (38 - scaled) + 1,
                0, 171,
                6, scaled);

        // -------------------------
        // INPUT CHECK
        // -------------------------
        boolean noInput = menu.getSlot(0).getItem().isEmpty();
        boolean noEnergy = (energy <= 20);

        // -------------------------
        // SLOT FRAMES
        // -------------------------
        int slotU = 177;
        int slotV = 0;

        gfx.blit(GUI_TEXTURE, leftPos + 44, topPos + 25, slotU, slotV, 18, 18);
        gfx.drawString(this.font, "IN", leftPos + 47, topPos + 15, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 116, topPos + 25, slotU, slotV, 18, 18);
        gfx.drawString(this.font, "OUT", leftPos + 116, topPos + 15, 0x404040, false);

        // -------------------------
        // PROGRESS BAR
        // -------------------------
        int barX = leftPos + 40;
        int barY = topPos + 60;
        int barW = 100;
        int barH = 8;

        int frameColor = 0xFF555555;

        // Default fill color (green)
        int fillColor = 0xFF55FF55;

        // Override fill color if no energy
        if (noEnergy) {
            fillColor = 0xFFFF5555; // red
        }

        // Override fill color if no input
        if (noInput) {
            fillColor = 0xFF777777; // gray
        }

        // Frame
        gfx.fill(barX, barY, barX + barW, barY + 1, frameColor);
        gfx.fill(barX, barY + barH - 1, barX + barW, barY + barH, frameColor);
        gfx.fill(barX, barY, barX + 1, barY + barH, frameColor);
        gfx.fill(barX + barW - 1, barY, barX + barW, barY + barH, frameColor);

        int innerW = barW - 2;
        int progress = menu.getProgressScaled(innerW);

        // Freeze progress if:
        // - no input
        // - no energy
        // - complete
        if (noInput || noEnergy || menu.getProgress() >= menu.getMaxProgress()) {
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

        if (noInput) {
            msg = "Idle";
            color = 0x404040;
        }
        else if (noEnergy) {
            msg = "Not enough energy";
            color = 0xFF5555;
        }
        else if (menu.getProgress() >= menu.getMaxProgress()) {
            msg = "Complete";
            color = 0xC0C0C0;
        }
        else {
            msg = "Cutting...";
            color = 0x228B22;
        }

        gfx.drawString(this.font, msg, leftPos + 8, topPos + 75, color, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}
