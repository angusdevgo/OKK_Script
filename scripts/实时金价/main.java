void fetchGoldPrice(String talker) {
    // 使用金投网的实时数据接口，JO_92233 为现货黄金代码
    var url = "https://api.jijinhao.com/sQuoteCenter/realTime.htm?code=JO_92233&isCalc=true";
    
    // 必须携带 Referer 和 User-Agent，否则会被拦截返回 666
    var headers = new HashMap<String, String>();
    headers.put("Referer", "https://m.cngold.org/");
    headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/04.1");

    get(url, headers, resp -> {
        // 返回格式示例：var hq_str = "现货黄金,0,866.1159,867.7693,870.48145,864.44934,...";
        if (resp != null && resp.contains("hq_str")) {
            try {
                int start = resp.indexOf("\"");
                int end = resp.lastIndexOf("\"");
                if (start != -1 && end != -1 && start < end) {
                    String dataStr = resp.substring(start + 1, end);
                    String[] parts = dataStr.split(",");
                    
                    if (parts.length > 37) {
                        String name = parts[0];          // 名称
                        String price = parts[3];         // 最新价
                        String open = parts[2];          // 开盘价
                        String high = parts[4];          // 最高价
                        String low = parts[5];           // 最低价
                        String change = parts[35];       // 涨跌额
                        String percent = parts[36];      // 涨跌幅
                        String date = parts[30];         // 日期
                        String time = parts[31];         // 时间

                        String message = "【" + name + "】实时行情\n" +
                                         "当前价格：" + price + " 元/克\n" +
                                         "今日涨跌：" + change + " (" + percent + "%)\n" +
                                         "今日最高：" + high + "\n" +
                                         "今日最低：" + low + "\n" +
                                         "开盘价格：" + open + "\n" +
                                         "更新时间：" + date + " " + time + "\n" +
                                         "数据来源：金投网";
                        sendText(talker, message);
                        return;
                    }
                }
            } catch (Exception e) {
                // 解析失败处理
            }
        }
        sendText(talker, "抱歉，获取实时金价数据失败，请稍后再试。");
    });
}

void onHandleMsg(Object msgInfoBean) {
    if (msgInfoBean.isText()) {
        var content = msgInfoBean.getContent();
        var talker = msgInfoBean.getTalker();
        if (content.equals("实时金价")) {
            fetchGoldPrice(talker);
        }
    }
}
