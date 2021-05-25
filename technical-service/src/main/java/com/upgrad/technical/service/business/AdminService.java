package com.upgrad.technical.service.business;


import com.upgrad.technical.service.dao.ImageDao;
import com.upgrad.technical.service.dao.UserDao;
import com.upgrad.technical.service.entity.ImageEntity;
import com.upgrad.technical.service.entity.UserAuthTokenEntity;
import com.upgrad.technical.service.exception.ImageNotFoundException;
import com.upgrad.technical.service.exception.UnauthorizedException;
import com.upgrad.technical.service.exception.UserNotSignedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Service
public class AdminService {

    @Autowired
    private ImageDao imageDao;

    public ImageEntity getImage(final String imageUuid, final String authorization) throws ImageNotFoundException, UnauthorizedException, UserNotSignedInException {
        //fetching user with given access token
        UserAuthTokenEntity userAuthTokenEntity = imageDao.getUserAuthToken(authorization);
        //handling user not signed exception
        if(userAuthTokenEntity==null){
            throw new UserNotSignedInException("USR-001","You are not Signed in, sign in first to get the details of the image");
        }
        //fetching user role
        String role = userAuthTokenEntity.getUser().getRole();
        //handling user not admin exception
        if(!role.equals("admin")){
            throw new UnauthorizedException("ATH-001","UNAUTHORIZED Access, Entered user is not an admin");
        }
        //fetching image details from database
        ImageEntity requiredImage = imageDao.getImage(imageUuid);
        //handling image not found exception
        if(requiredImage==null){
            throw new ImageNotFoundException("IMG-001","Image with Uuid not found");
        }
        return requiredImage; //sending image to controller
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ImageEntity updateImage(final ImageEntity imageEntity, final String authorization) throws ImageNotFoundException, UnauthorizedException, UserNotSignedInException {
        UserAuthTokenEntity userAuthTokenEntity = imageDao.getUserAuthToken(authorization);

        if(userAuthTokenEntity==null){
            throw new UserNotSignedInException("USR-001","You are not Signed in, sign in first to get the details of the image");
        }

        String role = userAuthTokenEntity.getUser().getRole();

        if(!role.equals("admin")){
            throw new UnauthorizedException("ATH-001","UNAUTHORIZED Access, Entered user is not an admin");
        }

        ImageEntity imageToBeUpdated = imageDao.getImageById(imageEntity.getId());
        if(imageToBeUpdated==null){
            throw new ImageNotFoundException("IMG-002","Image with Id not found");
        }
        imageToBeUpdated.setImage(imageEntity.getImage());
        imageToBeUpdated.setName(imageEntity.getName());
        imageToBeUpdated.setDescription(imageEntity.getDescription());
        imageToBeUpdated.setStatus(imageEntity.getStatus());
        imageDao.updateImage(imageToBeUpdated); //updating image
        return  imageToBeUpdated;
    }
}
