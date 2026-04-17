package org.hadiroyan.retailhub.resource;

import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateEmployeeRequest;
import org.hadiroyan.retailhub.dto.request.UpdateEmployeeRoleRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.EmployeeResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.EmployeeService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.jboss.logging.Logger;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/stores/{storeId}/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private final static Logger LOG = Logger.getLogger(EmployeeResource.class);

    @Inject
    EmployeeService employeeService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "ADMIN", "OWNER" })
    public Response createEmployee(@PathParam("storeId") UUID storeId, @Valid CreateEmployeeRequest request) {

        UUID requesterId = currentUser.getUserId();
        Set<String> roles = currentUser.getRoles();

        LOG.debugf("action=CREATE_EMPLOYEE_REQUEST userId=%s storeId=%s role=%s",
                requesterId, storeId, roles.toString());

        EmployeeResponse response = employeeService.createEmployee(request, requesterId, storeId, roles);

        LOG.infof("action=CREATE_EMPLOYEE_RESPONSE userId=%s storeId=%s employeeId=%s",
                requesterId, storeId, response.id);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Employee successfully created!", response))
                .build();
    }

    @GET
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response listEmployees(
            @PathParam("storeId") UUID storeId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=LIST_EMPLOYEE_REQUEST userId=%s storeId=%s page=%d size=%d",
                userId, storeId, page, size);

        PagedResponse<EmployeeResponse> listEmployees = employeeService.listEmployees(userId, storeId, page, size);

        LOG.infof("action=LIST_EMPLOYEE_RESPONSE userId=%s storeId=%s page=%d size=%d",
                userId, storeId, page, size);
        return Response.ok(ApiResponse.success("Employees retrieved successfully", listEmployees)).build();
    }

    @PUT
    @Path("/{targetUserId}")
    @RolesAllowed({ "OWNER", "ADMIN" })
    public Response updateEmployee(
            @PathParam("storeId") UUID storeId,
            @PathParam("targetUserId") UUID targetUserId,
            @Valid UpdateEmployeeRoleRequest request) {

        UUID requesterId = currentUser.getUserId();
        LOG.debugf("action=UPDATE_EMPLOYEE_REQUEST userId=%s storeId=%s targetUserId=%s",
                requesterId, storeId, targetUserId);

        EmployeeResponse response = employeeService.updateEmployeeRole(requesterId, storeId, targetUserId, request);

        LOG.infof("action=UPDATE_EMPLOYEE_RESPONSE userId=%s storeId=%s targetUserId=%s",
                requesterId, storeId, targetUserId);
        return Response.ok(ApiResponse.success("Employee updated successfully", response)).build();
    }

    @DELETE
    @Path("/{targetUserId}")
    @RolesAllowed({ "OWNER", "ADMIN" })
    public Response deleteEmployee(
            @PathParam("storeId") UUID storeId,
            @PathParam("targetUserId") UUID targetUserId) {

        UUID requesterId = currentUser.getUserId();
        LOG.debugf("action=DELETE_EMPLOYEE_REQUEST userId=%s storeId=%s targetUserId=%s",
                requesterId, storeId, targetUserId);

        employeeService.deleteEmployee(requesterId, targetUserId, storeId);

        LOG.infof("action=DELETE_EMPLOYEE_RESPONSE userId=%s storeId=%s targetUserId=%s",
                requesterId, storeId, targetUserId);
        return Response.ok(ApiResponse.success("Employee deleted successfully")).build();
    }
}
