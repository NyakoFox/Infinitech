package gay.nyako.infinitech.block.pipe;

import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public record PipeConnectorShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) implements PipeShapeBase {
    public static SpriteIdentifier CONNECTOR_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/connector"));

    public static PipeConnectorShape fromBlockCoords(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new PipeConnectorShape(minX/16.0D, minY/16.0D, minZ/16.0D, maxX/16.0D, maxY/16.0D, maxZ/16.0D);
    }

    @Override
    public VoxelShape toVoxelShape() {
        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void emit(QuadEmitter emitter, PipePartModelKey key) {
        for (Direction face : Direction.values()) {
            emitFace(emitter, CONNECTOR_SPRITE.getSprite(), face);
        }
    }

    private void emitFace(QuadEmitter emitter, Sprite sprite, Direction face) {
        switch (face) {
            case UP -> emitSingleQuad(emitter, sprite, Direction.UP, minX, 1 - maxZ, maxX, 1 - minZ, 1 - maxY);
            case DOWN -> emitSingleQuad(emitter, sprite, Direction.DOWN, minX, minZ, maxX, maxZ, minY);
            case EAST -> emitSingleQuad(emitter, sprite, Direction.EAST, 1 - maxZ, minY, 1 - minZ, maxY, 1 - maxX);
            case WEST -> emitSingleQuad(emitter, sprite, Direction.WEST, minZ, minY, maxZ, maxY, minX);
            case SOUTH -> emitSingleQuad(emitter, sprite, Direction.SOUTH, minX, minY, maxX, maxY, 1 - maxZ);
            case NORTH -> emitSingleQuad(emitter, sprite, Direction.NORTH, 1 - maxX, minY, 1 - minX, maxY, minZ);
        }
    }

    private void emitSingleQuad(QuadEmitter emitter, Sprite sprite, Direction face, double left, double bottom, double right, double top, double depth) {
        var vSize = sprite.getMaxV() - sprite.getMinV();
        var uSize = sprite.getMaxU() - sprite.getMinU();

        var minU = sprite.getMinU();
        var maxU = sprite.getMinU() + (uSize * (float)(right - left));
        var minV = sprite.getMinV();
        var maxV = sprite.getMinV() + (vSize * (float)(top - bottom));

        emitter.square(face, (float)left, (float)bottom, (float)right, (float)top, (float)depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_ROTATE_NONE);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.sprite(0, 0, minU, minV);
        emitter.sprite(1, 0, minU, maxV);
        emitter.sprite(2, 0, maxU, maxV);
        emitter.sprite(3, 0, maxU, minV);
        emitter.emit();
    }
}
