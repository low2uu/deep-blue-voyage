package io.github.low2uu.dbv.fleet.ui;

import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Tokens are scoped to one player and one nearby vessel. Every mutation still uses RefitService authorization. */
public final class FleetPanel {
    private static final Map<ServerPlayer,Session> SESSIONS=new WeakHashMap<>();
    private static final class Session {
        final UUID token=UUID.randomUUID(),body;
        boolean flagship;
        long lastPacket=-100,expires;
        String notice="关闭窗口后可继续建设；关闭窗口不会取消已开始的改装。";
        Session(UUID body,long now){this.body=body;expires=now+200;}
    }
    private FleetPanel(){}
    public static void unavailable(ServerPlayer player,String message){
        PacketDistributor.sendToPlayer(player,new PanelView(UUID.randomUUID(),true,"潜艇管理",message,"请先将船载电脑装配到潜艇船体。",0,List.of()));
    }
    public static void register(RegisterPayloadHandlersEvent event){
        var registrar=event.registrar("2");
        registrar.playToServer(PanelAction.TYPE,PanelAction.CODEC,(payload,context)->{
            if(context.player() instanceof ServerPlayer p)handle(p,payload);
        });
        registrar.playToClient(PanelView.TYPE,PanelView.CODEC,(payload,context)->
                io.github.low2uu.dbv.fleet.client.FleetScreen.receive(payload));
    }
    public static void open(ServerPlayer player,ServerSubLevel body,BlockPos controller){
        if(!(body.getLevel().getBlockEntity(controller) instanceof CommandSubBlockEntity)||!near(player,body))return;
        var session=new Session(body.getUniqueId(),player.serverLevel().getGameTime());
        try {session.flagship=FleetModule.get(player.getServer()).registry().flagship().map(i->i.bodyId().equals(body.getUniqueId())).orElse(false);}catch(Exception ignored){}
        SESSIONS.put(player,session);send(player,session,body,true);
    }
    public static void openMain(ServerPlayer player) throws java.io.IOException {
        var m=FleetModule.get(player.getServer());var body=m.refit().currentBody(m);
        if(m.refit().blockMode()? !m.refit().near(player):body==null||!near(player,body))throw new IllegalStateException("请靠近主艇或船坞改装区域");
        var s=new Session(m.registry().flagship().orElseThrow().bodyId(),player.serverLevel().getGameTime());s.flagship=true;
        SESSIONS.put(player,s);send(player,s,body,true);
    }
    private static boolean near(ServerPlayer player,ServerSubLevel body){
        if(player.serverLevel()!=body.getLevel()||body.isRemoved())return false;
        var p=body.logicalPose().transformPositionInverse(player.position());var b=body.getPlot().getBoundingBox();
        return p.x>=b.minX()-12&&p.x<=b.maxX()+12&&p.y>=b.minY()-12&&p.y<=b.maxY()+12&&p.z>=b.minZ()-12&&p.z<=b.maxZ()+12;
    }
    public static void handle(ServerPlayer player,PanelAction action){
        var s=SESSIONS.get(player);long now=player.serverLevel().getGameTime();
        if(s==null||!s.token.equals(action.session())||action.action()<0||action.action()>PanelAction.STORAGE_RESTORE)return;
        if(action.action()==PanelAction.CLOSE){SESSIONS.remove(player);return;}
        if(now>s.expires){
            PacketDistributor.sendToPlayer(player,new PanelView(s.token,false,"潜艇管理","连接已过期","请重新打开管理界面。",0,List.of()));
            SESSIONS.remove(player);return;
        }
        if(now-s.lastPacket<4&&action.action()==PanelAction.REFRESH)return;s.lastPacket=now;s.expires=now+200;
        try{
            var m=FleetModule.get(player.getServer());var body=s.flagship?m.refit().currentBody(m):m.adapter().find(player.serverLevel(),s.body);
            if(s.flagship&&m.refit().blockMode()?!m.refit().near(player):body==null||!near(player,body)){
                PacketDistributor.sendToPlayer(player,new PanelView(s.token,false,"潜艇管理","连接已断开","请靠近原潜艇，重新打开管理界面。",0,List.of()));
                SESSIONS.remove(player);return;
            }
            if(action.action()==PanelAction.STORAGE_BACKUP||action.action()==PanelAction.STORAGE_RESTORE){
                if(!s.flagship)throw new IllegalStateException("仅主艇可操作独立存储档案");
                s.notice=action.action()==PanelAction.STORAGE_BACKUP?m.storage().manualBackup(player,m):m.storage().restoreLatest(player,m);
            }else if(action.action()==PanelAction.CHECK){
                var q=(s.flagship?m.refit().report(m):m.inspection().inspect(body)).qualification();
                String access=m.refit().accessProblem(player,m);
                s.notice="检查完成："+(s.flagship&&m.refit().blockMode()?"当前为世界方块；点击完成改装后才执行运行资格验收。":q.eligible()?"控制器、供氧和密闭均符合。":String.join("；",q.reasons()))
                        +(access==null?"":"；操作限制："+access);
            }else if(action.action()!=PanelAction.REFRESH){
                if(!s.flagship||m.registry().flagship().isEmpty())throw new IllegalStateException("此船尚未登记为主潜艇，不能改装");
                s.notice=m.refit().execute(switch(action.action()){case 1->"begin";case 2->"commit";case 3->"cancel";default->throw new IllegalStateException();},player,m);
            }
            send(player,s,body,false);
        }catch(Exception error){
            com.mojang.logging.LogUtils.getLogger().error("DBV panel action {} failed",action.action(),error);
            s.notice="操作未完成："+safeMessage(error);
            try {var m=FleetModule.get(player.getServer());var body=s.flagship?m.refit().currentBody(m):m.adapter().find(player.serverLevel(),s.body);send(player,s,body,false);}
            catch(Exception ignored){PacketDistributor.sendToPlayer(player,new PanelView(s.token,false,"潜艇管理","暂时无法读取船况",s.notice,0,List.of()));}
        }
    }
    private static String safeMessage(Exception e){
        String message=e.getMessage();
        return message!=null&&message.matches("(?s).*[\\p{IsHan}].*")?message:"服务器处理失败，请保留备份并查看日志";
    }
    private static void send(ServerPlayer player,Session s,ServerSubLevel body,boolean open){
        try{
            var m=FleetModule.get(player.getServer());boolean registered=s.flagship;
            var report=registered?m.refit().report(m):m.inspection().inspect(body);var q=report.qualification();
            int flags=PanelView.CONNECTED|(q.controller()?1:0)|(q.oxygen()?2:0)|(q.sealed()?4:0)|(q.pending()?8:0)|(registered?16:0);
            String status=registered?m.refit().status():"此船尚未登记为主潜艇";
            if(registered&&m.refit().active())flags|=PanelView.ACTIVE;
            if(registered&&m.refit().checking())flags|=PanelView.CHECKING;
            if(registered&&m.refit().blockMode())flags|=PanelView.BLOCK_MODE;
            if(registered&&m.refit().accessProblem(player,m)==null)flags|=PanelView.EDITABLE;
            String title=registered?m.registry().flagship().orElseThrow().name():"潜艇设备检查";
            var devices=report.devices().stream().limit(128).map(d->d.type()+" · "+d.relativePosition().toShortString()+"\n"+d.status()).toList();
            String reason=registered?m.refit().accessProblem(player,m):null;
            if(reason!=null)status+="；"+reason;
            List<String> storage=List.of();String info="";
            if(registered){
                try{storage=m.storage().lines(m);}catch(Exception error){storage=List.of("存储扫描暂不可用\n"+safeMessage(error));}
                var identity=m.registry().flagship().orElseThrow();var telemetry=m.sample(player.getServer());
                info="永久主艇 ID："+identity.vesselId()+"\n载体代次："+identity.generation()+" · 状态："+m.refit().status()
                    +"\n质量："+(Double.isFinite(telemetry.mass())?String.format(java.util.Locale.ROOT,"%.1f",telemetry.mass()):"暂不可用")
                    +" · 速度："+(Double.isFinite(telemetry.speed())?String.format(java.util.Locale.ROOT,"%.2f",telemetry.speed()):"暂不可用");
                if(!m.refit().active()&&m.refit().accessProblem(player,m)==null)flags|=PanelView.STORAGE_EDITABLE;
            }
            storage=storage.stream().map(line->line.length()>1000?line.substring(0,1000):line).toList();
            PacketDistributor.sendToPlayer(player,new PanelView(s.token,open,title,status,s.notice,flags,devices,storage,info));
        }catch(Exception error){PacketDistributor.sendToPlayer(player,new PanelView(s.token,open,"潜艇管理","船况读取失败",safeMessage(error),0,List.of()));}
    }
    public static boolean notifyResult(ServerPlayer player,String result){
        var s=SESSIONS.get(player);
        if(s==null||player.serverLevel().getGameTime()>s.expires)return false;
        s.notice=result;return true;
    }
}
