package com.ragin.bdd.cucumber.core;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseExecutorService {
    @Autowired
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    DataSource datasource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Execute Liquibase script
     *
     * @param liquibaseScript   path/filename of the Liquibase script
     * @throws Exception        Unable to execute liquibase script
     */
    public void executeLiquibaseScript(String liquibaseScript) throws Exception {
        JdbcConnection connection = new JdbcConnection(datasource.getConnection());
        try {
            new Liquibase(
                    liquibaseScript,
                    new ClassLoaderResourceAccessor(),
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
            ).update(
                    new Contexts(""),
                    new LabelExpression(),
                    false
            );
        } finally {
            connection.close();
        }
    }

    /**
     * Execute SQL statements
     *
     * @param sql   SQL statements that should be executed
     * @return      List with a map per row which contains the result
     */
    public List<Map<String, Object>> executeSQL(final String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
