package gay.nyako.infinitech;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ConveyorBeltBlockEntity extends BlockEntity implements BlockEntityClientSerializable, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final InventoryStorage storage = InventoryStorage.of(this, null);
    public float progress = 0f;
    public int extract_cooldown = 0;

    public ConveyorBeltBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.CONVEYOR_BELT_BLOCK_ENTITY, pos, state);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ConveyorBeltBlockEntity entity) {
        if (entity.progress >= 1f) {
            entity.progress = 1f;
            if (!world.isClient()) {
                boolean succeeded = entity.Insert();
                if (succeeded) {
                    entity.progress = 0.0f;
                    entity.extract_cooldown = 10;
                }
            }
        } else {
            if (!entity.getStack(0).isEmpty()) entity.progress += 0.1f;
        }

        entity.extract_cooldown++;
        if (entity.extract_cooldown >= 10) {
            if (!world.isClient()) {
                if (entity.getStack(0).isEmpty()) entity.Extract();
            }
            entity.extract_cooldown = 0;
        }
    }

    public void Extract() {
        Direction dir = getCachedState().get(Properties.HORIZONTAL_FACING);
        if (world.getBlockEntity(pos.offset(dir.getOpposite())) instanceof ConveyorBeltBlockEntity) {
            return;
        }

        Storage<ItemVariant> back = ItemStorage.SIDED.find(world, pos.offset(dir.getOpposite()), dir);
        StorageUtil.move(back, storage , item -> true, 1, null);
    }

    public boolean Insert() {
        Direction dir = getCachedState().get(Properties.HORIZONTAL_FACING);
        Storage<ItemVariant> front = ItemStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());

        if (front == null) {
            double offsetX = 0d;
            double offsetZ = 0d;
            switch (dir) {
                case NORTH:
                    offsetZ = 0.5;
                    break;
                case SOUTH:
                    offsetZ = -0.5;
                    break;
                case EAST:
                    offsetX = -0.5;
                    break;
                case WEST:
                    offsetX = 0.5;
                    break;
            }
            ItemEntity itemEntity = new ItemEntity(world, pos.offset(dir).getX() + 0.5d + offsetX, pos.getY() + 0.15, pos.offset(dir).getZ() + 0.5d + offsetZ, getStack(0));
            itemEntity.setVelocity(dir.getOffsetX() * 0.1d, 0,dir.getOffsetZ() * 0.1d);
            world.spawnEntity(itemEntity);
            removeStack(0);
            sync();
            return true;
        }

        long output = StorageUtil.move(storage, front, item -> true, 64, null);

        return (output > 0);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        progress = tag.getFloat("progress");
        Inventories.readNbt(tag,items);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag,items);
        tag.putFloat("progress",progress);
        return super.writeNbt(tag);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        progress = tag.getFloat("progress");
        clear();
        Inventories.readNbt(tag,items);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        Inventories.writeNbt(tag,items);
        tag.putFloat("progress",progress);
        return tag;
    }
}
