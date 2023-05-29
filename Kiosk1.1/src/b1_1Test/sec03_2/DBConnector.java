package b1_1Test.sec03_2;
//데이터베이스 연결을 위한 클래스입니다. 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;

public class DBConnector {
    Connection connection;
    private PreparedStatement statement;

    public DBConnector() {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			
			//연결하기
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521/xe",
					"parrot",
					"12345"
					);
//			System.out.println("연결확인");				//연결확인용 콘솔출력
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("DB 연결에 문제가 생겨 프로그램을 종료합니다.");
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("DB 연결에 문제가 생겨 프로그램을 종료합니다.");
			System.exit(0);
		}
	}

    public ResultSet executeQuery(String query) throws SQLException {
        return statement.executeQuery(query);
    }

    public int executeUpdate(String query) throws SQLException {
        return statement.executeUpdate(query);
    }

    
    public void exit() {		//라이브러리에 존재하는 close() 메서드들과 혼동할 여지가 있으므로 exit() 메서드로 이름을 바꿀 것을 제안
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {			//무슨 필요로 만든 getter 메서드인지 파악 필요
        return connection;
    }



}