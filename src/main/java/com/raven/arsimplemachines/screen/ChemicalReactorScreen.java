package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.ChemicalReactorMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

        // Background
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // ENERGY BAR
        // -------------------------
        int energy = menu.getPowerScaled(48);
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 8,
                topPos + 16 + (48 - energy),
                176, 16,
                16, energy
        );

        // -------------------------
        // HYDROGEN TANK
        // -------------------------
        int hydrogen = menu.getHydrogenScaled(48);
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 40,
                topPos + 16 + (48 - hydrogen),
                192, 16,
                16, hydrogen
        );

        // -------------------------
        // OXYGEN TANK
        // -------------------------
        int oxygen = menu.getOxygenScaled(48);
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 72,
                topPos + 16 + (48 - oxygen),
                192, 16,
                16, oxygen
        );

        // -------------------------
        // OUTPUT TANK
        // -------------------------
        int output = menu.getOutputScaled(48);
        gfx.blit(
                GUI_TEXTURE,
                leftPos + 152,
                topPos + 16 + (48 - output),
                192, 16,
                16, output
        );
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.title, 8, 6, 0x404040, false);
        gfx.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }
}
