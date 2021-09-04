package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.render.PartModelBaker;
import alexiil.mc.lib.multipart.api.render.PartRenderContext;
import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class PipePartModelBaker implements PartModelBaker<PipePartModelKey> {
    public static SpriteIdentifier CONNECTOR_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/connector"));

    @Override
    public void emitQuads(PipePartModelKey key, PartRenderContext ctx) {
        var emitter = ctx.getEmitter();

        var centerShape = PipeShape.getCenterShape(key);
        if (centerShape != null) {
            centerShape.emit(emitter, CONNECTOR_SPRITE.getSprite(), null);
        }
        for (PipeShape shape : PipeShape.getConnectionShapes(key)) {
            shape.emit(emitter, key.getSprite(), key.getEndSprite());
        }
    }
}
