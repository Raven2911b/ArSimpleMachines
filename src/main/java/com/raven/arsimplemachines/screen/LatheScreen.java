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
        // POWER BAR
        // -------------------------
        int power = menu.getPowerStored();
        int maxPower = menu.getMaxPower();
        int fullHeight = 48;

        if (maxPower > 0) {
            int barHeight = (power * fullHeight) / maxPower;
            if (barHeight < 0) barHeight = 0;
            if (barHeight > fullHeight) barHeight = fullHeight;

            int yOffset = fullHeight - barHeight;

            gfx.fill(
                    leftPos + 8,
                    topPos + 136 + yOffset,
                    leftPos + 8 + 8,
                    topPos + 136 + fullHeight,
                    0xFF00AAFF
            );
        }

        // -------------------------
        // PROGRESS BAR
        // -------------------------
        int progress = menu.getProgressScaled(60);

        gfx.fill(
                leftPos + 60,
                topPos + 80,
                leftPos + 60 + progress,
                topPos + 80 + 10,
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
