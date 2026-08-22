// 触发指令
static final String COMMAND = "/微博热点";

void onHandleMsg(Object msg) {
    if (msg.isText()) {
        String content = msg.getContent().trim();
        String talker = msg.getTalker();
        
        if (content.equals(COMMAND)) {
            log("触发微博热搜指令，开始获取数据...");
            fetchWeiboHot(talker);
        }
    }
}

void fetchWeiboHot(String talker) {
    // 接口一：韩小韩微博热榜 API
    String url1 = "https://api.vvhan.com/api/hotlist/wbHot";
    
    log("尝试请求韩小韩微博热搜接口...");
    get(url1, null, new Object() {
        public void onSuccess(String resp) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(resp);
                if (json.optBoolean("success", false)) {
                    org.json.JSONArray data = json.getJSONArray("data");
                    String updateTime = json.optString("update_time", "");
                    sendHotList(talker, data, updateTime, "韩小韩API");
                    return;
                }
            } catch (Exception e) {
                log("解析韩小韩接口数据失败: " + e.toString());
            }
            // 失败则降级请求第二个接口
            fallbackFetch(talker);
        }

        public void onFailure(String err) {
            log("请求韩小韩接口失败: " + err);
            // 失败则降级请求第二个接口
            fallbackFetch(talker);
        }
    });
}

void fallbackFetch(String talker) {
    // 接口二：v2.xxapi.cn 微博热搜 API
    String url2 = "https://v2.xxapi.cn/api/weibohot";
    log("降级请求 v2.xxapi 微博热搜接口...");
    
    get(url2, null, new Object() {
        public void onSuccess(String resp) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(resp);
                if (json.optInt("code", 0) == 200) {
                    org.json.JSONArray data = json.getJSONArray("data");
                    sendHotList(talker, data, null, "XXAPI");
                    return;
                }
            } catch (Exception e) {
                log("解析 v2.xxapi 接口数据失败: " + e.toString());
            }
            sendText(talker, "抱歉，微博热搜数据获取失败，请稍后再试。");
        }

        public void onFailure(String err) {
            log("请求 v2.xxapi 接口也失败: " + err);
            sendText(talker, "抱歉，微博热搜数据获取失败，请稍后再试。");
        }
    });
}

void sendHotList(String talker, org.json.JSONArray data, String updateTime, String source) {
    if (data == null || data.length() == 0) {
        sendText(talker, "未获取到有效的微博热搜数据。");
        return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("🔥 微博实时热搜前十 🔥\n");
    if (updateTime != null && !updateTime.isEmpty()) {
        sb.append("🕒 更新时间：").append(updateTime).append("\n");
    }
    sb.append("------------------------\n");

    int count = Math.min(10, data.length());
    for (int i = 0; i < count; i++) {
        try {
            org.json.JSONObject item = data.getJSONObject(i);
            String title = item.optString("title", "未知话题");
            String hot = item.optString("hot", "");
            
            sb.append(i + 1).append(". ").append(title);
            if (!hot.isEmpty()) {
                sb.append(" (").append(hot).append(")");
            }
            if (i < count - 1) {
                sb.append("\n\n");
            }
        } catch (Exception e) {
            log("获取热搜单条数据失败: " + e.toString());
        }
    }
    
    sendText(talker, sb.toString());
    log("微博热搜数据发送完毕，来源：" + source);
}
