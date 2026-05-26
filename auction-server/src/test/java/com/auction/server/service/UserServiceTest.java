package com.auction.server.service;

import com.auction.common.entity.Admin;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.Seller;
import com.auction.common.entity.User;
import com.auction.common.enums.UserRole;
import com.auction.common.message.*;
import com.auction.server.datastore.DataStore;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.repository.SerializableUserRepository;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private SerializableUserRepository userRepo;

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getUsers().clear();
        userRepo = new SerializableUserRepository();
    }

    @AfterEach
    void tearDown() {
        DataStore.getInstance().getUsers().clear();
    }

    @Nested
    @DisplayName("signup tests")
    class SignupTests {

        @Test
        @DisplayName("Register BIDDER success")
        void signup_bidder_success() throws AuthenticationException {
            RegisterRequest req = new RegisterRequest("bidder1", "Pass@123", "bidder@test.com", UserRole.BIDDER);
            ClientResponse res = UserService.signup(req, "");
            assertTrue(res.isSuccess());
            User saved = userRepo.findByUsername("bidder1");
            assertNotNull(saved);
            assertInstanceOf(Bidder.class, saved);
        }

        @Test
        @DisplayName("Register SELLER success")
        void signup_seller_success() throws AuthenticationException {
            RegisterRequest req = new RegisterRequest("seller1", "Pass@123", "seller@test.com", UserRole.SELLER);
            ClientResponse res = UserService.signup(req, "");
            assertTrue(res.isSuccess());
            User saved = userRepo.findByUsername("seller1");
            assertNotNull(saved);
            assertInstanceOf(Seller.class, saved);
        }

        @Test
        @DisplayName("Register ADMIN rejected")
        void signup_admin_rejected() {
            RegisterRequest req = new RegisterRequest("admin1", "Pass@123", "admin@test.com", UserRole.ADMIN);
            assertThrows(AuthenticationException.class, () -> UserService.signup(req, ""));
        }

        @Test
        @DisplayName("Register duplicate username fails")
        void signup_duplicateUsername_fails() {
            RegisterRequest req1 = new RegisterRequest("dupuser", "Pass@123", "dup1@test.com", UserRole.BIDDER);
            assertDoesNotThrow(() -> UserService.signup(req1, ""));
            RegisterRequest req2 = new RegisterRequest("dupuser", "Other@123", "dup2@test.com", UserRole.SELLER);
            assertThrows(AuthenticationException.class, () -> UserService.signup(req2, ""));
        }

        @Test
        @DisplayName("Register invalid email fails")
        void signup_invalidEmail_fails() {
            RegisterRequest req = new RegisterRequest("user", "Pass@123", "not-an-email", UserRole.BIDDER);
            assertThrows(AuthenticationException.class, () -> UserService.signup(req, ""));
        }

        @Test
        @DisplayName("Register weak password fails")
        void signup_weakPassword_fails() {
            RegisterRequest req = new RegisterRequest("user", "short", "user@test.com", UserRole.BIDDER);
            assertThrows(AuthenticationException.class, () -> UserService.signup(req, ""));
        }

        @Test
        @DisplayName("Register null fields fail")
        void signup_nullFields_fails() {
            RegisterRequest req = new RegisterRequest(null, null, null, null);
            assertThrows(AuthenticationException.class, () -> UserService.signup(req, ""));
        }
    }

    @Nested
    @DisplayName("login tests")
    class LoginTests {

        @BeforeEach
        void createTestUser() {
            RegisterRequest req = new RegisterRequest("logintest", "Pass@123", "login@test.com", UserRole.BIDDER);
            assertDoesNotThrow(() -> UserService.signup(req, ""));
        }

        @Test
        @DisplayName("Login success with correct credentials")
        void login_success() throws AuthenticationException {
            LoginRequest req = new LoginRequest("logintest", "Pass@123");
            ClientResponse res = UserService.login(req);
            assertTrue(res.isSuccess());
            assertNotNull(res.getData());
            assertInstanceOf(AuthUserData.class, res.getData());
            AuthUserData data = (AuthUserData) res.getData();
            assertEquals("logintest", data.getUsername());
            assertEquals(UserRole.BIDDER, data.getRole());
        }

        @Test
        @DisplayName("Login with wrong password fails")
        void login_wrongPassword_fails() {
            LoginRequest req = new LoginRequest("logintest", "WrongPass@123");
            assertThrows(AuthenticationException.class, () -> UserService.login(req));
        }

        @Test
        @DisplayName("Login with non-existent username fails")
        void login_unknownUser_fails() {
            LoginRequest req = new LoginRequest("nobody", "Pass@123");
            assertThrows(AuthenticationException.class, () -> UserService.login(req));
        }

        @Test
        @DisplayName("Login banned user returns failure")
        void login_bannedUser_fails() throws AuthenticationException {
            User user = userRepo.findByUsername("logintest");
            user.setBanned(true);
            user.setBanReason("Test ban");
            userRepo.update(user);
            LoginRequest req = new LoginRequest("logintest", "Pass@123");
            ClientResponse res = UserService.login(req);
            assertFalse(res.isSuccess());
            assertTrue(res.getMessage().contains("banned"));
        }
    }

    @Nested
    @DisplayName("admin action tests")
    class AdminActionTests {

        private String adminId;
        private String bidderId;
        private String sellerId;

        @BeforeEach
        void setupUsers() throws AuthenticationException {
            UserService.signup(new RegisterRequest("adminuser", "Admin@123", "admin@test.com", UserRole.ADMIN), "IT");
            UserService.signup(new RegisterRequest("testbidder", "Pass@123", "bidder@test.com", UserRole.BIDDER), "");
            UserService.signup(new RegisterRequest("testseller", "Pass@123", "seller@test.com", UserRole.SELLER), "");
            adminId = userRepo.findByUsername("adminuser").getId();
            bidderId = userRepo.findByUsername("testbidder").getId();
            sellerId = userRepo.findByUsername("testseller").getId();
        }

        @Test
        @DisplayName("getAllUsers returns all users for admin")
        void getAllUsers_admin_success() throws AuthenticationException {
            GetAllUsersRequest req = new GetAllUsersRequest(adminId);
            ClientResponse res = UserService.getAllUsers(req);
            assertTrue(res.isSuccess());
        }

        @Test
        @DisplayName("getAllUsers rejected for non-admin")
        void getAllUsers_nonAdmin_fails() {
            GetAllUsersRequest req = new GetAllUsersRequest(bidderId);
            assertThrows(AuthenticationException.class, () -> UserService.getAllUsers(req));
        }

        @Test
        @DisplayName("banUser by admin success")
        void banUser_success() throws AuthenticationException {
            BanUserRequest req = new BanUserRequest(adminId, bidderId, "Spam");
            ClientResponse res = UserService.banUser(req);
            assertTrue(res.isSuccess());
            User banned = userRepo.findById(bidderId);
            assertTrue(banned.isBanned());
            assertEquals("Spam", banned.getBanReason());
        }

        @Test
        @DisplayName("banUser non-admin fails")
        void banUser_nonAdmin_fails() {
            BanUserRequest req = new BanUserRequest(bidderId, sellerId, "test");
            assertThrows(AuthenticationException.class, () -> UserService.banUser(req));
        }

        @Test
        @DisplayName("Cannot ban another admin")
        void banUser_adminTarget_fails() {
            String admin2Id = adminId;
            BanUserRequest req = new BanUserRequest(adminId, admin2Id, "test");
            assertThrows(AuthenticationException.class, () -> UserService.banUser(req));
        }

        @Test
        @DisplayName("unbanUser by admin success")
        void unbanUser_success() throws AuthenticationException {
            UserService.banUser(new BanUserRequest(adminId, bidderId, "Spam"));
            UnbanUserRequest req = new UnbanUserRequest(adminId, bidderId, "Appeal granted");
            ClientResponse res = UserService.unbanUser(req);
            assertTrue(res.isSuccess());
            User unbanned = userRepo.findById(bidderId);
            assertFalse(unbanned.isBanned());
        }

        @Test
        @DisplayName("unbanUser non-admin fails")
        void unbanUser_nonAdmin_fails() {
            UnbanUserRequest req = new UnbanUserRequest(bidderId, sellerId, "test");
            assertThrows(AuthenticationException.class, () -> UserService.unbanUser(req));
        }
    }
}
