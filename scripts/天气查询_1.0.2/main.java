/**
 * 作案工具:腾讯元宝
 * 作者QQ:1094292807
 */

// 一言列表（从文件读取）
static List<String> quoteList = new ArrayList<>();

void onLoad() {
    loadQuotesFromFile();
}

/**
 * 从插件目录下的 quotes.txt 读取一言列表
 */
void loadQuotesFromFile() {
    try {
        String filePath = pluginDir + "/quotes.txt";
        java.io.File file = new java.io.File(filePath);
        if (file.exists()) {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8")
            );
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    quoteList.add(line);
                }
            }
            reader.close();
        }
    } catch (Exception e) {
        // 读取失败，quoteList 保持空
    }
}

/**
 * 获取随机一言，若列表为空则返回空字符串
 */
String getRandomQuote() {
    if (quoteList.isEmpty()) {
        return "";
    }
    int index = (int)(Math.random() * quoteList.size());
    return quoteList.get(index);
}

// ==================== 天气查询核心代码 ====================

void onHandleMsg(Object msgInfoBean) {
    if (!msgInfoBean.isText()) return;

    String input = msgInfoBean.getContent().trim();
    if (input.isEmpty()) return;

    // 防循环
    if (input.contains("的天气：")) return;

    String weatherInfo = fetchWeatherByCity(input);
    if (weatherInfo == null) return;

    String talker = msgInfoBean.getTalker();
    sendText(talker, weatherInfo);
}

// ---------- 城市中英文映射 ----------
static final Map<String, String> CITY_CN_MAP = new HashMap<>();
static {
    CITY_CN_MAP.put("beijing", "北京"); CITY_CN_MAP.put("shanghai", "上海");
    CITY_CN_MAP.put("guangzhou", "广州"); CITY_CN_MAP.put("shenzhen", "深圳");
    CITY_CN_MAP.put("chengdu", "成都"); CITY_CN_MAP.put("hangzhou", "杭州");
    CITY_CN_MAP.put("nanjing", "南京"); CITY_CN_MAP.put("wuhan", "武汉");
    CITY_CN_MAP.put("chongqing", "重庆"); CITY_CN_MAP.put("tianjin", "天津");
    CITY_CN_MAP.put("suzhou", "苏州"); CITY_CN_MAP.put("xian", "西安");
    CITY_CN_MAP.put("changsha", "长沙"); CITY_CN_MAP.put("zhengzhou", "郑州");
    CITY_CN_MAP.put("qingdao", "青岛"); CITY_CN_MAP.put("dalian", "大连");
    CITY_CN_MAP.put("kunming", "昆明"); CITY_CN_MAP.put("xiamen", "厦门");
    CITY_CN_MAP.put("fuzhou", "福州"); CITY_CN_MAP.put("hefei", "合肥");
    CITY_CN_MAP.put("nanchang", "南昌"); CITY_CN_MAP.put("guiyang", "贵阳");
    CITY_CN_MAP.put("lanzhou", "兰州"); CITY_CN_MAP.put("harbin", "哈尔滨");
    CITY_CN_MAP.put("changchun", "长春"); CITY_CN_MAP.put("shenyang", "沈阳");
    CITY_CN_MAP.put("jinan", "济南"); CITY_CN_MAP.put("taipei", "台北");
    CITY_CN_MAP.put("hongkong", "香港"); CITY_CN_MAP.put("macau", "澳门");
    CITY_CN_MAP.put("tokyo", "东京"); CITY_CN_MAP.put("seoul", "首尔");
    CITY_CN_MAP.put("london", "伦敦"); CITY_CN_MAP.put("paris", "巴黎");
    CITY_CN_MAP.put("newyork", "纽约"); CITY_CN_MAP.put("losangeles", "洛杉矶");
    CITY_CN_MAP.put("sydney", "悉尼"); CITY_CN_MAP.put("singapore", "新加坡");
    CITY_CN_MAP.put("bangkok", "曼谷"); CITY_CN_MAP.put("moscow", "莫斯科");
}

