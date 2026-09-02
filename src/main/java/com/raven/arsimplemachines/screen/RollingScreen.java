package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.RollingMenu;

import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class RollingScreen extends AbstractContainerScreen<RollingMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/generic_menu.png");
    private static final ResourceLocation ROLLING_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/progressbars.png");
    public RollingScreen(RollingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = 9999;
        this.titleLabelY = 9999;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();
        int fluid = menu.getFluidAmount();
        int fluidMax = menu.getFluidCapacity();

        // ---------------------------------------------------------
        // FIXED INPUT DETECTION
        // ---------------------------------------------------------
        boolean noInput;

        var be = menu.getBlockEntity();
        if (be != null) {
            // If recipe is running, input is considered valid
            noInput = !be.recipeRunning && menu.getSlot(0).getItem().isEmpty();
        } else {
            noInput = menu.getSlot(0).getItem().isEmpty();
        }


        boolean noEnergy = (energy <= 25);
        boolean noFluid = (fluid <= 69);

        // SLOT FRAMES
        int slotU = 177;
        int slotV = 0;

        gfx.blit(GUI_TEXTURE, leftPos + 44, topPos + 35, slotU, slotV, 18, 18);
        gfx.blit(GUI_TEXTURE, leftPos + 126, topPos + 35, slotU, slotV, 18, 18);

        // POWER BAR
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

        // FLUID BAR
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
// ROLLING MACHINE PROGRESS BAR (LEFT → RIGHT)
// -------------------------

// Scale progress to the fill width (40 px)
        int fillWidth = menu.getProgressScaled(40);

// Draw background frame
        gfx.blit(ROLLING_PROGRESS_TEXTURE,
                leftPos + 70,          // X position of bar
                topPos + 25,           // Y position of bar
                84, 66,                // U, V of background frame
                41, 32);               // width, height


// Draw animated fill (left → right)
        gfx.blit(ROLLING_PROGRESS_TEXTURE,
                leftPos + 70,          // same X as background
                topPos + 25,           // same Y as background
                126, 66,               // U, V of fill region
                fillWidth, 32);        // width grows, height fixed


        String msg;
        int color;

// Fail-safe if BE missing
        if (be == null) {
            msg = "Idle";
            color = 0x404040;
        }
// If recipe is running
        else if (be.recipeRunning) {
            if (noEnergy) {
                msg = "Not enough energy";
                color = 0xFF5555;
            } else if (noFluid) {
                msg = "Not enough fluid";
                color = 0x00AAFF;
            } else {
                msg = "Processing...";
                color = 0x228B22;
            }
        }
// If recipe is NOT running
        else {
            int progress2 = menu.getProgress();
            int max = menu.getMaxProgress();

            // Completed recipe
            if (max > 0 && progress2 >= max) {
                msg = "Complete";
                color = 0x404040;
            }
            // No input at all
            else if (noInput) {
                msg = "Idle";
                color = 0x404040;
            }
            // Input exists but no recipe matched
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

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {

        gfx.drawString(
                this.font,
                "Input",
                42,
                25,
                0x404040,
                false
        );

        gfx.drawString(
                this.font,
                "Output",
                121,
                25,
                0x404040,
                false
        );
    }
}
