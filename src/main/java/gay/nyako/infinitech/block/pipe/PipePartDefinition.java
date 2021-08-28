package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class PipePartDefinition extends PartDefinition {
    public PipePartDefinition(Identifier identifier, IPartNbtReader reader, IPartNetLoader loader) {
        super(identifier, reader, loader);
    }

    @Override
    public AbstractPart loadFromBuffer(MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
        return super.loadFromBuffer(holder, buffer, ctx);
    }

    @Override
    public AbstractPart readFromNbt(MultipartHolder holder, NbtCompound nbt) {
        return super.readFromNbt(holder, nbt);
    }
}
