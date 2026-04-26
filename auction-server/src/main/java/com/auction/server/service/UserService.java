package com.auction.server.service;

import java.util.regex.*;

import com.auction.common.entity.User;
import com.auction.common.entity.Admin;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.Seller;
import com.auction.common.enums.UserRole;
import com.auction.common.message.AuthUserData;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.LoginRequest;
import com.auction.common.message.RegisterRequest;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.repository.SerializableUserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UserService{

    // Dang ki tai khoan moi,
    public static ClientResponse signup(RegisterRequest registerRequest, String department)throws AuthenticationException {
        SerializableUserRepository sur = new SerializableUserRepository();
        UserRole userRole=registerRequest.getRole();
        String username=registerRequest.getUsername();
        String password=registerRequest.getPassword();
        String email=registerRequest.getEmail();
        String mr="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";  ///Mail pattern
        Pattern mp=Pattern.compile(mr, Pattern.CASE_INSENSITIVE);
        Matcher m= mp.matcher(email);
        String pr="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\\\S+$).{8,}$";    ///Password pattern
        Pattern p=Pattern.compile(pr, Pattern.CASE_INSENSITIVE);
        Matcher pm=p.matcher(password);
        User newUser=null;
        if(userRole==null||username==null||email==null||password==null){
            throw new AuthenticationException("parameters cannot be null");
        } else if(sur.findByUsername(username)!=null){
            throw new AuthenticationException("Username is taken.");
        } else if(m.matches()==false){
            throw new AuthenticationException("Invalid email format.");
        } else if(pm.matches()==false){
            throw new AuthenticationException("Password must be at least 8 character and contain at least 1 upper case, 1 lower case, 1 number and 1 special character.");
        } else{
            String hashedPassword=BCrypt.hashpw(password, BCrypt.gensalt());
            switch (userRole){
                case BIDDER:
                    newUser=new Bidder(username, hashedPassword, email);
                    break;
                case SELLER:
                    newUser=new Seller(username, hashedPassword, email);
                    break;
                case ADMIN:
                    newUser=new Admin(username, hashedPassword, email, department);
                    break;
                default:
                    throw new AuthenticationException("Invalid role.");
            }
            sur.save(newUser);
        }
        return new ClientResponse(true, "Dang ki thanh cong", toAuthUserData(newUser));
    }
    public static ClientResponse login(LoginRequest loginRequest)throws AuthenticationException{
        SerializableUserRepository sur=new SerializableUserRepository();
        String username=loginRequest.getUsername();
        String password=loginRequest.getPassword();
        User log=sur.findByUsername(username);
        if(log==null){
            throw new AuthenticationException("Username not found.");
        } else if(BCrypt.checkpw(password, log.getPassword())==false){
            throw new AuthenticationException("Invalid password");
        }
        return new ClientResponse(true, "Login successfully", toAuthUserData(log));
    }

    private static AuthUserData toAuthUserData(User user) {
        return new AuthUserData(user.getId(), user.getUsername(), user.getRole());
    }
}
