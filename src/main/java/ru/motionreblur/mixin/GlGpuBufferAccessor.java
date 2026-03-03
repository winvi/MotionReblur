package ru.motionreblur.mixin;

import net.minecraft.client.gl.GlGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.ByteBuffer;

@Mixin(GlGpuBuffer.class)
public interface GlGpuBufferAccessor {
    @Accessor
    int getId();

    @Accessor
    ByteBuffer getBackingBuffer();
}
