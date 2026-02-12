package org.hadiroyan.retailhub.service;

import org.hadiroyan.retailhub.dto.request.LoginRequest;
import org.hadiroyan.retailhub.dto.request.RegisterCustomerRequest;
import org.hadiroyan.retailhub.dto.request.RegisterOwnerRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.AuthResponse;
import org.hadiroyan.retailhub.dto.response.UserResponse;
import org.hadiroyan.retailhub.exception.AccountDisabledException;
import org.hadiroyan.retailhub.exception.EmailAlreadyExistsException;
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
        LOG.infof("Login attempt for email: %s", email);

        User user = userRepository.findByEmailWithRolesAndPrivileges(email)
                .orElseThrow(() -> {
                    LOG.warnf("Login failed: user not found for email: %s", email);
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!user.enabled) {
            LOG.warnf("Login attempt for disabled account: %s", email);
            throw new AccountDisabledException();
        }

        if (!passwordService.verify(request.password, user.password)) {
            LOG.warnf("Login failed: incorrect password for email: %s", email);
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenService.generateToken(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.token = token;
        authResponse.user = UserResponse.fromUser(user);

        LOG.infof("Successful login for email: %s", email);
        return ApiResponse.success("Login successful", authResponse);
    }

    @Transactional
    public ApiResponse<UserResponse> registerOwner(RegisterOwnerRequest request) {
        LOG.infof("Register owner request by %s", request.email);
        return registerUser(
                request.email,
                request.password,
                request.fullName,
                "OWNER");
    }

    @Transactional
    public ApiResponse<UserResponse> registerCustomer(RegisterCustomerRequest request) {
        LOG.infof("Register customer request by %s", request.email);
        return registerUser(
                request.email,
                request.password,
                request.fullName,
                "CUSTOMER");
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
            LOG.warnf("Registration attempt with existing email: %s", email);
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(
                email,
                passwordService.hash(password),
                fullname);
        userRepository.persist(user);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        UserRole userRole = new UserRole(user, role);
        user.userRoles.add(userRole);
        userRoleRepository.persist(userRole);

        LOG.infof("Successfully registered %s: %s", roleName.toLowerCase(), email);
        return ApiResponse.created(
                "Account created successfully",
                UserResponse.fromUser(user));
    }
}
