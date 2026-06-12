package de.cadentem.cave_dweller.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CaveSoundPayload(ResourceLocation soundResource, BlockPos playerPosition, float volume, float pitch)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CaveSoundPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("cave_dweller", "cave_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CaveSoundPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CaveSoundPayload::soundResource,
            BlockPos.STREAM_CODEC, CaveSoundPayload::playerPosition,
            ByteBufCodecs.FLOAT, CaveSoundPayload::volume,
            ByteBufCodecs.FLOAT, CaveSoundPayload::pitch,
            CaveSoundPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
