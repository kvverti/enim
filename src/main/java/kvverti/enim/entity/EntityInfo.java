package kvverti.enim.entity;

import java.util.function.IntFunction;

import kvverti.enim.Vec3f;

public final class EntityInfo {

    public Vec3f pos;
    public float speedSq;
    public float partialTicks;
    public float entityYaw;
    public float headYaw;
    public float entityPitch;
    public float scale = 1.0f;
    public IntFunction<Vec3f> color = i -> Vec3f.IDENTITY;
    public float alpha = 1.0f;
}