package com.upgrad.technical.service.business;


import com.upgrad.technical.service.dao.UserDao;
import com.upgrad.technical.service.entity.UserAuthTokenEntity;
import com.upgrad.technical.service.entity.UserEntity;
import com.upgrad.technical.service.exception.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider CryptographyProvider;
    //implementing business logic for authentication
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticate(final String username, final String password) throws AuthenticationFailedException {
        //fetching user with given uuid
        UserEntity userEntity = userDao.getUserByEmail(username);
        //throwing user not found error message
        if(userEntity==null)
            throw new AuthenticationFailedException("ATH-001","User with email not found");
        //encrypting password , which was got from authorization header
        final String encryptedPassword = CryptographyProvider.encrypt(password, userEntity.getSalt());
        if(encryptedPassword.equals(userEntity.getPassword())){
            JwtTokenProvider jwtTokenProvider=new JwtTokenProvider(encryptedPassword);
            UserAuthTokenEntity userAuthToken=new UserAuthTokenEntity();
            userAuthToken.setUser(userEntity);
            final ZonedDateTime now=ZonedDateTime.now();
            final ZonedDateTime expiresAt=now.plusHours(8);
            //generating access token
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(now);
            userAuthToken.setExpiresAt(expiresAt);
            userDao.createAuthToken(userAuthToken); //updating user auth tokens table
            userEntity.setLastLoginAt(now);
            userDao.updateUser(userEntity); //updating user's last login in user table
            return userAuthToken; //returning authenticated user auth token
        }else {
            //throwing password failed error message
            throw new AuthenticationFailedException("ATH-002","Password failed");
        }
    }
}



