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


        // -------------------------
        // ENERGY BAR
        // -------------------------
        gfx.drawString(this.font, "P", leftPos + 8, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 55, 176, 57, 8, 1);

        int energy = menu.getEnergyScaled(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 8 + 1,
                topPos + 16 + (38 - energy) + 1,
                0, 171,
                6, energy);

        // -------------------------
        // INPUT TANK
        // -------------------------
        gfx.drawString(this.font, "IN", leftPos + 30, labelY, 0x404040, false);

        // 1. Draw frame
        gfx.blit(GUI_TEXTURE, leftPos + 30, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 30, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 30, topPos + 55, 176, 57, 8, 1);

        // 2. Draw fluid ABOVE frame
        int inputHeight = menu.getInputScaled(38);
        FluidStack inStack = menu.getBlockEntity().getClientInputFluid();

        if (!inStack.isEmpty()) {
            var ext = IClientFluidTypeExtensions.of(inStack.getFluid());
            var stillTex = ext.getStillTexture(inStack);
            int color = getFluidTint(inStack);

            int x = leftPos + 30 + 1;
            int y = topPos + 17 + (38 - inputHeight);
            int w = 6;
            int h = inputHeight;


            gfx.fill(x, y, x + w, y + h, color);
            gfx.blitSprite(stillTex, x, y, w, h, color);
        }

        // -------------------------
        // OUTPUT A
        // -------------------------
        gfx.drawString(this.font, "A", leftPos + 110, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 110, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 110, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 110, topPos + 55, 176, 57, 8, 1);

        int outAHeight = menu.getOutputAScaled(38);
        FluidStack outAStack = menu.getBlockEntity().getClientOutputAFluid();

        if (!outAStack.isEmpty()) {
            var ext = IClientFluidTypeExtensions.of(outAStack.getFluid());
            var stillTex = ext.getStillTexture(outAStack);
            int color = getFluidTint(outAStack);

            int x = leftPos + 110 + 1;
            int y = topPos + 17 + (38 - outAHeight);
            int w = 6;
            int h = outAHeight;

            gfx.fill(x, y, x + w, y + h, color);
            gfx.blitSprite(stillTex, x, y, w, h, color);
        }

        // -------------------------
        // OUTPUT B
        // -------------------------
        gfx.drawString(this.font, "B", leftPos + 140, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 55, 176, 57, 8, 1);

        int outBHeight = menu.getOutputBScaled(38);
        FluidStack outBStack = menu.getBlockEntity().getClientOutputBFluid();

        if (!outBStack.isEmpty()) {
            var ext = IClientFluidTypeExtensions.of(outBStack.getFluid());
            var stillTex = ext.getStillTexture(outBStack);
            int color = getFluidTint(outBStack);

            int x = leftPos + 140 + 1;
            int y = topPos + 17 + (38 - outBHeight);
            int w = 6;
            int h = outBHeight;

            gfx.fill(x, y, x + w, y + h, color);
            gfx.blitSprite(stillTex, x, y, w, h, color);
        }

        // -------------------------
        // PROGRESS BAR
        // -------------------------
        int progress = menu.getProgressScaled(65);

        gfx.blit(ELECTROLYZER_PROGRESS_TEXTURE,
                leftPos + 60,
                topPos + 5,
                0, 0,
                31, 65);

        gfx.blit(ELECTROLYZER_PROGRESS_TEXTURE,
                leftPos + 60,
                topPos + 5 + (65 - progress),
                0, (65 - progress),
                31, progress);

        if (menu.isRecipeRunning()) {
            int overlayMax = 50;
            int overlayHeight = (progress * overlayMax) / 65;

            gfx.blit(ELECTROLYZER_PROGRESS_TEXTURE,
                    leftPos + 60 + 4,
                    topPos + 21 + (overlayMax - overlayHeight),
                    31, (overlayMax - overlayHeight),
                    23, overlayHeight);

            RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        }

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg = menu.getStatusMessage();
        if (!msg.isEmpty()) {
            int color = switch (msg) {
                case "Not enough energy" -> 0xFF5555;
                case "Output A full", "Output B full" -> 0xFFFF55;
                case "Processing..." -> 0x228B22;
                case "Idle" -> 0x404040;
                default -> 0xC0C0C0;
            };

            gfx.drawString(this.font, msg, leftPos + 8, topPos + 72, color, false);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // Tooltips
        int inX = leftPos + 30 + 1;
        int inY = topPos + 16 + 1;
        if (mouseX >= inX && mouseX <= inX + 6 &&
                mouseY >= inY && mouseY <= inY + 38) {
            gfx.renderTooltip(this.font,
                    Component.literal(menu.getInputName() + ": " +
                            menu.getInputAmount() + " / " + menu.getInputCapacity() + " mB"),
                    mouseX, mouseY);
        }

        int outAX = leftPos + 110 + 1;
        int outAY = topPos + 16 + 1;
        if (mouseX >= outAX && mouseX <= outAX + 6 &&
                mouseY >= outAY && mouseY <= outAY + 38) {
            gfx.renderTooltip(this.font,
                    Component.literal(menu.getOutputAName() + ": " +
                            menu.getOutputAAmount() + " / " + menu.getOutputACapacity() + " mB"),
                    mouseX, mouseY);
        }

        int outBX = leftPos + 140 + 1;
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
