package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
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
            var mode = Mode.valueOf(nbt.getString("IOMode"));
        }
        super.createFromNbt(definition, holder, nbt);
    }

    @Override
    public NbtCompound toTag() {
        var nbt = super.toTag();
        nbt.putString("IOMode", mode.name());
        return nbt;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        super.onUse(player, hand, hit);
        if (!player.world.isClient) {
            switch (this.mode) {
                case INSERT: this.mode = Mode.EXTRACT; break;
                case EXTRACT: this.mode = Mode.INSERT; break;
            }
            player.sendMessage(new LiteralText("Switched mode: " + mode), true);
        }
        updateConnections();
        return ActionResult.SUCCESS;
    }

    public enum Mode {
        INSERT,
        EXTRACT
    }
}
