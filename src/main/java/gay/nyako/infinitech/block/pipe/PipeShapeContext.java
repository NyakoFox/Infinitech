package gay.nyako.infinitech.block.pipe;

import net.minecraft.util.math.Direction;

import java.util.*;

public interface PipeShapeContext {
    PipeTypes getPipeType();
    List<PipeTypes> getAllPipeTypes();

    List<Direction> getBlockConnectionDirs();

    Map<PipeTypes, List<Direction>> getPipeConnectionMap();

    default List<Direction> getConnectionsFor(PipeTypes type) {
        return getPipeConnectionMap().getOrDefault(type, new ArrayList<>());
    }

    default List<PipeTypes> getPipesFacing(Direction direction) {
        var result = new ArrayList<PipeTypes>();
        for (var type : getAllPipeTypes()) {
            if (getConnectionsFor(type).contains(direction)) {
                result.add(type);
            }
        }
        return result;
    }
}
