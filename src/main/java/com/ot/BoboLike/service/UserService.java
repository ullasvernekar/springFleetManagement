package com.ot.BoboLike.service;

import com.ot.BoboLike.dao.UserDao;
import com.ot.BoboLike.dto.ResponseStructure;
import com.ot.BoboLike.dto.User;
import com.ot.BoboLike.dto.request.AdminRegister;
import com.ot.BoboLike.util.Aes;
import com.ot.BoboLike.util.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailSender mailSender;

    public ResponseEntity<ResponseStructure<User>> delete(long id) {
        logger.info("Attempting to delete user by ID: {}", id);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        Optional<User> optionalUser = userDao.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            userDao.delete(user);

            logger.info("User deleted successfully with ID: {}", id);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("User deleted successfully");
            responseStructure.setData(user);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {

            logger.warn("User not found to delete with ID: {}", id);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("User not found to delete");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<User>> findById(long id) {
        logger.info("Attempting to find user by ID: {}", id);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        Optional<User> optionalUser = userDao.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            logger.info("User found with ID: {}", id);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("User found by ID " + id);
            responseStructure.setData(user);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {

            logger.warn("User does not exist with ID: {}", id);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("User does not exist");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<Page<User>>> findAll(int offset, int pageSize, String field) {
        logger.info("Attempting to find all users with offset: {}, pageSize: {}, and field: {}", offset, pageSize, field);
        ResponseStructure<Page<User>> responseStructure = new ResponseStructure<>();
        Page<User> users = userDao.findAll(offset, pageSize, field);
        if (users == null) {

            logger.warn("No users data exists");
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No data exists to find for users");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {

            logger.info("Found users data");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Found all the data of users");
            responseStructure.setData(users);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<User>> findByPhone(String phone) {
        logger.info("Attempting to find user by phone: {}", phone);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByPhone(phone);
        if (Objects.isNull(user)) {

            logger.warn("User does not exist with phone: {}", phone);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("User does not exist to be found by phone");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {

            logger.info("User found with phone: {}", phone);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("User found by phone = " + phone);
            responseStructure.setData(user);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<User>> findByEmail(String email) {
        logger.info("Attempting to find user by email: {}", email);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByEmail(email);
        if (Objects.isNull(user)) {

            logger.warn("User does not exist with email: {}", email);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("User does not exist to be found by email");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {

            logger.info("User found with email: {}", email);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("User found by email = " + email);
            responseStructure.setData(user);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<User>> login(String email, String password) {
        logger.info("Attempting to login user with email: {}", email);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByEmail(email);
        if (user == null) {
            logger.warn("User not found with email: {}", email);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("User not found with Email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseStructure);
        }
        if (password == null) {
            logger.error("Password not set for user: {}", email);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage("Password not set for user: " + email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseStructure);
        }
        if (!Aes.decrypt(user.getPassword()).equals(password)) {
            logger.warn("Incorrect password for user: {}", email);
            responseStructure.setStatus(HttpStatus.UNAUTHORIZED.value());
            responseStructure.setMessage("Incorrect password for user: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseStructure);
        }
        logger.info("Login successful for user: {}", email);
        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("Login successful for user: " + email);
        responseStructure.setData(user);
        return ResponseEntity.ok(responseStructure);
    }

    public ResponseEntity<ResponseStructure<String>> forgotPassword(String email) {
        logger.info("Attempting to process forgot password for email: {}", email);
        ResponseStructure<String> responseStructure = new ResponseStructure<>();
        User user = userDao.findByEmail(email);
        if (Objects.isNull(user)) {
            logger.warn("Email does not exist: {}", email);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Email does not exist: " + email);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            String otp = String.valueOf((int) (Math.random() * (9999 - 1000) + 1000));
            user.setOtp(otp);
            userDao.save(user);
            mailSender.sendEmail(user.getEmail(), "This is Your OTP \n" +
                            " Don't Share OTP with Anyone\n " +
                            "Enter this OTP To Update Password \n" + " -> OTP " + otp,
                    "Your OTP To Update Password");
            logger.info("OTP sent to email ID: {}", email);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("OTP sent to email ID: " + email);
            responseStructure.setData("OTP sent to the email of user");
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<User>> validateOtp(String otp) {
        logger.info("Attempting to validate OTP: {}", otp);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByOtp(otp);
        if (user != null) {
            logger.info("OTP validated successfully");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setData(user);
            responseStructure.setMessage("Success");
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("Invalid OTP: {}", otp);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setData(null);
            responseStructure.setMessage("OTP invalid");
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<User>> updateNewPassword(String password, String otp) {
        logger.info("Attempting to update password using OTP: {}", otp);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByOtp(otp);
        if (Objects.isNull(user)) {
            logger.warn("Invalid OTP: {}, password not updated", otp);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("OTP invalid, password not updated");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            user.setPassword(Aes.encrypt(password));
            user.setOtp(null);
            userDao.save(user);
            logger.info("Password updated for email: {}", user.getEmail());
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Password updated for " + user.getEmail());
            responseStructure.setData(user);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<User>> update(long id, AdminRegister userRegister) {
        logger.info("Attempting to update user with ID: {}", id);
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        Optional<User> optionalUser = userDao.findById(id);

        if (!optionalUser.isPresent()) {
            logger.warn("User not found with ID: {}", id);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("User not found ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }

        User existingUser = optionalUser.get();

        if (userDao.findByEmail(userRegister.getEmail()) != null && !existingUser.getEmail().equals(userRegister.getEmail())) {
            logger.warn("Email already in use: {}", userRegister.getEmail());
            responseStructure.setStatus(HttpStatus.CONFLICT.value());
            responseStructure.setMessage("Email already in use ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.CONFLICT);
        }

        if (userDao.findByPhone(userRegister.getPhone()) != null && !existingUser.getPhone().equals(userRegister.getPhone())) {
            logger.warn("Phone already in use: {}", userRegister.getPhone());
            responseStructure.setStatus(HttpStatus.CONFLICT.value());
            responseStructure.setMessage("Phone already in use ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.CONFLICT);
        }

        if (userRegister.getName() != null) {
            existingUser.setName(userRegister.getName());
        }
        if (userRegister.getPhone() != null) {
            existingUser.setPhone(userRegister.getPhone());
        }
        if (userRegister.getEmail() != null) {
            existingUser.setEmail(userRegister.getEmail());
        }
        if (userRegister.getPassword() != null) {
            existingUser.setPassword(Aes.encrypt(userRegister.getPassword()));
        }
        if (userRegister.getAddress() != null) {
            existingUser.setAddress(userRegister.getAddress());
        }

        userDao.save(existingUser);
        logger.info("User updated successfully with ID: {}", id);

        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("User updated successfully ");
        responseStructure.setData(existingUser);
        return new ResponseEntity<>(responseStructure, HttpStatus.OK);
    }
}
