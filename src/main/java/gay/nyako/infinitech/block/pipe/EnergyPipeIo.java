package gay.nyako.infinitech.block.pipe;

import com.google.common.collect.MapMaker;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.minecraft.util.math.Direction;

import java.util.Map;

public class EnergyPipeIo implements EnergyIo {
    private static final Map<PipeSideContext, EnergyPipeIo> CACHE = new MapMaker().weakValues().makeMap();

    public static EnergyPipeIo of(EnergyPipePart pipe, Direction side) {
        return CACHE.computeIfAbsent(new PipeSideContext(pipe, side), p -> new EnergyPipeIo(pipe, side));
    }

    private final EnergyPipePart pipe;
    private final Direction side;

    public EnergyPipeIo(EnergyPipePart pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    @Override
    public double getEnergy() {
        return 0;
    }

    @Override
    public double getEnergyCapacity() {
        return Double.MAX_VALUE;
    }

    @Override
    public double insert(double amount, Simulation simulation) {
        var storages = pipe.getConnectedStorages(side);

        if (storages.size() == 0) {
            return amount;
        }

        var validStorages = storages.stream().filter((storage) -> storage.insert(pipe.transferRate, Simulation.SIMULATE) < pipe.transferRate).toList();
        var perStorage = amount / validStorages.size();

        var toInsert = amount;
        for (EnergyIo storage : validStorages) {
            toInsert -= perStorage - storage.insert(perStorage, simulation);
        }

        return toInsert;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }
}
