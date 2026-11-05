package io.github.low2uu.dbv.fleet.ui;

import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.world.WorldHeights;
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
        boolean flagship,dock,recovering;
        BlockPos terminal;
        long lastPacket=-100,expires;
        String notice="关闭窗口后可继续建设；关闭窗口不会取消已开始的改装。";
        Session(UUID body,long now){this.body=body;expires=now+200;}
    }
    private FleetPanel(){}
    public static void unavailable(ServerPlayer player,String message){
        PacketDistributor.sendToPlayer(player,new PanelView(UUID.randomUUID(),true,"潜艇管理",message,"请先将船载电脑装配到潜艇船体。",0,List.of()));
    }
    public static void register(RegisterPayloadHandlersEvent event){
        var registrar=event.registrar("3");
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
        if(!m.refit().blockMode()&&nearDock(player)){openDock(player,null);return;}
        if(m.refit().blockMode()? !m.refit().near(player):body==null||!near(player,body))throw new IllegalStateException("请靠近主艇或船坞改装区域");
        var s=new Session(m.registry().flagship().orElseThrow().bodyId(),player.serverLevel().getGameTime());s.flagship=true;
        SESSIONS.put(player,s);send(player,s,body,true);
    }
    /** An onshore terminal belongs to the permanent identity, not its possibly destroyed body. */
    public static void openDock(ServerPlayer player,BlockPos terminal) throws java.io.IOException {
        var m=FleetModule.get(player.getServer());
        if(!nearDock(player)||(terminal!=null&&(!dockPosition(terminal)||!player.blockPosition().closerThan(terminal,12))))
            throw new IllegalStateException(localizedText(player,"message.dbv_world.recovery.near_dock"));
        var identity=m.registry().flagship().orElseThrow(()->new IllegalStateException(localizedText(player,"message.dbv_world.recovery.unregistered")));
        var s=new Session(identity.bodyId(),player.serverLevel().getGameTime());
        s.flagship=true;s.dock=true;s.terminal=terminal==null?null:terminal.immutable();
        s.notice=localizedText(player,"message.dbv_world.recovery.dock_notice");
        SESSIONS.put(player,s);send(player,s,m.refit().currentBody(m),true);
    }
    public static boolean dockPosition(BlockPos pos){
        return pos.getX()>=-12&&pos.getX()<=140&&pos.getZ()>=100&&pos.getZ()<=252
                &&pos.getY()>=WorldHeights.fromLegacy(1)&&pos.getY()<=WorldHeights.fromLegacy(112);
    }
    public static boolean nearDock(ServerPlayer player){return player.serverLevel()==player.getServer().overworld()&&dockPosition(player.blockPosition());}
    private static final Map<String,Map<String,String>> TRANSLATIONS=new HashMap<>();
    /** PanelView carries strings, so resolve new messages for the player's locale, not the dedicated server's English fallback. */
    public static String localizedText(ServerPlayer player,String key,Object...args){
        String locale=player.clientInformation().language().startsWith("zh")?"zh_cn":"en_us";
        var translations=TRANSLATIONS.computeIfAbsent(locale,language->{
            try(var input=FleetPanel.class.getResourceAsStream("/assets/dbv_world/lang/"+language+".json")){
                if(input==null)throw new java.io.IOException("Missing UI language resource");
                var values=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                var result=new HashMap<String,String>();values.entrySet().forEach(e->result.put(e.getKey(),e.getValue().getAsString()));return result;
            }catch(java.io.IOException error){throw new IllegalStateException("无法读取潜艇界面语言资源",error);}
        });
        return String.format(Locale.ROOT,translations.getOrDefault(key,key),args);
    }
    private static boolean near(ServerPlayer player,ServerSubLevel body){
        if(player.serverLevel()!=body.getLevel()||body.isRemoved())return false;
        var p=body.logicalPose().transformPositionInverse(player.position());var b=body.getPlot().getBoundingBox();
        return p.x>=b.minX()-12&&p.x<=b.maxX()+12&&p.y>=b.minY()-12&&p.y<=b.maxY()+12&&p.z>=b.minZ()-12&&p.z<=b.maxZ()+12;
    }
    public static void handle(ServerPlayer player,PanelAction action){
        var s=SESSIONS.get(player);long now=player.serverLevel().getGameTime();
        if(s==null||!s.token.equals(action.session())||action.action()<0||action.action()>PanelAction.VESSEL_RESTORE)return;
        if(action.action()==PanelAction.CLOSE){SESSIONS.remove(player);return;}
        if(now>s.expires){
            PacketDistributor.sendToPlayer(player,new PanelView(s.token,false,"潜艇管理","连接已过期","请重新打开管理界面。",0,List.of()));
            SESSIONS.remove(player);return;
        }
        if(now-s.lastPacket<4&&action.action()==PanelAction.REFRESH)return;s.lastPacket=now;s.expires=now+200;
        try{
            var m=FleetModule.get(player.getServer());var body=s.flagship?m.refit().currentBody(m):m.adapter().find(player.serverLevel(),s.body);
            boolean connected=s.dock?nearDock(player)&&(s.terminal==null||player.blockPosition().closerThan(s.terminal,12))
                    :(s.flagship&&m.refit().blockMode()?m.refit().near(player):body!=null&&near(player,body));
            if(!connected){
                PacketDistributor.sendToPlayer(player,new PanelView(s.token,false,"潜艇管理","连接已断开","请靠近原潜艇，重新打开管理界面。",0,List.of()));
                SESSIONS.remove(player);return;
            }
            if(m.recovery().active()&&action.action()!=PanelAction.REFRESH&&action.action()!=PanelAction.CHECK)
                throw new IllegalStateException(localizedText(player,"message.dbv_world.recovery.busy"));
            if(action.action()==PanelAction.VESSEL_RESTORE){
                if(!s.flagship)throw new IllegalStateException(localizedText(player,"message.dbv_world.recovery.unregistered"));
                s.notice=m.recovery().restoreLatest(player,m);
            }else if(action.action()==PanelAction.VESSEL_BACKUP){
                if(!s.flagship)throw new IllegalStateException(localizedText(player,"message.dbv_world.recovery.unregistered"));
                s.notice=m.storage().manualBackup(player,m);
            }else if(action.action()==PanelAction.STORAGE_OPEN){
                if(!s.flagship)throw new IllegalStateException("仅主潜艇可打开存储终端");
                StorageTerminalMenu.open(player,m);SESSIONS.remove(player);return;
            }else if(action.action()==PanelAction.MOOR){
                if(!s.flagship)throw new IllegalStateException("仅主潜艇可从此处停泊");
                s.notice=m.starter().moor(player,m);
            }else if(action.action()==PanelAction.DEPART){
                if(!s.flagship)throw new IllegalStateException("仅主潜艇可从此处出航");
                s.notice=m.starter().depart(player,m);
            }else if(action.action()==PanelAction.STORAGE_BACKUP||action.action()==PanelAction.STORAGE_RESTORE){
                if(!s.flagship)throw new IllegalStateException("仅主艇可操作独立存储档案");
                s.notice=action.action()==PanelAction.STORAGE_BACKUP?m.storage().manualBackup(player,m):m.storage().restoreLatest(player,m);
            }else if(action.action()==PanelAction.CHECK){
                if(s.flagship&&(body==null||m.recovery().active())){
                    s.notice=m.recovery().status(m);send(player,s,body,false);return;
                }
                var q=(s.flagship?m.refit().report(m):m.inspection().inspect(body)).qualification();
                String access=m.refit().accessProblem(player,m);
                s.notice="检查完成："+(s.flagship&&m.refit().blockMode()?"当前为世界方块；点击完成改装后才执行运行资格验收。":q.eligible()?"控制器、供氧和密闭均符合。":String.join("；",q.reasons()))
                        +(access==null?"":"；操作限制："+access);
            }else if(action.action()!=PanelAction.REFRESH){
                if(!s.flagship||m.registry().flagship().isEmpty())throw new IllegalStateException("此船尚未登记为主潜艇，不能改装");
                s.notice=m.refit().execute(switch(action.action()){case 1->"begin";case 2->"commit";case 3->"cancel";default->throw new IllegalStateException();},player,m);
            }
            send(player,s,s.flagship?m.refit().currentBody(m):body,false);
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
            boolean recovering=registered&&m.recovery().active();
            if(registered&&(recovering||s.recovering))s.notice=m.recovery().status(m);
            s.recovering=recovering;
            // Keep the dock session usable after loss of the controller and the entire physical hull.
            if(registered&&(m.recovery().active()||(body==null&&!m.refit().blockMode()))){
                sendArchive(player,s,m,open);return;
            }
            var report=registered?m.refit().report(m):m.inspection().inspect(body);var q=report.qualification();
            int flags=PanelView.CONNECTED|(q.controller()?1:0)|(q.oxygen()?2:0)|(q.sealed()?4:0)|(q.pending()?8:0)|(registered?16:0);
            if(body!=null&&!body.isRemoved())flags|=PanelView.BODY_PRESENT;
            String status=registered?m.refit().status():"此船尚未登记为主潜艇";
            if(registered&&m.starter().departed(m)){flags|=PanelView.DEPARTED;status="已出航：自由物理状态，船坞固定和岸供已断开。";}
            if(registered&&m.starter().departureProblem(player,m)==null)flags|=PanelView.CAN_DEPART;
            if(registered&&m.starter().departed(m)){
                String mooring=m.starter().mooringProblem(player,m);
                if(mooring==null)flags|=PanelView.CAN_MOOR;
                else status+="；停泊条件："+mooring;
            }
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
                info="永久主艇编号："+identity.vesselId()+"\n载体代次："+identity.generation()+" · 状态："+m.refit().status()
                    +"\n质量："+(Double.isFinite(telemetry.mass())?String.format(java.util.Locale.ROOT,"%.1f",telemetry.mass()):"暂不可用")
                    +" · 速度："+(Double.isFinite(telemetry.speed())?String.format(java.util.Locale.ROOT,"%.2f",telemetry.speed()):"暂不可用");
                if(!m.refit().active()&&m.refit().accessProblem(player,m)==null)flags|=PanelView.STORAGE_EDITABLE;
                if(m.recovery().accessProblem(player,m)==null&&m.recovery().hasSnapshot(m))flags|=PanelView.CAN_RESTORE;
                String recoveryProblem=m.recovery().accessProblem(player,m);
                if(recoveryProblem!=null)status+="；"+recoveryProblem;
                info+="\n"+m.recovery().status(m);
            }
            storage=storage.stream().map(line->line.length()>1000?line.substring(0,1000):line).toList();
            PacketDistributor.sendToPlayer(player,new PanelView(s.token,open,title,status,s.notice,flags,devices,storage,info));
        }catch(Exception error){
            // A broken device inspection must not hide recovery of an otherwise valid independent archive.
            if(s.flagship)try{sendArchive(player,s,FleetModule.get(player.getServer()),open);return;}catch(Exception ignored){}
            PacketDistributor.sendToPlayer(player,new PanelView(s.token,open,"潜艇管理","船况读取失败",safeMessage(error),0,List.of()));
        }
    }
    private static void sendArchive(ServerPlayer player,Session s,FleetModule m,boolean open) throws java.io.IOException {
        var identity=m.registry().flagship().orElseThrow();
        int flags=PanelView.CONNECTED|PanelView.REGISTERED;
        if(m.recovery().active())flags|=PanelView.RECOVERING;
        String problem=m.recovery().accessProblem(player,m);
        if(problem==null&&m.recovery().hasSnapshot(m))flags|=PanelView.CAN_RESTORE;
        String status=m.recovery().status(m);if(problem!=null)status+="；"+problem;
        String info=localizedText(player,"message.dbv_world.recovery.identity",identity.vesselId(),identity.generation());
        PacketDistributor.sendToPlayer(player,new PanelView(s.token,open,identity.name(),status,s.notice,flags,List.of(),List.of(),info));
    }
    public static boolean notifyResult(ServerPlayer player,String result){
        var s=SESSIONS.get(player);
        if(s==null||player.serverLevel().getGameTime()>s.expires)return false;
        s.notice=result;return true;
    }
}
