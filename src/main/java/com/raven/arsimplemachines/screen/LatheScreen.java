package com.raven.arsimplemachines.screen;

import com.raven.arsimplemachines.menu.LatheMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LatheScreen extends AbstractContainerScreen<LatheMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_menu.png");

    public LatheScreen(LatheMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, "Lathe", 10, 10, 0xFFFFFF, false);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {

        // Draw background
        gfx.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        // -------------------------
        // SLOT FRAMES
        // -------------------------
        int slotU = 177;   // your UV coords
        int slotV = 0;

        // Input slot frame
        gfx.blit(
                TEXTURE,
                leftPos + 44, topPos + 35,   // screen position
                slotU, slotV,                // UV in PNG
                18, 18                       // size
        );

        // Output slot frame
        gfx.blit(
                TEXTURE,
                leftPos + 116, topPos + 35,
                slotU, slotV,
                18, 18
        );
        gfx.drawString(this.font, "Input", leftPos + 44, topPos + 25, 0xFFFFFF, false);
        gfx.drawString(this.font, "Output", leftPos + 116, topPos + 25, 0xFFFFFF, false);

        // gfx.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // -------------------------
        // POWER BAR
        // -------------------------
        int power = menu.getPowerStored();
        int maxPower = menu.getMaxPower();
        int fullHeight = 48;

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
        }// -------------------------
// PROGRESS BAR FRAME (clean rectangle)
// -------------------------
        int barW = 100;
        int barH = 10;

        int barX = leftPos + 40;
        int barY = topPos + 60;

// Frame color (dark gray)
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
// PROGRESS BAR FILL (normal: empty → full)
// -------------------------
        int innerW = barW - 2; // inside the frame
        int progress = menu.getProgressScaled(innerW);

        gfx.fill(
                barX + 1,
                barY + 1,
                barX + 1 + progress,
                barY + barH - 1,
                0xFF00FF00
        );


    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        super.render(gfx, mouseX, mouseY, partialTicks);
        renderTooltip(gfx, mouseX, mouseY);

        // Tooltip for progress bar
        int barX = leftPos + 76;
        int barY = topPos + 35;
        int barW = 24;
        int barH = 16;

        if (mouseX >= barX && mouseX < barX + barW &&
                mouseY >= barY && mouseY < barY + barH) {

            int p = menu.getProgress();
            int max = menu.getMaxProgress();

            gfx.renderTooltip(
                    font,
                    Component.literal("Progress: " + p + " / " + max),
                    mouseX, mouseY
            );
        }
    }
}
