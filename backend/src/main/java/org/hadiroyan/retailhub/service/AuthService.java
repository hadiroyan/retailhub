package org.hadiroyan.retailhub.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hadiroyan.retailhub.dto.request.ChangePasswordRequest;
import org.hadiroyan.retailhub.dto.request.LoginRequest;
import org.hadiroyan.retailhub.dto.request.RegisterCustomerRequest;
import org.hadiroyan.retailhub.dto.request.RegisterOwnerRequest;
import org.hadiroyan.retailhub.dto.request.UpdateProfileRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.AuthResponse;
import org.hadiroyan.retailhub.dto.response.UserResponse;
import org.hadiroyan.retailhub.exception.AccountDisabledException;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.EmailAlreadyExistsException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.exception.RoleNotFoundException;
import org.hadiroyan.retailhub.exception.UnauthorizedException;
import org.hadiroyan.retailhub.model.EmailVerificationToken;
import org.hadiroyan.retailhub.model.Role;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.model.UserRole;
import org.hadiroyan.retailhub.repository.EmailVerificationTokenRepository;
import org.hadiroyan.retailhub.repository.RoleRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.hadiroyan.retailhub.utils.ValidationUtils;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    StoreRepository storeRepository;

    @Inject
    EmailVerificationTokenRepository tokenRepository;

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "app.otp.expiry-minutes", defaultValue = "10")
    int otpExpiryMinutes;

    @ConfigProperty(name = "app.otp.max-resend-per-hour", defaultValue = "5")
    int maxResendPerHour;

    private static Logger LOG = Logger.getLogger(AuthService.class);

    public ApiResponse<AuthResponse> login(LoginRequest request) {
        String email = ValidationUtils.normalizeEmail(request.email);
        LOG.debugf("action=LOGIN_START email=%s", email);

        User user = userRepository.findByEmailWithRolesAndPrivileges(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=LOGIN_FAILED_USER_NOT_FOUND email=%s", email);
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!user.enabled) {
            LOG.warnf("action=LOGIN_FAILED_ACCOUNT_DISABLED email=%s userId=%s",
                    email, user.id);
            throw new AccountDisabledException();
        }

        if (!passwordService.verify(request.password, user.password)) {
            LOG.warnf("action=LOGIN_FAILED_INVALID_PASSWORD email=%s userId=%s",
                    email, user.id);
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenService.generateToken(user, fetchStoreDataForUser(user));

        AuthResponse authResponse = new AuthResponse();
        authResponse.token = token;
        authResponse.user = buildUserResponse(user);

        LOG.infof("action=LOGIN_SUCCESS userId=%s email=%s",
                user.id, email);

        return ApiResponse.success("Login successful", authResponse);
    }

    @Transactional
    public ApiResponse<UserResponse> registerOwner(RegisterOwnerRequest request) {
        String email = ValidationUtils.normalizeEmail(request.email);
        LOG.debugf("action=REGISTER_OWNER_START email=%s", email);

        User user = registerUser(request.email, request.password, request.fullName, "OWNER");
        generateAndSendOtp(user);

        LOG.infof("action=REGISTER_OWNER_SUCCESS email=%s", email);
        return ApiResponse.created("Account created successfully", UserResponse.fromUser(user));
    }

    @Transactional
    public ApiResponse<UserResponse> registerCustomer(RegisterCustomerRequest request) {
        String email = ValidationUtils.normalizeEmail(request.email);
        LOG.debugf("action=REGISTER_CUSTOMER_START email=%s", email);

        User user = registerUser(request.email, request.password, request.fullName, "CUSTOMER");
        generateAndSendOtp(user);

        LOG.infof("action=REGISTER_CUSTOMER_SUCCESS email=%s", email);
        return ApiResponse.created("Account created successfully", UserResponse.fromUser(user));
    }

    private User registerUser(
            String email,
            String password,
            String fullname,
            String roleName) {

        email = ValidationUtils.normalizeAndValidateEmail(email);
        ValidationUtils.validatePassword(password);
        fullname = ValidationUtils.normalizeFullName(fullname);
        ValidationUtils.validateFullName(fullname);

        if (userRepository.existsByEmail(email)) {
            LOG.warnf("action=REGISTER_FAILED_EMAIL_EXISTS email=%s role=%s",
                    email, roleName);
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(
                email,
                passwordService.hash(password),
                fullname);
        userRepository.persist(user);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    LOG.errorf("action=ROLE_NOT_FOUND role=%s", roleName);
                    return new RoleNotFoundException(roleName);
                });

        UserRole userRole = new UserRole(user, role);
        user.userRoles.add(userRole);
        userRoleRepository.persist(userRole);

        LOG.infof("action=REGISTER_SUCCESS userId=%s email=%s role=%s",
                user.id, email, roleName);

        return user; 
    }

    public UserResponse getCurrentUser(String email) {
        LOG.debugf("action=GET_CURRENT_USER_START email=%s", email);

        User user = userRepository.findByEmailWithRolesAndPrivileges(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });

        LOG.infof("action=GET_CURRENT_USER_SUCCESS userId=%s email=%s",
                user.id, email);

        return buildUserResponse(user);
    }

    public String generateTokenForUser(User user) {
        return jwtTokenService.generateToken(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        LOG.debugf("action=UPDATE_PROFILE_START email=%s", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });

        user.fullName = request.fullName;
        user.phone = request.phone;
        user.address = request.address;

        LOG.infof("action=UPDATE_PROFILE_SUCCESS userId=%s email=%s",
                user.id, email);

        return buildUserResponse(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        LOG.debugf("action=CHANGE_PASSWORD_START email=%s", email);

        // Validasi newPassword dan confirmPassword match
        if (!request.newPassword.equals(request.confirmPassword)) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Validasi newPassword tidak sama dengan currentPassword
        if (request.currentPassword.equals(request.newPassword)) {
            throw new BadRequestException("New password must be different from current password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });

        // Verify current password
        if (!passwordService.verify(request.currentPassword, user.password)) {
            LOG.warnf("action=CHANGE_PASSWORD_FAILED_WRONG_PASSWORD email=%s", email);
            throw new BadRequestException("Current password is incorrect");
        }

        user.password = passwordService.hash(request.newPassword);

        LOG.infof("action=CHANGE_PASSWORD_SUCCESS userId=%s email=%s",
                user.id, email);
    }

    // Fetch store data for user to include in JWT token.
    private Object fetchStoreDataForUser(User user) {
        Set<String> roles = getRoles(user);

        if (roles.contains("OWNER")) {
            List<Store> stores = storeRepository.findAllByOwnerId(user.id);
            return stores.stream()
                    .map(s -> Map.of(
                            "id", s.id.toString(),
                            "name", s.name,
                            "slug", s.slug))
                    .toList();
        }

        if (roles.contains("ADMIN") || roles.contains("MANAGER") || roles.contains("STAFF")) {
            Store store = getAssignedStore(user);
            return Map.of(
                    "id", store.id.toString(),
                    "name", store.name,
                    "slug", store.slug);
        }

        return null;
    }

    private UserResponse buildUserResponse(User user) {
        UserResponse response = UserResponse.fromUser(user);

        Set<String> roles = getRoles(user);

        if (roles.contains("OWNER")) {
            List<Store> ownerStores = storeRepository.findAllByOwnerId(user.id);
            response.stores = ownerStores.stream()
                    .map(s -> new UserResponse.StoreInfo(s.id.toString(), s.name, s.slug))
                    .toList();
        } else if (roles.contains("ADMIN") || roles.contains("MANAGER") || roles.contains("STAFF")) {
            Store store = getAssignedStore(user);
            if (store != null) {
                response.assignedStore = new UserResponse.StoreInfo(
                        store.id.toString(),
                        store.name,
                        store.slug);
            }
        }

        return response;
    }

    private Set<String> getRoles(User user) {
        return user.userRoles.stream()
                .map(ur -> ur.role.name)
                .collect(Collectors.toSet());
    }

    private Store getAssignedStore(User user) {
        return user.userRoles.stream()
                .filter(ur -> ur.storeId != null)
                .findFirst()
                .map(ur -> storeRepository.findById(ur.storeId))
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_ROLE_NOT_FOUND email=%s", user.email);
                    return new NotFoundException("User not found");
                });
    }

    private String generateOtp() {
        int otp = new java.util.Random().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    @Transactional
    public void generateAndSendOtp(User user) {
        String otp = generateOtp();

        EmailVerificationToken token = new EmailVerificationToken();
        token.user = user;
        token.otp = otp;
        token.expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        token.persist();

        emailService.sendVerificationEmail(user.email, user.fullName, otp);

        LOG.infof("action=OTP_GENERATED email=%s", user.email);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });

        if (user.emailVerified) {
            LOG.warnf("action=VERIFY_OTP_ALREADY_VERIFIED email=%s", email);
            throw new BadRequestException("Email already verified");
        }

        EmailVerificationToken token = tokenRepository
                .findLatestActiveByUserId(user.id)
                .orElseThrow(() -> {
                    LOG.warnf("action=VERIFY_OTP_NO_ACTIVE_TOKEN email=%s", email);
                    return new BadRequestException("OTP expired or not found, please request a new one");
                });

        if (!token.otp.equals(otp)) {
            LOG.warnf("action=VERIFY_OTP_INVALID email=%s", email);
            throw new BadRequestException("Invalid OTP");
        }

        token.used = true;
        user.emailVerified = true;

        LOG.infof("action=VERIFY_OTP_SUCCESS email=%s", email);
    }

    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });

        if (user.emailVerified) {
            LOG.warnf("action=RESEND_OTP_ALREADY_VERIFIED email=%s", email);
            throw new BadRequestException("Email already verified");
        }

        long recentCount = tokenRepository.countRecentByUserId(user.id, LocalDateTime.now().minusHours(1));

        if (recentCount >= maxResendPerHour) {
            LOG.warnf("action=RESEND_OTP_RATE_LIMITED email=%s count=%d", email, recentCount);
            throw new BadRequestException("Too many OTP requests, please try again in an hour");
        }

        tokenRepository.invalidateAllByUserId(user.id);
        generateAndSendOtp(user);

        LOG.infof("action=RESEND_OTP_SUCCESS email=%s", email);
    }
}
