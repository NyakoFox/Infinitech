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
import net.minecraft.util.math.Direction;

public class WSideButton extends WWidget {
    private static final Identifier BUTTONS = new Identifier(InfinitechMod.MOD_ID, "textures/gui/sidebuttons.png");
    public MachineUtil.SideTypes side_id;
    public BlockPos blockPos;
    public MachineUtil.Sides side;

    public WSideButton(MachineUtil.SideTypes side_id, MachineUtil.Sides side, BlockPos blockPos) {
        this.side_id = side_id;
        this.side = side;
        this.blockPos = blockPos;
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
        int button_id = (side_id.ordinal() + 1) % 5;
        side_id = MachineUtil.SideTypes.values()[button_id];

        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeEnumConstant(side);
        packet.writeEnumConstant(side_id);
        packet.writeBlockPos(blockPos);
        ClientSidePacketRegistry.INSTANCE.sendToServer(InfinitechMod.SIDE_CHOICE_UI_PACKET_ID, packet);

        return InputResult.PROCESSED;
    }
}
