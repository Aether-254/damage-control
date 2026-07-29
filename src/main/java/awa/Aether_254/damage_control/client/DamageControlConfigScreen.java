package awa.Aether_254.damage_control.client;

import awa.Aether_254.damage_control.DamageControlConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class DamageControlConfigScreen {
    private DamageControlConfigScreen() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> create(parent));
    }

    private static Screen create(Screen parent) {
        DamageControlConfig.Data config = DamageControlConfig.get();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent)
            .setTitle(Component.literal("Damage Control"));
        ConfigCategory capture = builder.getOrCreateCategory(Component.literal("Container capture"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        capture.addEntry(entries.startIntField(Component.literal("Minimum dropped stacks"), config.minDroppedStacks)
            .setDefaultValue(10).setMin(0).setMax(4096)
            .setTooltip(Component.literal("A package is created when the number of non-empty stacks is greater than this value."))
            .setSaveConsumer(value -> config.minDroppedStacks = value).build());
        capture.addEntry(entries.startBooleanToggle(Component.literal("Enable for shulker boxes"),
                config.enableForShulkerBoxes)
            .setDefaultValue(false).setSaveConsumer(value -> config.enableForShulkerBoxes = value).build());
        capture.addEntry(entries.startStrList(Component.literal("Container blacklist"), config.containerBlacklist)
            .setDefaultValue(java.util.List.of())
            .setTooltip(Component.literal("Block IDs or block tags prefixed with #."))
            .setSaveConsumer(value -> config.containerBlacklist = value).build());

        ConfigCategory entity = builder.getOrCreateCategory(Component.literal("Damaged package"));
        entity.addEntry(entries.startBooleanToggle(Component.literal("Glow effect"), config.entity.glowEffect)
            .setDefaultValue(true).setSaveConsumer(value -> config.entity.glowEffect = value).build());
        entity.addEntry(entries.startBooleanToggle(Component.literal("Can be leashed"), config.entity.canBeLeashed)
            .setDefaultValue(true).setSaveConsumer(value -> config.entity.canBeLeashed = value).build());
        entity.addEntry(entries.startEnumSelector(Component.literal("Void handling"),
                DamageControlConfig.VoidHandling.class, config.voidHandling)
            .setDefaultValue(DamageControlConfig.VoidHandling.TELEPORT)
            .setSaveConsumer(value -> config.voidHandling = value).build());
        builder.setSavingRunnable(DamageControlConfig::save);
        return builder.build();
    }
}
