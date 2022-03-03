package gay.nyako.infinitech.block.pipe;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.shape.VoxelShape;

public interface PipeShapeBase {
    VoxelShape toVoxelShape();

    void emit(QuadEmitter emitter, PipePartModelKey key);
}
