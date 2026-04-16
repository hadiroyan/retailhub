package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateEmployeeRequest;
import org.hadiroyan.retailhub.dto.response.EmployeeResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
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
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class EmployeeServiceTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    RoleRepository roleRepository;

    @InjectMock
    UserRoleRepository userRoleRepository;

    @InjectMock
    StoreRepository storeRepository;

    @InjectMock
    PasswordService passwordService;

    @Inject
    EmployeeService employeeService;

    @Test
    void should_create_employee_successfully() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.email = "test@mail.com";
        request.fullName = "Test User";
        request.password = "Password123!";
        request.role = "ADMIN";

        // mock
        Store store = new Store();
        store.id = storeId;

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(userId, Set.of("OWNER", "ADMIN"), storeId))
                .thenReturn(true);

        when(userRoleRepository.userHasRoleInStore(userId, "OWNER", storeId))
                .thenReturn(true);

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(passwordService.hash(anyString()))
                .thenReturn("hashed-password");

        Role role = new Role();
        role.name = "ADMIN";

        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.of(role));

        // execute
        EmployeeResponse response = employeeService.createEmployee(
                request, userId, storeId, Set.of("OWNER"));

        // verify
        assertNotNull(response);
        verify(userRepository).persist(any(User.class));
        verify(userRoleRepository).persist(any(UserRole.class));
    }

    @Test
    void should_throw_exception_when_email_exists() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.email = "test@mail.com";
        request.fullName = "Test User";
        request.password = "Password123!";
        request.role = "ADMIN";

        Store store = new Store();
        store.id = storeId;

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(any(), any(), any()))
                .thenReturn(true);

        when(userRoleRepository.userHasRoleInStore(any(), eq("OWNER"), any()))
                .thenReturn(true);

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> employeeService.createEmployee(request, userId, storeId, Set.of("OWNER")));
    }

    @Test
    void should_throw_forbidden_when_user_no_permission() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.email = "test@mail.com";
        request.fullName = "Test User";
        request.password = "Password123!";
        request.role = "ADMIN";

        Store store = new Store();
        store.id = storeId;

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(userId, Set.of("OWNER", "ADMIN"), storeId))
                .thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> employeeService.createEmployee(request, userId, storeId, Set.of("STAFF")));
    }

    @Test
    void should_return_employee_list() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Store store = new Store();
        store.id = storeId;

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId))
                .thenReturn(true);

        User user = new User();
        user.email = "test@mail.com";

        when(userRepository.findEmployeesByStore(storeId, 0, 10))
                .thenReturn(List.of(user));

        when(userRepository.countEmployeesByStore(storeId))
                .thenReturn(1L);

        PagedResponse<EmployeeResponse> result = employeeService.listEmployees(userId, storeId, 0, 10);

        assertEquals(1, result.totalElements);
    }
}
