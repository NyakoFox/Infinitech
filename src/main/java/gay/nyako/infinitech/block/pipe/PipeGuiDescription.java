package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.impl.MultipartBlockEntity;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.MachineUtil;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTabPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PipeGuiDescription extends SyncedGuiDescription {
    public Direction direction;

    public PipeGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, BlockPos blockPos, long uniqueId) {
        super(InfinitechMod.PIPE_GUI_SCREEN_HANDLER, syncId, playerInventory);


        MultipartBlockEntity multipartBlockEntity = (MultipartBlockEntity) world.getBlockEntity(blockPos);
        AbstractPipePart mainPipe = (AbstractPipePart) multipartBlockEntity.getContainer().getPart(uniqueId);
        WTabPanel tabs = new WTabPanel();

        this.direction = mainPipe.lastDirection;

        var container = multipartBlockEntity.getContainer();
        var parts = container.getAllParts();
        for (AbstractPart abstractPart : parts) {
            AbstractPipePart pipePart = (AbstractPipePart) abstractPart;
            AbstractIOPipePart ioPipePart = null;
            if (pipePart instanceof AbstractIOPipePart) {
                ioPipePart = (AbstractIOPipePart) pipePart;
            }

            if (!pipePart.getConnectedSides().contains(direction)) continue;

            WPlainPanel testPanel = new WPlainPanel();

            testPanel.setSize(176, 166);
            testPanel.setInsets(Insets.ROOT_PANEL);


            testPanel.add(new WLabel(pipePart.getDisplayName()), 0, 0);

            if (ioPipePart != null) {
                WButton button = new WButton(Text.of("Current mode: " + ioPipePart.getMode(direction)));
                AbstractIOPipePart finalIoPipePart = ioPipePart;
                button.setOnClick(() -> {
                    // This code runs on the client when you click the button.
                    AbstractIOPipePart.Mode nextMode = finalIoPipePart.nextMode(direction);
                    finalIoPipePart.setMode(direction, nextMode);
                    button.setLabel(Text.of("Current mode: " + nextMode.toString()));

                    PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                    packet.writeEnumConstant(nextMode);
                    packet.writeEnumConstant(direction);
                    packet.writeBlockPos(blockPos);
                    packet.writeLong(abstractPart.holder.getUniqueId());
                    ClientSidePacketRegistry.INSTANCE.sendToServer(InfinitechMod.SWITCH_PIPE_MODE_PACKET_ID, packet);
                });
                testPanel.add(button, 0, 12, 140, 16);
            }

            testPanel.add(this.createPlayerInventoryPanel(), 0, 65);

            tabs.add(testPanel, tab -> tab.icon(new ItemIcon(pipePart.getPickStack())).tooltip(pipePart.getDisplayName()));
            //tabs.add(panelB, tab -> tab.title(new TranslatableText("gui.my_mod.panel_b")))
            //tabs.add(panelC, tab -> tab.title(new LiteralText("Hello!")).icon(new ItemIcon(new ItemStack(Items.BREAD))).tooltip(new LiteralText("I am a tooltip!")));
        }

        setRootPanel(tabs);

        setTitleAlignment(HorizontalAlignment.CENTER);

        tabs.validate(this);
    }

    @Override
    public void addPainters() {
        return;
    }

    @Override
    public boolean isTitleVisible() {
        return false;
    }
}
