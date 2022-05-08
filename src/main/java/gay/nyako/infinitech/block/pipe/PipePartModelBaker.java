package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.render.PartModelBaker;
import alexiil.mc.lib.multipart.api.render.PartRenderContext;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class PipePartModelBaker implements PartModelBaker<PipePartModelKey> {
    @Override
    public void emitQuads(PipePartModelKey key, PartRenderContext ctx) {
        var emitter = ctx.getEmitter();

        var centerShape = PipeShape.getCenterShape(key);
        if (centerShape != null) {
            centerShape.emit(emitter, key);
        }
        for (var shape : PipeShape.getPipeShapes(key)) {
            shape.emit(emitter, key);
        }
        for (var shape : PipeShape.getConnectorShapes(key)) {
            shape.emit(emitter, key);
        }
    }
}
