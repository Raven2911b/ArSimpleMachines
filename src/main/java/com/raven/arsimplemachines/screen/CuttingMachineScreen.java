package com.raven.arsimplemachines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.CuttingMachineMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CuttingMachineScreen extends AbstractContainerScreen<CuttingMachineMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/generic_menu.png");
    private static final ResourceLocation CUTTER_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID,"textures/gui/progressbars.png"
            );
    public CuttingMachineScreen(CuttingMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = 9999; // hide inventory label
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // Suppress default labels
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int labelY = topPos + 5;

        // BACKGROUND
        gfx.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // -------------------------
        // POWER BAR
        // -------------------------
        gfx.drawString(this.font, "ᴘᴡʀ", leftPos + 8, labelY, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 11,  topPos + 16, 176, 18, 8, 1);
        gfx.blit(GUI_TEXTURE, leftPos + 11,  topPos + 17, 176, 19, 8, 38);
        gfx.blit(GUI_TEXTURE, leftPos + 11,  topPos + 55, 176, 57, 8, 1);

        int energy = menu.getPowerStored();
        int maxEnergy = menu.getMaxPower();
        int scaled = menu.getPowerScaled(38);

        gfx.blit(GUI_TEXTURE,
                leftPos + 11 + 1,
                topPos + 16 + (38 - scaled) + 1,
                0, 171,
                6, scaled);

        // -------------------------
        // INPUT CHECK
        // -------------------------
        boolean noInput = menu.getSlot(0).getItem().isEmpty();
        boolean noEnergy = (energy <= 20);

        // -------------------------
        // SLOT FRAMES
        // -------------------------
        int slotU = 177;
        int slotV = 0;

        gfx.blit(GUI_TEXTURE, leftPos + 44, topPos + 25, slotU, slotV, 18, 18);
        gfx.drawString(this.font, "IN", leftPos + 47, topPos + 15, 0x404040, false);

        gfx.blit(GUI_TEXTURE, leftPos + 116, topPos + 25, slotU, slotV, 18, 18);
        gfx.drawString(this.font, "OUT", leftPos + 116, topPos + 15, 0x404040, false);

        // -------------------------
// PROGRESS BAR (TEXTURED, LEFT → RIGHT)
// -------------------------

// Scale progress to the fill width (37 px)
        int progress = menu.getProgressScaled(37);

// Draw background frame (static)
        gfx.blit(CUTTER_PROGRESS_TEXTURE,
                leftPos + 65,          // X position of bar
                topPos + 25,           // Y position of bar
                55, 0,                 // U, V of background frame
                40, 42);               // width, height

// Draw animated fill (left → right)
        gfx.blit(CUTTER_PROGRESS_TEXTURE,
                leftPos + 65,          // X stays fixed
                topPos + 25 + 4,       // slight vertical inset (optional)
                95, 0,                 // U start of fill region
                progress,              // width grows with progress
                35);                   // height stays constant

        // -------------------------
        // STATUS MESSAGE
        // -------------------------
        String msg;
        int color;

        if (noInput) {
            msg = "Idle";
            color = 0x404040;
        }
        else if (noEnergy) {
            msg = "Not enough energy";
            color = 0xFF5555;
        }
        else if (menu.getProgress() >= menu.getMaxProgress()) {
            msg = "Complete";
            color = 0xC0C0C0;
        }
        else {
            msg = "Cutting...";
            color = 0x228B22;
        }

        gfx.drawString(this.font, msg, leftPos + 8, topPos + 75, color, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}
