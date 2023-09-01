package com.ramon.securityclient.event;

import com.ramon.securityclient.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RegistrationCompleteEvent extends ApplicationEvent {

    private User user;
    private String applicationURL;
    public RegistrationCompleteEvent(User user, String applicationURL) {
        super(user);
        this.user = user;
        this.applicationURL = applicationURL;
    }
}
