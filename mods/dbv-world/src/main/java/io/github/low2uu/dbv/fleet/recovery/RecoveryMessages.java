package io.github.low2uu.dbv.fleet.recovery;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Localized text for the existing String-based server panel protocol. Default server notices remain Chinese. */
public final class RecoveryMessages {
    private static final String PREFIX="message.dbv_world.recovery.";
    private static final Map<String,Map<String,String>> TEXT=Map.of("zh_cn",load("zh_cn"),"en_us",load("en_us"));
    private RecoveryMessages() {}

    public static String text(String key,Object... arguments){return textIn("zh_cn",key,arguments);}
    public static String textIn(String language,String key,Object... arguments){
        String locale=language!=null&&language.toLowerCase(Locale.ROOT).startsWith("zh")?"zh_cn":"en_us";
        String pattern=TEXT.get(locale).get(PREFIX+key);
        if(pattern==null)throw new IllegalArgumentException("Missing recovery translation: "+key);
        return arguments.length==0?pattern:String.format(Locale.ROOT,pattern,arguments);
    }
    private static Map<String,String> load(String language){
        String path="/assets/dbv_world/lang/"+language+".json";
        try(var input=RecoveryMessages.class.getResourceAsStream(path)){
            if(input==null)throw new IOException("Missing language resource: "+path);
            var root=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
            var values=new HashMap<String,String>();
            for(var entry:root.entrySet())if(entry.getKey().startsWith(PREFIX))values.put(entry.getKey(),entry.getValue().getAsString());
            return Map.copyOf(values);
        }catch(IOException|RuntimeException e){throw new IllegalStateException("Cannot load recovery translations: "+language,e);}
    }
}