// ---------- WMO天气代码 → [中文描述, emoji] ----------
static final Map<Integer, String[]> WMO_INFO = new HashMap<>();
static {
    WMO_INFO.put(0,  new String[]{"晴", "☀️"});
    WMO_INFO.put(1,  new String[]{"大部晴", "🌤️"});
    WMO_INFO.put(2,  new String[]{"多云", "⛅"});
    WMO_INFO.put(3,  new String[]{"阴", "☁️"});
    WMO_INFO.put(45, new String[]{"雾", "🌫️"});
    WMO_INFO.put(48, new String[]{"雾凇", "🌫️"});
    WMO_INFO.put(51, new String[]{"小毛毛雨", "🌦️"});
    WMO_INFO.put(53, new String[]{"中毛毛雨", "🌦️"});
    WMO_INFO.put(55, new String[]{"大毛毛雨", "🌧️"});
    WMO_INFO.put(56, new String[]{"小冻毛毛雨", "🌧️"});
    WMO_INFO.put(57, new String[]{"大冻毛毛雨", "🌧️"});
    WMO_INFO.put(61, new String[]{"小雨", "🌦️"});
    WMO_INFO.put(63, new String[]{"中雨", "🌧️"});
    WMO_INFO.put(65, new String[]{"大雨", "🌧️"});
    WMO_INFO.put(66, new String[]{"小冻雨", "🌧️"});
    WMO_INFO.put(67, new String[]{"大冻雨", "🌧️"});
    WMO_INFO.put(71, new String[]{"小雪", "❄️"});
    WMO_INFO.put(73, new String[]{"中雪", "❄️"});
    WMO_INFO.put(75, new String[]{"大雪", "❄️"});
    WMO_INFO.put(77, new String[]{"雪粒", "❄️"});
    WMO_INFO.put(80, new String[]{"小阵雨", "🌦️"});
    WMO_INFO.put(81, new String[]{"中阵雨", "🌧️"});
    WMO_INFO.put(82, new String[]{"大阵雨", "🌧️"});
    WMO_INFO.put(85, new String[]{"小阵雪", "❄️"});
    WMO_INFO.put(86, new String[]{"大阵雪", "❄️"});
    WMO_INFO.put(95, new String[]{"雷暴", "⛈️"});
    WMO_INFO.put(96, new String[]{"雷暴伴小冰雹", "⛈️"});
    WMO_INFO.put(99, new String[]{"雷暴伴大冰雹", "⛈️"});
}

// ---------- 风向中文名（16个方向） ----------
static final String[] WIND_NAMES = {"北风","北东北风","东北风","东东北风","东风","东东南风","东南风","南东南风",
                                     "南风","南西南风","西南风","西西南风","西风","西西北风","西北风","北西北风"};
// ---------- 对应每个方向的箭头符号 ----------
static final String[] WIND_ARROWS = {
    "↑", "↗", "↗", "→", "→", "↘", "↘", "↓",
    "↓", "↙", "↙", "←", "←", "↖", "↖", "↑"
};

static String windDirWithArrow(double degrees) {
    int index = (int) Math.round(degrees / 22.5) % 16;
    return WIND_NAMES[index] + WIND_ARROWS[index];
}

// ---------- 星期 ----------
static final String[] WEEK_NAMES = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};

/**
 * 主流程
 */
