package me.melontini.andromeda.modules.entities.minecarts;

import static me.melontini.andromeda.common.Andromeda.id;

import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.entities.minecarts.entities.AnvilMinecartEntity;
import me.melontini.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import me.melontini.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;

public class MinecartEntities {

  public static final Keeper<EntityType<AnvilMinecartEntity>> ANVIL_MINECART_ENTITY =
      Keeper.create();
  public static final Keeper<EntityType<NoteBlockMinecartEntity>> NOTEBLOCK_MINECART_ENTITY =
      Keeper.create();
  public static final Keeper<EntityType<JukeboxMinecartEntity>> JUKEBOX_MINECART_ENTITY =
      Keeper.create();

  static void init(Minecarts.Config config) {
    ANVIL_MINECART_ENTITY.init(RegistryUtil.register(
        config.isAnvilMinecartOn,
        Registries.ENTITY_TYPE,
        id("anvil_minecart"),
        () -> FabricEntityTypeBuilder.<AnvilMinecartEntity>create(
                SpawnGroup.MISC, AnvilMinecartEntity::new)
            .dimensions(new EntityDimensions(0.98F, 0.7F, true))
            .build()));

    NOTEBLOCK_MINECART_ENTITY.init(RegistryUtil.register(
        config.isNoteBlockMinecartOn,
        Registries.ENTITY_TYPE,
        id("note_block_minecart"),
        () -> FabricEntityTypeBuilder.<NoteBlockMinecartEntity>create(
                SpawnGroup.MISC, NoteBlockMinecartEntity::new)
            .dimensions(new EntityDimensions(0.98F, 0.7F, true))
            .build()));

    JUKEBOX_MINECART_ENTITY.init(RegistryUtil.register(
        config.isJukeboxMinecartOn,
        Registries.ENTITY_TYPE,
        id("jukebox_minecart"),
        () -> FabricEntityTypeBuilder.<JukeboxMinecartEntity>create(
                SpawnGroup.MISC, JukeboxMinecartEntity::new)
            .dimensions(new EntityDimensions(0.98F, 0.7F, true))
            .build()));
  }
}
