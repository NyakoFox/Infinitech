package gay.nyako.infinitech.block;

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
}
