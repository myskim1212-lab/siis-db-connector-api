package sec.siis.jdbc.strategy;

import javax.sql.DataSource;

public class StrategyFactory {

    public static DbStrategy create(DataSource ds, String msgID) throws Exception{
        DbType type = DbTypeResolver.resolve(ds);

        switch (type) {
            case ORACLE:
                return new OracleStrategy(msgID);
            case MSSQL:
            	return new MsSqlStrategy(msgID);                
            case POSTGRES:
                return new PostgreStrategy(msgID);
            case MYSQL:
            	return new MySqlStrategy(msgID);
            case MARIADB:
                return new MySqlStrategy(msgID);
            default:
                throw new UnsupportedOperationException(
                    "Unsupported DB: " + type
                );
        }
    }
}