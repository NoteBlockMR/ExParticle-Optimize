# Introduction
It allows you to control particle motion using mathematical expressions.

## Music performance optimizations

This fork includes client-side safeguards for particle-heavy music datapacks.
Settings are read from `exparticle.json` in the game working directory on
startup. Existing settings are preserved; the defaults below apply when a field
is absent.

| Setting | Default | Effect |
| --- | --- | --- |
| `maxParticleCount` | `65536` | Capacity of each Minecraft particle render-type queue; not a global total. |
| `maxParticlesPerTick` | `0` | Optional limit on calls through ExParticle's particle creation helper per client tick. Extra particles are dropped, not deferred; `0` disables the limit. |
| `maxExpressionCacheEntries` | `4096` | Limits cached compiled expressions using least-recently-used eviction. `0` disables caching. |
| `groupCleanupIntervalTicks` | `20` | Periodically removes dead particle references from groups and removes empty groups; `0` disables automatic cleanup. |

The optional spawn limit can make dense effects visibly incomplete, so it is
disabled by default to preserve particle density. These
changes do not modify datapack paths, timings, or cube sampling parameters.
They do not cap all vanilla particles or necessarily solve server-side lag.
For existing `exparticle.json` files, explicitly check these values if
you want the new default rather than the previously saved value.
