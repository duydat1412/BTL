package com.auction.server.service;

import java.io.Serializable;
import java.util.regex.*;

import com.auction.common.entity.User;
import com.auction.common.entity.Admin;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.Seller;
import com.auction.common.enums.UserRole;
import com.auction.common.message.*;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.repository.SerializableUserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UserService{
    static SerializableUserRepository sur = new SerializableUserRepository();
    // Dang ki tai khoan moi,
    public static synchronized ClientResponse signup(RegisterRequest registerRequest, String department)throws AuthenticationException {
        UserRole userRole=registerRequest.getRole();
        String username=registerRequest.getUsername();
        String password=registerRequest.getPassword();
        String email=registerRequest.getEmail();
        String mr="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";  ///Mail pattern
        Pattern mp=Pattern.compile(mr, Pattern.CASE_INSENSITIVE);
        Matcher m= mp.matcher(email);
        String pr="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";    ///Password pattern
        Pattern p=Pattern.compile(pr, Pattern.CASE_INSENSITIVE);
        Matcher pm=p.matcher(password);
        User newUser=null;
        if(userRole==null||username==null||email==null||password==null){
            throw new AuthenticationException("parameters cannot be null");
        } else if(sur.findByUsername(username)!=null){
            throw new AuthenticationException("Username is taken.");
        } else if(!m.matches()){
            throw new AuthenticationException("Invalid email format.");
        } else if(!pm.matches()){
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
        String username=loginRequest.getUsername();
        String password=loginRequest.getPassword();
        User log=sur.findByUsername(username);
        if(log==null){
            throw new AuthenticationException("Username not found.");
        } else if(BCrypt.checkpw(password, log.getPassword())==false){
            throw new AuthenticationException("Invalid password");
        } else if(log.isBanned()){
            return new ClientResponse(false, "You has been banned for: "+log.getBanReason(), null);
        }
        return new ClientResponse(true, "Login successfully", toAuthUserData(log));
    }

    public static ClientResponse getAllUsers(GetAllUsersRequest gaur) throws AuthenticationException{
        try{
            User admin = requireAdminById(gaur.getAdminId());
            if (admin == null) {
                throw new AuthenticationException("Admin not found.");
            }
            return new ClientResponse(true, "Successfully retrieved all users.", (Serializable) sur.findAll());
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e){
            return new ClientResponse(false, e.getMessage(), null);
        }
    }

    public static ClientResponse banUser(BanUserRequest bur) throws AuthenticationException{
        try{
            requireAdminById(bur.getAdminId());
            User target = sur.findById(bur.getTargetUserId());
            if (target == null) {
                return new ClientResponse(false, "Target user not found.", null);
            }
            if (target.getRole() == UserRole.ADMIN) {
                throw new AuthenticationException("Cannot ban an admin.");
            }
            if (target.isBanned()){
                return new ClientResponse(false, "Target user was already banned.", null);
            }
            target.setBanned(true);
            target.setBanReason(bur.getReason());
            sur.update(target);
            return new ClientResponse(true, "Successfully banned user.", null);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e){
            return new ClientResponse(false, e.getMessage(), null);
        }
    }

    public static ClientResponse unbanUser(UnbanUserRequest ubur) throws AuthenticationException{
        try{
            requireAdminById(ubur.getAdminId());
            User target = sur.findById(ubur.getTargetUserId());
            if (target == null) {
                return new ClientResponse(false, "Target user not found.", null);
            }
            if(!target.isBanned()){
                return new ClientResponse(false, "Target user is not banned.", null);
            }
            target.setBanned(false);
            target.setBanReason(null);
            sur.update(target);
            return new ClientResponse(true, "Unbanned target user.", null);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e){
            return new ClientResponse(false, e.getMessage(), null);
        }

    }

    private static User requireAdminById(String adminId) throws AuthenticationException {
        User admin = sur.findById(adminId);
        if (admin == null) {
            throw new AuthenticationException("Admin not found.");
        }
        if (admin.getRole() != UserRole.ADMIN) {
            throw new AuthenticationException("ADMIN PERMISSION REQUIRED.");
        }
        return admin;
    }

    private static AuthUserData toAuthUserData(User user) {
        return new AuthUserData(user.getId(), user.getUsername(), user.getRole());
    }
}
