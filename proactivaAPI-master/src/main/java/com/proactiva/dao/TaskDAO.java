package com.proactiva.dao;

import com.proactiva.model.Task;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para a entidade Task.
 * Implementa operações CRUD utilizando JDBC puro.
 */
@ApplicationScoped
public class TaskDAO {

    @Inject
    DatabaseConnection databaseConnection;

    /**
     * Cria uma nova tarefa no banco de dados.
     *
     * @param task tarefa a ser criada
     * @return tarefa criada com ID gerado
     * @throws SQLException se houver erro na operação
     */
    public Task create(Task task) throws SQLException {
        String sql = "INSERT INTO TASKS (TITLE, DESCRIPTION, CATEGORY, PRIORITY, STATUS, DUE_DATE, USER_ID, CREATED_AT, UPDATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCategory());
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getStatus());

            if (task.getDueDate() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setLong(7, task.getUserId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar tarefa, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao criar tarefa, nenhum ID obtido.");
                }
            }
        }

        return task;
    }

    /**
     * Busca uma tarefa por ID.
     *
     * @param id ID da tarefa
     * @return Optional contendo a tarefa se encontrada
     * @throws SQLException se houver erro na operação
     */
    public Optional<Task> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM TASKS WHERE ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    /**
     * Lista todas as tarefas de um usuário.
     *
     * @param userId ID do usuário
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM TASKS WHERE USER_ID = ? ORDER BY CREATED_AT DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }

        return tasks;
    }

    /**
     * Lista tarefas de um usuário filtradas por status.
     *
     * @param userId ID do usuário
     * @param status status da tarefa
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findByUserIdAndStatus(Long userId, String status) throws SQLException {
        String sql = "SELECT * FROM TASKS WHERE USER_ID = ? AND STATUS = ? ORDER BY CREATED_AT DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }

        return tasks;
    }

    /**
     * Lista todas as tarefas.
     *
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findAll() throws SQLException {
        String sql = "SELECT * FROM TASKS ORDER BY CREATED_AT DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }

        return tasks;
    }

    /**
     * Atualiza uma tarefa existente.
     *
     * @param task tarefa com dados atualizados
     * @return tarefa atualizada
     * @throws SQLException se houver erro na operação
     */
    public Task update(Task task) throws SQLException {
        String sql = "UPDATE TASKS SET TITLE = ?, DESCRIPTION = ?, CATEGORY = ?, PRIORITY = ?, " +
                "STATUS = ?, DUE_DATE = ?, COMPLETED_AT = ?, UPDATED_AT = SYSTIMESTAMP " +
                "WHERE ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCategory());
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getStatus());

            if (task.getDueDate() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            if (task.getCompletedAt() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(task.getCompletedAt()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setLong(8, task.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Tarefa não encontrada para atualização.");
            }
        }

        return task;
    }

    /**
     * Deleta uma tarefa por ID (método original).
     *
     * @param id ID da tarefa
     * @return true se deletada com sucesso
     * @throws SQLException se houver erro na operação
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM TASKS WHERE ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    /**
     * Deleta uma tarefa e seu histórico em cascata usando transação.
     * @param taskId ID da tarefa a ser deletada
     * @return true se deletado com sucesso, false se não encontrado
     * @throws SQLException se houver erro no banco
     */
    public boolean deleteWithCascade(Long taskId) throws SQLException {
        Connection conn = null;
        try {
            System.out.println("=== 🗑️ TASKDAO.DELETE_WITH_CASCADE() INICIANDO ===");
            System.out.println("📦 Deletando task ID: " + taskId + " com cascade");

            conn = databaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transação

            // 1️⃣ PRIMEIRO: Deletar o histórico associado à tarefa
            String deleteHistorySQL = "DELETE FROM TASK_HISTORY WHERE TASK_ID = ?";
            try (PreparedStatement historyStmt = conn.prepareStatement(deleteHistorySQL)) {
                historyStmt.setLong(1, taskId);
                int historyRowsDeleted = historyStmt.executeUpdate();
                System.out.println("✅ Histórico deletado: " + historyRowsDeleted + " registros");
            }

            // 2️⃣ DEPOIS: Deletar a tarefa
            String deleteTaskSQL = "DELETE FROM TASKS WHERE ID = ?";
            try (PreparedStatement taskStmt = conn.prepareStatement(deleteTaskSQL)) {
                taskStmt.setLong(1, taskId);
                int taskRowsAffected = taskStmt.executeUpdate();

                conn.commit(); // Confirmar transação

                System.out.println("✅ Tarefa deletada: " + taskRowsAffected + " registros");
                System.out.println("🎉 DELETE CASCADE CONCLUÍDO COM SUCESSO");

                return taskRowsAffected > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ ❌ ❌ ERRO SQL NO TASKDAO.DELETE_WITH_CASCADE() ❌ ❌ ❌");
            System.err.println("❌ Mensagem: " + e.getMessage());
            System.err.println("❌ SQL State: " + e.getSQLState());
            System.err.println("❌ Error Code: " + e.getErrorCode());

            if (conn != null) {
                try {
                    System.out.println("🔄 Realizando ROLLBACK...");
                    conn.rollback();
                    System.out.println("✅ ROLLBACK realizado devido a erro");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ ERRO NO ROLLBACK: " + rollbackEx.getMessage());
                }
            }
            throw e; // Re-lançar a exceção para tratamento no BO
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ Erro ao fechar conexão: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Mapeia um ResultSet para um objeto Task.
     *
     * @param rs ResultSet contendo dados da tarefa
     * @return objeto Task
     * @throws SQLException se houver erro ao ler dados
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("ID"));
        task.setTitle(rs.getString("TITLE"));
        task.setDescription(rs.getString("DESCRIPTION"));
        task.setCategory(rs.getString("CATEGORY"));
        task.setPriority(rs.getString("PRIORITY"));
        task.setStatus(rs.getString("STATUS"));
        task.setUserId(rs.getLong("USER_ID"));

        Timestamp dueDate = rs.getTimestamp("DUE_DATE");
        if (dueDate != null) {
            task.setDueDate(dueDate.toLocalDateTime());
        }

        Timestamp completedAt = rs.getTimestamp("COMPLETED_AT");
        if (completedAt != null) {
            task.setCompletedAt(completedAt.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            task.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
        if (updatedAt != null) {
            task.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return task;
    }
}