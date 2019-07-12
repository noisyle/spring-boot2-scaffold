package com.noisyle.scaffold.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/hello")
    public void greeting(String message, Principal principal) {
        logger.debug("greeting principal: {}", principal);
        template.convertAndSend("/topic/greetings", message);
//        template.convertAndSendToUser(principal.getName(), "/queue/greetings", message);
    }

}
