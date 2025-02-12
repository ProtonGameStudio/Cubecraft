package net.cubecraft.server.world;

import me.gb2022.commons.container.CollectionUtil;
import me.gb2022.commons.container.Vector3;
import net.cubecraft.event.chunk.ChunkUnloadEvent;
import net.cubecraft.internal.entity.EntityPlayer;
import net.cubecraft.level.Level;
import net.cubecraft.server.CubecraftServer;
import net.cubecraft.world.World;
import net.cubecraft.world.access.ChunkLoadAccess;
import net.cubecraft.world.chunk.ChunkState;
import net.cubecraft.world.chunk.WorldChunk;
import net.cubecraft.world.chunk.pos.ChunkPos;
import net.cubecraft.world.chunk.task.ChunkLoadLevel;
import net.cubecraft.world.chunk.task.ChunkLoadTaskType;
import net.cubecraft.world.chunk.task.ChunkLoadTicket;
import net.cubecraft.world.entity.Entity;
import net.cubecraft.world.worldGen.WorldGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ServerWorld extends World {
    private final WorldGenerator worldGenerator = new WorldGenerator(this);
    private final CubecraftServer server;
    private Thread ownerThread;
    private Executor chunkIOService;

    public ServerWorld(String id, Level level, CubecraftServer server) {
        super(id, level);
        this.server = server;
    }


    @Override
    public WorldChunk getChunk(int cx, int cz, ChunkState state) {
        var ch = !this.isThreadSafe() ? this.chunkCache.get(cx, cz) : this.chunkCache.cachedGet(cx, cz);

        if (ch != null && ch.getState().isComplete(state)) {
            return ch;
        }

        if (!isThreadSafe()) {
            return CompletableFuture.supplyAsync(() -> {
                var c = this.worldGenerator.load(cx, cz, state);
                this.chunkCache.add(c);
                return c;
            }, this.chunkIOService).join();
        }
        var c = this.worldGenerator.load(cx, cz, state);
        this.chunkCache.add(c);

        return c;
    }

    @Override
    public boolean isThreadSafe() {
        return this.ownerThread == Thread.currentThread();
    }

    @Override
    public WorldChunk threadSafeGetChunk(int cx, int cz, ChunkState state) {
        var c = this.worldGenerator.load(cx, cz, state);
        this.chunkCache.add(c);

        return c;
    }


    public void setOwnerThread() {
        this.ownerThread = Thread.currentThread();
    }

    public void setChunkIOService(Executor chunkIO) {
        this.chunkIOService = chunkIO;
    }

    public WorldGenerator getWorldGenerator() {
        return this.worldGenerator;
    }


    @Override
    public void setTick(long x, long y, long z) {
        setTickSchedule(x, y, z, -1);
    }

    @Override
    public void setTickSchedule(long x, long y, long z, int time) {
        this.scheduledTickEvents.put(new Vector3<>(x, y, z), time);
    }

    @Override
    public void tick() {
        super.tick();

        var worldSpawnPoint = this.level.getLocation(null);
        if (Objects.equals(worldSpawnPoint.getWorldId(), this.getId())) {
            ChunkLoadAccess.loadChunkRange(this, worldSpawnPoint.getChunkPos(), 12, new ChunkLoadTicket(ChunkLoadLevel.Entity_TICKING, 20));
        }

        for (Entity e : this.entities.values()) {
            if (!(e instanceof EntityPlayer)) {
                continue;
            }

            ChunkLoadAccess.loadChunkRange(this, ChunkPos.fromWorldPos(e.x, e.z), 8, ChunkLoadTicket.ENTITY);
        }

        Iterator<Entity> it2 = this.entities.values().iterator();
        while (it2.hasNext()) {
            Entity e = it2.next();
            if (e instanceof EntityPlayer) {
                continue;
            }

            var cx = ChunkPos.x(e);
            var cz = ChunkPos.z(e);

            WorldChunk c = this.getChunk(cx, cz, ChunkState.COMPLETE);
            if (c.task.shouldProcess(ChunkLoadTaskType.ENTITY_TICK)) {
                e.tick();
            } else {
                this.level.getPersistentEntityHolder().save(e);
                it2.remove();
            }
        }

        HashMap<Vector3<Long>, Integer> times = (HashMap<Vector3<Long>, Integer>) this.scheduledTickEvents.clone();
        this.scheduledTickEvents.clear();
        CollectionUtil.iterateMap(times, (key, item) -> {
            WorldChunk c = getChunk((int) (key.x() >> 4), (int) (key.z() >> 4), ChunkState.COMPLETE);
            if (item > 0 || c == null || !c.task.shouldProcess(ChunkLoadTaskType.BLOCK_TICK)) {
                this.scheduledTickEvents.put(key, item - 1);
            } else {
                getBlock(key.x(), key.y(), key.z()).get().onBlockUpdate(getBlockAccess(key.x(), key.y(), key.z()));
                this.scheduledTickEvents.remove(key);
            }
        });


        this.chunkCache.values().forEach((chunk) -> {
            if (chunk == null) {
                return;
            }
            chunk.tick();
        });


        var it = this.chunkCache.values().iterator();

        try {
            while (it.hasNext()) {
                var chunk = it.next();
                if (chunk.getDataLock().isLocked()) {
                    continue;
                }
                if (chunk.getTask().shouldLoad()) {
                    continue;
                }
                ChunkPos p = chunk.getKey();
                this.getEventBus().callEvent(new ChunkUnloadEvent(chunk, chunk.getWorld(), p.getX(), p.getZ()));
                this.worldGenerator.unload(chunk);

                it.remove();
            }
        } catch (Exception ignored) {
        }
    }

    public CubecraftServer getServer() {
        return server;
    }

    public void save() {
        for (WorldChunk chunk : this.chunkCache.map().values()) {
            this.worldGenerator.unload(chunk);
        }

        this.chunkCache.map().clear();
    }
}

//todo:计划刻，方块更新调度
