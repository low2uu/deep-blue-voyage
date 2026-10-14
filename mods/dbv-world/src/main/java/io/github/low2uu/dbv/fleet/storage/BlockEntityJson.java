package io.github.low2uu.dbv.fleet.storage;

import com.google.gson.*;
import net.minecraft.nbt.*;

/** Lossless JSON boundary adapter for third-party block metadata. No NBT files or binary blobs. */
public final class BlockEntityJson {
    private BlockEntityJson(){}
    public static JsonObject encode(Tag tag){
        var out=new JsonObject();out.addProperty("type",tag.getId());
        if(tag instanceof CompoundTag compound){
            var values=new JsonObject();for(String key:compound.getAllKeys())values.add(key,encode(compound.get(key)));out.add("value",values);
        }else if(tag instanceof ListTag list){
            var values=new JsonArray();for(Tag value:list)values.add(encode(value));out.add("value",values);
        }else if(tag instanceof ByteArrayTag a){var values=new JsonArray();for(byte v:a.getAsByteArray())values.add(v);out.add("value",values);
        }else if(tag instanceof IntArrayTag a){var values=new JsonArray();for(int v:a.getAsIntArray())values.add(v);out.add("value",values);
        }else if(tag instanceof LongArrayTag a){var values=new JsonArray();for(long v:a.getAsLongArray())values.add(Long.toString(v));out.add("value",values);
        }else if(tag instanceof NumericTag number){out.addProperty("value",number.getAsNumber().toString());
        }else if(tag instanceof StringTag string){out.addProperty("value",string.getAsString());}
        return out;
    }
    public static Tag decode(JsonObject json){
        var v=json.get("value");return switch(json.get("type").getAsInt()){
            case 0->EndTag.INSTANCE;
            case 1->ByteTag.valueOf(v.getAsByte());case 2->ShortTag.valueOf(v.getAsShort());
            case 3->IntTag.valueOf(v.getAsInt());case 4->LongTag.valueOf(v.getAsLong());
            case 5->FloatTag.valueOf(v.getAsFloat());case 6->DoubleTag.valueOf(v.getAsDouble());
            case 7->{var a=v.getAsJsonArray();byte[] values=new byte[a.size()];for(int i=0;i<values.length;i++)values[i]=a.get(i).getAsByte();yield new ByteArrayTag(values);}
            case 8->StringTag.valueOf(v.getAsString());
            case 9->{var list=new ListTag();for(var item:v.getAsJsonArray())list.add(decode(item.getAsJsonObject()));yield list;}
            case 10->{var compound=new CompoundTag();for(var entry:v.getAsJsonObject().entrySet())compound.put(entry.getKey(),decode(entry.getValue().getAsJsonObject()));yield compound;}
            case 11->{var a=v.getAsJsonArray();int[] values=new int[a.size()];for(int i=0;i<values.length;i++)values[i]=a.get(i).getAsInt();yield new IntArrayTag(values);}
            case 12->{var a=v.getAsJsonArray();long[] values=new long[a.size()];for(int i=0;i<values.length;i++)values[i]=a.get(i).getAsLong();yield new LongArrayTag(values);}
            default->throw new IllegalArgumentException("Unknown block metadata type");
        };
    }
}
