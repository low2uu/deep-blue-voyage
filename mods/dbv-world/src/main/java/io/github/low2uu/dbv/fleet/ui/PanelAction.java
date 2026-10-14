package io.github.low2uu.dbv.fleet.ui;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PanelAction(UUID session,int action) implements CustomPacketPayload {
    public static final int REFRESH=0,BEGIN=1,COMMIT=2,CANCEL=3,CLOSE=4,CHECK=5;
    public static final Type<PanelAction> TYPE=new Type<>(ResourceLocation.parse("dbv_world:panel_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PanelAction> CODEC=StreamCodec.of(
            (b,v)->{b.writeUUID(v.session);b.writeVarInt(v.action);},b->new PanelAction(b.readUUID(),b.readVarInt()));
    @Override public Type<PanelAction> type(){return TYPE;}
}
