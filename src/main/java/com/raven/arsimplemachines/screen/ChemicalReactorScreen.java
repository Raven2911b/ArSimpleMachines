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
    private static final ResourceLocation CHEM_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArSimpleMachines.MODID,
                    "textures/gui/progressbars.png"
            );

    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = this.imageHeight - 94;
    }
    private int getFluidTint(FluidStack stack) {
        if (stack.isEmpty()) return 0xFFFFFFFF;

        var id = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id == null) return 0xFFFFFFFF;

        // Oxygen → cyan
        if (id.equals(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "oxygen"))) {
            return 0xFF00FFFF;
        }

        // Hydrogen → light pink
        if (id.equals(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "hydrogen"))) {
            return 0xFFFFAACC;
        }
        // Rocket Fuel → light pink
        if (id.equals(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "rocket_fuel"))) {
            return 0xFFE6A300;
        }
        // Default tint
        return net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
                .of(stack.getFluid())
                .getTintColor(stack);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int labelY = topPos + 5;

        // Background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // ENERGY BAR A (DOWN +6)
        // -------------------------
        gfx.drawString(this.font, "ᴄʜᴇᴍɪᴄᴀʟ ʀᴇᴀᴄᴛᴏʀ", leftPos + 5, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 8,  topPos + 61, 176, 57, 8, 1);

        int energyA = menu.getEnergyScaledA(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 9,
                topPos + 23 + (38 - energyA),
                0, 171,
                6, energyA);

        // -------------------------
        // ENERGY BAR B (DOWN +6, LEFT -4)
        // -------------------------
        gfx.blit(GUI_TEXTURE, leftPos + 20, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 20, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 20, topPos + 61, 176, 57, 8, 1);

        int energyB = menu.getEnergyScaledB(38);
        gfx.blit(GUI_TEXTURE,
                leftPos + 21,
                topPos + 23 + (38 - energyB),
                0, 171,
                6, energyB);

        // -------------------------
        // INPUT TANK A (DOWN +6)
        // -------------------------
        gfx.drawString(this.font, "ᴀ", leftPos + 51, labelY + 8, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 50, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 50, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 50, topPos + 61, 176, 57, 8, 1);

        int inputAHeight = menu.getInputAScaled(38);
        FluidStack inA = new FluidStack(
                net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                        ResourceLocation.parse(menu.getInputAName())
                ),
                menu.getInputAAmount()
        );

        int tintA = getFluidTint(inA);

        gfx.fill(
                leftPos + 51,
                topPos + 23 + (38 - inputAHeight),
                leftPos + 51 + 6,
                topPos + 23 + 38,
                tintA
        );

        // -------------------------
        // INPUT TANK B (DOWN +6)
        // -------------------------
        gfx.drawString(this.font, "ʙ", leftPos + 71, labelY + 8, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 70, topPos + 61, 176, 57, 8, 1);

        int inputBHeight = menu.getInputBScaled(38);
        FluidStack inB = new FluidStack(
                net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                        ResourceLocation.parse(menu.getInputBName())
                ),
                menu.getInputBAmount()
        );

        int tintB = getFluidTint(inB);

        gfx.fill(
                leftPos + 71,
                topPos + 23 + (38 - inputBHeight),
                leftPos + 71 + 6,
                topPos + 23 + 38,
                tintB
        );

        // -------------------------
        // OUTPUT TANK (DOWN +6)
        // -------------------------
        gfx.drawString(this.font, "ᴏᴜᴛ", leftPos + 151, labelY + 8, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 153, topPos + 22, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 153, topPos + 23, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 153, topPos + 61, 176, 57, 8, 1);

        int outputHeight = menu.getOutputScaled(38);
        FluidStack out = new FluidStack(
                net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                        ResourceLocation.parse(menu.getOutputName())
                ),
                menu.getOutputAmount()
        );

        int tintOut = getFluidTint(out);

        gfx.fill(
                leftPos + 154,
                topPos + 23 + (38 - outputHeight),
                leftPos + 154 + 6,
                topPos + 23 + 38,
                tintOut
        );

        // -------------------------
        // PROGRESS BAR (DOWN +6)
        // -------------------------
        int progress = menu.getProgressScaled(65);

        gfx.blit(CHEM_PROGRESS_TEXTURE,
                leftPos + 100,
                topPos + 11,
                0, 0,
                31, 65);

        gfx.blit(CHEM_PROGRESS_TEXTURE,
                leftPos + 100,
                topPos + 11 + (65 - progress),
                0, (65 - progress),
                31, progress);

        if (menu.getProgress() > 0 && menu.getProgress() < menu.getMaxProgress()) {
            int overlayMax = 50;
            int overlayHeight = (progress * overlayMax) / 65;

            gfx.blit(CHEM_PROGRESS_TEXTURE,
                    leftPos + 100 + 4,
                    topPos + 27 + (overlayMax - overlayHeight),
                    31, (overlayMax - overlayHeight),
                    23, overlayHeight);

            RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        }

        // -------------------------
        // STATUS MESSAGE (DOWN +3)
        // -------------------------
        String msg = menu.getStatusMessage();
        int color = switch (msg) {
            case "Not enough energy" -> 0xFF5555;
            case "Output tank full" -> 0xFFFF55;
            case "Processing..." -> 0x228B22;
            case "Idle" -> 0x404040;
            default -> 0xC0C0C0;
        };

        gfx.drawString(
                this.font,
                msg,
                leftPos + 8,
                topPos + 78,
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

        // -------------------------
        // POWER BAR A TOOLTIP
        // -------------------------
        int pAX = leftPos + 8 + 1;   // inside frame
        int pAY = topPos + 22 + 1;
        int pAW = 6;
        int pAH = 38;

        int storedA = menu.getEnergyStoredA();
        int maxA = menu.getEnergyMaxA();

        if (mouseX >= pAX && mouseX <= pAX + pAW &&
                mouseY >= pAY && mouseY <= pAY + pAH) {

            if (storedA <= 0) {
                gfx.renderTooltip(this.font,
                        Component.literal("Power Input A offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy A: " + storedA + " / " + maxA + " FE"),
                        mouseX, mouseY);
            }
        }

        // -------------------------
        // POWER BAR B TOOLTIP
        // -------------------------
        int pBX = leftPos + 20 + 1;
        int pBY = topPos + 22 + 1;
        int pBW = 6;
        int pBH = 38;

        int storedB = menu.getEnergyStoredB();
        int maxB = menu.getEnergyMaxB();

        if (mouseX >= pBX && mouseX <= pBX + pBW &&
                mouseY >= pBY && mouseY <= pBY + pBH) {

            if (storedB <= 0) {
                gfx.renderTooltip(this.font,
                        Component.literal("Power Input B offline"),
                        mouseX, mouseY);
            } else {
                gfx.renderTooltip(this.font,
                        Component.literal("Energy B: " + storedB + " / " + maxB + " FE"),
                        mouseX, mouseY);
            }
        }

        // -------------------------
        // INPUT A tooltip (DOWN +6)
        // -------------------------
        int tankAX = leftPos + 50;
        int tankAY = topPos + 23;
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

        // -------------------------
        // INPUT B tooltip (DOWN +6)
        // -------------------------
        int tankBX = leftPos + 70;
        int tankBY = topPos + 23;
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

        // -------------------------
        // OUTPUT tooltip (DOWN +6)
        // -------------------------
        int outX = leftPos + 153;
        int outY = topPos + 23;
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
      //  gfx.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }
}
