package com.auction.server.service;

import java.util.regex.*;

import com.auction.common.entity.User;
import com.auction.common.entity.Admin;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.Seller;
import com.auction.common.enums.UserRole;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.RegisterRequest;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.repository.SerializableUserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UserService{

    // Dang ki tai khoan moi,
    public static void signup(RegisterRequest registerRequest, String department)throws AuthenticationException {
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
                    sur.save(new Bidder(username, hashedPassword, email));
                    break;
                case SELLER:
                    sur.save(new Seller(username, hashedPassword, email));
                    break;
                case ADMIN:
                    sur.save(new Admin(username, hashedPassword, email, department));
                    break;
                default:
                    throw new AuthenticationException("Invalid role.");
            }
        }
    }
    public static ClientResponse login(String username, String password)throws AuthenticationException{
        SerializableUserRepository sur=new SerializableUserRepository();
        User log=sur.findByUsername(username);
        if(log==null){
            throw new AuthenticationException("Username not found.");
        } else if(BCrypt.checkpw(password, log.getPassword())==false){
            throw new AuthenticationException("Invalid password");
        }
        return new ClientResponse(true,"Login successfully", log);
    }
}
