package com.noisyle.scaffold.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.noisyle.scaffold.security.JwtTokenProvider;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private TaskScheduler messageBrokerTaskScheduler;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 仅当客户端不使用SockJS时(如微信小程序)，添加TaskScheduler，由stomp协商heartbeat。
        // 否则可以不设置，由SockJS自行协商heartbeat。
        // https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#websocket-stomp-handle-simple-broker
        // https://stackoverflow.com/a/42308169
        registry.enableSimpleBroker("/topic", "/queue").setTaskScheduler(messageBrokerTaskScheduler);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket-endpoint").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (!StringUtils.isEmpty(token)) {
                        token = token.substring(7, token.length());
                        if(jwtTokenProvider.validateToken(token)) {
                            Authentication auth = jwtTokenProvider.getAuthentication(token);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            accessor.setUser(auth);
                        }
                    }
                }
                return message;
            }
        });
    }
    
    @Bean
    public ApplicationListener<SessionConnectEvent> webSocketSessionConnectListener () {
        return event -> {
            logger.debug("创建WebSocket会话: {}", event);
        };
    }

    @Bean
    public ApplicationListener<SessionDisconnectEvent> webSocketSessionDisconnectListener () {
        return event -> {
            logger.debug("断开WebSocket会话: {}", event);
        };
    }
}
