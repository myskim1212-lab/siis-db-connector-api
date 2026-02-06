package sec.siis.jdbc.strategy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public final class DbTypeResolver {
    
    private DbTypeResolver() {}
    
    public static DbType resolve(DataSource ds) throws Exception {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            String name = metaData.getDatabaseProductName();
            
            if (name == null) {
                return DbType.UNKNOWN;
            }
            
            name = name.toLowerCase();
            
            if (name.contains("oracle")) {
                return DbType.ORACLE;
            }
            if (name.contains("postgres")) {
                return DbType.POSTGRES;
            }
            if (name.contains("mysql")) {
                return DbType.MYSQL;
            }
            if (name.contains("maria")) {
                return DbType.MARIADB;
            }
            if (name.contains("sql server")) {
                return DbType.MSSQL;
            }
            
            return DbType.UNKNOWN;
            
        } catch (SQLException e) {
            throw new Exception("Failed to resolve database type", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
        }
    }
}