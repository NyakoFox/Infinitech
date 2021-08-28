package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.NetByteBuf;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class ItemPipePart extends AbstractStoragePipePart<ItemVariant> {
    public static SpriteIdentifier ITEM_PIPE_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("infinitech", "block/pipe/item"));

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
    public SpriteIdentifier getSpriteIdentifier() {
        return ITEM_PIPE_SPRITE;
    }
}
