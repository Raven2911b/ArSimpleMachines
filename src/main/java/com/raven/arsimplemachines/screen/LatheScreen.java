package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.LatheMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LatheScreen extends AbstractContainerScreen<LatheMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/generic_menu.png");

    private static final ResourceLocation LATHE_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/progressbars.png");

    public LatheScreen(LatheMenu menu, Inventory inv, Component title) {
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

        gfx.drawString(this.font, "ʟᴀᴛʜᴇ", leftPos + 8, labelY, 0x404040, false);
        // -------------------------
        // POWER BAR
        // -------------------------
        gfx.drawString(this.font, "ᴘᴡʀ", leftPos + 10, labelY+9, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 14,  topPos + 23, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 14,  topPos + 24, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 14,  topPos + 62, 176, 57, 8, 1);

        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();
        int scaled = menu.getPowerScaled(38);

        gfx.blit(GUI_TEXTURE,
                leftPos + 14 + 1,
                topPos + 23 + (38 - scaled) + 1,
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

        gfx.blit(GUI_TEXTURE, leftPos + 44, topPos + 35, slotU, slotV, 18, 18);
        gfx.drawString(this.font, "ɪɴ", leftPos + 47, topPos + 25, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 116, topPos + 35, slotU, slotV, 18, 18);
        gfx.drawString(this.font, "ᴏᴜᴛ", leftPos + 116, topPos + 25, 0x404040, false);

        // -------------------------
// LATHE ANIMATED PROGRESS BAR
// -------------------------
        RenderSystem.setShaderTexture(0, LATHE_PROGRESS_TEXTURE);

// Background frame (217–234, 0–16) → 17×16
        gfx.blit(LATHE_PROGRESS_TEXTURE,
                leftPos + 80,
                topPos + 35,
                217, 0,
                17, 16);

// Correct progress width (14 px)
        int progress = menu.getProgressScaled(14);

// Recipe running?
        boolean running = (menu.getProgress() > 0 && menu.getProgress() < menu.getMaxProgress());

// Only animate if machine is actually running
        if (running && !noEnergy) {
            gfx.blit(LATHE_PROGRESS_TEXTURE,
                    leftPos + 80,
                    topPos + 35,
                    235, 0,      // animated bar region
                    progress, 16);
        }

// -------------------------
// STATUS MESSAGE (corrected)
// -------------------------
        String msg;
        int color;

// Output slot check
        boolean outputEmpty = menu.getSlot(1).getItem().isEmpty();


// 1. If recipe is running → ALWAYS show Processing
        if (running) {
            if (noEnergy) {
                msg = "Not enough energy";
                color = 0xFF5555;
            } else {
                msg = "Processing...";
                color = 0x228B22;
            }
        }

// 2. Recipe finished
        else if (menu.getProgress() >= menu.getMaxProgress()) {

            // Output present → Completed
            if (!outputEmpty) {
                msg = "Complete";
                color = 0x006400;
            }
            // Output empty → Idle
            else {
                msg = "Idle";
                color = 0x404040;
            }
        }

// 3. Recipe not running
        else {

            if (noInput) {
                msg = "Idle";
                color = 0x404040;
            }
            else if (noEnergy) {
                msg = "Not enough energy";
                color = 0xFF5555;
            }
            else {
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
        int barX = leftPos + 14;
        int barY = topPos + 23;
        int barW = 8;
        int barH = 40;

        // Recompute energy here (renderBg variables are not visible)
        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();

        boolean offline = (energy <= 0);

        if (mouseX >= barX && mouseX < barX + barW &&
                mouseY >= barY && mouseY < barY + barH) {

            if (offline) {
                gfx.renderTooltip(this.font,
                        Component.literal("Power input offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy: " +
                                energy + " / " + maxEnergy + " FE"),
                        mouseX, mouseY);
            }
        }


        this.renderTooltip(gfx, mouseX, mouseY);
    }

}
