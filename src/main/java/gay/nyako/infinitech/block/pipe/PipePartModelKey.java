package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.render.PartModelKey;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PipePartModelKey extends PartModelKey {
    public final Class<?> clazz;
    public final SpriteIdentifier spriteIdentifier;
    public final Set<Direction> connections;

    public PipePartModelKey(AbstractPipePart pipePart) {
        this.clazz = pipePart.getClass();
        this.spriteIdentifier = pipePart.getSpriteIdentifier();
        this.connections = pipePart.getConnectedSides();
    }

    public VoxelShape getCenterShape() {
        return Block.createCuboidShape(5.5, 5.5, 5.5, 10.5, 10.5, 10.5);
    }

    public List<VoxelShape> getConnectionShapes() {
        var list = Lists.<VoxelShape>newArrayList();
        if (connections.contains(Direction.NORTH)) {
            list.add(Block.createCuboidShape(6, 6, 0, 10, 10, 6));
        }
        if (connections.contains(Direction.EAST)) {
            list.add(Block.createCuboidShape(10, 6, 6, 16, 10, 10));
        }
        if (connections.contains(Direction.SOUTH)) {
            list.add(Block.createCuboidShape(6, 6, 10, 10, 10, 16));
        }
        if (connections.contains(Direction.WEST)) {
            list.add(Block.createCuboidShape(0, 6, 6, 6, 10, 10));
        }
        if (connections.contains(Direction.UP)) {
            list.add(Block.createCuboidShape(6, 10, 6, 10, 16, 10));
        }
        if (connections.contains(Direction.DOWN)) {
            list.add(Block.createCuboidShape(6, 0, 6, 10, 6, 10));
        }
        return list;
    }

    public Sprite getSprite() {
        return spriteIdentifier.getSprite();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PipePartModelKey key) {
            return key.clazz == clazz && connections.equals(key.connections);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, spriteIdentifier, connections);
    }
}
