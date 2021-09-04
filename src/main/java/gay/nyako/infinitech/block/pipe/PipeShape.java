package gay.nyako.infinitech.block.pipe;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record PipeShape(@Nullable Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, boolean endFaces) {
    public static PipeShape of(Direction direction, Vec2f offset, boolean small) {
        /*var min = small ? 2 : 0;
        var max = small ? 14 : 16;
        return switch (direction) {
            case NORTH -> fromBlockCoords(direction, 6 + offset.x, 6 + offset.y, min, 10 + offset.x, 10 + offset.y, 6);
            case SOUTH -> fromBlockCoords(direction, 6 + offset.x, 6 + offset.y, 10, 10 + offset.x, 10 + offset.y, max);
            case EAST -> fromBlockCoords(direction, 10, 6 + offset.y, 6 + offset.x, max, 10 + offset.y, 10 + offset.x);
            case WEST -> fromBlockCoords(direction, min, 6 + offset.y, 6 + offset.x, 6, 10 + offset.y, 10 + offset.x);
            case UP -> fromBlockCoords(direction, 6 + offset.x, 10, 6 + offset.y, 10 + offset.x, max, 10 + offset.y);
            case DOWN -> fromBlockCoords(direction, 6 + offset.x, min, 6 + offset.y, 10 + offset.x, 6, 10 + offset.y);
        };*/
        var min = small ? 2 : 0;
        var max = small ? 14 : 16;
        return switch (direction) {
            case NORTH -> fromBlockCoords(direction, 6.5 + offset.x, 6.5 + offset.y, min, 9.5 + offset.x, 9.5 + offset.y, 6.5, small);
            case SOUTH -> fromBlockCoords(direction, 6.5 + offset.x, 6.5 + offset.y, 9.5, 9.5 + offset.x, 9.5 + offset.y, max, small);
            case EAST -> fromBlockCoords(direction, 9.5, 6.5 + offset.y, 6.5 + offset.x, max, 9.5 + offset.y, 9.5 + offset.x, small);
            case WEST -> fromBlockCoords(direction, min, 6.5 + offset.y, 6.5 + offset.x, 6.5, 9.5 + offset.y, 9.5 + offset.x, small);
            case UP -> fromBlockCoords(direction, 6.5 + offset.x, 9.5, 6.5 + offset.y, 9.5 + offset.x, max, 9.5 + offset.y, small);
            case DOWN -> fromBlockCoords(direction, 6.5 + offset.x, min, 6.5 + offset.y, 9.5 + offset.x, 6.5, 9.5 + offset.y, small);
        };
    }

    public static PipeShape ofStraight(Direction direction, Vec2f offset) {
        /*return switch (direction) {
            case NORTH, SOUTH -> fromBlockCoords(direction, 6 + offset.x, 6 + offset.y, 0, 10 + offset.x, 10 + offset.y, 16);
            case EAST, WEST -> fromBlockCoords(direction, 0, 6 + offset.y, 6 + offset.x, 16, 10 + offset.y, 10 + offset.x);
            case UP, DOWN -> fromBlockCoords(direction, 6 + offset.x, 0, 6 + offset.y, 10 + offset.x, 16, 10 + offset.y);
        };*/
        return switch (direction) {
            case NORTH, SOUTH -> fromBlockCoords(direction, 6.5 + offset.x, 6.5 + offset.y, 0, 9.5 + offset.x, 9.5 + offset.y, 16, false);
            case EAST, WEST -> fromBlockCoords(direction, 0, 6.5 + offset.y, 6.5 + offset.x, 16, 9.5 + offset.y, 9.5 + offset.x, false);
            case UP, DOWN -> fromBlockCoords(direction, 6.5 + offset.x, 0, 6.5 + offset.y, 9.5 + offset.x, 16, 9.5 + offset.y, false);
        };
    }

    public static PipeShape fromBlockCoords(Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, boolean endFaces) {
        return new PipeShape(direction, minX/16.0D, minY/16.0D, minZ/16.0D, maxX/16.0D, maxY/16.0D, maxZ/16.0D, endFaces);
    }

    public static Vec2f getPipeOffset(PipeShapeContext ctx, Direction direction) {
        var type = ctx.getPipeType();
        var frens = ctx.getContainerConnections().get(direction);
        if (frens.size() == 2) {
            var first = (frens.get(0) == type ? type.compareTo(frens.get(1)) : type.compareTo(frens.get(0))) < 0;
            return new Vec2f(first ? -2.5f : 2.5f, 0f);
        } else if (frens.size() == 3) {
            /*return switch(type) {
                case ITEM -> new Vec2f(-2.5f, 2.5f);
                case FLUID -> new Vec2f(2.5f, 2.5f);
                case ENERGY -> new Vec2f(0f, -2.5f);
            };*/
            return switch(type) {
                case ITEM -> new Vec2f(-2f, 2f);
                case FLUID -> new Vec2f(2f, 2f);
                case ENERGY -> new Vec2f(0f, -2f);
            };
        }
        return Vec2f.ZERO;
    }

    private static Vec3d get3DPipeOffset(Vec2f offset, Direction direction) {
        var axis = direction.getAxis();

        var x = axis.choose(0f, offset.x, offset.x);
        var y = axis.choose(offset.y, 0f, offset.y);
        var z = axis.choose(offset.x, offset.y, 0f);

        return new Vec3d(x, y, z);
    }

    public static boolean isStraight(PipeShapeContext ctx) {
        var ownConnections = ctx.getContainerConnections(ctx.getPipeType());
        if (ownConnections.size() == 2) {
            var first = ownConnections.get(0);
            var second = ownConnections.get(1);
            var allConnections = ctx.getContainerConnections();
            for (Direction direction : Direction.values()) {
                var types = allConnections.get(direction);
                if ((types.size() > 0 && direction != first && direction != second) || !types.equals(allConnections.get(direction.getOpposite()))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static double getCenterSize(PipeShapeContext ctx) {
        var connections = ctx.getContainerConnections();
        for (var list : connections.values()) {
            if (list.size() > 1) {
                return 9;
            }
        }
        return 4;
    }

    public static PipeShape getCenterShape(PipeShapeContext ctx) {
        if (isStraight(ctx)) {
            return null;
        }
        var centerSize = getCenterSize(ctx);
        var min = 8 - centerSize/2;
        var max = 8 + centerSize/2;
        return fromBlockCoords(null, min, min, min, max, max, max, true);
    }

    public static List<PipeShape> getConnectionShapes(PipeShapeContext ctx) {
        var connections = ctx.getContainerConnections(ctx.getPipeType());
        if (!connections.isEmpty()) {
            if (!isStraight(ctx)) {
                return new ArrayList<>(connections.stream().map(dir -> of(dir, getPipeOffset(ctx, dir), false)).toList());
            } else {
                return new ArrayList<>(connections.stream().map((dir) -> Direction.from(dir.getAxis(), Direction.AxisDirection.POSITIVE))
                        .distinct().map(dir -> ofStraight(dir, getPipeOffset(ctx, dir))).toList());
            }
        } else {
            var allConnections = ctx.getContainerConnections();
            var list = new ArrayList<PipeShape>();
            for (Direction direction : Direction.values()) {
                if (allConnections.get(direction).isEmpty()) {
                    list.add(of(direction, getPipeOffset(ctx, direction), true));
                }
            }
            return list;
        }
    }

    /*
     Instance Methods
     */

    public VoxelShape toVoxelShape() {
        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void emit(QuadEmitter emitter, Sprite sprite, Sprite endSprite) {
        for (Direction face : Direction.values()) {
            emitFace(emitter, sprite, endSprite, face);
        }
    }

    private void emitFace(QuadEmitter emitter, Sprite sprite, Sprite endSprite, Direction face) {
        var isEndFace = direction != null && face.getAxis() == direction.getAxis();
        if (isEndFace && !endFaces) {
            return;
        }
        var faceSprite = isEndFace ? endSprite : sprite;
        switch (face) {
            case UP -> emitSingleQuad(emitter, faceSprite, isEndFace, Direction.UP, minX, 1 - maxZ, maxX, 1 - minZ, 1 - maxY);
            case DOWN -> emitSingleQuad(emitter, faceSprite, isEndFace, Direction.DOWN, minX, minZ, maxX, maxZ, minY);
            case EAST -> emitSingleQuad(emitter, faceSprite, isEndFace, Direction.EAST, 1 - maxZ, minY, 1 - minZ, maxY, 1 - maxX);
            case WEST -> emitSingleQuad(emitter, faceSprite, isEndFace, Direction.WEST, minZ, minY, maxZ, maxY, minX);
            case SOUTH -> emitSingleQuad(emitter, faceSprite, isEndFace, Direction.SOUTH, minX, minY, maxX, maxY, 1 - maxZ);
            case NORTH -> emitSingleQuad(emitter, faceSprite, isEndFace, Direction.NORTH, 1 - maxX, minY, 1 - minX, maxY, minZ);
        }
    }

    private void emitSingleQuad(QuadEmitter emitter, Sprite sprite, boolean full, Direction face, double left, double bottom, double right, double top, double depth) {
        var tall = direction == null || (direction.getAxis() == Direction.Axis.Y || (face.getAxis() == Direction.Axis.Y && direction.getAxis() == Direction.Axis.Z));

        var small = (tall ? (float)(right - left) : (float)(top - bottom));
        var big = (tall ? (float)(top - bottom) : (float)(right - left));

        var vSize = sprite.getMaxV() - sprite.getMinV();
        var uSize = sprite.getMaxU() - sprite.getMinU();

        var minU = sprite.getMinU();
        var maxU = full ? sprite.getMaxU() : (direction != null ? sprite.getMaxU() : (sprite.getMinU() + (uSize * small)));
        var minV = full ? sprite.getMinV() : (direction == null ? sprite.getMinV() : (sprite.getMinV() + (vSize * (1f - (float)(tall ? top : right)))));
        var maxV = full ? sprite.getMaxV() : (direction == null ? sprite.getMinV() + (vSize * big) : (sprite.getMinV() + (vSize * (1f - (float)(tall ? bottom : left)))));

        emitter.square(face, (float)left, (float)bottom, (float)right, (float)top, (float)depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_ROTATE_NONE);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.sprite(tall ? 0 : 3, 0, minU, minV);
        emitter.sprite(tall ? 1 : 0, 0, minU, maxV);
        emitter.sprite(tall ? 2 : 1, 0, maxU, maxV);
        emitter.sprite(tall ? 3 : 2, 0, maxU, minV);
        emitter.emit();
    }
}
