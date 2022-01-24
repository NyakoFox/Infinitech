package gay.nyako.infinitech.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleBatteryItem;

import java.awt.*;
import java.util.List;

public class StaffOfEnderItem extends Item implements SimpleBatteryItem {
    public final int TELEPORT_COST = 2_000;
    public StaffOfEnderItem(FabricItemSettings settings) {
        super(settings);
    }



    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        NbtCompound stackNbt = stack.getOrCreateNbt();
        if (stackNbt.getInt("oldEnergy") != stackNbt.getInt("energy")) {
            stackNbt.putInt("oldEnergy", stackNbt.getInt("energy"));
            stack.setNbt(stackNbt);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (getStoredEnergy(stack) >= TELEPORT_COST) {
            HitResult result = user.raycast(10d, 0.0f, false);
            double x = Math.floor(result.getPos().getX()) + 0.5f;
            double y = Math.floor(result.getPos().getY());
            double z = Math.floor(result.getPos().getZ()) + 0.5f;
            user.requestTeleportAndDismount(x, y, z);
            world.playSound(x, y, z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
            tryUseEnergy(stack, TELEPORT_COST);
            return TypedActionResult.success(stack, world.isClient());
        }
        return super.use(world, user, hand);
    }

    @Override
    public long getEnergyCapacity() {
        return 400_000;
    }

    @Override
    public long getEnergyMaxInput() {
        return 10_000;
    }

    @Override
    public long getEnergyMaxOutput() {
        return 0;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) Math.round(((double)getStoredEnergy(stack) / (double) getEnergyCapacity()) * 13d);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return Color.decode("#C060B0").getRGB();
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        NbtCompound stackNbt = itemStack.getOrCreateNbt();
        int difference = (int) (getStoredEnergy(itemStack) - stackNbt.getInt("oldEnergy"));

        String differenceText = "(";
        if (difference < 0) {
            differenceText += "§c";
        } else if (difference > 0) {
            differenceText += "§a+";
        }
        differenceText += difference + " E/t§r)";

        tooltip.add(Text.of("Energy: " + getStoredEnergy(itemStack) + "/" + getEnergyCapacity() + " E " + differenceText));
    }


}
