package gay.nyako.infinitech.block.energy_infuser;

import gay.nyako.infinitech.FakePlayerEntity;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.block_breaker.BlockBreakerGuiDescription;
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
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

public class EnergyInfuserBlockEntity extends AbstractMachineBlockEntity implements PropertyDelegateHolder, NamedScreenHandlerFactory, ExtendedScreenHandlerFactory {
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return (int) energy;
                case 1:
                    return (int) oldEnergy;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 1:
                    energy = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public EnergyInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.ENERGY_INFUSER_BLOCK_ENTITY, pos, state, 100_000_000, 100_000);
    }

    @Override
    public int getSlotAmount() {
        // 0 slots, the battery slot is all we need
        return 0;
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

    public static void tick(World world, BlockPos pos, BlockState state, EnergyInfuserBlockEntity blockEntity) {
        if (!world.isClient()) {
            blockEntity.oldEnergy = blockEntity.energy;

            // First, let's try to charge an item.
            blockEntity.processChargeSlot();

            // Now we should attempt to pull in or push out items, depending on the side configuration.
            blockEntity.attemptSideTransfers(blockEntity.storage);

        }
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new EnergyInfuserGuiDescription(syncId, playerInventory, ScreenHandlerContext.create(world, pos), this);
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