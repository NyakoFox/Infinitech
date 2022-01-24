package gay.nyako.infinitech.block;

import gay.nyako.infinitech.WEnergyBar;
import gay.nyako.infinitech.WSideButton;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class MachineUtil {
    public enum SideTypes {
        UNSET,
        INPUT,
        OUTPUT,
        BOTH,
        OFF
    }

    public enum Sides {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    public static Direction SideToRelativeDirection(Sides side, Direction direction) {
        return switch (side) {
            case FRONT -> direction;
            case BACK -> direction.getOpposite();
            case LEFT -> direction.rotateClockwise(Direction.Axis.Y);
            case RIGHT -> direction.rotateCounterclockwise(Direction.Axis.Y);
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            default -> direction;
        };
    }

    public static Sides DirectionToSide(Direction base, Direction direction) {
        if (direction.rotateCounterclockwise(Direction.Axis.Y) == base) {
            return Sides.LEFT;
        } else if (direction.rotateClockwise(Direction.Axis.Y) == base) {
            return Sides.RIGHT;
        } else if (direction.getOpposite() == base) {
            return Sides.BACK;
        } else if (direction == base) {
            return Sides.FRONT;
        } else if (direction == Direction.UP) {
            return Sides.TOP;
        } else if (direction == Direction.DOWN) {
            return Sides.BOTTOM;
        }
        return Sides.FRONT;
    }


    public static class ColoredSlotBackgroundPainter implements BackgroundPainter {
        private Identifier coloredSlotSprite = new Identifier("infinitech:textures/gui/specialslots.png");
        SideTypes sideType;

        public ColoredSlotBackgroundPainter(SideTypes sideType) {
            this.sideType = sideType;
        }

        @Override
        public void paintBackground(MatrixStack matrices, int left, int top, WWidget panel) {
            float imageWidth = 36f;
            float imageHeight = 18f;
            switch (sideType) {
                case INPUT -> ScreenDrawing.texturedRect(matrices, left, top, panel.getWidth(), panel.getHeight(), coloredSlotSprite, 0f / imageWidth, 0f / imageHeight, 18f / imageWidth, 18f / imageHeight, 0xFF_FFFFFF);
                case OUTPUT -> ScreenDrawing.texturedRect(matrices, left, top, panel.getWidth(), panel.getHeight(), coloredSlotSprite, 18f / imageWidth, 0f / imageHeight, 36f / imageWidth, 18f / imageHeight, 0xFF_FFFFFF);
            }
        }
    }

    public static void ColorSlot(WItemSlot itemSlot, SideTypes sideType) {
        itemSlot.setBackgroundPainter(new ColoredSlotBackgroundPainter(sideType));
    }

    public static void DrawEnergyBar(int x, int y, WPlainPanel root, Inventory blockInventory, AbstractMachineBlockEntity blockEntity, int field, int oldField) {
        WEnergyBar energyBar = new WEnergyBar(field, oldField, (int) blockEntity.capacity, true);
        energyBar.setTooltipCallback(information -> {
            information.add(Text.of("Transfer rate: " + blockEntity.transferRate + " E/t"));
        });
        root.add(energyBar, x, y, 8, 42);

        if (blockEntity.hasBatterySlot()) {
            WItemSlot itemSlot = WItemSlot.of(blockInventory, blockEntity.getBatteryIndex());
            root.add(itemSlot, x - 5, y + 45);
        }
    }

    public static void DrawSideButtons(int xoff, int yoff, WPlainPanel root, AbstractMachineBlockEntity blockEntity, boolean inputDisabled, boolean ouputDisabled) {
        root.add(new WSideButton(blockEntity.sides.get(MachineUtil.Sides.LEFT  ), MachineUtil.Sides.LEFT,   blockEntity.getPos(), inputDisabled, ouputDisabled), xoff + 0,  yoff + 10, 10, 10);
        root.add(new WSideButton(blockEntity.sides.get(MachineUtil.Sides.FRONT ), MachineUtil.Sides.FRONT,  blockEntity.getPos(), inputDisabled, ouputDisabled), xoff + 10, yoff + 10, 10, 10);
        root.add(new WSideButton(blockEntity.sides.get(MachineUtil.Sides.RIGHT ), MachineUtil.Sides.RIGHT,  blockEntity.getPos(), inputDisabled, ouputDisabled), xoff + 20, yoff + 10, 10, 10);
        root.add(new WSideButton(blockEntity.sides.get(MachineUtil.Sides.TOP   ), MachineUtil.Sides.TOP,    blockEntity.getPos(), inputDisabled, ouputDisabled), xoff + 10, yoff + 0,  10, 10);
        root.add(new WSideButton(blockEntity.sides.get(MachineUtil.Sides.BOTTOM), MachineUtil.Sides.BOTTOM, blockEntity.getPos(), inputDisabled, ouputDisabled), xoff + 10, yoff + 20, 10, 10);
        root.add(new WSideButton(blockEntity.sides.get(MachineUtil.Sides.BACK  ), MachineUtil.Sides.BACK,   blockEntity.getPos(), inputDisabled, ouputDisabled), xoff + 20, yoff + 20, 10, 10);
    }
}
