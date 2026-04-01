package com.proactiva.bo;

import com.proactiva.dao.TaskDAO;
import com.proactiva.dao.TaskHistoryDAO;
import com.proactiva.model.Task;
import com.proactiva.model.TaskHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business Object para a entidade Task.
 * Contém a lógica de negócio, validações e gerenciamento de histórico.
 */
@ApplicationScoped
public class TaskBO {

    @Inject
    TaskDAO taskDAO;

    @Inject
    TaskHistoryDAO taskHistoryDAO;

    /**
     * Cria uma nova tarefa e registra no histórico.
     *
     * @param task tarefa a ser criada
     * @return tarefa criada
     * @throws SQLException se houver erro na operação
     */
    public Task create(@Valid Task task) throws SQLException {
        System.out.println("TaskBO.create() - Criando nova tarefa: " + task.getTitle());

        // Validar status inicial
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("EM_ANDAMENTO");
        }

        // Validar categoria
        validateCategory(task.getCategory());

        // Validar prioridade
        validatePriority(task.getPriority());

        // Validar status
        validateStatus(task.getStatus());

        // Criar tarefa
        Task createdTask = taskDAO.create(task);

        // Registrar no histórico (Tratamento de erro para evitar que a falha no histórico
        // cause um erro 500 na criação da tarefa, que já foi salva no banco)
        try {
            TaskHistory history = new TaskHistory(
                    createdTask.getId(),
                    "CRIADA",
                    null,
                    createdTask.getStatus(),
                    "Tarefa criada: " + createdTask.getTitle()
            );
            taskHistoryDAO.create(history);
        } catch (SQLException e) {
            // Logar o erro, mas não re-lançar, pois a tarefa principal foi criada
            System.err.println("Erro ao registrar histórico da tarefa " + createdTask.getId() + ": " + e.getMessage());
        }

