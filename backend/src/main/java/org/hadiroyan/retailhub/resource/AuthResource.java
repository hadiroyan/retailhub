package org.hadiroyan.retailhub.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hadiroyan.retailhub.dto.request.LoginRequest;
import org.hadiroyan.retailhub.dto.request.RegisterCustomerRequest;
import org.hadiroyan.retailhub.dto.request.RegisterOwnerRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.AuthResponse;
import org.hadiroyan.retailhub.dto.response.UserResponse;
import org.hadiroyan.retailhub.service.AuthService;
import org.hadiroyan.retailhub.utils.CookieUtil;
import org.jboss.logging.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    AuthService authService;

    @Inject
    CookieUtil cookieUtil;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest request) {
        LOG.infof("Login request for email: %s", request.email);

        ApiResponse<AuthResponse> response = authService.login(request);
        NewCookie cookie = cookieUtil.createJwtCookie(response.data.token);

        return Response.ok(response)
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/register-customer")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerCustomer(@Valid RegisterCustomerRequest request) {
        LOG.infof("Customer registration request for email: %s", request.email);

        ApiResponse<UserResponse> response = authService.registerCustomer(request);

        // Auto-login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = response.data.email;
        loginRequest.password = request.password;

        ApiResponse<AuthResponse> loginResponse = authService.login(loginRequest);

        ApiResponse<AuthResponse> registrationResponse = new ApiResponse<>();
        registrationResponse.status = 201;
        registrationResponse.message = "Account created successfully. You are now logged in.";
        registrationResponse.data = loginResponse.data;
        NewCookie cookie = cookieUtil.createJwtCookie(loginResponse.data.token);

        return Response.status(Response.Status.CREATED)
                .entity(registrationResponse)
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/register-owner")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerOwner(@Valid RegisterOwnerRequest request) {
        LOG.infof("Owner registration request for email: %s", request.email);

        ApiResponse<UserResponse> response = authService.registerOwner(request);

        // Auto-login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = response.data.email;
        loginRequest.password = request.password;

        ApiResponse<AuthResponse> loginResponse = authService.login(loginRequest);

        ApiResponse<AuthResponse> registrationResponse = new ApiResponse<>();
        registrationResponse.status = 201;
        registrationResponse.message = "Account created successfully. You are now logged in.";
        registrationResponse.data = loginResponse.data;
        NewCookie cookie = cookieUtil.createJwtCookie(loginResponse.data.token);

        return Response.status(Response.Status.CREATED)
                .entity(registrationResponse)
                .cookie(cookie)
                .build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "SUPER_ADMIN", "OWNER", "ADMIN", "MANAGER", "STAFF", "CUSTOMER" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCurrentUser() {
        // Get email from JWT token's upn claim (principal name)
        String email = jwt.getName();
        LOG.infof("Get current user request for email: %s", email);

        UserResponse userResponse = authService.getCurrentUser(email);

        return Response.ok(ApiResponse.success(
                "Current user retrieved successfully",
                userResponse)).build();
    }

    @POST
    @Path("/logout")
    @PermitAll
    public Response logout() {
        LOG.info("Logout request");
        var logoutCookie = cookieUtil.createLogoutCookie();

        return Response.ok(ApiResponse.success("Logout successful"))
                .cookie(logoutCookie)
                .build();
    }
}
