package gay.nyako.infinitech.block.pipe;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record PipeShape(@Nullable Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) implements PipeShapeBase {
    public static PipeShape of(Direction direction, Vec2f offset) {
        return switch (direction) {
            case NORTH -> fromBlockCoords(direction, 6.5 + offset.x, 6.5 + offset.y, 0, 9.5 + offset.x, 9.5 + offset.y, 8);
            case SOUTH -> fromBlockCoords(direction, 6.5 + offset.x, 6.5 + offset.y, 8, 9.5 + offset.x, 9.5 + offset.y, 16);
            case EAST -> fromBlockCoords(direction, 8, 6.5 + offset.y, 6.5 + offset.x, 16, 9.5 + offset.y, 9.5 + offset.x);
            case WEST -> fromBlockCoords(direction, 0, 6.5 + offset.y, 6.5 + offset.x, 8, 9.5 + offset.y, 9.5 + offset.x);
            case UP -> fromBlockCoords(direction, 6.5 + offset.x, 8, 6.5 + offset.y, 9.5 + offset.x, 16, 9.5 + offset.y);
            case DOWN -> fromBlockCoords(direction, 6.5 + offset.x, 0, 6.5 + offset.y, 9.5 + offset.x, 8, 9.5 + offset.y);
        };
    }

    public static PipeShape fromBlockCoords(Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new PipeShape(direction, minX/16.0D, minY/16.0D, minZ/16.0D, maxX/16.0D, maxY/16.0D, maxZ/16.0D);
    }

    public static Vec2f getPipeOffset(PipeShapeContext ctx, Direction direction) {
        var type = ctx.getPipeType();
        var frens = ctx.getPipesFacing(direction);
        if (frens.size() == 2) {
            var first = (frens.get(0) == type ? type.compareTo(frens.get(1)) : type.compareTo(frens.get(0))) < 0;
            return new Vec2f(first ? -2f : 2f, 0f);
        } else if (frens.size() == 3) {
            return switch(type) {
                case ITEM -> new Vec2f(-2f, 2f);
                case FLUID -> new Vec2f(2f, 2f);
                case ENERGY -> new Vec2f(0f, -2f);
            };
        }
        return Vec2f.ZERO;
    }

    public static Vec3d getSinglePipePosition(PipeShapeContext ctx) {
        var type = ctx.getPipeType();
        var maxDirs = 0;
        for (var dir : Direction.values()) {
            maxDirs = Math.max(maxDirs, ctx.getPipesFacing(dir).size());
        }
        if (maxDirs == 0) {
            var pipes = ctx.getAllPipeTypes();
            var pipeCount = pipes.size();
            if (pipeCount == 1) {
                return new Vec3d(6.5, 6.5, 6.5);
            } else if (pipeCount == 2) {
                var first = (pipes.get(0) == type ? type.compareTo(pipes.get(1)) : type.compareTo(pipes.get(0))) < 0;
                return first ? new Vec3d(4.5, 6.5, 6.5) : new Vec3d(8.5, 6.5, 6.5);
            } else if (pipeCount == 3) {
                return switch(type) {
                    case ITEM -> new Vec3d(4.5, 6.5, 4.5);
                    case FLUID -> new Vec3d(8.5, 6.5, 4.5);
                    case ENERGY -> new Vec3d(6.5, 6.5, 8.5);
                };
            }
        } else if (maxDirs == 1) {
            return switch(type) {
                case ITEM -> new Vec3d(3, 10, 3);
                case FLUID -> new Vec3d(10, 10, 3);
                case ENERGY -> new Vec3d(3, 10, 10);
            };
        } else if (maxDirs == 2) {
            return switch(type) {
                case ITEM -> new Vec3d(2.5, 10.5, 2.5);
                case FLUID -> new Vec3d(10.5, 10.5, 2.5);
                case ENERGY -> new Vec3d(2.5, 10.5, 10.5);
            };
        }
        return new Vec3d(0, 0, 0);
    }

    public static boolean isStraight(PipeShapeContext ctx) {
        var ownConnections = ctx.getConnectionsFor(ctx.getPipeType());
        if (ownConnections.size() == 2) {
            var first = ownConnections.get(0);
            var second = ownConnections.get(1);
            for (Direction direction : Direction.values()) {
                var types = ctx.getPipesFacing(direction);
                if ((types.size() > 0 && direction != first && direction != second) || !types.equals(ctx.getPipesFacing(direction.getOpposite()))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static List<PipeConnectorShape> getConnectorShapes(PipeShapeContext ctx) {
        var result = new ArrayList<PipeConnectorShape>();
        for (var dir : ctx.getBlockConnectionDirs()) {
            result.add(switch(dir) {
                case NORTH -> PipeConnectorShape.fromBlockCoords(dir, 3, 3, 0, 13, 13, 1);
                case SOUTH -> PipeConnectorShape.fromBlockCoords(dir, 3, 3, 15, 13, 13, 16);
                case EAST -> PipeConnectorShape.fromBlockCoords(dir, 15, 3, 3, 16, 13, 13);
                case WEST -> PipeConnectorShape.fromBlockCoords(dir, 0, 3, 3, 1, 13, 13);
                case UP -> PipeConnectorShape.fromBlockCoords(dir, 3, 15, 3, 13, 16, 13);
                case DOWN -> PipeConnectorShape.fromBlockCoords(dir, 3, 0, 3, 13, 1, 13);
            });
        }
        return result;
    }

    public static PipeConnectorShape getCenterShape(PipeShapeContext ctx) {
        var size = new Vec3d(4, 4, 4);
        var foundDirections = new ArrayList<Direction>();
        for (var dir : Direction.values()) {
            var pipes = ctx.getPipesFacing(dir);
            var pipeCount = pipes.size();
            if (pipeCount > 0) {
                foundDirections.add(dir);
                var localSize = switch(pipeCount) {
                    case 2 -> new Vec2f(8f, 4f);
                    case 3 -> new Vec2f(8f, 8f);
                    default -> new Vec2f(4f, 4f);
                };
                size = switch (dir) {
                    case NORTH, SOUTH -> new Vec3d(Math.max(size.x, localSize.x), Math.max(size.y, localSize.y), size.z);
                    case EAST, WEST -> new Vec3d(size.x, Math.max(size.y, localSize.y), Math.max(size.z, localSize.x));
                    case UP, DOWN -> new Vec3d(Math.max(size.x, localSize.x), size.y, Math.max(size.z, localSize.y));
                };
            }
        }
        if (foundDirections.size() <= 1) {
            return null;
        } else if (foundDirections.size() == 2) {
            var first = foundDirections.get(0);
            var second = foundDirections.get(1);
            if (first.getOpposite() == second && ctx.getPipesFacing(first).equals(ctx.getPipesFacing(second))) {
                return null;
            }
        }
        var pos = new Vec3d(8 - (size.x/2), 8 - (size.y/2), 8 - (size.z/2));
        return PipeConnectorShape.fromBlockCoords(null, pos.x, pos.y, pos.z, pos.x + size.x, pos.y + size.y, pos.z + size.z);
    }

    public static List<PipeShapeBase> getPipeShapes(PipeShapeContext ctx) {
        var connections = ctx.getConnectionsFor(ctx.getPipeType());
        if (!connections.isEmpty()) {
            return new ArrayList<>(connections.stream().map(dir -> of(dir, getPipeOffset(ctx, dir))).toList());
        } else {
            var pos = getSinglePipePosition(ctx);
            return Lists.newArrayList(fromBlockCoords(null, pos.x, pos.y, pos.z, pos.x + 3, pos.y + 3, pos.z + 3));
        }
    }

    /*
     Instance Methods
     */

    @Override
    public VoxelShape toVoxelShape() {
        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void emit(QuadEmitter emitter, PipePartModelKey key) {
        for (Direction face : Direction.values()) {
            emitFace(emitter, key.getSprite(), key.getEndSprite(), face, key);
        }
    }

    private void emitFace(QuadEmitter emitter, Sprite sprite, Sprite endSprite, Direction face, PipeShapeContext ctx) {
        var isEndFace = direction == null || face == direction.getOpposite();
        var faceSprite = isEndFace ? endSprite : sprite;
        if (direction != null && face == direction) {
            var axis = face.getAxis();
            var axisDir = face.getDirection();
            if ((axisDir == Direction.AxisDirection.NEGATIVE && axis.choose(minX, minY, minZ) == 0) || (axisDir == Direction.AxisDirection.POSITIVE && axis.choose(maxX, maxY, maxZ) == 1)) {
                return;
            }
        }
        if (isEndFace && isStraight(ctx)) {
            return;
        }
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
