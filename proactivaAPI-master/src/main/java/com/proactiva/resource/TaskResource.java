package com.proactiva.resource;

import com.proactiva.bo.TaskBO;
import com.proactiva.dto.ErrorResponse;
import com.proactiva.model.Task;
import com.proactiva.model.TaskHistory;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Resource REST para gerenciamento de tarefas.
 * Endpoints: /api/tasks
 */
@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    TaskBO taskBO;

    /**
     * Cria uma nova tarefa.
     * POST /api/tasks
     */
    @POST
    public Response create(@Valid Task task) {
        try {
            Task createdTask = taskBO.create(task);
            return Response.status(Response.Status.CREATED).entity(createdTask).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao criar tarefa (DB)", 500))
                    .build();
        } catch (Exception e) { // Captura qualquer outra exceção de runtime
            e.printStackTrace(); // Loga a exceção completa para debug
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro inesperado ao criar tarefa: " + e.getMessage(), 500))
                    .build();
        }
    }

    /**
     * Busca uma tarefa por ID.
     * GET /api/tasks/{id}
     */
    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        try {
            Optional<Task> task = taskBO.findById(id);

            if (task.isPresent()) {
                return Response.ok(task.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Not Found", "Tarefa não encontrada", 404))
                        .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao buscar tarefa", 500))
                    .build();
        }
    }

    /**
     * Lista todas as tarefas de um usuário.
     * GET /api/tasks/user/{userId}
     */
    @GET
    @Path("/user/{userId}")
    public Response findByUserId(@PathParam("userId") Long userId) {
        try {
            List<Task> tasks = taskBO.findByUserId(userId);
            return Response.ok(tasks).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao listar tarefas", 500))
                    .build();
        }
    }

    /**
     * Lista tarefas de um usuário filtradas por status.
     * GET /api/tasks/user/{userId}/status/{status}
     *
     * Exemplos:
     * - /api/tasks/user/1/status/EM_ANDAMENTO (tarefas em andamento)
     * - /api/tasks/user/1/status/CONCLUIDO (histórico completo de tarefas concluídas)
     */
    @GET
    @Path("/user/{userId}/status/{status}")
    public Response findByUserIdAndStatus(
            @PathParam("userId") Long userId,
            @PathParam("status") String status) {
        try {
            List<Task> tasks = taskBO.findByUserIdAndStatus(userId, status);
            return Response.ok(tasks).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao listar tarefas", 500))
                    .build();
        }
    }

    /**
     * Lista todas as tarefas (admin).
     * GET /api/tasks
     */
    @GET
    public Response findAll() {
        try {
            List<Task> tasks = taskBO.findAll();
            return Response.ok(tasks).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao listar tarefas", 500))
                    .build();
        }
    }

    /**
     * Atualiza uma tarefa existente.
     * PUT /api/tasks/{id}
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid Task task) {
        try {
            Task updatedTask = taskBO.update(id, task);
            return Response.ok(updatedTask).build();
        } catch (IllegalArgumentException e) {
            // Se a tarefa não for encontrada, o TaskBO lança IllegalArgumentException.
            // O erro 400 Bad Request é o mais apropriado para validação de dados.
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao atualizar tarefa (DB)", 500))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro inesperado ao atualizar tarefa: " + e.getMessage(), 500))
                    .build();
        }
    }

    /**
     * Marca uma tarefa como concluída.
     * PATCH /api/tasks/{id}/complete
     */
    @PATCH
    @Path("/{id}/complete")
    public Response markAsCompleted(@PathParam("id") Long id) {
        try {
            Task updatedTask = taskBO.markAsCompleted(id);
            return Response.ok(updatedTask).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao marcar tarefa como concluída", 500))
                    .build();
        }
    }

    /**
     * Deleta uma tarefa por ID (COM DELETE CASCADE).
     * DELETE /api/tasks/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            boolean deleted = taskBO.delete(id);

            if (deleted) {
                return Response.ok(new ErrorResponse("Success", "Tarefa e seu histórico deletados com sucesso", 200)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Not Found", "Tarefa não encontrada", 404))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            // Tratamento específico para erro de integridade referencial
            if (e.getMessage() != null && e.getMessage().contains("integrity constraint")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Conflict", "Não é possível deletar a tarefa devido a referências existentes", 409))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao deletar tarefa (DB): " + e.getMessage(), 500))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro inesperado ao deletar tarefa: " + e.getMessage(), 500))
                    .build();
        }
    }

    /**
     * Deleta uma tarefa por ID SEM CASCADE (método alternativo).
     * DELETE /api/tasks/{id}/no-cascade
     */
    @DELETE
    @Path("/{id}/no-cascade")
    public Response deleteWithoutCascade(@PathParam("id") Long id) {
        try {
            boolean deleted = taskBO.deleteWithoutCascade(id);

            if (deleted) {
                return Response.ok(new ErrorResponse("Success", "Tarefa deletada com sucesso (sem cascade)", 200)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Not Found", "Tarefa não encontrada", 404))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Bad Request", e.getMessage(), 400))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao deletar tarefa (DB)", 500))
                    .build();
        }
    }

    /**
     * Lista o histórico de uma tarefa.
     * GET /api/tasks/{id}/history
     */
    @GET
    @Path("/{id}/history")
    public Response getHistory(@PathParam("id") Long id) {
        try {
            List<TaskHistory> history = taskBO.getHistory(id);
            return Response.ok(history).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "Erro ao buscar histórico", 500))
                    .build();
        }
    }
}