package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.NetByteBuf;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public class EnergyPipePart extends AbstractIOPipePart {
    public static SpriteIdentifier ENERGY_PIPE_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy"));

    public double transferRate = 1_000;

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

    public List<EnergyIo> getConnectedStorages(@Nullable Direction ignoreSide) {
        var checked = new HashSet<BlockPos>();
        if (ignoreSide != null) {
            checked.add(holder.getContainer().getMultipartPos().offset(ignoreSide));
        }
        return getNetworkConnections(checked).stream()
                .map((ctx) -> ctx.lookup(EnergyApi.SIDED))
                .filter((storage) -> storage != null && storage.supportsInsertion()).toList();
    }

    @Override
    public void tick() {
        super.tick();
        if (!holder.getContainer().isClientWorld() && mode == Mode.EXTRACT) {
            var connections = getConnections();
            for (PipeConnectionContext context : connections) {
                var storage = context.lookup(EnergyApi.SIDED);
                var ownStorage = EnergyPipeIo.of(this, context.direction());
                if (storage.supportsExtraction()) {
                    var extracted = transferRate - storage.extract(transferRate, Simulation.SIMULATE);

                    if (extracted > 0) {
                        var inserted = extracted - ownStorage.insert(extracted, Simulation.ACT);

                        if (inserted > 0) {
                            storage.extract(inserted, Simulation.ACT);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canConnectTo(PipeConnectionContext context) {
        var storage = context.lookup(EnergyApi.SIDED);
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
    public SpriteIdentifier getSpriteIdentifier() {
        return ENERGY_PIPE_SPRITE;
    }

    @Override
    protected BlockState getClosestBlockState() {
        return InfinitechMod.POWER_BANK_BLOCK.getDefaultState();
    }
}
