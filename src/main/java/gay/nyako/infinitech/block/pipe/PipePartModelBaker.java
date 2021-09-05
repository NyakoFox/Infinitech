package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.render.PartModelBaker;
import alexiil.mc.lib.multipart.api.render.PartRenderContext;
import gay.nyako.infinitech.InfinitechMod;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

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
