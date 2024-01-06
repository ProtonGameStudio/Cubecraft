package net.cubecraft.client.render.chunk;

public enum ChunkUpdateStatus {
    OUTSIDE_VISIBLE_AREA, OUTSIDE_WORLD,
    UPDATE_REQUIRED, UPDATING, UPDATE_FAILED, UPDATE_SUCCESS
}
