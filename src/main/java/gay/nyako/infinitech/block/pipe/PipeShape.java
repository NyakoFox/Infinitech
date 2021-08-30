package gay.nyako.infinitech.block.pipe;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public record PipeShape(@Nullable Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    public static PipeShape of(Direction direction) {
        return switch (direction) {
            case NORTH -> fromBlockCoords(direction, 6, 6, 0, 10, 10, 6);
            case EAST -> fromBlockCoords(direction, 10, 6, 6, 16, 10, 10);
            case SOUTH -> fromBlockCoords(direction, 6, 6, 10, 10, 10, 16);
            case WEST -> fromBlockCoords(direction, 0, 6, 6, 6, 10, 10);
            case UP -> fromBlockCoords(direction, 6, 10, 6, 10, 16, 10);
            case DOWN -> fromBlockCoords(direction, 6, 0, 6, 10, 6, 10);
        };
    }

    public static PipeShape ofStraight(Direction direction) {
        return switch (direction) {
            case NORTH, SOUTH -> fromBlockCoords(direction, 6, 6, 0, 10, 10, 16);
            case EAST, WEST -> fromBlockCoords(direction, 0, 6, 6, 16, 10, 10);
            case UP, DOWN -> fromBlockCoords(direction, 6, 0, 6, 10, 16, 10);
        };
    }

    public static PipeShape fromBlockCoords(Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new PipeShape(direction, minX/16.0D, minY/16.0D, minZ/16.0D, maxX/16.0D, maxY/16.0D, maxZ/16.0D);
    }

    public VoxelShape toVoxelShape() {
        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void emit(QuadEmitter emitter, Sprite sprite) {
        for (Direction face : Direction.values()) {
            emitFace(emitter, sprite, face);
        }
    }

    private void emitFace(QuadEmitter emitter, Sprite sprite, Direction face) {
        if (direction != null && face.getAxis() == direction.getAxis()) {
            return;
        }
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
        var tall = direction == null || (direction.getAxis() == Direction.Axis.Y || (face.getAxis() == Direction.Axis.Y && direction.getAxis() == Direction.Axis.Z));

        var small = (tall ? (float)(right - left) : (float)(top - bottom));
        var big = (tall ? (float)(top - bottom) : (float)(right - left));

        var vSize = sprite.getMaxV() - sprite.getMinV();
        var uSize = sprite.getMaxU() - sprite.getMinU();

        var minU = sprite.getMinU();
        var maxU = direction != null ? sprite.getMaxU() : (sprite.getMinU() + (uSize * small));
        var minV = direction == null ? sprite.getMinV() : (sprite.getMinV() + (vSize * (1f - (float)(tall ? top : right))));
        var maxV = direction == null ? sprite.getMinV() + (vSize * big) : (sprite.getMinV() + (vSize * (1f - (float)(tall ? bottom : left))));

        emitter.square(face, (float)left, (float)bottom, (float)right, (float)top, (float)depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_ROTATE_NONE);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.sprite(tall ? 0 : 3, 0, minU, minV);
        emitter.sprite(tall ? 1 : 0, 0, minU, maxV);
        emitter.sprite(tall ? 2 : 1, 0, maxU, maxV);
        emitter.sprite(tall ? 3 : 2, 0, maxU, minV);
        emitter.emit();
    }

    private void setSpriteUVs(QuadEmitter emitter, Sprite sprite, int index, double u, double v) {

    }
}
