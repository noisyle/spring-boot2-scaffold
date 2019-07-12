package com.noisyle.scaffold.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/public/hello")
    public Object publicHello(Principal principal) {
        logger.debug("principal: {}", principal);
        return ResponseEntity.ok("hello public");
    }
    
    @GetMapping("/private/hello")
    public Object privateHello(Principal principal) {
        logger.debug("principal: {}", principal);
        return ResponseEntity.ok("hello private");
    }

}
