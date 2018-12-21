package com.options.database;

import com.options.entities.DownloadableOption;
import com.options.entities.OptionMetadata;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DBService {
    private static DataSource dataSource;
    private static JdbcTemplate jdbcTemplate;


    public static <T> void saveBatch(List<T> values, String tableName) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(tableName);

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(values);
        jdbcInsert.executeBatch(batch);
    }

    public static <T> void saveMetadataBatch(List<OptionMetadata> values) {

        String sql = "INSERT INTO VALUES \"option_metadata\" (?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OptionMetadata option = values.get(i);
                ps.setString(1, option.getContract_name());
                ps.setString(2, option.getStock());
                ps.setString(3, option.getExchange());
                ps.setString(4, option.getType());
                ps.setDouble(5, option.getStrike());
                ps.setDate(6, new java.sql.Date(option.getExpiration_date().getTime()));
                ps.setString(7, option.getCurrency());
                ps.setString(8, option.getContract_size());
            }

            @Override
            public int getBatchSize() {
                return values.size();
            }
        });

    }

    public static List<DownloadableOption> getOptionsList() {

        List<DownloadableOption> customers = jdbcTemplate.query("SELECT * FROM \"downloadable_options\"",
               new BeanPropertyRowMapper<>(DownloadableOption.class));
        return customers;
    }


    public static void setup() throws IllegalArgumentException {
        DriverManagerDataSource managerDataSource = new DriverManagerDataSource();
        String jdbcURL = System.getenv("JDBC_URL");
        if(jdbcURL == null || jdbcURL.isEmpty()){
            throw new IllegalArgumentException("Missing JDBC_URL paramter");
        }
        managerDataSource.setUrl(jdbcURL);

        String user = System.getenv("DB_USER");
        if(user == null || user.isEmpty()){
            throw new IllegalArgumentException("Missing DB_USER paramter");
        }
        managerDataSource.setUsername(user);

        String password = System.getenv("DB_PASSWORD");
        if(password == null || password.isEmpty()){
            throw new IllegalArgumentException("Missing DB_PASSWORD paramter");
        }
        managerDataSource.setPassword(password);

        dataSource = managerDataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

}
