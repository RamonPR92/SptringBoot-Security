package com.ramon.securityclient.service;

import com.ramon.securityclient.entity.PasswordResetToken;
import com.ramon.securityclient.entity.User;
import com.ramon.securityclient.entity.VerificationToken;
import com.ramon.securityclient.model.UserModel;

import java.util.Optional;

public interface UserService {

    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(User user, String token);

    boolean validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    Optional<PasswordResetToken> validatePasswordResetToken(String token);

    User getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);
}
