String COMMAND = "/摸鱼日报";
String API_URL = "https://apix.iqfk.top/api/moyuya";
String FILE_PREFIX = "/myrb";


void onHandleMsg(Object msgInfoBean) {
    if (msgInfoBean.isText()) {
        var content = msgInfoBean.getContent()
        var talker = msgInfoBean.getTalker()
        if (talker == null || talker.isEmpty()) {
            if (talker == null || talker.isEmpty()) {
                log("talker无效，取消操作: content=" + content);
                toast("操作失败：请先发送消息以初始化会话");
                return true;
            }
        }
        log("检测到发送按钮点击事件: content=" + content + ", talker=" + talker);
        
        if (content.equals(COMMAND)) {
            log("触发摸鱼日报指令: talker=" + talker);
            fetchImage(talker);
            return true;
        } else {
            log("未匹配触发指令: content=" + content);
            return false;
        }
    }
}

void fetchImage(String talker) {
    String savePath = cacheDir + FILE_PREFIX + "_" + System.currentTimeMillis() + ".jpg";
    try {
        log("摸鱼日报API请求: url=" + API_URL + ", savePath=" + savePath);
        
        download(API_URL, savePath, null, file -> {
			log("图片下载成功: filePath=" + file.getAbsolutePath());
                try {
					sendImage(talker, file.getAbsolutePath());
					log("摸鱼日报图片发送成功: talker=" + talker);
                    toast("图片发送成功");
                    
                delay(3000, () -> {
					file.delete();
				});
                } catch (Exception e) {
                    log("发送图片失败: " + e.toString());
                    sendText(talker, "摸鱼日报图片发送失败");
                }
		});
        log("图片下载请求发起");
    } catch (Throwable t) {
        log("图片下载调用失败: " + t.toString());
        sendText(talker, "摸鱼日报图片获取失败：网络不可用");
    }
}
