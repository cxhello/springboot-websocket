package top.cxhello.websocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author cxhello
 * @date 2022/8/6
 */
@Component
@ServerEndpoint("/api/websocket/{id}")
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    // 静态变量,用来记录当前在线连接数.应该把它设计成线程安全的
    private static int onlineCount = 0;

    // concurrent包的线程安全Set,用来存放每个客户端对应的MyWebSocket对象

    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    // 与某个客户端的连接会话,需要通过它来给客户端发送数据
    private Session session;

    // 接收id
    private String id = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        this.session = session;
        webSocketSet.add(this);     // 加入set中
        this.id = id;
        addOnlineCount();           // 在线数加1
        try {
            sendMessage("conn_success");
            logger.info("有新窗口开始监听: {}, 当前在线人数为: {}", id, getOnlineCount());
        } catch (IOException e) {
            logger.error("websocket IO Exception");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  // 从set中删除
        subOnlineCount();           // 在线数减1
        // 断开连接情况下,更新主板占用情况为释放
        logger.info("释放的id为: {}", id);
        // 这里写你 释放的时候，要处理的业务
        logger.info("有一连接关闭！当前在线人数为" + getOnlineCount());

    }

    /**
     * 收到客户端消息后调用的方法
     * @ Param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("收到来自窗口: {} 的信息: {}",id, message);
        // 群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @ Param session
     * @ Param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, @PathParam("id") String id) throws IOException {
        logger.info("推送消息到窗口: {}, 推送内容: {}", id, message);

        for (WebSocketServer item : webSocketSet) {
            try {
                // 这里可以设定只推送给这个id的，为null则全部推送
                if (id == null) {
                    item.sendMessage(message);
                } else if (item.id.equals(id)) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

    public static CopyOnWriteArraySet<WebSocketServer> getWebSocketSet() {
        return webSocketSet;
    }

}
