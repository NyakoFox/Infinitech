package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.*;
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent;
import alexiil.mc.lib.multipart.api.event.PartTickEvent;
import alexiil.mc.lib.multipart.api.render.PartModelKey;
import alexiil.mc.lib.net.*;
import com.google.common.collect.Lists;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.block_breaker.BlockBreakerGuiDescription;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractPipePart extends AbstractPart implements PipeShapeContext, ExtendedScreenHandlerFactory {
    public static final ParentNetIdSingle<AbstractPipePart> NET_PIPE;
    public static final NetIdDataK<AbstractPipePart> CONNECTION_DATA;

    static {
        NET_PIPE = NET_ID.subType(AbstractPipePart.class, InfinitechMod.MOD_ID + ":pipe");
        CONNECTION_DATA = NET_PIPE.idData("pipe_connection_data").toClientOnly().setReceiver(AbstractPipePart::receiveConnectionData);
    }

    protected Set<Direction> connectedSides;
    protected Hashtable<Direction, AbstractPipePart> pipeConnections;
    protected Hashtable<Direction, PipeConnectionContext> connections;
    public Direction lastDirection;
    private boolean needsUpdate;

    public AbstractPipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
        this.connectedSides = new HashSet<>();
        this.pipeConnections = new Hashtable<>();
        this.connections = new Hashtable<>();
        this.lastDirection = Direction.DOWN;
    }

    public void createFromNbt(PartDefinition definition, MultipartHolder holder, NbtCompound nbt) {
        if (nbt.contains("Connections")) {
            var list = nbt.getList("Connections", NbtElement.STRING_TYPE);
            for (var dirStr : list) {
                connectedSides.add(Direction.byName(dirStr.asString()));
            }
        }
    }

    public void createFromBuffer(PartDefinition definition, MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) {
        receiveConnectionData(buffer, ctx);
    }

    @Override
    protected BlockState getClosestBlockState() {
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    protected void playBreakSound() {
        playBreakSound(Blocks.STONE.getDefaultState());
    }

    @Override
    public void onAdded(MultipartEventBus bus) {
        bus.addListener(this, NeighbourUpdateEvent.class, (event) -> {
            needsUpdate = true;
        });
        bus.addListener(this, PartTickEvent.class, (event) -> tick());
        needsUpdate = true;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.world.isClient) {
            var pos = holder.getContainer().getMultipartPos();

            var tickDelta = MinecraftClient.getInstance().getTickDelta();
            var maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
            var startPos = player.getCameraPosVec(tickDelta);
            var lookVec = player.getRotationVec(tickDelta);
            var endPos = startPos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);

            PipeShapeBase closestHit = null;
            double closestDist = 0;

            var shapes = PipeShape.getPipeShapes(this);
            shapes.addAll(PipeShape.getConnectorShapes(this));
            shapes.add(PipeShape.getCenterShape(this));

            for (var shape : shapes) {
                if (shape == null) continue;

                var result = shape.toVoxelShape().raycast(startPos, endPos, pos);

                if (result != null) {
                    var dist = result.getPos().distanceTo(startPos);

                    if (closestHit == null || dist < closestDist) {
                        closestHit = shape;
                        closestDist = dist;
                    }
                }
            }

            if (closestHit != null) {
                if (closestHit instanceof PipeShape pipe) {
                    player.sendMessage(new LiteralText("Hit " + getPipeType().name().toLowerCase() + " pipe: " + pipe.direction().getName()), false);
                } else if (closestHit instanceof PipeConnectorShape connector) {
                    if (connector.direction() != null) {
                        // The player just right-clicked a connector.

                        // player.sendMessage(new LiteralText("Hit connector: " + connector.direction().getName()), false);

                        // Save which connector it was
                        this.lastDirection = connector.direction();

                        // Tell the server we need to open a screen!!!
                        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                        passedData.writeBlockPos(pos);
                        passedData.writeLong(holder.getUniqueId());
                        passedData.writeEnumConstant(connector.direction());
                        ClientSidePacketRegistry.INSTANCE.sendToServer(InfinitechMod.OPEN_PIPE_SCREEN_PACKET_ID, passedData);

                        return ActionResult.SUCCESS;
                    } else {
                        // We hit one of the center points...
                        player.sendMessage(new LiteralText("Hit center"), false);
                    }
                }
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        var container = holder.getContainer();
        var pos = container.getMultipartPos();
        return new PipeGuiDescription(syncId, playerInventory, ScreenHandlerContext.create(container.getMultipartWorld(), pos), pos, holder.getUniqueId());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(holder.getContainer().getMultipartPos());
        buf.writeLong(holder.getUniqueId());
    }

    public void tick() {
        if (!holder.getContainer().isClientWorld()) {
            if (needsUpdate) {
                updateConnections();
                syncConnections();
                needsUpdate = false;
            }
        }
    }

    public abstract boolean isValidPipe(AbstractPipePart pipe, Direction directionTo);

    public abstract boolean canConnectTo(PipeConnectionContext context);

    public abstract SpriteIdentifier getSpriteId();

    public abstract SpriteIdentifier getEndSpriteId();

    @Override
    public abstract PipeTypes getPipeType();

    public void updateConnections() {
        var container = holder.getContainer();
        var world = container.getMultipartWorld();

        if (world.isClient) {
            return;
        }

        clearConnections();
        for (Direction direction : Direction.values()) {
            var pos = container.getMultipartPos().offset(direction);

            var neighbor = MultipartUtil.get(container.getMultipartWorld(), container.getMultipartPos().offset(direction));
            var context = new PipeConnectionContext(this, world, pos, direction);
            AbstractPipePart connectedPipe;
            if (neighbor != null && (connectedPipe = neighbor.getFirstPart(AbstractPipePart.class, (part) -> isValidPipe(part, direction))) != null) {
                connectedSides.add(direction);
                pipeConnections.put(direction, connectedPipe);
            } else if (canConnectTo(context)) {
                connectedSides.add(direction);
                connections.put(direction, context);
            }
        }

        container.recalculateShape();
    }

    protected void syncConnections() {
        sendNetworkUpdate(this, CONNECTION_DATA, AbstractPipePart::sendConnectionData);
    }

    public Set<Direction> getConnectedSides() {
        if (this.connectedSides == null) {
            updateConnections();
        }
        return this.connectedSides;
    }

    public List<AbstractPipePart> getConnectedPipes() {
        if (this.pipeConnections == null) {
            updateConnections();
        }
        return Lists.newArrayList(pipeConnections.elements().asIterator());
    }

    public List<PipeConnectionContext> getConnections() {
        if (this.connections == null) {
            updateConnections();
        }
        return Lists.newArrayList(connections.elements().asIterator());
    }

    @Override
    public List<PipeTypes> getAllPipeTypes() {
        var result = new ArrayList<PipeTypes>();
        var parts = holder.getContainer().getAllParts(part -> part instanceof AbstractPipePart);
        for (AbstractPart part : parts) {
            var pipe = (AbstractPipePart) part;
            result.add(pipe.getPipeType());
        }
        return result;
    }

    @Override
    public Map<PipeTypes, List<Direction>> getPipeConnectionMap() {
        var result = new HashMap<PipeTypes, List<Direction>>();
        var parts = holder.getContainer().getAllParts(part -> part instanceof AbstractPipePart);
        for (AbstractPart part : parts) {
            var pipe = (AbstractPipePart) part;
            result.put(pipe.getPipeType(), new ArrayList<>(pipe.getConnectedSides()));
        }
        return result;
    }

    @Override
    public List<Direction> getBlockConnectionDirs() {
        return getConnections().stream().map(ctx -> ctx.direction()).toList();
    }

    protected void clearConnections() {
        connectedSides.clear();
        pipeConnections.clear();
        connections.clear();
    }


    public List<PipeConnectionContext> getNetworkConnections() {
        return getNetworkConnections(new HashSet<>());
    }

    public List<PipeConnectionContext> getNetworkConnections(Set<BlockPos> checked) {
        var list = new ArrayList<PipeConnectionContext>();
        var basePos = holder.getContainer().getMultipartPos();

        for (Direction direction : connectedSides) {
            if (checked.contains(basePos.offset(direction))) {
                continue;
            }
            checked.add(basePos.offset(direction));
            if (pipeConnections.containsKey(direction)) {
                list.addAll(pipeConnections.get(direction).getNetworkConnections(checked));
            } else if (connections.containsKey(direction)) {
                list.add(connections.get(direction));
            }
        }

        return list;
    }

    public List<AbstractPipePart> getNetworkPipes() {
        return getNetworkPipes(new HashSet<>());
    }

    public List<AbstractPipePart> getNetworkPipes(Set<BlockPos> checked) {
        var list = new ArrayList<AbstractPipePart>();
        var basePos = holder.getContainer().getMultipartPos();

        list.add(this);

        for (Direction direction : connectedSides) {
            if (checked.contains(basePos.offset(direction))) {
                continue;
            }
            checked.add(basePos.offset(direction));
            if (pipeConnections.containsKey(direction)) {
                list.addAll(pipeConnections.get(direction).getNetworkPipes(checked));
            }
        }

        return list;
    }

    @Override
    public boolean canOverlapWith(AbstractPart other) {
        if (other instanceof AbstractPipePart pipe) {
            return pipe.getPipeType() != getPipeType();
        }
        return false;
    }

    @Override
    public VoxelShape getShape() {
        var shapes = PipeShape.getPipeShapes(this);
        var centerShape = PipeShape.getCenterShape(this);
        if (centerShape != null) {
            shapes.add(centerShape);
        }
        shapes.addAll(PipeShape.getConnectorShapes(this));
        return shapes.stream().map(PipeShapeBase::toVoxelShape).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    }

    @Nullable
    @Override
    public PartModelKey getModelKey() {
        return new PipePartModelKey(this);
    }

    @Override
    public void writeRenderData(NetByteBuf buffer, IMsgWriteCtx ctx) {
        sendConnectionData(buffer, ctx);
    }

    @Override
    public void readRenderData(NetByteBuf buffer, IMsgReadCtx ctx) {
        receiveConnectionData(buffer, ctx);
    }

    @Override
    public void writeCreationData(NetByteBuf buffer, IMsgWriteCtx ctx) {
        sendConnectionData(buffer, ctx);
    }

    @Override
    public NbtCompound toTag() {
        var nbt = super.toTag();

        var list = new NbtList();
        for (Direction direction : connectedSides) {
            list.add(NbtString.of(direction.asString()));
        }
        nbt.put("Connections", list);

        return nbt;
    }

    public void sendConnectionData(NetByteBuf buffer, IMsgWriteCtx ctx) {
        buffer.writeVarInt(connectedSides.size());
        for (Direction direction : connectedSides) {
            buffer.writeEnumConstant(direction);
        }
        buffer.writeVarInt(connections.size());
        for (var dir : connections.keySet()) {
            buffer.writeEnumConstant(dir);
        }
    }

    public void receiveConnectionData(NetByteBuf buffer, IMsgReadCtx ctx) {
        connectedSides.clear();
        var size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            connectedSides.add(buffer.readEnumConstant(Direction.class));
        }
        connections.clear();
        var container = holder.getContainer();
        size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            var dir = buffer.readEnumConstant(Direction.class);
            connections.put(dir, new PipeConnectionContext(this, container.getMultipartWorld(), container.getMultipartPos().offset(dir), dir));
        }
        container.recalculateShape();
        container.redrawIfChanged();
    }
}
