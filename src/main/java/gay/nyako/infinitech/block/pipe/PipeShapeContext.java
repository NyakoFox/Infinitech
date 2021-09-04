package gay.nyako.infinitech.block.pipe;

import net.minecraft.util.math.Direction;

import java.util.*;

public interface PipeShapeContext {
    PipeTypes getPipeType();

    Map<Direction, List<PipeTypes>> getContainerConnections();

    default List<Direction> getContainerConnections(PipeTypes type) {
        var connections = getContainerConnections();
        var result = new ArrayList<Direction>();
        for (Direction direction : Direction.values()) {
            if (connections.get(direction).contains(type)) {
                result.add(direction);
            }
        }
        return result;
    }
}
