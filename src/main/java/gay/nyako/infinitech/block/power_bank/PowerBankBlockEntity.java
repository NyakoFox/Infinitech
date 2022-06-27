package gay.nyako.infinitech.block.power_bank;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.block_breaker.BlockBreakerBlockEntity;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerBankBlockEntity extends AbstractMachineBlockEntity implements PropertyDelegateHolder, NamedScreenHandlerFactory {
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) energy;
                case 1 -> (int) capacity;
                case 2 -> (int) difference;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: energy = value; break;
                case 1: capacity = value; break;
                case 2: difference = value; break;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public PowerBankBlockEntity(BlockPos pos, BlockState state, long capacity, long transferRate) {
        super(InfinitechMod.POWER_BANK_BLOCK_ENTITY, pos, state, capacity, transferRate);
        canInsert = true;
        canExtract = true;
    }

    @Override
    public int getSlotAmount() {
        // No slots.
        return 0;
    }

    @Override
    public boolean hasBatterySlot() {
        // It should have a battery slot.
        return true;
    }

    public PowerBankBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, 2_000_000, 10_000);
    }

    @Override
    public void markDirty() {
        world.setBlockState(pos, getCachedState().with(PowerBankBlock.PERCENTAGE, (int) (((float) energy / ((float) capacity)) * 10)));
        super.markDirty();
    }

    @Override
    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    @Override
    public Text getDisplayName() {
        // Using the block name as the screen title
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new PowerBankGuiDescription(syncId, inventory, ScreenHandlerContext.create(world, pos));
    }

    public static void tick(World world, BlockPos pos, BlockState state, PowerBankBlockEntity blockEntity) {
        if (!world.isClient()) {
            // Let's try to push energy in anything beside us.
            blockEntity.attemptEnergyTransfers();

            blockEntity.calculateEnergyDifference();
        }
    }
}
