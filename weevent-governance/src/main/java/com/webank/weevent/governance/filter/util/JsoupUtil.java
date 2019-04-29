package com.webank.weevent.governance.filter.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

public class JsoupUtil {

    private static final Whitelist whitelist = Whitelist.basicWithImages();
    
    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
    static {
        whitelist.addAttributes(":all", "style");
    }

    public static String clean(String content) {
        if(StringUtils.isNotBlank(content)){
            content = content.trim();
        }
        return Jsoup.clean(content, "", whitelist, outputSettings);
    }
}