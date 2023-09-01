package com.ramon.securityclient.service;

import com.ramon.securityclient.entity.PasswordResetToken;
import com.ramon.securityclient.entity.User;
import com.ramon.securityclient.entity.VerificationToken;
import com.ramon.securityclient.model.UserModel;
import com.ramon.securityclient.repository.PasswordResetTokenRepository;
import com.ramon.securityclient.repository.UserRepository;
import com.ramon.securityclient.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserModel userModel) {
        User user = new User();
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setEmail(userModel.getEmail());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public void saveVerificationTokenForUser(User user, String token) {
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public boolean validateVerificationToken(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        if (!verificationToken.isPresent()) {
            log.info("El token {} NO existe en la BD", token);
            return false;
        }


        User user = verificationToken.get().getUser();
        if (LocalDateTime.now().isAfter(verificationToken.get().getExpirationTime())) {
            verificationTokenRepository.delete(verificationToken.get());
            log.info("El token {} ya expiro", token);
            return false;
        }


        user.setEnabled(true);
        userRepository.save(user);
        log.info("El token {} fue valido y el usuario se actualizo a activo", token);
        return true;
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        Optional<VerificationToken> verificationToken
                = verificationTokenRepository.findByToken(oldToken);

        if (verificationToken.isPresent()) {
            verificationToken.get().setToken(UUID.randomUUID().toString());
            verificationTokenRepository.save(verificationToken.get());
            return verificationToken.get();
        }
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken
                = new PasswordResetToken(token, user);

        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public Optional<PasswordResetToken> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (!passwordResetToken.isPresent()) {
            log.info("El token {} NO existe en la BD", token);
            return Optional.empty();
        }

        if (LocalDateTime.now().isAfter(passwordResetToken.get().getExpirationTime())) {
            passwordResetTokenRepository.delete(passwordResetToken.get());
            log.info("El token {} ya expiro", token);
            return Optional.empty();
        }

        return passwordResetToken;
    }

    @Override
    public User getUserByPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token).get().getUser();
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
