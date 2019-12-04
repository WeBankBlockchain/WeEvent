package com.webank.weevent.governance.utils;

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
        String param = content;
        if (StringUtils.isNotBlank(param)) {
            param = param.trim();
        }
        return Jsoup.clean(param, "", whitelist, outputSettings);
    }
}