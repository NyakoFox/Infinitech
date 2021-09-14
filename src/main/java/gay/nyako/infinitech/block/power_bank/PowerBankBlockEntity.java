package gay.nyako.infinitech.block.power_bank;

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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

public class PowerBankBlockEntity extends AbstractMachineBlockEntity implements PropertyDelegateHolder, NamedScreenHandlerFactory, BlockEntityClientSerializable {
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) energy;
                case 1 -> (int) capacity;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: energy = value;
                case 1: capacity = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public PowerBankBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.POWER_BANK_BLOCK_ENTITY, pos, state, 2_000_000, 10_000);
        canInsert = true;
        canExtract = true;
    }

    public PowerBankBlockEntity(BlockPos pos, BlockState state, long capacity, long transferRate) {
        super(InfinitechMod.POWER_BANK_BLOCK_ENTITY, pos, state, capacity, transferRate);
        canInsert = true;
        canExtract = true;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        world.setBlockState(pos, getCachedState().with(PowerBankBlock.PERCENTAGE, (int) (((float) energy / ((float) capacity)) * 10)));
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
        this.energy = tag.getLong("Energy");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        tag.putLong("Energy", this.energy);
        return tag;
    }
}
