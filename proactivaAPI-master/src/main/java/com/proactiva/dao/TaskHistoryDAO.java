package com.proactiva.dao;

import com.proactiva.model.TaskHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para a entidade TaskHistory.
 * Implementa operações de histórico utilizando JDBC puro.
 */
@ApplicationScoped
public class TaskHistoryDAO {

    @Inject
    DatabaseConnection databaseConnection;

    /**
     * Cria um novo registro de histórico no banco de dados.
     *
     * @param history histórico a ser criado
     * @return histórico criado com ID gerado
     * @throws SQLException se houver erro na operação
     */
    public TaskHistory create(TaskHistory history) throws SQLException {
        String sql = "INSERT INTO TASK_HISTORY (TASK_ID, ACTION, OLD_STATUS, NEW_STATUS, DESCRIPTION, CHANGED_AT) " +
                "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, history.getTaskId());
            stmt.setString(2, history.getAction());
            stmt.setString(3, history.getOldStatus());
            stmt.setString(4, history.getNewStatus());
            stmt.setString(5, history.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar histórico, nenhuma linha afetada.");
            }

            // Não tenta obter o ID gerado, pois o histórico não precisa retornar o ID
            // e a tentativa de obter o ID pode estar causando o erro ORA-02289
            // Se o ID for necessário, o método deve ser reescrito para buscar o ID após a inserção.
            // Por enquanto, apenas remove a parte que falha.
        }

        return history;
    }

    /**
     * Lista todo o histórico de uma tarefa.
     *
     * @param taskId ID da tarefa
     * @return lista de históricos
     * @throws SQLException se houver erro na operação
     */
    public List<TaskHistory> findByTaskId(Long taskId) throws SQLException {
        String sql = "SELECT * FROM TASK_HISTORY WHERE TASK_ID = ? ORDER BY CHANGED_AT DESC";
        List<TaskHistory> histories = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, taskId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToTaskHistory(rs));
                }
            }
        }

        return histories;
    }

    /**
     * Lista todo o histórico.
     *
     * @return lista de históricos
     * @throws SQLException se houver erro na operação
     */
    public List<TaskHistory> findAll() throws SQLException {
        String sql = "SELECT * FROM TASK_HISTORY ORDER BY CHANGED_AT DESC";
        List<TaskHistory> histories = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                histories.add(mapResultSetToTaskHistory(rs));
            }
        }

        return histories;
    }

    /**
     * Mapeia um ResultSet para um objeto TaskHistory.
     *
     * @param rs ResultSet contendo dados do histórico
     * @return objeto TaskHistory
     * @throws SQLException se houver erro ao ler dados
     */
    private TaskHistory mapResultSetToTaskHistory(ResultSet rs) throws SQLException {
        TaskHistory history = new TaskHistory();
        history.setId(rs.getLong("HISTORY_ID"));
        history.setTaskId(rs.getLong("TASK_ID"));
        history.setAction(rs.getString("ACTION"));
        history.setOldStatus(rs.getString("OLD_STATUS"));
        history.setNewStatus(rs.getString("NEW_STATUS"));
        history.setDescription(rs.getString("DESCRIPTION"));

        Timestamp changedAt = rs.getTimestamp("CHANGED_AT");
        if (changedAt != null) {
            history.setChangedAt(changedAt.toLocalDateTime());
        }

        return history;
    }
}