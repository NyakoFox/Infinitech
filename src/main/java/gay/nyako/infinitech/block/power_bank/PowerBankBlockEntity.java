package gay.nyako.infinitech.block.power_bank;

import dev.technici4n.fasttransferlib.api.Simulation;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PowerBankBlockEntity extends AbstractMachineBlockEntity implements PropertyDelegateHolder, NamedScreenHandlerFactory, BlockEntityClientSerializable {
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return (int) energy;
        }

        @Override
        public void set(int index, int value) {
            energy = (double) value;
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public PowerBankBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.POWER_BANK_BLOCK_ENTITY, pos, state, 2_000_000, 10_000);
        canInsert = true;
        canExtract = true;
    }



    @Override
    public double insert(double maxAmount, Simulation simulation) {
        double returnValue = super.insert(maxAmount,simulation);
        world.setBlockState(pos, getCachedState().with(PowerBankBlock.PERCENTAGE, (int)(((float) energy / 2_000_000f) * 10)));
        return returnValue;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        double returnValue = super.extract(maxAmount, simulation);
        world.setBlockState(pos, getCachedState().with(PowerBankBlock.PERCENTAGE, (int)(((float) energy / 2_000_000f) * 10)));
        return returnValue;
    }

    @Override
    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    @Override
    public Text getDisplayName() {
        // Using the block name as the screen title
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new PowerBankGuiDescription(syncId, inventory, ScreenHandlerContext.create(world, pos));
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.energy = tag.getDouble("Energy");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        tag.putDouble("Energy", this.energy);
        return tag;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        world.setBlockState(pos, getCachedState().with(PowerBankBlock.PERCENTAGE, (int)(((float) energy / 2_000_000f) * 10)));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt = super.writeNbt(nbt);
        nbt.putFloat("percentage",((float) energy / 2_000_000f));
        return nbt;
    }
}
