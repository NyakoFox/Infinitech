package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.NetByteBuf;
import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class ItemPipePart extends AbstractStoragePipePart<ItemVariant> {
    public static SpriteIdentifier ITEM_PIPE_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/item"));
    public static SpriteIdentifier ITEM_PIPE_END_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(InfinitechMod.MOD_ID, "block/pipe/item_end"));

    public ItemPipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
    }

    public ItemPipePart(PartDefinition definition, MultipartHolder holder, NbtCompound nbt) {
        this(definition, holder);
        createFromNbt(definition, holder, nbt);
    }

    public ItemPipePart(PartDefinition definition, MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) {
        this(definition, holder);
        createFromBuffer(definition, holder, buffer, ctx);
    }

    @Override
    public long getTransferRate() {
        return 1;
    }

    @Override
    public Storage<ItemVariant> getStorage(Direction side) {
        return StoragePipeRelay.of(this, side, ItemVariant.blank());
    }

    @Override
    protected BlockApiLookup<Storage<ItemVariant>, Direction> getLookup() {
        return ItemStorage.SIDED;
    }

    @Override
    public boolean isValidPipe(AbstractPipePart pipe, Direction directionTo) {
        return pipe instanceof ItemPipePart;
    }

    @Override
    public SpriteIdentifier getSpriteId() {
        return ITEM_PIPE_SPRITE;
    }

    @Override
    public SpriteIdentifier getEndSpriteId() {
        return ITEM_PIPE_END_SPRITE;
    }

    @Override
    public PipeTypes getPipeType() {
        return PipeTypes.ITEM;
    }

    @Override
    protected BlockState getClosestBlockState() {
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public ItemStack getPickStack() {
        return InfinitechMod.ITEM_PIPE_ITEM.getDefaultStack();
    }
}
