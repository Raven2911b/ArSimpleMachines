package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.ChemicalReactorMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChemicalReactorScreen extends AbstractContainerScreen<ChemicalReactorMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/generic_menu.png"
            );

    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int labelY = topPos + 5;   // safely below title bar

        // Background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // ENERGY BAR A OUTLINE
        // -------------------------
        gfx.drawString(this.font, "P1", leftPos + 8, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 16,        176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 17,        176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 55,        176, 57, 8, 1);

        // ENERGY BAR A FILL (texture)
        int energyA = menu.getEnergyScaledA(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 8 + 1,
                topPos + 16 + (38 - energyA) + 1,
                0, 171,
                6, energyA);

        // -------------------------
        // ENERGY BAR B OUTLINE
        // -------------------------
        gfx.drawString(this.font, "P2", leftPos + 24, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 24, topPos + 16,        176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 24, topPos + 17,        176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 24, topPos + 55,        176, 57, 8, 1);

        // ENERGY BAR B FILL (texture)
        int energyB = menu.getEnergyScaledB(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 24 + 1,
                topPos + 16 + (38 - energyB) + 1,
                0, 171,
                6, energyB);

        // -------------------------
        // HYDROGEN BAR OUTLINE
        // -------------------------
        gfx.drawString(this.font, "T1", leftPos + 70, labelY, 0x404040, false);
        // TANK1 OUTLINE
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 55, 176, 57, 8, 1);

// TANK1 FILL
        int hydrogenHeight = menu.getHydrogenScaled(38);
        int hydrogenColor = 0xFF808080; // gray
        gfx.fill(
                leftPos + 70 + 1,
                topPos + 16 + (38 - hydrogenHeight) + 1,
                leftPos + 70 + 1 + 6,
                topPos + 16 + 1 + 38,
                hydrogenColor
        );


        // -------------------------
        // OXYGEN BAR OUTLINE
        // -------------------------
        gfx.drawString(this.font, "T2", leftPos + 100, labelY, 0x404040, false);
        // TANK2 OUTLINE
        gfx.blit(GUI_TEXTURE, leftPos + 100, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 100, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 100, topPos + 55, 176, 57, 8, 1);

// TANK2 FILL
        int oxygenHeight = menu.getOxygenScaled(38);
        int oxygenColor = 0xFF00FFFF; // cyan
        gfx.fill(
                leftPos + 100 + 1,
                topPos + 16 + (38 - oxygenHeight) + 1,
                leftPos + 100 + 1 + 6,
                topPos + 16 + 1 + 38,
                oxygenColor
        );


        // -------------------------
        // OUTPUT BAR OUTLINE
        // -------------------------
        gfx.drawString(this.font, "ᴏᴜᴛᴘᴜᴛ", leftPos + 130, labelY, 0x404040, false);
        // OUTPUT OUTLINE
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 55, 176, 57, 8, 1);

// OUTPUT FILL
        int outputHeight = menu.getOutputScaled(38);
        int outputColor = 0xFF66A3FF; // light blue
        gfx.fill(
                leftPos + 140 + 1,
                topPos + 16 + (38 - outputHeight) + 1,
                leftPos + 140 + 1 + 6,
                topPos + 16 + 1 + 38,
                outputColor
        );

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg = menu.getStatusMessage();
        if (!msg.isEmpty()) {
            int color = 0xC0C0C0;

            switch (msg) {
                case "Not enough energy":
                case "Missing hydrogen":
                case "Missing oxygen":
                    color = 0xFF5555;
                    break;
                case "Output tank full":
                    color = 0xFFFF55;
                    break;
                case "Processing...":
                    color = 0x228B22;
                    break;
                case "Idle":
                    color = 0x404040;
                    break;
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

        // TANK A tooltip
        int tankAX = leftPos + 70 + 1;
        int tankAY = topPos + 16 + 1;
        if (mouseX >= tankAX && mouseX <= tankAX + 6 &&
                mouseY >= tankAY && mouseY <= tankAY + 38) {

            int amt = menu.getHydrogenAmount();
            int cap = menu.getHydrogenCapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Tank1: " + amt + " / " + cap + " mB"),
                    mouseX, mouseY
            );
        }

        // TANK B tooltip
        int tankBX = leftPos + 100 + 1;
        int tankBY = topPos + 16 + 1;
        if (mouseX >= tankBX && mouseX <= tankBX + 6 &&
                mouseY >= tankBY && mouseY <= tankBY + 38) {

            int amt = menu.getOxygenAmount();
            int cap = menu.getOxygenCapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Tank2: " + amt + " / " + cap + " mB"),
                    mouseX, mouseY
            );
        }

        // OUTPUT tooltip
        int outX = leftPos + 140 + 1;
        int outY = topPos + 16 + 1;
        if (mouseX >= outX && mouseX <= outX + 6 &&
                mouseY >= outY && mouseY <= outY + 38) {

            int amt = menu.getOutputAmount();
            int cap = menu.getOutputCapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Output: " + amt + " / " + cap + " mB"),
                    mouseX, mouseY
            );
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
       // gfx.drawString(this.font, this.title, 8, 6, 0x404040, false);
        gfx.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }
}
