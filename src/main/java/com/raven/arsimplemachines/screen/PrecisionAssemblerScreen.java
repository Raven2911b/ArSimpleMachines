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
