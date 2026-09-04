package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.ElectrolyzerMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectrolyzerScreen extends AbstractContainerScreen<ElectrolyzerMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/generic_menu.png"
            );
    private static final ResourceLocation ELECTROLYZER_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/progressbars.png"
            );
    private static final String TITLE = "ᴇʟᴇᴄᴛʀᴏʟʏᴢᴇʀ";

    public ElectrolyzerScreen(ElectrolyzerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = this.imageHeight - 94;
    }
    private int getFluidTint(FluidStack stack) {
        if (stack.isEmpty()) return 0xFFFFFFFF;

        // NeoForge 1.21 way to get the fluid ID
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id == null) return 0xFFFFFFFF;

        // Custom colors for AR fluids
        if (id.equals(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "hydrogen"))) {
            return 0xFFFFAACC; // light pink
        }

        if (id.equals(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "oxygen"))) {
            return 0xFF00FFFF; // cyan
        }

        // Default tint
        return IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
    }


    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int labelY = topPos + 5;

        // Background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        gfx.drawString(this.font, TITLE, leftPos + 8, labelY, 0x404040, false);

        // -------------------------
        // ENERGY BAR (DOWN +6)
        // -------------------------
        //gfx.drawString(this.font, "P", leftPos + 8, labelY+10, 0x404040, false);
        gfx.blit(GUI_TEXTURE,
                leftPos + 16,      // X position
                labelY +60,           // Y position (same as the old text)
                15, 171,           // U, V of the icon inside the PNG
                4, 10);           // width, height of the icon


        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 61, 176, 57, 8, 1);

        int energy = menu.getEnergyScaled(0,38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 9,
                topPos + 22 + (38 - energy) + 1,
                0, 171,
                6, energy);

        int eB = menu.getEnergyScaled(1, 38);

        gfx.blit(GUI_TEXTURE, leftPos + 20,  topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 20,  topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 20,  topPos + 61, 176, 57, 8, 1);

        gfx.blit(GUI_TEXTURE,
                leftPos + 21,
                topPos + 22 + (38 - eB) + 1,
                0, 171,
                6, eB);

        // -------------------------
        // INPUT TANK (DOWN +6)
        // -------------------------
        //gfx.drawString(this.font, "IN", leftPos + 30, labelY+8, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 50, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 50, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 50, topPos + 61, 176, 57, 8, 1);

        int inputHeight = menu.getInputScaled(38);
        FluidStack inStack = menu.getBlockEntity().getClientInputFluid();

        if (!inStack.isEmpty()) {
            var ext = IClientFluidTypeExtensions.of(inStack.getFluid());
            var stillTex = ext.getStillTexture(inStack);
            int color = getFluidTint(inStack);

            int x = leftPos + 51;
            int y = topPos + 23 + (38 - inputHeight);
            int w = 6;
            int h = inputHeight;

            gfx.fill(x, y, x + w, y + h, color);
            gfx.blitSprite(stillTex, x, y, w, h, color);
        }

        // -------------------------
        // OUTPUT A (DOWN +6)
        // -------------------------
        //gfx.drawString(this.font, "A", leftPos + 110, labelY + 8, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 130, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 130, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 130, topPos + 61, 176, 57, 8, 1);

        int outAHeight = menu.getOutputAScaled(38);
        FluidStack outAStack = menu.getBlockEntity().getClientOutputAFluid();

        if (!outAStack.isEmpty()) {
            var ext = IClientFluidTypeExtensions.of(outAStack.getFluid());
            var stillTex = ext.getStillTexture(outAStack);
            int color = getFluidTint(outAStack);

            int x = leftPos + 131;
            int y = topPos + 23 + (38 - outAHeight);
            int w = 6;
            int h = outAHeight;

            gfx.fill(x, y, x + w, y + h, color);
            gfx.blitSprite(stillTex, x, y, w, h, color);
        }

        // -------------------------
        // OUTPUT B (DOWN +6)
        // -------------------------
        //gfx.drawString(this.font, "B", leftPos + 140, labelY+8, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 155, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 155, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 155, topPos + 61, 176, 57, 8, 1);

        int outBHeight = menu.getOutputBScaled(38);
        FluidStack outBStack = menu.getBlockEntity().getClientOutputBFluid();

        if (!outBStack.isEmpty()) {
            var ext = IClientFluidTypeExtensions.of(outBStack.getFluid());
            var stillTex = ext.getStillTexture(outBStack);
            int color = getFluidTint(outBStack);

            int x = leftPos + 156;
            int y = topPos + 23 + (38 - outBHeight);
            int w = 6;
            int h = outBHeight;

            gfx.fill(x, y, x + w, y + h, color);
            gfx.blitSprite(stillTex, x, y, w, h, color);
        }

        // -------------------------
        // PROGRESS BAR (DOWN +6)
        // -------------------------
        int progress = menu.getProgressScaled(65);

        gfx.blit(ELECTROLYZER_PROGRESS_TEXTURE,
                leftPos + 80,
                topPos + 11,
                0, 0,
                31, 65);

        gfx.blit(ELECTROLYZER_PROGRESS_TEXTURE,
                leftPos + 80,
                topPos + 11 + (65 - progress),
                0, (65 - progress),
                31, progress);

        if (menu.isRecipeRunning()) {
            int overlayMax = 50;
            int overlayHeight = (progress * overlayMax) / 65;

            gfx.blit(ELECTROLYZER_PROGRESS_TEXTURE,
                    leftPos + 84,
                    topPos + 27 + (overlayMax - overlayHeight),
                    31, (overlayMax - overlayHeight),
                    23, overlayHeight);

            RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        }

        // -------------------------
        // STATUS MESSAGE (DOWN +6)
        // -------------------------
        String msg = menu.getStatusMessage();
        if (!msg.isEmpty()) {
            int color = switch (msg) {

                // POWER WARNINGS
                case "Power 1 input low",
                     "Power 2 input low",
                     "Both power inputs low" -> 0xFF5555;   // red

                // OUTPUT WARNINGS
                case "Output A full",
                     "Output B full" -> 0xFFFF55;          // yellow

                // PROCESSING
                case "Processing..." -> 0x228B22;          // green

                // IDLE
                case "Idle" -> 0x404040;                   // gray

                // FALLBACK
                default -> 0xC0C0C0;                       // light gray
            };

            gfx.drawString(this.font, msg, leftPos + 8, topPos + 78, color, false);
        }

    }


    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // POWER BAR TOOLTIP
        int pX = leftPos + 8 + 1;   // inside the frame
        int pY = topPos + 22 + 1;
        int pW = 6;
        int pH = 38;

        int stored = menu.getEnergyStored(0);
        int max = menu.getEnergyMax(0);

        boolean offline = stored <= 0;

        if (mouseX >= pX && mouseX <= pX + pW &&
                mouseY >= pY && mouseY <= pY + pH) {

            if (offline) {
                gfx.renderTooltip(this.font,
                        Component.literal("Power 1 input offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy: " + stored + " / " + max + " FE"),
                        mouseX, mouseY);
            }
        }
