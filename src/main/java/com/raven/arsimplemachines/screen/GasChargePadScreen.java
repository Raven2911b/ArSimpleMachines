package com.raven.arsimplemachines.screen;

import com.raven.arsimplemachines.menu.GasChargePadMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GasChargePadScreen extends AbstractContainerScreen<GasChargePadMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/gas_charge_pad.png");
    private static final ResourceLocation OXYGEN_BAR =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/oxygen_flow.png");

    private static final ResourceLocation HYDROGEN_BAR =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/hydrogen_flow.png");

    private static final ResourceLocation NITROGEN_BAR =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/nitrogen_flow.png");

    public GasChargePadScreen(GasChargePadMenu menu, Inventory inv, Component title) {
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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, "Gas Stored:", 10, 10, 0xFFFFFF, false);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        String type = menu.getGasType();

        ResourceLocation barTexture = switch (type) {
            case "oxygen" -> OXYGEN_BAR;
            case "hydrogen" -> HYDROGEN_BAR;
            case "nitrogen" -> NITROGEN_BAR;
            default -> OXYGEN_BAR;
        };

        // Draw background

        gfx.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Draw gas bar background (16x60)
        gfx.blit(TEXTURE, leftPos + 32, topPos + 20, 176, 58, 16, 60);
// --- FIXED: use synced menu values ---
        int gas = this.menu.getGasStored();
        int maxGas = this.menu.getMaxGas();

// The oxygen_flow.png height
        int fullHeight = 52;

// Calculate bar height
        int barHeight = (gas * fullHeight) / maxGas;

// Clamp to avoid rounding gaps
        barHeight = Math.min(barHeight, fullHeight);

// Offset so the bar rises from the bottom
        int yOffset = fullHeight - barHeight;

// Draw the fill bar
        gfx.blit(
                barTexture,
                leftPos + 33,
                topPos + 21 + yOffset,
                0, yOffset,          // oxygen_flow.png starts at 0,0
                12, barHeight
        );


        // Draw the 18x18 icon
        gfx.blit(TEXTURE, leftPos + 55, topPos + 20, 176, 0, 18, 18);
        gfx.blit(TEXTURE, leftPos + 55, topPos + 50, 176, 0, 18, 18);
    }
    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        super.render(gfx, mouseX, mouseY, partialTicks);
        renderTooltip(gfx, mouseX, mouseY);

        // Tooltip for gas bar
        int barX = leftPos + 32;
        int barY = topPos + 20;
        int barW = 16;
        int barH = 60;

        if (mouseX >= barX && mouseX < barX + barW &&
                mouseY >= barY && mouseY < barY + barH) {

            int stored = menu.getGasStored();
            int max = menu.getMaxGas();

            String type = menu.getGasType();
            gfx.renderTooltip(font,
                    Component.literal(type + ": " + stored + " / " + max + " mB"),
                    mouseX, mouseY
            );

        }
    }

}
