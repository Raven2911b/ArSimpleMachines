package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.FluidConverterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidConverterScreen extends AbstractContainerScreen<FluidConverterMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/generic_menu.png");
    private static final ResourceLocation PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "textures/gui/progressbars.png");

    public FluidConverterScreen(FluidConverterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 9999;
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        gfx.drawString(this.font, "Fluid Converter", leftPos + 8, topPos + 6, 0x404040, false);

        // POWER
        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();
        int powerScaled = menu.getPowerScaled(38);

        gfx.blit(GUI_TEXTURE, leftPos + 14, topPos + 23, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 14, topPos + 24, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 14, topPos + 62, 176, 57, 8, 1);

        gfx.blit(GUI_TEXTURE,
                leftPos + 15,
                topPos + 23 + (38 - powerScaled) + 1,
                0, 171,
                6, powerScaled
        );

        // INPUT TANK BAR
        int inputStored = menu.getInputFluidStored();
        int inputCap = menu.getInputFluidCapacity();
        int inputScaled = inputCap > 0 ? (inputStored * 38 / inputCap) : 0;
        if (inputStored > 0 && inputScaled <= 0) inputScaled = 1;

        gfx.blit(GUI_TEXTURE, leftPos + 38, topPos + 23, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 38, topPos + 24, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 38, topPos + 62, 176, 57, 8, 1);

        int inputColor = 0xFF00D8FF;
        int inputBarHeight = Math.max(1, inputScaled);

        gfx.fill(
                leftPos + 39,
                topPos + 24 + (38 - inputBarHeight),
                leftPos + 39 + 6,
                topPos + 24 + 38,
                inputColor
        );

        // OUTPUT TANK BAR
        int outputStored = menu.getOutputFluidStored();
        int outputCap = menu.getOutputFluidCapacity();
        int outputScaled = outputCap > 0 ? (outputStored * 38 / outputCap) : 0;
        if (outputStored > 0 && outputScaled <= 0) outputScaled = 1;

        gfx.blit(GUI_TEXTURE, leftPos + 142, topPos + 23, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 142, topPos + 24, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 142, topPos + 62, 176, 57, 8, 1);

        int outputColor = 0xFFFF6EEA;
        int outputBarHeight = Math.max(1, outputScaled);

        gfx.fill(
                leftPos + 143,
                topPos + 24 + (38 - outputBarHeight),
                leftPos + 143 + 6,
                topPos + 24 + 38,
                outputColor
        );

        // PROGRESS BAR
        int progress = menu.getProgressScaled(65);

        gfx.blit(PROGRESS_TEXTURE, leftPos + 80, topPos + 11, 0, 0, 31, 65);
        gfx.blit(PROGRESS_TEXTURE,
                leftPos + 80,
                topPos + 11 + (65 - progress),
                0, (65 - progress),
                31, progress);

        if (menu.isRecipeRunning()) {
            int overlayMax = 50;
            int overlayHeight = (progress * overlayMax) / 65;

            gfx.blit(PROGRESS_TEXTURE,
                    leftPos + 84,
                    topPos + 27 + (overlayMax - overlayHeight),
                    31, (overlayMax - overlayHeight),
                    23, overlayHeight);

            RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        }

        // STATUS
        String status = "Idle";
        int color = 0x404040;

        if (menu.getProgress() > 0 && menu.getProgress() < menu.getMaxProgress()) {
            status = "Processing";
            color = 0x2acb5a;
        } else if (maxEnergy > 0 && energy <= 0) {
            status = "No power";
            color = 0xff5555;
        } else if (inputCap > 0 && inputStored <= 0) {
            status = "No fluid";
            color = 0x4aa3ff;
        }

        gfx.drawString(this.font, status, leftPos + 8, topPos + 75, color, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        java.util.function.Function<String, String> localizeFluid = (raw) -> {
            if (raw == null || raw.isEmpty()) return "Empty";
            try {
                ResourceLocation rl = ResourceLocation.parse(raw);
                var fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(rl);
                if (fluid != null) {
                    return new net.neoforged.neoforge.fluids.FluidStack(fluid, 1).getDisplayName().getString();
                }
            } catch (Exception ignored) {}
            return raw;
        };

        int powerX = leftPos + 14;
        int powerY = topPos + 23;
        if (mouseX >= powerX && mouseX <= powerX + 6 &&
                mouseY >= powerY && mouseY <= powerY + 38) {
            gfx.renderTooltip(this.font,
                    Component.literal("Power: " + menu.getPowerStored() + " / " + menu.getMaxPower() + " FE"),
                    mouseX, mouseY);
        }

        int inX = leftPos + 38;
        int inY = topPos + 23;
        if (mouseX >= inX && mouseX <= inX + 6 &&
                mouseY >= inY && mouseY <= inY + 38) {
            String name = localizeFluid.apply(menu.getInputFluidName());
            gfx.renderTooltip(this.font,
                    Component.literal(name + ": " + menu.getInputFluidStored() + " / " + menu.getInputFluidCapacity() + " mB"),
                    mouseX, mouseY);
        }

        int outX = leftPos + 142;
        int outY = topPos + 23;
        if (mouseX >= outX && mouseX <= outX + 6 &&
                mouseY >= outY && mouseY <= outY + 38) {
            String name = localizeFluid.apply(menu.getOutputFluidName());
            gfx.renderTooltip(this.font,
                    Component.literal(name + ": " + menu.getOutputFluidStored() + " / " + menu.getOutputFluidCapacity() + " mB"),
                    mouseX, mouseY);
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }
}