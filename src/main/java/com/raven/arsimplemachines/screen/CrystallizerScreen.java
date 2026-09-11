package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.CrystallizerMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrystallizerScreen extends AbstractContainerScreen<CrystallizerMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/generic_menu.png");

    private static final ResourceLocation CRYSTALLIZER_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/progressbars.png"
            );

    public CrystallizerScreen(CrystallizerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = 9999;
        this.titleLabelY = 9999;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        // Draw full GUI background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();
        int fluid = menu.getFluidAmount();
        int fluidMax = menu.getFluidCapacity();

        var be = menu.getBlockEntity();

        boolean noInput;

        if (be != null) {
            // These refer to the actual ITEMSTACKS inside the item slots:
            // Slot 0 = Input Item Slot 0 (X=44, Y=35)
            // Slot 1 = Input Item Slot 1 (X=62, Y=35)
            boolean slot0Empty = menu.getSlot(0).getItem().isEmpty();   // Input item 0
            boolean slot1Empty = menu.getSlot(1).getItem().isEmpty();   // Input item 1

            noInput = !be.recipeRunning && slot0Empty && slot1Empty;
        } else {
            // Same slot references when BE is null
            noInput = menu.getSlot(0).getItem().isEmpty() &&            // Input item 0
                    menu.getSlot(1).getItem().isEmpty();              // Input item 1
        }

        boolean noEnergy = (energy <= 25);
        boolean noFluid = (fluid <= 69);

        int slotU = 177;
        int slotV = 0;

        // ---------------------------------------------------------
        // IMPORTANT:
        // These coordinates come from CrystallizerMenu.addSlot().
        // ---------------------------------------------------------

        gfx.drawString(this.font, "ᴄʀʏꜱᴛᴀʟʟɪᴢᴇʀ", leftPos + 5, topPos + 5, 0x404040, false);
        //gfx.drawString(this.font, "P", leftPos + 12, topPos + 5, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 11, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 11, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 11, topPos + 60, 176, 57, 8, 1);

        int scaledPower = menu.getPowerScaled(38);

        gfx.blit(
                GUI_TEXTURE,
                leftPos + 12,
                topPos + 22 + (38 - scaledPower) + 1,
                0, 171,
                6, scaledPower
        );

        //gfx.drawString(this.font, "F", leftPos + 24, topPos + 5, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 23, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 23, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 23, topPos + 60, 176, 57, 8, 1);

        int scaledFluid = menu.getFluidScaled(38);

        int fluidTopColor = 0xFF0044FF;
        int fluidBottomColor = 0xFF66CCFF;

        for (int i = 0; i < scaledFluid; i++) {
            float t = (float) i / (float) scaledFluid;

            int r = (int)(((fluidTopColor >> 16) & 0xFF) * (1 - t) + ((fluidBottomColor >> 16) & 0xFF) * t);
            int g = (int)(((fluidTopColor >> 8) & 0xFF) * (1 - t) + ((fluidBottomColor >> 8) & 0xFF) * t);
            int b = (int)(((fluidTopColor) & 0xFF) * (1 - t) + ((fluidBottomColor) & 0xFF) * t);

            int color = 0xFF000000 | (r << 16) | (g << 8) | b;

            gfx.fill(
                    leftPos + 24,
                    topPos + 22 + (38 - scaledFluid) + 1 + i,
                    leftPos + 24 + 6,
                    topPos + 22 + (38 - scaledFluid) + 2 + i,
                    color
            );
        }
        // ---------------------------------------------------------
        // SLOT BACKGROUND GRAPHICS (NOT THE ITEMS)
        // ---------------------------------------------------------
        gfx.blit(GUI_TEXTURE, leftPos + 44,  topPos + 36, slotU, slotV, 18, 18);   // Input Slot 0 frame
        gfx.blit(GUI_TEXTURE, leftPos + 62,  topPos + 36, slotU, slotV, 18, 18);   // Input Slot 1 frame
        gfx.blit(GUI_TEXTURE, leftPos + 145, topPos + 36, slotU, slotV, 18, 18);   // Output Slot frame


        // -------------------------
        // PROGRESS BAR
        // -------------------------
        int progress = menu.getProgressScaled(65);

        gfx.blit(CRYSTALLIZER_PROGRESS_TEXTURE,
                leftPos + 100,
                topPos + 5,
                0, 0,
                31, 65);

        gfx.blit(CRYSTALLIZER_PROGRESS_TEXTURE,
                leftPos + 100,
                topPos + 5 + (65 - progress),
                0, (65 - progress),
                31, progress);

        if (menu.isRecipeRunning()) {
            int overlayMax = 50;
            int overlayHeight = (progress * overlayMax) / 65;

            gfx.blit(CRYSTALLIZER_PROGRESS_TEXTURE,
                    leftPos + 100 + 4,
                    topPos + 21 + (overlayMax - overlayHeight),
                    31, (overlayMax - overlayHeight),
                    23, overlayHeight);

            RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        }

        String msg;
        int color;
        if (be == null) {
            msg = "Idle";
            color = 0x606060;
        }
        else if (be.recipeRunning) {

            if (noEnergy) {
                msg = "Not enough energy";
                color = 0xFF2222;
            }
            else if (noFluid) {
                msg = "Not enough fluid";
                color = 0x2288CC;
            }
            else {
                msg = "Processing...";
                color = 0x228B22;
            }
        }
        else {
            int progress2 = menu.getProgress();
            int max = menu.getMaxProgress();

            if (max > 0 && progress2 >= max) {
                msg = "Complete";
                color = 0x606060;
            }
            else if (noInput) {
                msg = "Idle";
                color = 0x606060;
            }
            else {
                msg = "No matching recipe";
                color = 0xCC4444;
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
        int pX = leftPos + 11 + 1;   // inside the frame
        int pY = topPos + 22 + 1;
        int pW = 6;
        int pH = 38;

        int stored = menu.getPowerStored();
        int max = menu.getMaxPower();

        if (mouseX >= pX && mouseX <= pX + pW &&
                mouseY >= pY && mouseY <= pY + pH) {

            if (stored <= 0) {
                gfx.renderTooltip(
                        this.font,
                        Component.literal("Power: offline"),
                        mouseX, mouseY
                );
            } else {
                gfx.renderTooltip(
                        this.font,
                        Component.literal("Power: " + stored + " / " + max + " FE"),
                        mouseX, mouseY
                );
            }
        }

        // -------------------------
        // FLUID BAR TOOLTIP
        // -------------------------
        int fX = leftPos + 23 + 1;   // inside the frame
        int fY = topPos + 22 + 1;
        int fW = 6;
        int fH = 38;

        int amount = menu.getFluidAmount();
        int cap = menu.getFluidCapacity();

        if (mouseX >= fX && mouseX <= fX + fW &&
                mouseY >= fY && mouseY <= fY + fH) {

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Fluid: " + amount + " / " + cap + " mB"),
                    mouseX,
                    mouseY
            );
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }


    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {

//        gfx.drawString(
//                this.font,
//                "Inputs",
//                46,
//                25,
//                0x404040,
//                false
//        );
//
//        gfx.drawString(
//                this.font,
//                "Output",
//                140,
//                25,
//                0x404040,
//                false
//        );
    }
}
