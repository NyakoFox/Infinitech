package gay.nyako.infinitech;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class FurnaceGeneratorScreen extends HandledScreen<ScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    private static final Identifier TEXTURE = new Identifier("infinitech", "textures/gui/container/furnace_generator.png");

    public FurnaceGeneratorScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int l;
        if (((FurnaceGeneratorScreenHandler)this.handler).isBurning()) {
            l = ((FurnaceGeneratorScreenHandler)this.handler).getFuelProgress();
            this.drawTexture(matrices, x + 56, y + 36 + 12 - l, 176, 12 - l, 14, l + 1);
        }

        int power    = ((FurnaceGeneratorScreenHandler)this.handler).getEnergy();
        int powerMax = 200_000;
        int percentage = (int) (((double) power / (double) powerMax) * 48); // no it isnt ?? that's not a percentage ally
        this.drawTexture(matrices, x + 108, y + 67 - percentage, 176, 62 - percentage, 8, percentage + 1);

        if (mouseX > (x + 107) && mouseY > (y + 19) && mouseX < (x + 108 + 4 + 4) && mouseY < (y + 67)) {
            List tooltipList = new ArrayList();
            tooltipList.add(Text.of("Energy stored: " + power + "/200000 E"));
            tooltipList.add(Text.of("Generating: 20 E/t"));
            tooltipList.add(Text.of("Transfer rate: 1000 E/t"));
            this.renderTooltip(matrices, tooltipList, mouseX, mouseY);
        }

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        //int power = ((FurnaceGeneratorScreenHandler) this.handler).getEnergy();
        //String text = Integer.toString(power);
        //int titleX2 = (backgroundWidth - textRenderer.getWidth(text)) / 2;
        //this.textRenderer.draw(matrices, text, (float)titleX2, (float)this.titleY + 64, 16711935);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}