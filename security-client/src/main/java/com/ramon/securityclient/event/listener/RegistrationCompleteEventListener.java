package com.ramon.securityclient.event.listener;

import com.ramon.securityclient.entity.User;
import com.ramon.securityclient.event.RegistrationCompleteEvent;
import com.ramon.securityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(user, token);

        String url = event.getApplicationURL() + "/verifyRegistration?token=" + token;
        log.info("Click the link to verify your account: {}", url);
    }
}