// POWER BAR 2 TOOLTIP
        int pBX = leftPos + 20 + 1;   // inside the frame
        int pBY = topPos + 22 + 1;
        int pBW = 6;
        int pBH = 38;

        int storedB = menu.getEnergyStored(1);
        int maxB = menu.getEnergyMax(1);

        boolean offlineB = storedB <= 0;

        if (mouseX >= pBX && mouseX <= pBX + pBW &&
                mouseY >= pBY && mouseY <= pBY + pBH) {

            if (offlineB) {
                gfx.renderTooltip(this.font,
                        Component.literal("Power 2 input offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy: " + storedB + " / " + maxB + " FE"),
                        mouseX, mouseY);
            }
        }


        // Tooltips
        int inX = leftPos + 50 + 1;
        int inY = topPos + 16 + 1;
        if (mouseX >= inX && mouseX <= inX + 6 &&
                mouseY >= inY && mouseY <= inY + 38) {
            gfx.renderTooltip(this.font,
                    Component.literal(menu.getInputName() + ": " +
                            menu.getInputAmount() + " / " + menu.getInputCapacity() + " mB"),
                    mouseX, mouseY);
        }

        int outAX = leftPos + 130 + 1;
        int outAY = topPos + 16 + 1;
        if (mouseX >= outAX && mouseX <= outAX + 6 &&
                mouseY >= outAY && mouseY <= outAY + 38) {
            gfx.renderTooltip(this.font,
                    Component.literal(menu.getOutputAName() + ": " +
                            menu.getOutputAAmount() + " / " + menu.getOutputACapacity() + " mB"),
                    mouseX, mouseY);
        }

        int outBX = leftPos + 155 + 1;
        int outBY = topPos + 16 + 1;
        if (mouseX >= outBX && mouseX <= outBX + 6 &&
                mouseY >= outBY && mouseY <= outBY + 38) {
            gfx.renderTooltip(this.font,
                    Component.literal(menu.getOutputBName() + ": " +
                            menu.getOutputBAmount() + " / " + menu.getOutputBCapacity() + " mB"),
                    mouseX, mouseY);
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
    }
}
