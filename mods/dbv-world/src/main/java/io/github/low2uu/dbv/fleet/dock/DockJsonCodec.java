package io.github.low2uu.dbv.fleet.dock;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.TreeSet;
import net.minecraft.nbt.*;

/** Readable, lossless typed JSON; streaming avoids creating millions of boxed byte-array elements. */
final class DockJsonCodec {
    static final String FORMAT="dbv-dock-json";
    static final int SCHEMA_VERSION=1;
    private DockJsonCodec() {}

    static byte[] encode(CompoundTag data) throws IOException {
        byte[] payload=payload(data);
        var output=new ByteArrayOutputStream();
        try(var writer=new JsonWriter(new OutputStreamWriter(output,StandardCharsets.UTF_8))){
            writer.beginObject();writer.name("format").value(FORMAT);
            writer.name("schemaVersion").value(SCHEMA_VERSION);writer.name("sha256").value(hash(payload));
            writer.name("payload").jsonValue(new String(payload,StandardCharsets.UTF_8));writer.endObject();
        }
        return output.toByteArray();
    }

    static CompoundTag decode(InputStream input) throws IOException {
        try(var reader=new JsonReader(new InputStreamReader(input,StandardCharsets.UTF_8))){
            String format=null,checksum=null;int version=-1;Tag payload=null;var fields=new HashSet<String>();
            reader.beginObject();
            while(reader.hasNext()){
                String name=reader.nextName();if(!fields.add(name))throw new IOException("船坞 JSON 字段重复："+name);
                switch(name){
                    case "format" -> format=reader.nextString();
                    case "schemaVersion" -> version=reader.nextInt();
                    case "sha256" -> checksum=reader.nextString();
                    case "payload" -> payload=readTag(reader,0);
                    default -> throw new IOException("船坞 JSON 含未知字段："+name);
                }
            }
            reader.endObject();
            if(reader.peek()!=JsonToken.END_DOCUMENT||!FORMAT.equals(format)||version!=SCHEMA_VERSION||!(payload instanceof CompoundTag root))
                throw new IOException("船坞 JSON 格式或版本不支持");
            if(!hash(payload(root)).equals(checksum))throw new IOException("船坞 JSON 校验失败");
            return root;
        }catch(IllegalArgumentException|IllegalStateException e){throw new IOException("船坞 JSON 数据损坏",e);}
    }

    private static byte[] payload(CompoundTag data) throws IOException {
        var output=new ByteArrayOutputStream();
        try(var writer=new JsonWriter(new OutputStreamWriter(output,StandardCharsets.UTF_8))){writeTag(writer,data,0);}
        return output.toByteArray();
    }
    private static String hash(byte[] bytes){
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private static void writeTag(JsonWriter writer,Tag tag,int depth) throws IOException {
        if(depth>512)throw new IOException("船坞 JSON 结构嵌套过深");
        writer.beginObject();writer.name("type").value(tag.getId());
        if(tag.getId()!=Tag.TAG_END){
            writer.name("value");
            if(tag instanceof CompoundTag c){
                writer.beginObject();for(String key:new TreeSet<>(c.getAllKeys())){writer.name(key);writeTag(writer,c.get(key),depth+1);}writer.endObject();
            }else if(tag instanceof ListTag list){
                writer.beginArray();for(Tag child:list)writeTag(writer,child,depth+1);writer.endArray();
            }else if(tag instanceof ByteArrayTag a){
                writer.beginArray();for(byte value:a.getAsByteArray())writer.value(value);writer.endArray();
            }else if(tag instanceof IntArrayTag a){
                writer.beginArray();for(int value:a.getAsIntArray())writer.value(value);writer.endArray();
            }else if(tag instanceof LongArrayTag a){
                writer.beginArray();for(long value:a.getAsLongArray())writer.value(Long.toString(value));writer.endArray();
            }else if(tag instanceof NumericTag n)writer.value(n.getAsNumber().toString());
            else if(tag instanceof StringTag s)writer.value(s.getAsString());
            else throw new IOException("船坞 JSON 无法编码类型："+tag.getId());
        }
        writer.endObject();
    }
    private static Tag readTag(JsonReader reader,int depth) throws IOException {
        if(depth>512)throw new IOException("船坞 JSON 结构嵌套过深");
        reader.beginObject();if(!reader.nextName().equals("type"))throw new IOException("船坞 JSON 缺少数据类型");
        int type=reader.nextInt();if(type==Tag.TAG_END){reader.endObject();return EndTag.INSTANCE;}
        if(!reader.nextName().equals("value"))throw new IOException("船坞 JSON 缺少数据值");
        Tag tag=switch(type){
            case Tag.TAG_BYTE -> ByteTag.valueOf(Byte.parseByte(reader.nextString()));
            case Tag.TAG_SHORT -> ShortTag.valueOf(Short.parseShort(reader.nextString()));
            case Tag.TAG_INT -> IntTag.valueOf(Integer.parseInt(reader.nextString()));
            case Tag.TAG_LONG -> LongTag.valueOf(Long.parseLong(reader.nextString()));
            case Tag.TAG_FLOAT -> FloatTag.valueOf(Float.parseFloat(reader.nextString()));
            case Tag.TAG_DOUBLE -> DoubleTag.valueOf(Double.parseDouble(reader.nextString()));
            case Tag.TAG_STRING -> StringTag.valueOf(reader.nextString());
            case Tag.TAG_BYTE_ARRAY -> {
                var values=new it.unimi.dsi.fastutil.bytes.ByteArrayList();reader.beginArray();
                while(reader.hasNext())values.add(Byte.parseByte(reader.nextString()));
                reader.endArray();yield new ByteArrayTag(values.toByteArray());
            }
            case Tag.TAG_INT_ARRAY -> {
                var values=new it.unimi.dsi.fastutil.ints.IntArrayList();reader.beginArray();
                while(reader.hasNext())values.add(Integer.parseInt(reader.nextString()));
                reader.endArray();yield new IntArrayTag(values.toIntArray());
            }
            case Tag.TAG_LONG_ARRAY -> {
                var values=new it.unimi.dsi.fastutil.longs.LongArrayList();reader.beginArray();
                while(reader.hasNext())values.add(Long.parseLong(reader.nextString()));
                reader.endArray();yield new LongArrayTag(values.toLongArray());
            }
            case Tag.TAG_LIST -> {
                var values=new ListTag();reader.beginArray();
                while(reader.hasNext()){
                    Tag value=readTag(reader,depth+1);
                    if(value.getId()==Tag.TAG_END||(!values.isEmpty()&&values.getElementType()!=value.getId()))throw new IOException("船坞 JSON 列表类型不一致");
                    values.add(value);
                }
                reader.endArray();yield values;
            }
            case Tag.TAG_COMPOUND -> {
                var values=new CompoundTag();reader.beginObject();
                while(reader.hasNext()){
                    String key=reader.nextName();if(values.contains(key))throw new IOException("船坞 JSON 数据键重复："+key);
                    Tag value=readTag(reader,depth+1);if(value.getId()==Tag.TAG_END)throw new IOException("船坞 JSON 数据项缺少值");
                    values.put(key,value);
                }
                reader.endObject();yield values;
            }
            default -> throw new IOException("船坞 JSON 数据类型不支持："+type);
        };
        reader.endObject();return tag;
    }
}
