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
        // SLOT BACKGROUND GRAPHICS (NOT THE ITEMS)
        //
        // These draw the visual frames behind the item slots.
        // The actual item stacks are drawn later by AbstractContainerScreen.
        //
        // Input Slot 0 background:  (44, 35)
        // Input Slot 1 background:  (62, 35)
        // Output Slot background:   (145, 35)
        //
        // These EXACT coordinates match the item slot positions defined in
        // CrystallizerMenu:
        //
        //   addSlot(input, 0, 44, 35);   // Input item 0
        //   addSlot(input, 1, 62, 35);   // Input item 1
        //   addSlot(output, 0, 145, 35); // Output item
        //
        // When a recipe is running, the recipe items appear INSIDE these slots.
        // ---------------------------------------------------------
        gfx.blit(GUI_TEXTURE, leftPos + 44,  topPos + 35, slotU, slotV, 18, 18);   // Input Slot 0 frame
        gfx.blit(GUI_TEXTURE, leftPos + 62,  topPos + 35, slotU, slotV, 18, 18);   // Input Slot 1 frame
        gfx.blit(GUI_TEXTURE, leftPos + 145, topPos + 35, slotU, slotV, 18, 18);   // Output Slot frame

        // ---------------------------------------------------------
        // IMPORTANT:
        // The actual ITEMSTACKS (recipe items, player items, etc.)
        // are NOT drawn here.
        //
        // They are drawn automatically by:
        //   super.render(gfx, mouseX, mouseY, partialTicks)
        //
        // That call happens in render(), NOT renderBg().
        //
        // So the recipe items appear at:
        //   Input item 0:  leftPos + 44,  topPos + 35
        //   Input item 1:  leftPos + 62,  topPos + 35
        //   Output item:   leftPos + 145, topPos + 35
        //
        // These coordinates come from CrystallizerMenu.addSlot().
        // ---------------------------------------------------------

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

        gfx.drawString(this.font, "F", leftPos + 24, topPos + 5, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 23, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 23, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 23, topPos + 55, 176, 57, 8, 1);

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
                    topPos + 16 + (38 - scaledFluid) + 1 + i,
                    leftPos + 24 + 6,
                    topPos + 16 + (38 - scaledFluid) + 2 + i,
                    color
            );
        }

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

        // ---------------------------------------------------------
        // Draw the darkened background behind the GUI
        // (does NOT draw items or slots)
        // ---------------------------------------------------------
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);

        // ---------------------------------------------------------
        // Draw the entire GUI:
        // - Slot backgrounds (from renderBg)
        // - Item stacks inside slots (input/output items)
        // - Item stack overlays (stack counts, durability bars)
        //
        // IMPORTANT:
        // This is the call that actually renders the ITEMSTACKS
        // at their defined slot coordinates:
        //
        //   Input item 0:  (leftPos + 44,  topPos + 35)
        //   Input item 1:  (leftPos + 62,  topPos + 35)
        //   Output item:   (leftPos + 145, topPos + 35)
        //
        // These coordinates come from CrystallizerMenu.addSlot().
        //
        // renderBg() ONLY draws the background graphics.
        // super.render() draws the actual items.
        // ---------------------------------------------------------
        super.render(gfx, mouseX, mouseY, partialTicks);


        // ---------------------------------------------------------
        // FLUID BAR TOOLTIP REGION
        //
        // This defines the hover area for the fluid tank tooltip:
        //   X: leftPos + 23 → leftPos + 23 + 8
        //   Y: topPos + 16 → topPos + 16 + 40
        //
        // When the mouse is inside this rectangle, the fluid tooltip
        // is displayed showing:
        //   "Fluid: <amount> / <capacity> mB"
        //
        // This does NOT draw items — only the tooltip.
        // ---------------------------------------------------------
        int fx = leftPos + 23;
        int fy = topPos + 16;
        int fw = 8;
        int fh = 40;

        if (mouseX >= fx && mouseX < fx + fw &&
                mouseY >= fy && mouseY < fy + fh) {

            int amount = menu.getFluidAmount();
            int cap = menu.getFluidCapacity();

            gfx.renderTooltip(
                    this.font,
                    Component.literal("Fluid: " + amount + " / " + cap + " mB"),
                    mouseX,
                    mouseY
            );
        }

        // ---------------------------------------------------------
        // Draw tooltips for:
        // - Item stacks inside slots (input/output items)
        // - Any other tooltip regions defined in renderBg()
        //
        // This includes:
        //   Input item 0 tooltip
        //   Input item 1 tooltip
        //   Output item tooltip
        //
        // Minecraft automatically detects which slot the mouse is over
        // and shows the correct item tooltip.
        // ---------------------------------------------------------
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {

        gfx.drawString(
                this.font,
                "Inputs",
                46,
                25,
                0x404040,
                false
        );

        gfx.drawString(
                this.font,
                "Output",
                140,
                25,
                0x404040,
                false
        );
    }
}
