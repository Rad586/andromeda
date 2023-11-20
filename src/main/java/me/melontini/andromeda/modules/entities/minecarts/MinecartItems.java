package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.minecarts.items.AnvilMinecartItem;
import me.melontini.andromeda.modules.entities.minecarts.items.JukeBoxMinecartItem;
import me.melontini.andromeda.modules.entities.minecarts.items.NoteBlockMinecartItem;
import me.melontini.andromeda.modules.entities.minecarts.items.SpawnerMinecartItem;
import me.melontini.andromeda.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.start;

public class MinecartItems {

    public static final Keeper<SpawnerMinecartItem> SPAWNER_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("spawner_minecart"), () -> new SpawnerMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(ItemGroup.TRANSPORTATION)
            .register(() -> ModuleManager.quick(Minecarts.class).config().isSpawnerMinecartOn));

    public static final Keeper<AnvilMinecartItem> ANVIL_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("anvil_minecart"), () -> new AnvilMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(ItemGroup.TRANSPORTATION)
            .register(() -> ModuleManager.quick(Minecarts.class).config().isAnvilMinecartOn));

    public static final Keeper<NoteBlockMinecartItem> NOTE_BLOCK_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("note_block_minecart"), () -> new NoteBlockMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(ItemGroup.TRANSPORTATION)
            .register(() -> ModuleManager.quick(Minecarts.class).config().isNoteBlockMinecartOn));

    public static final Keeper<JukeBoxMinecartItem> JUKEBOX_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("jukebox_minecart"), () -> new JukeBoxMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(ItemGroup.TRANSPORTATION)
            .register(() -> ModuleManager.quick(Minecarts.class).config().isJukeboxMinecartOn));

}