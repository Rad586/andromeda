package me.melontini.andromeda.registries;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.items.LockpickItem;
import me.melontini.andromeda.items.RoseOfTheValley;
import me.melontini.andromeda.items.boats.FurnaceBoatItem;
import me.melontini.andromeda.items.boats.HopperBoatItem;
import me.melontini.andromeda.items.boats.JukeboxBoatItem;
import me.melontini.andromeda.items.boats.TNTBoatItem;
import me.melontini.andromeda.items.minecarts.AnvilMinecartItem;
import me.melontini.andromeda.items.minecarts.JukeBoxMinecartItem;
import me.melontini.andromeda.items.minecarts.NoteBlockMinecartItem;
import me.melontini.andromeda.items.minecarts.SpawnerMinecartItem;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.content.ContentBuilder;
import me.melontini.crackerutil.util.Utilities;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.List;

import static me.melontini.andromeda.Andromeda.MODID;
import static me.melontini.crackerutil.content.RegistryUtil.asItem;
import static me.melontini.crackerutil.content.RegistryUtil.createItem;

public class ItemRegistry {
    public static RoseOfTheValley ROSE_OF_THE_VALLEY = asItem(BlockRegistry.ROSE_OF_THE_VALLEY);
    public static SpawnerMinecartItem SPAWNER_MINECART = ContentBuilder.ItemBuilder.create(SpawnerMinecartItem.class, new Identifier(MODID, "spawner_minecart"), AbstractMinecartEntity.Type.SPAWNER, new FabricItemSettings())
            .maxCount(1).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.REDSTONE)).build();
    public static AnvilMinecartItem ANVIL_MINECART = ContentBuilder.ItemBuilder.create(AnvilMinecartItem.class, new Identifier(MODID, "anvil_minecart"), new FabricItemSettings())
            .maxCount(1).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.REDSTONE)).loadCondition(Andromeda.CONFIG.newMinecarts.isAnvilMinecartOn).build();
    public static NoteBlockMinecartItem NOTE_BLOCK_MINECART = ContentBuilder.ItemBuilder.create(NoteBlockMinecartItem.class, new Identifier(MODID, "note_block_minecart"), new FabricItemSettings())
            .maxCount(1).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.REDSTONE)).loadCondition(Andromeda.CONFIG.newMinecarts.isNoteBlockMinecartOn).build();
    public static JukeBoxMinecartItem JUKEBOX_MINECART = ContentBuilder.ItemBuilder.create(JukeBoxMinecartItem.class, new Identifier(MODID, "jukebox_minecart"), new FabricItemSettings())
            .maxCount(1).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.REDSTONE)).loadCondition(Andromeda.CONFIG.newMinecarts.isJukeboxMinecartOn).build();
    public static Item INFINITE_TOTEM = ContentBuilder.ItemBuilder.create(Item.class, new Identifier(MODID, "infinite_totem"), new FabricItemSettings())
            .maxCount(1).rarity(Rarity.EPIC).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.REDSTONE)).loadCondition(Andromeda.CONFIG.totemSettings.enableInfiniteTotem).build();
    public static Item LOCKPICK = ContentBuilder.ItemBuilder.create(LockpickItem.class, new Identifier(MODID, "lockpick"), new FabricItemSettings())
            .maxCount(16).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.TOOLS)).loadCondition(Andromeda.CONFIG.lockpickEnabled).build();
    public static BlockItem INCUBATOR = asItem(BlockRegistry.INCUBATOR_BLOCK);
    private static final ItemStack ITEM_GROUP_ICON = Utilities.supply(() -> {
        if (Andromeda.CONFIG.unknown) {
            return new ItemStack(ROSE_OF_THE_VALLEY);
        }
        if (Andromeda.CONFIG.incubatorSettings.enableIncubator) {
            return new ItemStack(INCUBATOR);
        }
        return new ItemStack(SPAWNER_MINECART);
    });
    public static ItemGroup GROUP = ContentBuilder.ItemGroupBuilder.create(new Identifier(MODID, "group"))
            .entries(itemStacks -> {
                List<ItemStack> misc = new ArrayList<>();
                if (Andromeda.CONFIG.incubatorSettings.enableIncubator) misc.add(ItemRegistry.INCUBATOR.getDefaultStack());
                if (Andromeda.CONFIG.totemSettings.enableInfiniteTotem) misc.add(ItemRegistry.INFINITE_TOTEM.getDefaultStack());
                Utilities.appendStacks(itemStacks, misc);

                List<ItemStack> carts = new ArrayList<>();
                if (Andromeda.CONFIG.newMinecarts.isAnvilMinecartOn) carts.add(ItemRegistry.ANVIL_MINECART.getDefaultStack());
                if (Andromeda.CONFIG.newMinecarts.isJukeboxMinecartOn)
                    carts.add(ItemRegistry.JUKEBOX_MINECART.getDefaultStack());
                if (Andromeda.CONFIG.newMinecarts.isNoteBlockMinecartOn)
                    carts.add(ItemRegistry.NOTE_BLOCK_MINECART.getDefaultStack());
                carts.add(ItemRegistry.SPAWNER_MINECART.getDefaultStack());
                Utilities.appendStacks(itemStacks, carts);

                List<ItemStack> boats = new ArrayList<>();
                for (BoatEntity.Type value : BoatEntity.Type.values()) {
                    if (Andromeda.CONFIG.newBoats.isFurnaceBoatOn)
                        boats.add(Registries.ITEM.get(new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_furnace")).getDefaultStack());
                    if (Andromeda.CONFIG.newBoats.isJukeboxBoatOn)
                        boats.add(Registries.ITEM.get(new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_jukebox")).getDefaultStack());
                    if (Andromeda.CONFIG.newBoats.isTNTBoatOn)
                        boats.add(Registries.ITEM.get(new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_tnt")).getDefaultStack());
                    if (Andromeda.CONFIG.newBoats.isHopperBoatOn)
                        boats.add(Registries.ITEM.get(new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_hopper")).getDefaultStack());
                }
                Utilities.appendStacks(itemStacks, boats, false);
            }).icon(() -> ITEM_GROUP_ICON).animatedIcon(() -> (context, itemX, itemY, selected, isTopRow) -> {
                MinecraftClient client = MinecraftClient.getInstance();

                float angle = Util.getMeasuringTimeMs() * 0.09f;
                MatrixStack matrixStack = context.getMatrices();
                matrixStack.push();
                matrixStack.translate(itemX, itemY, 100f);
                matrixStack.translate(8.0, 8.0, 0.0);
                matrixStack.scale(1.0F, -1.0F, 1.0F);
                matrixStack.scale(16.0F, 16.0F, 16.0F);
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                BakedModel model = client.getItemRenderer().getModel(ITEM_GROUP_ICON, null, null, 0);
                DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, ITEM_GROUP_ICON, model);
                matrixStack.pop();
            }).displayName(AndromedaTexts.ITEM_GROUP_NAME).build();

    public static void register() {
        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            createItem(Andromeda.CONFIG.newBoats.isFurnaceBoatOn, FurnaceBoatItem.class, new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_furnace"), Registries.ITEM_GROUP.get(ItemGroups.TOOLS), value, new FabricItemSettings().maxCount(1));
            createItem(Andromeda.CONFIG.newBoats.isJukeboxBoatOn, JukeboxBoatItem.class, new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_jukebox"), Registries.ITEM_GROUP.get(ItemGroups.TOOLS), value, new FabricItemSettings().maxCount(1));
            createItem(Andromeda.CONFIG.newBoats.isTNTBoatOn, TNTBoatItem.class, new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_tnt"), Registries.ITEM_GROUP.get(ItemGroups.TOOLS), value, new FabricItemSettings().maxCount(1));
            createItem(Andromeda.CONFIG.newBoats.isHopperBoatOn, HopperBoatItem.class, new Identifier(MODID, value.getName().replace(":", "_") + "_boat_with_hopper"), Registries.ITEM_GROUP.get(ItemGroups.TOOLS), value, new FabricItemSettings().maxCount(1));
        }
        AndromedaLog.info("ItemRegistry init complete!");
    }
}
