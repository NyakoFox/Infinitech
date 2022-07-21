package gay.nyako.infinitech;

import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class WFakeSlot extends WItemSlot {
    public static final Inventory FAKE_INVENTORY = new ImplementedInventory() {
        @Override
        public DefaultedList<ItemStack> getItems() {
            return DefaultedList.ofSize(1, ItemStack.EMPTY);
        }
    };

    public WFakeSlot(Inventory inventory, int startIndex, int slotsWide, int slotsHigh, boolean big) {
        super(FAKE_INVENTORY, startIndex, slotsWide, slotsHigh, big);
    }

    public static WFakeSlot of(int index) {
        WFakeSlot w = new WFakeSlot(FAKE_INVENTORY, index, 1, 1, false);
        return w;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public boolean isInsertingAllowed() {
        return false;
    }

    @Override
    public boolean isTakingAllowed() {
        return false;
    }
}
