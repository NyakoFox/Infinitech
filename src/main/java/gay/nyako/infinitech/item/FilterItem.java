package gay.nyako.infinitech.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Optional;

public abstract class FilterItem<T> extends Item implements ExtendedScreenHandlerFactory {

    private static final String ITEMS_KEY = "Items";
    private static final int FILTER_SLOTS = 9;

    public FilterItem(FabricItemSettings settings) {
        super(settings);
        inventory = new ArrayList<T>();
    }

    public boolean accepts(ItemStack stack, T resource) {
        getFilterInventory(stack).contains(resource);
        return true;
    }

    public ArrayList<T> getFilterInventory(ItemStack stack) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if (!nbtCompound.contains(ITEMS_KEY)) {
            nbtCompound.put(ITEMS_KEY, new NbtList());
        }

        NbtList nbtList = nbtCompound.getList(ITEMS_KEY, NbtList.COMPOUND_TYPE);
        for (NbtElement element : nbtList) {
            ItemStack item = ItemStack.fromNbt((NbtCompound) element);
        }

        if (optional.isPresent()) {
            NbtCompound nbtCompound2 = optional.get();
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
            itemStack.increment(k);
            itemStack.writeNbt(nbtCompound2);
            nbtList.remove(nbtCompound2);
            nbtList.add(0, nbtCompound2);
        } else {
            ItemStack itemStack2 = stack.copy();
            itemStack2.setCount(k);
            NbtCompound nbtCompound3 = new NbtCompound();
            itemStack2.writeNbt(nbtCompound3);
            nbtList.add(0, nbtCompound3);
        }
        return k;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.openHandledScreen(this);

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {

    }

    @Override
    public Text getDisplayName() {
        return Text.of("Unnamed Filter");
    }
}
