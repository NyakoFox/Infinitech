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
    public final PipeTypes pipeType;
    public final List<PipeTypes> containerPipes;
    public final List<Direction> blockConnectionDirs;
    public final Map<PipeTypes, List<Direction>> connections;

    public PipePartModelKey(AbstractPipePart pipePart) {
        this.clazz = pipePart.getClass();
        this.spriteId = pipePart.getSpriteId();
        this.endSpriteId = pipePart.getEndSpriteId();
        this.containerPipes = pipePart.getAllPipeTypes();
        this.blockConnectionDirs = pipePart.getBlockConnectionDirs();
        this.connections = pipePart.getPipeConnectionMap();
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
    public List<PipeTypes> getAllPipeTypes() {
        return containerPipes;
    }

    @Override
    public List<Direction> getBlockConnectionDirs() {
        return blockConnectionDirs;
    }

    @Override
    public Map<PipeTypes, List<Direction>> getPipeConnectionMap() {
        return connections;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PipePartModelKey key) {
            return key.clazz == clazz && connections.equals(key.connections) && blockConnectionDirs.equals(key.blockConnectionDirs) && key.spriteId == spriteId && key.pipeType == pipeType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, spriteId, connections, blockConnectionDirs, pipeType);
    }
}
