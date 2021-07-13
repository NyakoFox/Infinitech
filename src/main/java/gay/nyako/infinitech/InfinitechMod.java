package gay.nyako.infinitech;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class InfinitechMod implements ModInitializer {

	public static final Block CONVEYOR_BELT_BLOCK = new ConveyorBeltBlock(FabricBlockSettings
															 			  .of(Material.METAL)
															  			  .strength(4.0f)
															  			  .breakByTool(FabricToolTags.PICKAXES, 1)
																		 );

	@Override
	public void onInitialize() { // modid is "infinitech"
		System.out.println("hi from infinitech!!");

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "conveyor_belt"), CONVEYOR_BELT_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "conveyor_belt"), new BlockItem(CONVEYOR_BELT_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
	}
}
