package org.hadiroyan.retailhub.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateEmployeeRequest;
import org.hadiroyan.retailhub.dto.request.UpdateEmployeeRoleRequest;
import org.hadiroyan.retailhub.dto.response.EmployeeResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.EmailAlreadyExistsException;
import org.hadiroyan.retailhub.exception.ForbiddenException;
import org.hadiroyan.retailhub.model.Role;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.model.UserRole;
import org.hadiroyan.retailhub.repository.RoleRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.hadiroyan.retailhub.utils.ValidationUtils;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.exception.RoleNotFoundException;
import org.hadiroyan.retailhub.mapper.EmployeeMapper;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EmployeeService {

    private final Logger LOG = Logger.getLogger(EmployeeService.class);

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordService passwordService;

    @Inject
    private StoreRepository storeRepository;

    @Inject
    private EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeResponse createEmployee(
            CreateEmployeeRequest request,
            UUID userId,
            UUID storeId,
            Set<String> roles) {

        LOG.debugf("action=CREATE_EMPLOYEE_START userId=%s storeId=%s role=%s",
                userId,
                storeId,
                request.role);

        String email = ValidationUtils.normalizeAndValidateEmail(request.email);
        String fullName = ValidationUtils.normalizeFullName(request.fullName);
        ValidationUtils.validatePassword(request.password);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);
        validateRoleAssignment(userId, storeId, request.role);

        if (userRepository.existsByEmail(email)) {
            LOG.warnf("action=CREATE_EMPLOYEE_EMAIL_EXISTS userId=%s storeId=%s email=%s role=%s",
                    userId,
                    storeId,
                    email,
                    request.role);
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(email, passwordService.hash(request.password), fullName);
        userRepository.persist(user);

        Role role = roleRepository.findByName(request.role).orElseThrow(() -> {
            LOG.warnf("action=ROLE_NOT_FOUND userId=%s role=%s", userId, request.role);
            return new RoleNotFoundException("Role not found role=" + request.role);
        });

        UserRole userRole = new UserRole(user, role, storeId);
        user.userRoles.add(userRole);
        userRoleRepository.persist(userRole);

        LOG.infof("action=CREATE_EMPLOYEE_SUCCESS userId=%s storeId=%s targetUserId=%s role=%s",
                userId,
                storeId,
                user.id,
                role.name);

        return employeeMapper.toResponse(user);
    }

    public PagedResponse<EmployeeResponse> listEmployees(UUID userId, UUID storeId, int page, int size) {
        LOG.debugf("action=LIST_EMPLOYEES_START userId=%s storeId=%s page=%d size=%d",
                userId,
                storeId,
                page,
                size);
        findStoreOrThrow(storeId);

        if (!userRoleRepository.userHasAnyRoleInStore(userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId)) {
            LOG.warnf("action=LIST_EMPLOYEES_ACCESS_DENIED userId=%s storeId=%s",
                    userId, storeId);
            throw new ForbiddenException("No permission");
        }

        List<User> listEmployees = userRepository.findEmployeesByStore(storeId, page, size);
        List<EmployeeResponse> content = listEmployees.stream()
                .map(employeeMapper::toResponse)
                .toList();
        long total = userRepository.countEmployeesByStore(storeId);

        LOG.infof("action=LIST_EMPLOYEES_SUCCESS userId=%s storeId=%s total=%d page=%d size=%d",
                userId,
                storeId,
                total,
                page,
                size);

        return new PagedResponse<>(content, page, size, total);
    }

    @Transactional
    public EmployeeResponse updateEmployeeRole(
            UUID requesterId,
            UUID storeId,
            UUID targetUserId,
            UpdateEmployeeRoleRequest request) {
        LOG.debugf("action=UPDATE_EMPLOYEE_ROLE_START userId=%s storeId=%s targetUserId=%s role=%s",
                requesterId,
                storeId,
                targetUserId,
                request.role);

        findStoreOrThrow(storeId);
        checkWritePermission(requesterId, storeId);
        validateRoleAssignment(requesterId, storeId, request.role);

        Role role = roleRepository.findByName(request.role).orElseThrow(() -> {
            LOG.warnf("action=ROLE_NOT_FOUND role=%s", request.role);
            return new RoleNotFoundException("Role not found role=" + request.role);
        });

        boolean targetIsOwner = userRoleRepository.userHasRoleInStore(targetUserId, "OWNER", storeId);
        if (targetIsOwner) {
            LOG.warnf("action=EMPLOYEE_FORBIDDEN_ROLE_UPDATE userId=%s", requesterId);
            throw new ForbiddenException("Cannot update OWNER role");
        }

        UserRole userRole = userRoleRepository.findEmployeeRoleInStore(targetUserId, storeId).orElseThrow(() -> {
            LOG.warnf("action=EMPLOYEE_NOT_FOUND_IN_STORE requestUserId=%s targetUserId=%s storeId=%s",
                    requesterId,
                    targetUserId,
                    storeId);
            return new NotFoundException("Employee not found in this store");
        });
        userRole.role = role;

        LOG.infof("action=UPDATE_EMPLOYEE_ROLE_SUCCESS userId=%s storeId=%s targetUserId=%s role=%s",
                requesterId,
                storeId,
                targetUserId,
                request.role);
        return employeeMapper.toResponse(userRole.user);
    }

    @Transactional
    public void deleteEmployee(UUID requesterId, UUID targetUserId, UUID storeId) {
        LOG.debugf("action=DELETE_EMPLOYEE_START userId=%s storeId=%s targetUserId=%s",
                requesterId, storeId, targetUserId);
        findStoreOrThrow(storeId);
        checkWritePermission(requesterId, storeId);

        boolean targetIsOwner = userRoleRepository.userHasRoleInStore(targetUserId, "OWNER", storeId);
        if (targetIsOwner) {
            LOG.warnf("action=EMPLOYEE_FORBIDDEN_ROLE_DELETE userId=%s", requesterId);
            throw new ForbiddenException("Cannot remove OWNER from store");
        }

        UserRole userRole = userRoleRepository.findEmployeeRoleInStore(targetUserId, storeId).orElseThrow(() -> {
            LOG.warnf("action=EMPLOYEE_NOT_FOUND_IN_STORE requestUserId=%s targetUserId=%s storeId=%s",
                    requesterId,
                    targetUserId,
                    storeId);
            return new NotFoundException("Employee not found in this store");
        });

        userRoleRepository.delete(userRole);
        LOG.infof("action=DELETE_EMPLOYEE_SUCCESS userId=%s storeId=%s targetUserId=%s",
                requesterId, storeId, targetUserId);
    }

    // Helper
    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN"), storeId);

        if (!canWrite) {
            LOG.warnf("action=EMPLOYEE_WRITE_DENIED userId=%s storeId=%s",
                    userId, storeId);
            throw new ForbiddenException("No permission");
        }
    }

    private void validateRoleAssignment(UUID requesterId, UUID storeId, String requestedRole) {
        Set<String> acceptedRoles = Set.of("ADMIN", "MANAGER", "STAFF");
        if (!acceptedRoles.contains(requestedRole)) {
            LOG.warnf("action=EMPLOYEE_INVALID_ROLE_REQUEST userId=%s", requesterId);
            throw new BadRequestException("Invalid role: " + requestedRole);
        }

        boolean isOwner = userRoleRepository.userHasRoleInStore(requesterId, "OWNER", storeId);
        if (isOwner) {
            return; // only OWNER can assign all role
        }

        if (requestedRole.equals("ADMIN")) {
            LOG.warnf("action=EMPLOYEE_FORBIDDEN_ROLE_REQUEST userId=%s requestedRole=%s", requesterId, requestedRole);
            throw new ForbiddenException("ADMIN cannot assign ADMIN role");
        }
    }
}