        return createdTask;
    }

    /**
     * Busca uma tarefa por ID.
     *
     * @param id ID da tarefa
     * @return Optional contendo a tarefa se encontrada
     * @throws SQLException se houver erro na operação
     */
    public Optional<Task> findById(Long id) throws SQLException {
        return taskDAO.findById(id);
    }

    /**
     * Lista todas as tarefas de um usuário.
     *
     * @param userId ID do usuário
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findByUserId(Long userId) throws SQLException {
        return taskDAO.findByUserId(userId);
    }

    /**
     * Lista tarefas de um usuário filtradas por status.
     *
     * @param userId ID do usuário
     * @param status status da tarefa (EM_ANDAMENTO ou CONCLUIDO)
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findByUserIdAndStatus(Long userId, String status) throws SQLException {
        validateStatus(status);
        return taskDAO.findByUserIdAndStatus(userId, status);
    }

    /**
     * Lista todas as tarefas.
     *
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findAll() throws SQLException {
        return taskDAO.findAll();
    }

    /**
     * Atualiza uma tarefa existente e registra no histórico.
     *
     * @param id ID da tarefa
     * @param updatedTask dados atualizados
     * @return tarefa atualizada
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se tarefa não for encontrada
     */
    public Task update(Long id, @Valid Task updatedTask) throws SQLException {
        System.out.println("TaskBO.update() - Iniciando atualização para ID: " + id);
        System.out.println("TaskBO.update() - Dados recebidos: " + updatedTask);

        Optional<Task> existingTask = taskDAO.findById(id);
        if (existingTask.isEmpty()) {
            System.err.println("TaskBO.update() - Tarefa não encontrada: " + id);
            throw new IllegalArgumentException("Tarefa não encontrada");
        }

        Task task = existingTask.get();
        String oldStatus = task.getStatus();
        System.out.println("TaskBO.update() - Status anterior: " + oldStatus);

        // Validar categoria
        validateCategory(updatedTask.getCategory());

        // Validar prioridade
        validatePriority(updatedTask.getPriority());

        // Validar status
        validateStatus(updatedTask.getStatus());

        // Atualizar campos
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setCategory(updatedTask.getCategory());
        task.setPriority(updatedTask.getPriority());
        task.setStatus(updatedTask.getStatus());
        task.setDueDate(updatedTask.getDueDate());

        // Se status mudou para CONCLUIDO, registrar data de conclusão
        if ("CONCLUIDO".equals(updatedTask.getStatus()) && !"CONCLUIDO".equals(oldStatus)) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (!"CONCLUIDO".equals(updatedTask.getStatus())) {
            task.setCompletedAt(null); // Limpar completedAt se não estiver concluído
        }

        // Atualizar tarefa
        Task updated = taskDAO.update(task);
        System.out.println("TaskBO.update() - Tarefa atualizada com sucesso: " + updated.getId());

        // Registrar no histórico
        try {
            String action = oldStatus.equals(updatedTask.getStatus()) ? "EDITADA" : "STATUS_ALTERADO";
            TaskHistory history = new TaskHistory(
                    updated.getId(),
                    action,
                    oldStatus,
                    updatedTask.getStatus(),
                    "Tarefa atualizada: " + updated.getTitle()
            );
            taskHistoryDAO.create(history);
            System.out.println("TaskBO.update() - Histórico registrado com sucesso");
        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico de atualização da tarefa " + updated.getId() + ": " + e.getMessage());
        }

        return updated;
    }

    /**
     * Marca uma tarefa como concluída.
     *
     * @param id ID da tarefa
     * @return tarefa atualizada
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se tarefa não for encontrada
     */
    public Task markAsCompleted(Long id) throws SQLException {
        Optional<Task> existingTask = taskDAO.findById(id);
        if (existingTask.isEmpty()) {
            throw new IllegalArgumentException("Tarefa não encontrada");
        }

        Task task = existingTask.get();
        String oldStatus = task.getStatus();

        task.setStatus("CONCLUIDO");
        task.setCompletedAt(LocalDateTime.now());

        Task updated = taskDAO.update(task);

        // Registrar no histórico
        try {
            TaskHistory history = new TaskHistory(
                    updated.getId(),
                    "CONCLUIDA",
                    oldStatus,
                    "CONCLUIDO",
                    "Tarefa marcada como concluída: " + updated.getTitle()
            );
            taskHistoryDAO.create(history);
        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico de conclusão da tarefa " + updated.getId() + ": " + e.getMessage());
        }

        return updated;
    }

    /**
     * Deleta uma tarefa por ID e registra no histórico.
     * UTILIZA DELETE CASCADE para remover automaticamente os registros relacionados.
     *
     * @param id ID da tarefa
     * @return true se deletada com sucesso
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se tarefa não for encontrada
     */
    public boolean delete(Long id) throws SQLException {
        Optional<Task> task = taskDAO.findById(id);
        if (task.isEmpty()) {
            throw new IllegalArgumentException("Tarefa não encontrada");
        }

        // Registrar no histórico antes de deletar
        try {
            TaskHistory history = new TaskHistory(
                    task.get().getId(),
                    "DELETADA",
                    task.get().getStatus(),
                    null,
                    "Tarefa deletada: " + task.get().getTitle()
            );
            taskHistoryDAO.create(history);
        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico de exclusão da tarefa " + task.get().getId() + ": " + e.getMessage());
        }

        // Usar DELETE CASCADE para remover tarefa e histórico automaticamente
        return taskDAO.deleteWithCascade(id);
    }

    /**
     * Deleta uma tarefa por ID SEM CASCADE (método alternativo).
     * Mantido para compatibilidade com operações específicas.
     *
     * @param id ID da tarefa
     * @return true se deletada com sucesso
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se tarefa não for encontrada
     */
    public boolean deleteWithoutCascade(Long id) throws SQLException {
        Optional<Task> task = taskDAO.findById(id);
        if (task.isEmpty()) {
            throw new IllegalArgumentException("Tarefa não encontrada");
        }

        // Registrar no histórico antes de deletar
        try {
            TaskHistory history = new TaskHistory(
                    task.get().getId(),
                    "DELETADA",
                    task.get().getStatus(),
                    null,
                    "Tarefa deletada: " + task.get().getTitle()
            );
            taskHistoryDAO.create(history);
        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico de exclusão da tarefa " + task.get().getId() + ": " + e.getMessage());
        }

        return taskDAO.delete(id);
    }

    /**
     * Lista o histórico de uma tarefa.
     *
     * @param taskId ID da tarefa
     * @return lista de históricos
     * @throws SQLException se houver erro na operação
     */
    public List<TaskHistory> getHistory(Long taskId) throws SQLException {
        return taskHistoryDAO.findByTaskId(taskId);
    }

    /**
     * Valida a categoria da tarefa.
     *
     * @param category categoria
     * @throws IllegalArgumentException se categoria for inválida
     */
    private void validateCategory(String category) {
        List<String> validCategories = List.of(
                "TECNOLOGIA", "CERTIFICACAO", "TRABALHO", "PESSOAL", "SAUDE", "EDUCACAO", "OUTRO"
        );

        if (category == null || !validCategories.contains(category.toUpperCase())) {
            throw new IllegalArgumentException("Categoria inválida. Valores aceitos: " + validCategories);
        }
    }

    /**
     * Valida a prioridade da tarefa.
     *
     * @param priority prioridade
     * @throws IllegalArgumentException se prioridade for inválida
     */
    private void validatePriority(String priority) {
        List<String> validPriorities = List.of("BAIXA", "MEDIA", "ALTA", "URGENTE");

        if (priority == null || !validPriorities.contains(priority.toUpperCase())) {
            throw new IllegalArgumentException("Prioridade inválida. Valores aceitos: " + validPriorities);
        }
    }

    /**
     * Valida o status da tarefa.
     *
     * @param status status
     * @throws IllegalArgumentException se status for inválido
     */
    private void validateStatus(String status) {
        List<String> validStatuses = List.of("EM_ANDAMENTO", "CONCLUIDO");

        if (status == null || !validStatuses.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Status inválido. Valores aceitos: " + validStatuses);
        }
    }
}