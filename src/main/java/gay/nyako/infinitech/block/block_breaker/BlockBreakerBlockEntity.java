package gay.nyako.infinitech.block.block_breaker;

import gay.nyako.infinitech.FakePlayerEntity;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockBreakerBlockEntity extends AbstractMachineBlockEntity implements PropertyDelegateHolder, NamedScreenHandlerFactory, ExtendedScreenHandlerFactory {
    private FakePlayerEntity fakePlayerEntity;
    public int breakProgress;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return breakProgress;
                case 1:
                    return (int) energy;
                case 2:
                    return (int) oldEnergy;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    breakProgress = value;
                case 1:
                    energy = value;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public BlockBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.BLOCK_BREAKER_BLOCK_ENTITY, pos, state, 1_000_000, 1_000);
    }

    @Override
    public int getSlotAmount() {
        // 1 slot for the tool, 10 slots for output.
        return 1 + 10;
    }

    @Override
    public boolean hasBatterySlot() {
        // It should have a battery slot.
        return true;
    }

    @Override
    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public FakePlayerEntity getFakePlayer() {
        if (fakePlayerEntity == null) {
            fakePlayerEntity = new FakePlayerEntity(getWorld(), getPos());
        }
        return fakePlayerEntity;
    }

    public Direction getFacing() {
        if (this.getWorld() == null) return Direction.NORTH;
        return this.getWorld().getBlockState(this.getPos()).get(Properties.FACING);
    }

    public BlockPos getBreakPos() {
        return this.getPos().add(getFacing().getVector());
    }

    public BlockState getBreakState() {
        return this.getWorld().getBlockState(this.getBreakPos());
    }

    public float getBreakingTime() {
        BlockState breakState = this.getBreakState();
        if (breakState == null) return 0.0F;
        float hardness = breakState.getHardness(this.getWorld(), this.getBreakPos());
        if (hardness == -1.0F) {
            return 0.0F;
        } else {
            int multiplier = isToolEffective() ? 30 : 100;
            return hardness * (float) multiplier;
        }
    }

    public boolean isToolEffective() {
        BlockState breakState = this.getBreakState();
        if (breakState == null) return false;
        return (!breakState.isToolRequired()) || (getToolStack().isSuitableFor(breakState));
    }

    public ItemStack getToolStack() {
        return getStack(0);
    }

    public int getBreakTime() {
        BlockState blockState = getBreakState();
        ItemStack itemStack = getToolStack();
        if (blockState == null) {
            return -1;
        }
        float baseTime = this.getBreakingTime();
        float itemMultiplier = itemStack.getMiningSpeedMultiplier(blockState);
        if (isToolEffective()) {
            int level = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
            if (level > 0) {
                itemMultiplier += (level * level + 1);
            }
        }
        float time = baseTime / itemMultiplier;
        return (int) time;
    }

    public int getBreakProgress() {
        return breakProgress;
    }

    public int getBreakPercentage() {
        return getBreakPercentage(this.getBreakProgress(), this.getBreakTime());
    }

    public static int getBreakPercentage(int breakProgress, int breakTime) {
        if (breakTime > 0) {
            float percentage = ((float) breakProgress / (float) breakTime);
            return Math.min((int) (percentage * 100), 100);
        }
        return 0;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("BreakProgress", this.getBreakProgress());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        breakProgress = nbt.getInt("BreakProgress");
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockBreakerBlockEntity blockEntity) {
        if (world.isClient()) {
            world.setBlockBreakingInfo(blockEntity.getFakePlayer().getId(), blockEntity.getBreakPos(), blockEntity.getBreakPercentage() / 10);
        } else {
            blockEntity.oldEnergy = blockEntity.energy;
            int oldBreakPercentage = blockEntity.getBreakPercentage();
            if (blockEntity.getBreakState().isAir() || blockEntity.getBreakState().getHardness(world, pos) < 0) {
                // We should probably cancel breaking...
                blockEntity.breakProgress = 0;
            }
            else if (blockEntity.getBreakProgress() >= blockEntity.getBreakTime()) {
                // We're done breaking, drop the block
                world.breakBlock(blockEntity.getBreakPos(), true, null);
                blockEntity.breakProgress = 0;
            } else {
                // We're currently breaking the block.
                blockEntity.breakProgress++;
            }
            if (oldBreakPercentage != blockEntity.getBreakPercentage()) {
                blockEntity.sync();
            }
        }
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BlockBreakerGuiDescription(syncId, playerInventory, ScreenHandlerContext.create(world, pos), this);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }
}