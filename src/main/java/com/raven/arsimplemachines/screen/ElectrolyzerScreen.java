package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.ElectrolyzerMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectrolyzerScreen extends AbstractContainerScreen<ElectrolyzerMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/generic_menu.png"
            );

    public ElectrolyzerScreen(ElectrolyzerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int labelY = topPos + 5;

        // Background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // ENERGY BAR
        // -------------------------
        gfx.drawString(this.font, "P", leftPos + 8, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 55, 176, 57, 8, 1);

        int energy = menu.getEnergyScaled(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 8 + 1,
                topPos + 16 + (38 - energy) + 1,
                0, 171,
                6, energy);

        // -------------------------
        // INPUT TANK
        // -------------------------
        gfx.drawString(this.font, "IN", leftPos + 40, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 40, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 40, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 40, topPos + 55, 176, 57, 8, 1);

        int inputHeight = menu.getInputScaled(38);
        int inputColor = 0xFF808080;
        gfx.fill(
                leftPos + 40 + 1,
                topPos + 16 + (38 - inputHeight) + 1,
                leftPos + 40 + 1 + 6,
                topPos + 16 + 1 + 38,
                inputColor
        );

        // -------------------------
        // OUTPUT A
        // -------------------------
        gfx.drawString(this.font, "A", leftPos + 90, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 90, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 90, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 90, topPos + 55, 176, 57, 8, 1);

        int outAHeight = menu.getOutputAScaled(38);
        int outAColor = 0xFF00FFFF;
        gfx.fill(
                leftPos + 90 + 1,
                topPos + 16 + (38 - outAHeight) + 1,
                leftPos + 90 + 1 + 6,
                topPos + 16 + 1 + 38,
                outAColor
        );

        // -------------------------
        // OUTPUT B
        // -------------------------
        gfx.drawString(this.font, "B", leftPos + 120, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 120, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 120, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 120, topPos + 55, 176, 57, 8, 1);

        int outBHeight = menu.getOutputBScaled(38);
        int outBColor = 0xFF66A3FF;
        gfx.fill(
                leftPos + 120 + 1,
                topPos + 16 + (38 - outBHeight) + 1,
                leftPos + 120 + 1 + 6,
                topPos + 16 + 1 + 38,
                outBColor
        );

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg = menu.getStatusMessage();
        if (!msg.isEmpty()) {
            int color = 0xC0C0C0;

            switch (msg) {
                case "Not enough energy" -> color = 0xFF5555;
                case "Missing input fluid" -> color = 0xFF5555;
                case "Output A full", "Output B full" -> color = 0xFFFF55;
                case "Processing..." -> color = 0x228B22;
                case "Idle" -> color = 0x404040;
            }

            gfx.drawString(
                    this.font,
                    msg,
                    leftPos + 8,
                    topPos + 60,
                    color,
                    false
            );
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // -------------------------
        // TOOLTIP HITBOXES
        // -------------------------

        // INPUT
        int inX = leftPos + 40 + 1;
        int inY = topPos + 16 + 1;
        if (mouseX >= inX && mouseX <= inX + 6 &&
                mouseY >= inY && mouseY <= inY + 38) {

            int amt = menu.getInputAmount();
            int cap = menu.getInputCapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Input: " + amt + " / " + cap + " mB"),
                    mouseX, mouseY
            );
        }

        // OUTPUT A
        int outAX = leftPos + 90 + 1;
        int outAY = topPos + 16 + 1;
        if (mouseX >= outAX && mouseX <= outAX + 6 &&
                mouseY >= outAY && mouseY <= outAY + 38) {

            int amt = menu.getOutputAAmount();
            int cap = menu.getOutputACapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Output A: " + amt + " / " + cap + " mB"),
                    mouseX, mouseY
            );
        }

        // OUTPUT B
        int outBX = leftPos + 120 + 1;
        int outBY = topPos + 16 + 1;
        if (mouseX >= outBX && mouseX <= outBX + 6 &&
                mouseY >= outBY && mouseY <= outBY + 38) {

            int amt = menu.getOutputBAmount();
            int cap = menu.getOutputBCapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Output B: " + amt + " / " + cap + " mB"),
                    mouseX, mouseY
            );
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }
}
