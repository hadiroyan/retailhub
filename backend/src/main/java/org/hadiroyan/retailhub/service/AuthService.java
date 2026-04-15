package org.hadiroyan.retailhub.service;

import org.hadiroyan.retailhub.dto.request.LoginRequest;
import org.hadiroyan.retailhub.dto.request.RegisterCustomerRequest;
import org.hadiroyan.retailhub.dto.request.RegisterOwnerRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.AuthResponse;
import org.hadiroyan.retailhub.dto.response.UserResponse;
import org.hadiroyan.retailhub.exception.AccountDisabledException;
import org.hadiroyan.retailhub.exception.EmailAlreadyExistsException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.exception.RoleNotFoundException;
import org.hadiroyan.retailhub.exception.UnauthorizedException;
import org.hadiroyan.retailhub.model.Role;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.model.UserRole;
import org.hadiroyan.retailhub.repository.RoleRepository;
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

        String token = jwtTokenService.generateToken(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.token = token;
        authResponse.user = UserResponse.fromUser(user);

        LOG.infof("action=LOGIN_SUCCESS userId=%s email=%s",
                user.id, email);

        return ApiResponse.success("Login successful", authResponse);
    }

    @Transactional
    public ApiResponse<UserResponse> registerOwner(RegisterOwnerRequest request) {
        String email = ValidationUtils.normalizeEmail(request.email);
        LOG.debugf("action=REGISTER_OWNER_START email=%s", email);

        ApiResponse<UserResponse> response = registerUser(
                request.email,
                request.password,
                request.fullName,
                "OWNER");

        LOG.infof("action=REGISTER_OWNER_SUCCESS email=%s", email);
        return response;
    }

    @Transactional
    public ApiResponse<UserResponse> registerCustomer(RegisterCustomerRequest request) {
        String email = ValidationUtils.normalizeEmail(request.email);
        LOG.debugf("action=REGISTER_CUSTOMER_START email=%s", email);

        ApiResponse<UserResponse> response = registerUser(
                request.email,
                request.password,
                request.fullName,
                "CUSTOMER");

        LOG.infof("action=REGISTER_CUSTOMER_SUCCESS email=%s", email);
        return response;
    }

    private ApiResponse<UserResponse> registerUser(
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

        return ApiResponse.created(
                "Account created successfully",
                UserResponse.fromUser(user));
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

        return UserResponse.fromUser(user);
    }

    public String generateTokenForUser(User user) {
        return jwtTokenService.generateToken(user);
    }

    // ============================================
    // Future: Fetch Store Data Helper (TODO: Implement when Store entity is ready)
    // ============================================

    /**
     * Fetch store data for user to include in JWT token.
     * 
     * private Object fetchStoreDataForUser(User user) {
     * Set<String> roles = user.userRoles.stream()
     * .map(ur -> ur.role.name)
     * .collect(Collectors.toSet());
     * 
     * if (roles.contains("OWNER")) {
     * List<Store> stores = storeRepository.findByOwnerId(user.id);
     * return stores.stream()
     * .map(s -> Map.of(
     * "id", s.id.toString(),
     * "name", s.name,
     * "slug", s.slug))
     * .toList();
     * }
     * 
     * if (roles.contains("ADMIN") || roles.contains("MANAGER") ||
     * roles.contains("STAFF")) {
     * Optional<UserRole> userRole = user.userRoles.stream()
     * .filter(ur -> ur.storeId != null)
     * .findFirst();
     * 
     * if (userRole.isPresent()) {
     * Store store = storeRepository.findById(userRole.get().storeId).orElseThrow();
     * return Map.of(
     * "id", store.id.toString(),
     * "name", store.name,
     * "slug", store.slug);
     * }
     * }
     * 
     * return null;
     * }
     */
}
