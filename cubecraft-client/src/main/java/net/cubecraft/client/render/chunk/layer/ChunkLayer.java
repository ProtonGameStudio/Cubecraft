package net.cubecraft.client.render.chunk.layer;

import ink.flybird.quantum3d_legacy.drawcall.IRenderCall;
import me.gb2022.commons.registry.TypeItem;
import net.cubecraft.client.context.ClientRenderContext;
import net.cubecraft.client.render.IRenderType;
import net.cubecraft.client.render.LevelRenderContext;
import net.cubecraft.client.render.RenderType;
import net.cubecraft.client.render.WorldRenderObject;
import net.cubecraft.client.render.chunk.RenderChunkPos;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;


public abstract class ChunkLayer implements WorldRenderObject {
    private final IRenderCall renderCall;
    private final IRenderCall[] batches = new IRenderCall[7];

    private final RenderChunkPos pos;
    private boolean filled = false;

    public ChunkLayer(boolean vbo, RenderChunkPos pos) {
        this.renderCall = IRenderCall.create(vbo);

        for (int i = 0; i < 7; i++) {
            this.batches[i] = IRenderCall.create(vbo);
        }

        this.pos = pos;
    }

    public static String encode(String layer, long x, long y, long z) {
        return layer + "{" + x + "," + y + "," + z + "}";
    }

    public static String encode(String layer, RenderChunkPos pos) {
        return encode(layer, pos.getX(), pos.getY(), pos.getZ());
    }

    public void render(RenderType type) {
        if (getRenderType() != type) {
            return;
        }
        this.render();
    }

    @Override
    public double distanceTo(Vector3d position) {
        return this.pos.distanceTo(position);
    }

    public void allocate() {
        this.renderCall.allocate();
    }

    public void destroy() {
        this.renderCall.free();
    }

    public void render() {
        if (!this.filled) {
            return;
        }


        this.renderCall.call();
    }

    public void render(double vx, double vy, double vz) {
        if (!this.filled) {
            return;
        }

        long x = this.pos.getWorldX();
        long y = this.pos.getWorldY();
        long z = this.pos.getWorldZ();

        if (vy > y) {
            this.batches[0].call();
        }
        if (vy < y + 16) {
            this.batches[1].call();
        }
        if (vz > z) {
            this.batches[2].call();
        }
        if (vz < z + 16) {
            this.batches[3].call();
        }
        if (vx > x) {
            this.batches[4].call();
        }
        if (vx < x + 16) {
            this.batches[5].call();
        }

        this.batches[6].call();
    }

    @Override
    public void render(LevelRenderContext context) {
        ClientRenderContext.TEXTURE.getTexture2DTileMapContainer().bind("cubecraft:terrain");
        GL11.glPushMatrix();
        context.getCamera().setupObjectCamera(this.getPos().getBaseWorldPosition());
        this.render();
        GL11.glPopMatrix();
    }

    public RenderChunkPos getPos() {
        return this.pos;
    }

    public boolean isFilled() {
        return this.filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public IRenderCall getRenderCall() {
        return this.renderCall;
    }

    public abstract IRenderType getRenderType();

    public String getID() {
        return this.getClass().getAnnotation(TypeItem.class).value();
    }

    public String encode() {
        return encode(this.getID(), this.getPos());
    }

    @Override
    public String toString() {
        return "%s{call=%d,code=%s}".formatted(this.getClass().getSimpleName(), this.getRenderCall().getHandle(), this.encode());
    }

    public IRenderCall[] getBatches() {
        return this.batches;
    }
}
