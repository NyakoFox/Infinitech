package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import java.util.HashMap;

public abstract class AbstractIOPipePart extends AbstractPipePart {
    public HashMap<Direction, Mode> modeMap;

    public AbstractIOPipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
        this.modeMap = new HashMap<>();
        for (var dir : Direction.values()) {
            modeMap.put(dir, Mode.INSERT);
        }
    }

    @Override
    public void createFromNbt(PartDefinition definition, MultipartHolder holder, NbtCompound nbt) {
        var modes = nbt.getCompound("IOModes");
        if (modes != null) {
            for (var dir : Direction.values()) {
                if (modes.contains(dir.name())) {
                    modeMap.put(dir, Mode.valueOf(modes.getString(dir.name())));
                }
            }
        }
        super.createFromNbt(definition, holder, nbt);
    }

    @Override
    public void createFromBuffer(PartDefinition definition, MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) {
        for (var dir : Direction.values()) {
            modeMap.put(dir, buffer.readEnumConstant(Mode.class));
        }
        super.createFromBuffer(definition, holder, buffer, ctx);
    }

    @Override
    public NbtCompound toTag() {
        var nbt = super.toTag();
        var modes = new NbtCompound();
        for (var dir : Direction.values()) {
            modes.putString(dir.name(), modeMap.get(dir).name());
        }
        nbt.put("IOModes", modes);
        return nbt;
    }

    @Override
    public void writeCreationData(NetByteBuf buffer, IMsgWriteCtx ctx) {
        for (var dir : Direction.values()) {
            buffer.writeEnumConstant(modeMap.get(dir));
        }
        super.writeCreationData(buffer, ctx);
    }

    public Mode getMode(Direction dir) {
        return modeMap.get(dir);
    }

    public void setMode(Direction dir, Mode mode) {
        modeMap.put(dir, mode);
    }

    public Mode nextMode(Direction dir) {
        // Yikes
        var mode = modeMap.get(dir);
        return Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
    }

    public enum Mode {
        INSERT(true, false),
        EXTRACT(false, true),
        BOTH(true, true);

        private final boolean insert;
        private final boolean extract;

        Mode(boolean insert, boolean extract) {
            this.insert = insert;
            this.extract = extract;
        }

        public boolean isInsert() {
            return this.insert;
        }

        public boolean isExtract() {
            return this.extract;
        }
    }
}
