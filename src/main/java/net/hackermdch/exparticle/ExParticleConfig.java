package net.hackermdch.exparticle;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExParticleConfig {
    public static ConfigData config = new ConfigData();

    public static void init() throws IOException {
        var gson = new Gson();
        var file = Paths.get(".").resolve("exparticle.json");
        if (!Files.exists(file)) {
            try (var writer = Files.newBufferedWriter(file)) {
                gson.toJson(config, writer);
            }
        }
        try (var reader = Files.newBufferedReader(file)) {
            config = gson.fromJson(reader, ConfigData.class);
        }
    }

    public static class ConfigData {
        /**
         * Client-side ceiling for each particle render-type queue.
         * The original default is retained to avoid reducing effect density.
         */
        public int maxParticleCount = 65536;
        /**
         * Maximum number of ExParticle particles that may be created during a
         * single client tick.  Commands over this budget are safely dropped
         * for that tick instead of queuing more client work.
         *
         * Set to 0 to disable this limiter.
         */
        public int maxParticlesPerTick = 0;
        /**
         * Number of compiled particle expressions retained for reuse.  Music
         * generators create many expressions with unique coordinates, so an
         * unbounded cache steadily retained generated classes for a whole song.
         */
        public int maxExpressionCacheEntries = 4096;
        /**
         * How often dead particle references are pruned from named groups.
         * Groups are used by particle commands for cleanup/effects, but must
         * not retain every expired particle from a long music performance.
         */
        public int groupCleanupIntervalTicks = 20;
        public int maxParticleTickMillis = 1000;
        public boolean ParallelParticleUpdate = false;
    }
}
