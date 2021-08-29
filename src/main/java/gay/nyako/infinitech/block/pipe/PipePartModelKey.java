package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.render.PartModelKey;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.*;

public class PipePartModelKey extends PartModelKey {
    public final Class<?> clazz;
    public final SpriteIdentifier spriteIdentifier;
    public final Set<Direction> connections;

    public PipePartModelKey(AbstractPipePart pipePart) {
        this.clazz = pipePart.getClass();
        this.spriteIdentifier = pipePart.getSpriteIdentifier();
        this.connections = pipePart.getConnectedSides();
    }

    public double getCenterSize() {
        return 5;
    }

    public PipeShape getCenterShape() {
        var min = 8 - getCenterSize()/2;
        var max = 8 + getCenterSize()/2;
        return PipeShape.fromBlockCoords(null, min, min, min, max, max, max);
    }

    public List<PipeShape> getConnectionShapes() {
        if (connections.size() > 0) {
            return new ArrayList<>(connections.stream().map(PipeShape::of).toList());
        } else {
            return new ArrayList<>();
        }
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
