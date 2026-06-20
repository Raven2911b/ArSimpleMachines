package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.RollingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RollingScreen extends AbstractContainerScreen<RollingMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/generic_menu.png");

    public RollingScreen(RollingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }
    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // Title (already positioned by titleLabelX/Y)
        gfx.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0x404040, false);

        // Slot labels
        gfx.drawString(this.font, "Input", 44, 25, 0xFFFFFF, false);
        gfx.drawString(this.font, "Output", 116, 25, 0xFFFFFF, false);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        // Draw background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
// -------------------------
// SLOT FRAMES
// -------------------------
        int slotU = 177;   // same UV as lathe
        int slotV = 0;

// Input slot frame
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 44, topPos + 35,   // screen position
                slotU, slotV,                // UV in PNG
                18, 18                       // size
        );

// Output slot frame
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 116, topPos + 35,
                slotU, slotV,
                18, 18
        );

        // -------------------------
// PROGRESS BAR FRAME
// -------------------------
        int barW = 100;
        int barH = 10;

        int barX = leftPos + 40;
        int barY = topPos + 60;

        int frameColor = 0xFF555555;

// top border
        gfx.fill(barX, barY, barX + barW, barY + 1, frameColor);
// bottom border
        gfx.fill(barX, barY + barH - 1, barX + barW, barY + barH, frameColor);
// left border
        gfx.fill(barX, barY, barX + 1, barY + barH, frameColor);
// right border
        gfx.fill(barX + barW - 1, barY, barX + barW, barY + barH, frameColor);

// -------------------------
// PROGRESS BAR FILL
// -------------------------
        int innerW = barW - 2;
        int progress = menu.getProgressScaled(innerW);

        gfx.fill(
                barX + 1,
                barY + 1,
                barX + 1 + progress,
                barY + barH - 1,
                0xFF00FF00   // green fill
        );

        // -------------------------
// POWER BAR
// -------------------------
        int power = menu.getPowerStored();
        int maxPower = menu.getMaxPower();
        int fullHeight = 48;

// Frame
        gfx.fill(leftPos + 8, topPos + 20, leftPos + 16, topPos + 21, 0xFF555555); // top
        gfx.fill(leftPos + 8, topPos + 20 + 47, leftPos + 16, topPos + 20 + 48, 0xFF555555); // bottom
        gfx.fill(leftPos + 8, topPos + 20, leftPos + 9, topPos + 20 + 48, 0xFF555555); // left
        gfx.fill(leftPos + 15, topPos + 20, leftPos + 16, topPos + 20 + 48, 0xFF555555); // right

        if (maxPower > 0) {
            int barHeight = (power * fullHeight) / maxPower;
            if (barHeight < 0) barHeight = 0;
            if (barHeight > fullHeight) barHeight = fullHeight;

            int yOffset = fullHeight - barHeight;

            gfx.fill(
                    leftPos + 9,
                    topPos + 21 + yOffset,
                    leftPos + 15,
                    topPos + 20 + fullHeight - 1,
                    0xFF00AAFF
            );
        }
// -------------------------
// FLUID BAR
// -------------------------
        int fluid = menu.getFluidAmount();
        int fluidMax = menu.getFluidCapacity();
        int fluidFull = 48;  // same height as power bar

// Frame
        gfx.fill(leftPos + 20, topPos + 20, leftPos + 28, topPos + 21, 0xFF555555); // top
        gfx.fill(leftPos + 20, topPos + 20 + 47, leftPos + 28, topPos + 20 + 48, 0xFF555555); // bottom
        gfx.fill(leftPos + 20, topPos + 20, leftPos + 21, topPos + 20 + 48, 0xFF555555); // left
        gfx.fill(leftPos + 27, topPos + 20, leftPos + 28, topPos + 20 + 48, 0xFF555555); // right

        if (fluidMax > 0) {
            int barHeight = (fluid * fluidFull) / fluidMax;
            barHeight = Math.max(0, Math.min(barHeight, fluidFull));
            int yOffset = fluidFull - barHeight;

            // Blue-ish fluid color
            gfx.fill(
                    leftPos + 21,
                    topPos + 21 + yOffset,
                    leftPos + 27,
                    topPos + 20 + fluidFull - 1,
                    0xFF00FFC8
            );
        }

    }
    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

}
