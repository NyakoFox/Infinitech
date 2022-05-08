package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.multipart.impl.MultipartBlockEntity;
import alexiil.mc.lib.multipart.impl.PartContainer;
import alexiil.mc.lib.net.*;
import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashSet;
import java.util.List;

public class EnergyPipePart extends AbstractIOPipePart {
    public static SpriteIdentifier ENERGY_PIPE_ON_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_on"));
    public static SpriteIdentifier ENERGY_PIPE_OFF_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_off"));
    public static SpriteIdentifier ENERGY_PIPE_ON_END_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_on_end"));
    public static SpriteIdentifier ENERGY_PIPE_OFF_END_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_off_end"));

    public static final ParentNetIdSingle<EnergyPipePart> NET_ENERGY_PIPE;
    public static final NetIdDataK<EnergyPipePart> ENERGY_TRANSFER_DATA;

    public static final int TRANSFER_TIMER = 5;

    static {
        NET_ENERGY_PIPE = NET_ID.subType(EnergyPipePart.class, InfinitechMod.MOD_ID + ":energy_pipe");
        ENERGY_TRANSFER_DATA = NET_ENERGY_PIPE.idData("energy_transferring_data").toClientOnly().setReceiver(EnergyPipePart::receiveTransferData);
    }

    public long transferRate = 1_000;
    public int transferCountdown = 0;

    public EnergyPipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
    }

    public EnergyPipePart(PartDefinition definition, MultipartHolder holder, NbtCompound nbt) {
        this(definition, holder);
        createFromNbt(definition, holder, nbt);
    }

    public EnergyPipePart(PartDefinition definition, MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) {
        this(definition, holder);
        createFromBuffer(definition, holder, buffer, ctx);
    }

    public List<EnergyStorage> getConnectedStorages(@Nullable Direction ignoreSide) {
        var checked = new HashSet<BlockPos>();
        if (ignoreSide != null) {
            checked.add(holder.getContainer().getMultipartPos().offset(ignoreSide));
        }
        return getNetworkConnections(checked).stream()
                .filter((ctx) -> ((AbstractIOPipePart)ctx.pipe()).getMode(ctx.direction()).isInsert())
                .map((ctx) -> ctx.lookup(EnergyStorage.SIDED))
                .filter((storage) -> storage != null && storage.supportsInsertion()).toList();
    }

    @Override
    public void tick() {
        super.tick();
        var client = holder.getContainer().isClientWorld();
        if (!client && transferCountdown == TRANSFER_TIMER) {
            sendNetworkUpdate(this, ENERGY_TRANSFER_DATA, (part, buf, ctx) -> sendTransferData(buf, ctx, getNetworkPipes()));
        }
        if (transferCountdown > 0) {
            transferCountdown--;
            if (client && transferCountdown == 0) {
                holder.getContainer().redrawIfChanged();
            }
        }
        if (!client) {
            var connections = getConnections();
            for (PipeConnectionContext context : connections) {
                if (!getMode(context.direction()).isExtract()) continue;

                var storage = context.lookup(EnergyStorage.SIDED);
                var ownStorage = EnergyPipeStorage.of(this, context.direction());
                if (storage.supportsExtraction()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted;
                        try (Transaction testExtraction = transaction.openNested()) {
                            extracted = storage.extract(transferRate, testExtraction);
                        }

                        if (extracted > 0) {
                            var inserted = ownStorage.insert(extracted, transaction);

                            if (inserted > 0) {
                                storage.extract(inserted, transaction);
                                transaction.commit();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canConnectTo(PipeConnectionContext context) {
        var storage = context.lookup(EnergyStorage.SIDED);
        if (storage != null) {
            return storage.supportsInsertion() || storage.supportsExtraction();
        }
        return false;
    }

    @Override
    public boolean isValidPipe(AbstractPipePart pipe, Direction directionTo) {
        return pipe instanceof EnergyPipePart;
    }

    @Override
    public SpriteIdentifier getSpriteId() {
        return transferCountdown > 0 ? ENERGY_PIPE_ON_SPRITE : ENERGY_PIPE_OFF_SPRITE;
    }

    @Override
    public SpriteIdentifier getEndSpriteId() {
        return transferCountdown > 0 ? ENERGY_PIPE_ON_END_SPRITE : ENERGY_PIPE_OFF_END_SPRITE;
    }

    @Override
    public PipeTypes getPipeType() {
        return PipeTypes.ENERGY;
    }

    @Override
    protected BlockState getClosestBlockState() {
        return InfinitechMod.POWER_BANK_BLOCK.getDefaultState();
    }

    @Override
    public ItemStack getPickStack() {
        return InfinitechMod.ENERGY_PIPE_ITEM.getDefaultStack();
    }

    public void sendTransferData(NetByteBuf buffer, IMsgWriteCtx ctx, List<AbstractPipePart> parts) {
        buffer.writeVarInt(parts.size());
        for (var part : parts) {
            buffer.writeBlockPos(part.holder.getContainer().getMultipartPos());
            buffer.writeVarLong(part.holder.getUniqueId());
        }
    }

    public void receiveTransferData(NetByteBuf buffer, IMsgReadCtx ctx) {
        var world = holder.getContainer().getMultipartWorld();
        var size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            var pos = buffer.readBlockPos();
            var id = buffer.readVarLong();
            var blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MultipartBlockEntity mbe) {
                var container = mbe.getContainer();
                var part = container.getPart(id);
                if (part != null) {
                    ((EnergyPipePart)part).transferCountdown = TRANSFER_TIMER;
                    container.redrawIfChanged();
                }
            }
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Energy Pipe");
    }

}
