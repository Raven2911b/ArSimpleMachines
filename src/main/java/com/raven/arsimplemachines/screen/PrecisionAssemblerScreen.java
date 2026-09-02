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
    private static final ResourceLocation PRECISION_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/progressbars.png");
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

        int energy0 = menu.getPowerStored(0);
        int energy1 = menu.getPowerStored(1);
        boolean offlineA = (energy0 <= 0);
        boolean offlineB = (energy1 <= 0);

        var be = menu.getBlockEntity();
        boolean noInput = (be == null || !be.getClientHasInputItems());

        boolean noEnergy = false;
        if (be != null) {

            int perBlock = 0;
            if (be.currentRecipe != null) {
                perBlock = be.currentRecipe.getEnergyPerTick() / 2;
            }

            if (be.recipeRunning && be.currentRecipe != null) {
                noEnergy = (energy0 < perBlock) || (energy1 < perBlock);
            } else if (be.getClientHasInputItems()) {
                noEnergy = (energy0 < 80) || (energy1 < 80);
            }
        }

        // -------------------------
        // POWER BAR ICON LABELS
        // -------------------------
        gfx.drawString(this.font, "ᴘᴡʀ", leftPos + 11, topPos + 5, 0x404040, false);
        //gfx.drawString(this.font, "⚡", leftPos + 19, topPos + 5, 0x404040, false);

        // -------------------------
        // POWER BARS (two inputs)
        // -------------------------

        // Bar positions
        int barX1 = leftPos + 11;
        int barX2 = leftPos + 22;   // second bar shifted right
        int barY  = topPos + 16;
        int barH  = 38;

        // Draw frames for both bars
        gfx.blit(GUI_TEXTURE, barX1, barY,     176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, barX1, barY + 1, 176, 19, 8, barH);
        gfx.blit(GUI_TEXTURE, barX1, barY + barH + 1, 176, 57, 8, 1);

        gfx.blit(GUI_TEXTURE, barX2, barY,     176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, barX2, barY + 1, 176, 19, 8, barH);
        gfx.blit(GUI_TEXTURE, barX2, barY + barH + 1, 176, 57, 8, 1);

        // Fill bars
        int scaled0 = menu.getPowerScaled(0, barH);
        int scaled1 = menu.getPowerScaled(1, barH);

        gfx.blit(GUI_TEXTURE,
                barX1 + 1,
                barY + (barH - scaled0) + 1,
                0, 171,
                6, scaled0);

        gfx.blit(GUI_TEXTURE,
                barX2 + 1,
                barY + (barH - scaled1) + 1,
                0, 171,
                6, scaled1);


        // -------------------------
        // PRECISION ASSEMBLER PROGRESS BAR (6‑SEGMENT)
        // -------------------------

        // Scale progress to 66 px (height of background frame)
        int progress = menu.getProgressScaled(66);

        // Draw static background frame
        gfx.blit(PRECISION_PROGRESS_TEXTURE,
                leftPos + 70,          // X position of bar
                topPos + 10,            // Y position of bar
                132, 0,                // U, V of background frame
                54, 66);               // width, height


        // =========================================================
        //  FIRST THREE SEGMENTS — INSTANT ON/OFF (NO ANIMATION)
        // =========================================================

        if (be != null && be.recipeRunning) {

            // ---------------------------------------------------------
            // SEGMENT A — REAL COORDINATES (X90–101, Y45–56)
            // ---------------------------------------------------------
            gfx.blit(PRECISION_PROGRESS_TEXTURE,
                    leftPos + 70 + 4,
                    topPos + 6 + 4,
                    90, 45,
                    11, 11);


            // ---------------------------------------------------------
            // SEGMENT B — REAL COORDINATES (X77–90, Y42–55)
            // ---------------------------------------------------------
            gfx.blit(PRECISION_PROGRESS_TEXTURE,
                    leftPos + 70 + 2,     // TEMPORARY screen X (adjust later)
                    topPos + 27 + 4,       // TEMPORARY screen Y (adjust later)
                    78, 42,                // REAL U, V
                    12, 13);               // REAL width, height


            // ---------------------------------------------------------
            // SEGMENT C — REAL COORDINATES (X54–68, Y56–65)
            // ---------------------------------------------------------
            gfx.blit(PRECISION_PROGRESS_TEXTURE,
                    leftPos + 70 + 2,     // TEMPORARY screen X (adjust later)
                    topPos + 56 + 4,       // TEMPORARY screen Y (adjust later)
                    54, 57,                // REAL U, V
                    14, 9);                // REAL width, height
        }


        // =========================================================
        //  LAST THREE SEGMENTS — SEQUENTIAL ANIMATION
        // =========================================================

        int total = menu.getProgress();
        int max   = menu.getMaxProgress();

        if (be != null && be.recipeRunning && max > 0) {
            int phase = max / 3;

            // Guard against tiny max values
            if (phase <= 0) {
                phase = 1;
            }

            // Phase 1 (Segment D)
            int p1 = Math.min(total, phase);

            // Phase 2 (Segment E)
            int p2 = Math.max(0, Math.min(total - phase, phase));

            // Phase 3 (Segment F)
            int p3 = Math.max(0, Math.min(total - (phase * 2), phase));


            // ---------------------------------------------------------
            // SEGMENT D — top → bottom
            // ---------------------------------------------------------
            int dHeight = (p1 * 15) / phase;

            gfx.blit(PRECISION_PROGRESS_TEXTURE,
                    leftPos + 105,
                    topPos + 32 ,
                    54, 42,              // use real base V, not (66 - dHeight)
                    12, dHeight);


            // ---------------------------------------------------------
            // SEGMENT E — top → bottom (still dummy coords for now)
            // ---------------------------------------------------------
            int eHeight = (p2 * 14) / phase;

            gfx.blit(PRECISION_PROGRESS_TEXTURE,
                    leftPos + 105,
                    topPos + 52,
                    67, 43,              // dummy U/V
                    11, eHeight);


            // ---------------------------------------------------------
            // SEGMENT F — left → right (still dummy coords for now)
            // ---------------------------------------------------------
            int fWidth = (p3 * 21) / phase;

            gfx.blit(PRECISION_PROGRESS_TEXTURE,
                    leftPos + 101,
                    topPos + 73,
                    89, 42,              // dummy U/V
                    fWidth, 3);
        }

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
            color = 0x228B22;
        } else {

            if (offlineA && offlineB) {
                msg = "Both power inputs offline";
                color = 0xFF5555;

            } else if (offlineA) {
                msg = "Energy Input 1 offline";
                color = 0xFF5555;

            } else if (offlineB) {
                msg = "Energy Input 2 offline";
                color = 0xFF5555;

            } else if (noInput) {
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


        gfx.drawString(this.font, msg, leftPos + 5, topPos + 78, color, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // -------------------------
        // POWER BAR TOOLTIP
        // -------------------------
        int barX1 = leftPos + 11;
        int barX2 = leftPos + 22;
        int barY  = topPos + 16;
        int barW  = 8;
        int barH  = 40;

        // Recompute energy values here (render() cannot see renderBg() variables)
        int energy0 = menu.getPowerStored(0);
        int energy1 = menu.getPowerStored(1);

        boolean offlineA = (energy0 <= 0);
        boolean offlineB = (energy1 <= 0);

        // Tooltip for bar 1
        if (mouseX >= barX1 && mouseX < barX1 + barW &&
                mouseY >= barY  && mouseY < barY + barH) {

            if (offlineA) {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy Input 1 offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy Input 1: " +
                                energy0 + " / " +
                                menu.getMaxPower(0) + " FE"),
                        mouseX, mouseY);
            }
        }

        // Tooltip for bar 2
        if (mouseX >= barX2 && mouseX < barX2 + barW &&
                mouseY >= barY  && mouseY < barY + barH) {

            if (offlineB) {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy Input 2 offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy Input 2: " +
                                energy1 + " / " +
                                menu.getMaxPower(1) + " FE"),
                        mouseX, mouseY);
            }
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // No labels
    }
}
