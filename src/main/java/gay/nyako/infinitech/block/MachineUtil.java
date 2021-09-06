package gay.nyako.infinitech.block;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.util.math.MatrixStack;
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
}
