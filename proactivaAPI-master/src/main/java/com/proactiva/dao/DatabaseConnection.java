package com.proactiva.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados Oracle.
 * Utiliza o DataSource configurado no Quarkus.
 */
@ApplicationScoped
public class DatabaseConnection {

    @Inject
    DataSource dataSource;

    /**
     * Obtém uma conexão com o banco de dados.
     *
     * @return Connection objeto de conexão
     * @throws SQLException se houver erro ao obter conexão
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Fecha a conexão com o banco de dados de forma segura.
     *
     * @param connection conexão a ser fechada
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
