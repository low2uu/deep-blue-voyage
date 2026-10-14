package io.github.low2uu.dbv.fleet.ui;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Bounded server-authored display data; clients never supply qualification or block data. */
public record PanelView(UUID session, boolean open, String title, String status, String notice,
        int flags, List<String> devices) implements CustomPacketPayload {
    public static final int CONTROLLER=1, OXYGEN=2, SEALED=4, PENDING=8, REGISTERED=16, ACTIVE=32, EDITABLE=64, CHECKING=128, CONNECTED=256;
    public static final Type<PanelView> TYPE=new Type<>(ResourceLocation.parse("dbv_world:panel_view"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PanelView> CODEC=StreamCodec.of((b,v)->{
        b.writeUUID(v.session);b.writeBoolean(v.open);b.writeUtf(v.title,256);b.writeUtf(v.status,2048);b.writeUtf(v.notice,2048);
        b.writeVarInt(v.flags);b.writeVarInt(v.devices.size());for(String s:v.devices)b.writeUtf(s,1024);
    },b->{
        var id=b.readUUID();boolean open=b.readBoolean();String title=b.readUtf(256),status=b.readUtf(2048),notice=b.readUtf(2048);
        int flags=b.readVarInt(),count=b.readVarInt();if(count<0||count>128)throw new IllegalArgumentException("Invalid device count");
        var devices=new java.util.ArrayList<String>();for(int i=0;i<count;i++)devices.add(b.readUtf(1024));
        return new PanelView(id,open,title,status,notice,flags,devices);
    });
    public PanelView { devices=List.copyOf(devices);if(devices.size()>128)throw new IllegalArgumentException("Too many devices"); }
    public boolean has(int flag){return (flags&flag)!=0;}
    @Override public Type<PanelView> type(){return TYPE;}
}
