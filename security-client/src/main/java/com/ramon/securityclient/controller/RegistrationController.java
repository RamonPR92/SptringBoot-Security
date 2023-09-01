package com.ramon.securityclient.controller;

import com.ramon.securityclient.entity.PasswordResetToken;
import com.ramon.securityclient.entity.User;
import com.ramon.securityclient.entity.VerificationToken;
import com.ramon.securityclient.event.RegistrationCompleteEvent;
import com.ramon.securityclient.model.PasswordModel;
import com.ramon.securityclient.model.UserModel;
import com.ramon.securityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationURL(request)));
        return "Success";
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        if (userService.validateVerificationToken(token)) {
            return "User Verified Successfully";
        } else {
            return "User NOT Was Verified";

        }
    }
    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken
                = userService.generateNewVerificationToken(oldToken);

        resendVerificationTokenMail(verificationToken, applicationURL(request));
        return "Verification Link Sent";
    }

    private void resendVerificationTokenMail(VerificationToken verificationToken, String appliactionURL) {
        String url = appliactionURL + "/verifyRegistration?token=" + verificationToken.getToken();
        log.info("Click the link to verify your account: {}", url);
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            passwordResetTokenEmail(applicationURL(request), token);
            return "Successful";
        }
        return "Failed";
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel) {
        Optional<PasswordResetToken> passwordResetToken = userService.validatePasswordResetToken(token);
        if (!passwordResetToken.isEmpty()) {
            User user = passwordResetToken.get().getUser();
            userService.changePassword(user, passwordModel.getNewPassword());
            return "Password Saved Successfully";
        }
        return "Password Was Not Saved";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if (userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())) {
            userService.changePassword(user, passwordModel.getNewPassword());
            return "Password Changed Successfully";
        }
        return "Invalid Old Password";
    }

    private void passwordResetTokenEmail(String applicationURL, String token) {
        String url = applicationURL + "/savePassword?token=" + token;
        log.info("Click the link to reset your password: {}", url);
    }

    private String applicationURL(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
