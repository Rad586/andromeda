package me.melontini.andromeda.registries;

import me.melontini.andromeda.blocks.IncubatorBlock;
import me.melontini.andromeda.blocks.entities.IncubatorBlockEntity;
import me.melontini.andromeda.items.RoseOfTheValley;
import me.melontini.andromeda.util.annotations.AndromedaRegistry;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.start;

@AndromedaRegistry("andromeda:block_registry")
public class BlockRegistry {

    @Feature("unknown")
    public static final Keeper<FlowerBlock> ROSE_OF_THE_VALLEY = start(() -> ContentBuilder.BlockBuilder.create(id("rose_of_the_valley"), () -> new FlowerBlock(StatusEffects.REGENERATION, 12, AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY)))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new RoseOfTheValley(block, new FabricItemSettings().rarity(Rarity.UNCOMMON)))));

    @Feature("incubator.enable")
    public static final Keeper<IncubatorBlock> INCUBATOR_BLOCK = start(() -> ContentBuilder.BlockBuilder.create(id("incubator"), () -> new IncubatorBlock(FabricBlockSettings.of(Material.WOOD).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new BlockItem(block, new FabricItemSettings())).itemGroup(ItemGroup.REDSTONE))
            .blockEntity((block, id) -> ContentBuilder.BlockEntityBuilder.create(id, IncubatorBlockEntity::new, block)));

    @Feature("incubator.enable")
    public static final Keeper<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = Keeper.of(() -> () ->
            INCUBATOR_BLOCK.get() == null ? null : RegistryUtil.asBlockEntity(INCUBATOR_BLOCK.get()));
}
