package top.cxhello.websocket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cxhello.websocket.config.WebSocketServer;

import java.io.IOException;
import java.util.UUID;

/**
 * @author cxhello
 * @date 2022/8/6
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String socket() throws IOException {
        WebSocketServer.sendInfo("测试", UUID.randomUUID().toString());
        WebSocketServer.sendInfo("测试123456", null);
        return "success";
    }

}
