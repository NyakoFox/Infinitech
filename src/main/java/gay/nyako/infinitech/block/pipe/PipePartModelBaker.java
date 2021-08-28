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
    public static SpriteIdentifier ENERGY_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy"));

    @Override
    public void emitQuads(PipePartModelKey key, PartRenderContext ctx) {
        var emitter = ctx.getEmitter();

        emitVoxelShape(emitter, CONNECTOR_SPRITE.getSprite(), key.getCenterShape());
        for (VoxelShape shape : key.getConnectionShapes()) {
            emitVoxelShape(emitter, key.getSprite(), shape);
        }
    }

    private void emitVoxelShape(QuadEmitter emitter, Sprite sprite, VoxelShape shape) {
        var box = shape.getBoundingBox();

        emitSingleQuad(emitter, sprite, Direction.UP, box.minX, 1 - box.maxZ, box.maxX, 1 - box.minZ, 1 - box.maxY);
        emitSingleQuad(emitter, sprite, Direction.DOWN, box.minX, box.minZ, box.maxX, box.maxZ, box.minY);
        emitSingleQuad(emitter, sprite, Direction.EAST, 1 - box.maxZ, box.minY, 1 - box.minZ, box.maxY, 1 - box.maxX);
        emitSingleQuad(emitter, sprite, Direction.WEST, box.minZ, box.minY, box.maxZ, box.maxY, box.minX);
        emitSingleQuad(emitter, sprite, Direction.SOUTH, box.minX, box.minY, box.maxX, box.maxY, 1 - box.maxZ);
        emitSingleQuad(emitter, sprite, Direction.NORTH, 1 - box.maxX, box.minY, 1 - box.minX, box.maxY, box.minZ);
    }

    private void emitSingleQuad(QuadEmitter emitter, Sprite sprite, Direction direction, double left, double bottom, double right, double top, double depth) {
        emitter.square(direction, (float)left, (float)bottom, (float)right, (float)top, (float)depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
    }
}
