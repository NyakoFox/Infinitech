package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.render.PartModelKey;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.Direction;

import java.util.*;

public class PipePartModelKey extends PartModelKey implements PipeShapeContext {
    public final Class<?> clazz;
    public final SpriteIdentifier spriteId;
    public final SpriteIdentifier endSpriteId;
    public final Map<Direction, List<PipeTypes>> connections;
    public final PipeTypes pipeType;

    public PipePartModelKey(AbstractPipePart pipePart) {
        this.clazz = pipePart.getClass();
        this.spriteId = pipePart.getSpriteId();
        this.endSpriteId = pipePart.getEndSpriteId();
        this.connections = pipePart.getContainerConnections();
        this.pipeType = pipePart.getPipeType();
    }

    public Sprite getSprite() {
        return spriteId.getSprite();
    }

    public Sprite getEndSprite() { return endSpriteId.getSprite(); }

    @Override
    public PipeTypes getPipeType() {
        return pipeType;
    }

    @Override
    public Map<Direction, List<PipeTypes>> getContainerConnections() {
        return connections;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PipePartModelKey key) {
            return key.clazz == clazz && connections.equals(key.connections) && key.spriteId == spriteId && key.pipeType == pipeType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, spriteId, connections, pipeType);
    }
}
