package me.gb2022.quantum3d.render.vertex;

import me.gb2022.quantum3d.util.GLUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.atomic.AtomicInteger;

public interface VertexBuilderUploader {
    AtomicInteger UPLOAD_COUNT = new AtomicInteger();

    static void uploadPointer(VertexBuilder builder) {
        VertexFormat format = builder.getFormat();

        GLUtil.checkError("upload_builder:setup-pointer");

        DataFormat vertexFormat = format.getVertexFormat();
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        var addr = MemoryUtil.memAddress(builder.generateData());
        var vBytes = format.getVertexBufferSize(1);
        var cBytes = format.getColorBufferSize(1);
        var tBytes = format.getTextureBufferSize(1);
        var nBytes = format.getNormalBufferSize(1);

        var stride = vBytes + cBytes + tBytes + nBytes;

        GL11.glVertexPointer(vertexFormat.getSize(), vertexFormat.getType().getGlId(), stride, addr);

        if (format.hasColorData()) {
            var fmt = format.getColorFormat();
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            GL11.glColorPointer(fmt.getSize(), fmt.getType().getGlId(), stride, addr + vBytes);
        }

        if (format.hasTextureData()) {
            DataFormat fmt = format.getTextureFormat();
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glTexCoordPointer(fmt.getSize(), fmt.getType().getGlId(), stride, addr + vBytes + cBytes);
        }
        if (format.hasNormalData()) {
            DataFormat fmt = format.getNormalFormat();
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            GL11.glNormalPointer(fmt.getSize(), fmt.getType().getGlId(), addr + vBytes + cBytes + tBytes);
        }

        GLUtil.checkError("upload_builder:data_upload");
        GL11.glDrawArrays(builder.getDrawMode().glId(), 0, builder.getVertexCount());
        GLUtil.checkError("upload_builder:draw_array");

        UPLOAD_COUNT.addAndGet(builder.getVertexCount());

        disableState(format);
        GLUtil.checkError("upload_builder:close_state");
    }


    static void enableState(VertexFormat format) {
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        if (format.hasColorData()) {
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }
        if (format.hasTextureData()) {
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        if (format.hasNormalData()) {
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        }
    }

    static void disableState(VertexFormat format) {
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        if (format.hasNormalData()) {
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }
        if (format.hasTextureData()) {
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        if (format.hasNormalData()) {
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        }
    }

    static void setPointerAndEnableState(VertexFormat format, long offset) {
        DataFormat vertexFormat = format.getVertexFormat();
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        var stride = format.getTotalBytes();


        GL11.glVertexPointer(vertexFormat.getSize(), vertexFormat.getType().getGlId(), stride, offset);
        offset += (long) vertexFormat.getSize() * vertexFormat.getType().getBytes();

        if (format.hasColorData()) {
            DataFormat fmt = format.getColorFormat();
            GL11.glColorPointer(fmt.getSize(), fmt.getType().getGlId(), stride, offset);
            offset += (long) fmt.getSize() * fmt.getType().getBytes();
        }
        if (format.hasTextureData()) {
            DataFormat fmt = format.getTextureFormat();
            GL11.glTexCoordPointer(fmt.getSize(), fmt.getType().getGlId(), stride, offset);
            offset += (long) fmt.getSize() * fmt.getType().getBytes();
        }
        if (format.hasNormalData()) {
            DataFormat fmt = format.getNormalFormat();
            GL11.glNormalPointer(fmt.getType().getGlId(), stride, offset);
        }
    }

    static void uploadBuffer(VertexBuilder builder, int handle) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, handle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, builder.generateData(), GL15.GL_STATIC_DRAW);

        /*
        var format = builder.getFormat();
        var index = 0;
        var offset = 0;

        var stride = format.getTotalBytes();

        var vf = format.getVertexFormat();
        GL21.glVertexAttribPointer(index, vf.getSize(), vf.getType().getGlId(), true, stride, offset);
        GL21.glEnableVertexAttribArray(index);

        offset += vf.getSize() * vf.getType().getBytes();

        if (format.hasColorData()) {
            var f = format.getColorFormat();

            index++;
            GL21.glVertexAttribPointer(index, f.getSize(), f.getType().getGlId(), true, stride, offset);
            GL21.glEnableVertexAttribArray(index);

            offset += f.getSize() * f.getType().getBytes();
        }

        if (format.hasTextureData()) {
            var f = format.getTextureFormat();

            index++;
            GL21.glVertexAttribPointer(index, f.getSize(), f.getType().getGlId(), true, stride, offset);
            GL21.glEnableVertexAttribArray(index);

            offset += f.getSize() * f.getType().getBytes();
        }

        if (format.hasNormalData()) {
            var f = format.getTextureFormat();

            index++;
            GL21.glVertexAttribPointer(index, f.getSize(), f.getType().getGlId(), true, stride, offset);
            GL21.glEnableVertexAttribArray(index);
        }

         */
    }

    static int getUploadedCount() {
        return UPLOAD_COUNT.getAndSet(0);
    }
}
