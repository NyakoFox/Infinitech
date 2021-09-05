package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.NetByteBuf;
import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class FluidPipePart extends AbstractStoragePipePart<FluidVariant> {
    public static SpriteIdentifier FLUID_PIPE_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/item"));
    public static SpriteIdentifier FLUID_PIPE_END_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/item_end"));

    public FluidPipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
    }

    public FluidPipePart(PartDefinition definition, MultipartHolder holder, NbtCompound nbt) {
        this(definition, holder);
        createFromNbt(definition, holder, nbt);
    }

    public FluidPipePart(PartDefinition definition, MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) {
        this(definition, holder);
        createFromBuffer(definition, holder, buffer, ctx);
    }

    @Override
    public long getTransferRate() {
        return FluidConstants.BUCKET / 10;
    }

    @Override
    public Storage<FluidVariant> getStorage(Direction side) {
        return StoragePipeRelay.of(this, side, FluidVariant.blank());
    }

    @Override
    protected BlockApiLookup<Storage<FluidVariant>, Direction> getLookup() {
        return FluidStorage.SIDED;
    }

    @Override
    public boolean isValidPipe(AbstractPipePart pipe, Direction directionTo) {
        return pipe instanceof FluidPipePart;
    }

    @Override
    public SpriteIdentifier getSpriteId() {
        return FLUID_PIPE_SPRITE;
    }

    @Override
    public SpriteIdentifier getEndSpriteId() {
        return FLUID_PIPE_END_SPRITE;
    }

    @Override
    public PipeTypes getPipeType() {
        return PipeTypes.FLUID;
    }

    @Override
    protected BlockState getClosestBlockState() {
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public ItemStack getPickStack() {
        return InfinitechMod.FLUID_PIPE_ITEM.getDefaultStack();
    }
}