String fetchWeatherByCity(String city) {
    try {
        double[] coords = geocode(city);
        if (coords == null) return null;
        double lat = coords[0], lon = coords[1];

        String apiUrl = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=" + lat + "&longitude=" + lon +
                        "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m" +
                        "&daily=temperature_2m_max,temperature_2m_min" +
                        "&timezone=Asia/Shanghai&forecast_days=1";
        java.net.URL url = new java.net.URL(apiUrl);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        int code = conn.getResponseCode();
        if (code != 200) return null;

        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(conn.getInputStream(), "UTF-8")
        );
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();

        String json = sb.toString();
        org.json.JSONObject root = new org.json.JSONObject(json);
        org.json.JSONObject current = root.getJSONObject("current");
        org.json.JSONObject daily = root.getJSONObject("daily");

        double temp = current.getDouble("temperature_2m");
        double feels = current.getDouble("apparent_temperature");
        int humidity = current.getInt("relative_humidity_2m");
        int weatherCode = current.getInt("weather_code");
        double windSpeed = current.getDouble("wind_speed_10m");
        double windDeg = current.getDouble("wind_direction_10m");

        String dateStr = daily.getJSONArray("time").getString(0);
        double maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0);
        double minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0);

        String[] weatherInfo = WMO_INFO.getOrDefault(weatherCode, new String[]{"未知", "❓"});
        String weatherCn = weatherInfo[0];
        String weatherEmoji = weatherInfo[1];

        String windDisplay = windDirWithArrow(windDeg);
        int beaufort = windLevel((int)Math.round(windSpeed));

        String cityKey = city.toLowerCase().replaceAll("[^a-z]", "");
        String displayCity = CITY_CN_MAP.getOrDefault(cityKey, city);

        String dateWeek = formatDateAndWeek(dateStr);

        String quote = getRandomQuote();

        StringBuilder result = new StringBuilder();
        // 地名行居中：前面加五个全角空格
        result.append("\u3000\u3000\u3000\u3000\u3000").append("🗺️").append(displayCity).append("\n");
        result.append(weatherEmoji).append(weatherCn).append("，气温").append((int)Math.round(temp)).append("°C（体感").append((int)Math.round(feels)).append("°C）\n");
        result.append("🌡️今日最高 ").append((int)Math.round(maxTemp)).append("°C / 最低 ").append((int)Math.round(minTemp)).append("°C\n");
        result.append("🌪️风力：").append(windDisplay).append(" ").append(beaufort).append("级（").append((int)Math.round(windSpeed)).append(" km/h）\n");
        result.append("💧湿度：").append(humidity).append("%\n");
        result.append("📆今天是").append(dateWeek);
        if (!quote.isEmpty()) {
            result.append("\n📖").append(quote);
        }
        return result.toString();

    } catch (org.json.JSONException je) {
        return null;
    } catch (Exception e) {
        return null;
    }
}

/**
 * 地理编码
 */
double[] geocode(String city) throws Exception {
    String encoded = java.net.URLEncoder.encode(city, "UTF-8");
    String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=1&language=zh&format=json";
    java.net.URL url = new java.net.URL(urlStr);
    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(5000);
    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

    int code = conn.getResponseCode();
    if (code != 200) return null;

    java.io.BufferedReader reader = new java.io.BufferedReader(
        new java.io.InputStreamReader(conn.getInputStream(), "UTF-8")
    );
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) sb.append(line);
    reader.close();
    conn.disconnect();

    String json = sb.toString();
    if (json.contains("\"results\":[")) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        org.json.JSONObject result = obj.getJSONArray("results").getJSONObject(0);
        double lat = result.getDouble("latitude");
        double lon = result.getDouble("longitude");
        return new double[]{lat, lon};
    }
    return null;
}

/** 蒲福风力等级 */
int windLevel(int kmh) {
    if (kmh < 1) return 0;
    if (kmh <= 5) return 1;
    if (kmh <= 11) return 2;
    if (kmh <= 19) return 3;
    if (kmh <= 28) return 4;
    if (kmh <= 38) return 5;
    if (kmh <= 49) return 6;
    if (kmh <= 61) return 7;
    if (kmh <= 74) return 8;
    if (kmh <= 88) return 9;
    if (kmh <= 102) return 10;
    if (kmh <= 117) return 11;
    return 12;
}

/** 格式化日期 */
String formatDateAndWeek(String ymd) {
    try {
        java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Date d = sdfIn.parse(ymd);
        java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat("M月d日");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(d);
        int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
        String week = WEEK_NAMES[(dow == 1 ? 0 : dow - 1)];
        return sdfOut.format(d) + " " + week;
    } catch (Exception e) {
        return ymd;
    }
}
