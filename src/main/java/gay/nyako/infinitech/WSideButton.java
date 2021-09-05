package gay.nyako.infinitech;

import gay.nyako.infinitech.block.MachineUtil;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class WSideButton extends WWidget {
    private static final Identifier BUTTONS = new Identifier(InfinitechMod.MOD_ID, "textures/gui/sidebuttons.png");
    public MachineUtil.SideTypes side_id;
    public BlockPos blockPos;
    public MachineUtil.Sides side;
    public boolean inputDisabled;
    public boolean outputDisabled;

    public WSideButton(MachineUtil.SideTypes side_id, MachineUtil.Sides side, BlockPos blockPos, boolean inputDisabled, boolean outputDisabled) {
        this.side_id = side_id;
        this.side = side;
        this.blockPos = blockPos;
        this.inputDisabled = inputDisabled;
        this.outputDisabled = outputDisabled;
    }

    public WSideButton(MachineUtil.SideTypes side_id, MachineUtil.Sides side, BlockPos blockPos) {
        this.side_id = side_id;
        this.side = side;
        this.blockPos = blockPos;
        this.inputDisabled = false;
        this.outputDisabled = false;
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        int button_id = side_id.ordinal();
        boolean button_hovered = (mouseX>=0 && mouseY>=0 && mouseX<getWidth() && mouseY<getHeight());

        ScreenDrawing.texturedRect(matrices, x, y, width, height, BUTTONS, (button_hovered ? 0.5f : 0f), (button_id * 10f) / 50f, (button_hovered ? 1f : 0.5f), (10f + (button_id * 10f)) / 50f, 0xFFFFFFFF);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InputResult onClick(int x, int y, int button) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        if (button == 0) {
            int button_id = (side_id.ordinal() + 1) % 5;
            if (inputDisabled && button_id == 1) button_id++;
            if (outputDisabled && ((button_id == 2) || (button_id == 3))) button_id = 4;
            side_id = MachineUtil.SideTypes.values()[button_id];
        } else if (button == 1) {
            int button_id = (side_id.ordinal() - 1);
            if (button_id < 0) button_id = 4;

            if (outputDisabled && ((button_id == 2) || (button_id == 3))) button_id = 1;
            if (inputDisabled && button_id == 1) button_id--;
            side_id = MachineUtil.SideTypes.values()[button_id];
        }

        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeEnumConstant(side);
        packet.writeEnumConstant(side_id);
        packet.writeBlockPos(blockPos);
        ClientSidePacketRegistry.INSTANCE.sendToServer(InfinitechMod.SIDE_CHOICE_UI_PACKET_ID, packet);

        return InputResult.PROCESSED;
    }
}
