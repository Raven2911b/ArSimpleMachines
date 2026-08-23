package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.ChemicalReactorMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class ChemicalReactorScreen extends AbstractContainerScreen<ChemicalReactorMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/generic_menu.png"
            );

    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int labelY = topPos + 5;

        // Background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // ENERGY BAR A
        // -------------------------
        gfx.drawString(this.font, "P1", leftPos + 8, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 55, 176, 57, 8, 1);

        int energyA = menu.getEnergyScaledA(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 9,
                topPos + 17 + (38 - energyA),
                0, 171,
                6, energyA);

        // -------------------------
        // ENERGY BAR B
        // -------------------------
        gfx.drawString(this.font, "P2", leftPos + 24, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 24, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 24, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 24, topPos + 55, 176, 57, 8, 1);

        int energyB = menu.getEnergyScaledB(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 25,
                topPos + 17 + (38 - energyB),
                0, 171,
                6, energyB);

        // -------------------------
        // INPUT TANK A
        // -------------------------
        gfx.drawString(this.font, "A", leftPos + 70, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 55, 176, 57, 8, 1);

        int inputAHeight = menu.getInputAScaled(38);
        gfx.fill(
                leftPos + 71,
                topPos + 17 + (38 - inputAHeight),
                leftPos + 71 + 6,
                topPos + 17 + 38,
                0xFF808080
        );

        // -------------------------
        // INPUT TANK B
        // -------------------------
        gfx.drawString(this.font, "B", leftPos + 100, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 100, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 100, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 100, topPos + 55, 176, 57, 8, 1);

        int inputBHeight = menu.getInputBScaled(38);
        gfx.fill(
                leftPos + 101,
                topPos + 17 + (38 - inputBHeight),
                leftPos + 101 + 6,
                topPos + 17 + 38,
                0xFF00FFFF
        );

        // -------------------------
        // OUTPUT TANK
        // -------------------------
        gfx.drawString(this.font, "OUT", leftPos + 130, labelY, 0x404040, false);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 140, topPos + 55, 176, 57, 8, 1);

        int outputHeight = menu.getOutputScaled(38);
        gfx.fill(
                leftPos + 141,
                topPos + 17 + (38 - outputHeight),
                leftPos + 141 + 6,
                topPos + 17 + 38,
                0xFF66A3FF
        );

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg = menu.getStatusMessage();
        int color = switch (msg) {
            case "Not enough energy" -> 0xFF5555;
            case "Missing input fluid A" -> 0xFF5555;
            case "Missing input fluid B" -> 0xFF5555;
            case "Output tank full" -> 0xFFFF55;
            case "Processing..." -> 0x228B22;
            case "Idle" -> 0x404040;
            default -> 0xC0C0C0;
        };

        gfx.drawString(
                this.font,
                msg,
                leftPos + 8,
                topPos + 60,
                color,
                false
        );
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // Helper: convert registry ID → localized fluid name
        java.util.function.Function<String, String> localizeFluid = (raw) -> {
            if (raw == null || raw.isEmpty()) return "Empty";
            try {
                ResourceLocation rl = ResourceLocation.parse(raw);
                var fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(rl);
                if (fluid != null) {
                    return new FluidStack(fluid, 1).getDisplayName().getString();
                }
            } catch (Exception ignored) {}
            return raw; // fallback
        };

        // INPUT A tooltip
        int tankAX = leftPos + 71;
        int tankAY = topPos + 17;
        if (mouseX >= tankAX && mouseX <= tankAX + 6 &&
                mouseY >= tankAY && mouseY <= tankAY + 38) {

            String name = localizeFluid.apply(menu.getInputAName());

            gfx.renderTooltip(
                    this.font,
                    Component.literal(name + ": " +
                            menu.getInputAAmount() + " / " +
                            menu.getInputACapacity() + " mB"),
                    mouseX, mouseY
            );
        }

        // INPUT B tooltip
        int tankBX = leftPos + 101;
        int tankBY = topPos + 17;
        if (mouseX >= tankBX && mouseX <= tankBX + 6 &&
                mouseY >= tankBY && mouseY <= tankBY + 38) {

            String name = localizeFluid.apply(menu.getInputBName());

            gfx.renderTooltip(
                    this.font,
                    Component.literal(name + ": " +
                            menu.getInputBAmount() + " / " +
                            menu.getInputBCapacity() + " mB"),
                    mouseX, mouseY
            );
        }

        // OUTPUT tooltip
        int outX = leftPos + 141;
        int outY = topPos + 17;
        if (mouseX >= outX && mouseX <= outX + 6 &&
                mouseY >= outY && mouseY <= outY + 38) {

            String name = localizeFluid.apply(menu.getOutputName());

            gfx.renderTooltip(
                    this.font,
                    Component.literal(name + ": " +
                            menu.getOutputAmount() + " / " +
                            menu.getOutputCapacity() + " mB"),
                    mouseX, mouseY
            );
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }



    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }
}
