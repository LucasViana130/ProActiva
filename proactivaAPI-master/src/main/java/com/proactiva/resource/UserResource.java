package com.proactiva.resource;

import com.proactiva.bo.UserBO;
import com.proactiva.dto.AuthResponse;
import com.proactiva.dto.ErrorResponse;
import com.proactiva.dto.LoginRequest;
import com.proactiva.model.User;
import com.proactiva.service.TokenService; // NOVO IMPORT
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Resource REST para gerenciamento de usuários.
 * Endpoints: /api/users
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserBO userBO;

    @Inject // NOVO
    TokenService tokenService; // NOVO

    /**
     * Registra um novo usuário.
     * POST /api/users/register
     */
    @POST
    @Path("/register")
    public Response register(@Valid User user) {
        try {
            User createdUser = userBO.create(user);

            // Não retornar a senha na resposta
            createdUser.setPassword(null);

            // Gerar token para o usuário recém-criado // NOVO
            String token = tokenService.generateToken(createdUser); // NOVO

            return Response.status(Response.Status.CREATED)
                    .entity(new AuthResponse(createdUser, "Usuário registrado com sucesso", token)) // CONSTRUTOR ATUALIZADO
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao registrar usuário", 500))
                    .build();
        }
    }

    /**
     * Autentica um usuário (login).
     * POST /api/users/login
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest loginRequest) {
        try {
            Optional<User> user = userBO.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

            if (user.isPresent()) {
                User authenticatedUser = user.get();
                authenticatedUser.setPassword(null);

                // Gerar token para o usuário autenticado // NOVO
                String token = tokenService.generateToken(authenticatedUser); // NOVO

                return Response.ok()
                        .entity(new AuthResponse(authenticatedUser, "Login realizado com sucesso", token)) // CONSTRUTOR ATUALIZADO
                        .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("Unauthorized", "Credenciais inválidas", 401))
                        .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao autenticar usuário", 500))
                    .build();
        }
    }

    /**
     * Busca um usuário por ID.
     * GET /api/users/{id}
     */
    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        try {
            Optional<User> user = userBO.findById(id);

            if (user.isPresent()) {
                User foundUser = user.get();
                foundUser.setPassword(null);
                return Response.ok(foundUser).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Not Found", "Usuário não encontrado", 404))
                        .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao buscar usuário", 500))
                    .build();
        }
    }

    /**
     * Lista todos os usuários.
     * GET /api/users
     */
    @GET
    public Response findAll() {
        try {
            List<User> users = userBO.findAll();

            // Remover senhas da resposta
            users.forEach(user -> user.setPassword(null));

            return Response.ok(users).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao listar usuários", 500))
                    .build();
        }
    }

    /**
     * Atualiza um usuário existente.
     * PUT /api/users/{id}
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid User user) {
        try {
            User updatedUser = userBO.update(id, user);
            updatedUser.setPassword(null);

            return Response.ok(updatedUser).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao atualizar usuário", 500))
                    .build();
        }
    }

    /**
     * Deleta um usuário por ID.
     * DELETE /api/users/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            boolean deleted = userBO.delete(id);

            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Not Found", "Usuário não encontrado", 404))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao deletar usuário", 500))
                    .build();
        }
    }
}
