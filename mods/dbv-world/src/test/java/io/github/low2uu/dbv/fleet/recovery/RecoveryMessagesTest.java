package io.github.low2uu.dbv.fleet.recovery;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class RecoveryMessagesTest {
    @Test void serverStringProtocolDefaultsToChineseAndEnglishIsSeparatelyAvailable(){
        assertTrue(RecoveryMessages.text("flow.permission").contains("创造模式"));
        assertEquals("The physical hull storage sector is corrupt: test.slvlr · 17",
                RecoveryMessages.textIn("en_us","native.sector_corrupt","test.slvlr",17));
        assertTrue(RecoveryMessages.text("native.sector_corrupt","test.slvlr",17).contains("test.slvlr · 17"));
    }

    @Test void bothLanguagesHaveTheSameRecoveryKeysAndUsablePlaceholders() throws Exception {
        var chinese=language("zh_cn");var english=language("en_us");
        var keys=new TreeSet<String>();var translated=new TreeSet<String>();
        chinese.keySet().stream().filter(k->k.startsWith("message.dbv_world.recovery.")).forEach(keys::add);
        english.keySet().stream().filter(k->k.startsWith("message.dbv_world.recovery.")).forEach(translated::add);
        assertEquals(keys,translated);
        for(String key:keys){
            String zh=chinese.get(key).getAsString(),en=english.get(key).getAsString();
            assertFalse(zh.isBlank(),key);assertFalse(en.isBlank(),key);
            assertFalse(en.matches("(?s).*[\\p{IsHan}].*"),"Chinese text in English resource: "+key);
            int arguments=zh.split("%s",-1).length-1;assertEquals(arguments,en.split("%s",-1).length-1,key);
            var values=new Object[arguments];Arrays.fill(values,"sample");String shortKey=key.substring("message.dbv_world.recovery.".length());
            assertDoesNotThrow(()->RecoveryMessages.text(shortKey,values),key);
            assertDoesNotThrow(()->RecoveryMessages.textIn("en_us",shortKey,values),key);
        }
    }

    private static JsonObject language(String name) throws Exception {
        try(var stream=RecoveryMessages.class.getResourceAsStream("/assets/dbv_world/lang/"+name+".json")){
            assertNotNull(stream);return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
