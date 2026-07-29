package awa.Aether_254.damage_control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.loading.FMLPaths;

public final class DamageControlConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("damage_control.json");
    private static Data data = new Data();

    private DamageControlConfig() {
    }

    public static Data get() {
        return data;
    }

    public static void load() {
        try {
            if (Files.isRegularFile(PATH)) {
                Data loaded = GSON.fromJson(Files.readString(PATH), Data.class);
                data = loaded == null ? new Data() : loaded;
            }
        } catch (IOException | RuntimeException ignored) {
            data = new Data();
        }
        sanitize();
        save();
    }

    public static void save() {
        sanitize();
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException ignored) {
        }
    }

    private static void sanitize() {
        data.minDroppedStacks = Math.max(0, Math.min(4096, data.minDroppedStacks));
        if (data.containerBlacklist == null)
            data.containerBlacklist = new ArrayList<>();
        if (data.entity == null)
            data.entity = new EntitySettings();
        if (data.voidHandling == null)
            data.voidHandling = VoidHandling.TELEPORT;
    }

    public static final class Data {
        public int minDroppedStacks = 10;
        public boolean enableForShulkerBoxes = false;
        public List<String> containerBlacklist = new ArrayList<>();
        public EntitySettings entity = new EntitySettings();
        public VoidHandling voidHandling = VoidHandling.TELEPORT;
    }

    public static final class EntitySettings {
        public boolean glowEffect = true;
        public boolean canBeLeashed = true;
    }

    public enum VoidHandling {
        FLOAT,
        TELEPORT,
        SCATTER,
        DESTROY
    }
}
