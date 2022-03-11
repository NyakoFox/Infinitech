package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import gay.nyako.infinitech.block.MachineUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public abstract class AbstractIOPipePart extends AbstractPipePart {
    public Mode mode;

    public AbstractIOPipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
        this.mode = Mode.INSERT;
    }

    @Override
    public void createFromNbt(PartDefinition definition, MultipartHolder holder, NbtCompound nbt) {
        if (nbt.contains("IOMode")) {
            mode = Mode.valueOf(nbt.getString("IOMode"));
        }
        super.createFromNbt(definition, holder, nbt);
    }

    @Override
    public NbtCompound toTag() {
        var nbt = super.toTag();
        nbt.putString("IOMode", mode.name());
        return nbt;
    }

    public Mode nextMode() {
        // Yikes
        return Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
    }

    public enum Mode {
        INSERT,
        EXTRACT
    }
}
