package me.gb2022.quantum3d.render.vertex;

import ink.flybird.fcommon.memory.BufferAllocator;

import java.util.concurrent.atomic.AtomicInteger;

public final class VertexBuilderAllocator {
    private final AtomicInteger counter = new AtomicInteger();
    private final BufferAllocator allocator;

    public VertexBuilderAllocator(BufferAllocator allocator) {
        this.allocator = allocator;
    }

    public void free(VertexBuilder builder) {
        this.counter.decrementAndGet();
        builder.free();
    }

    public VertexBuilder allocate(VertexFormat format, DrawMode mode, int capacity) {
        this.counter.incrementAndGet();
        return new VertexBuilder(format, capacity, mode, this.allocator);
    }
}
