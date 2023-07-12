package springbook.user.dao;

import com.mysql.cj.exceptions.MysqlErrorNumbers;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

//public abstract class UserDao {
public class UserDao {
    // 생성자 파라미터를 통해 전달 받은 런타임 의존관계를 갖는 오브젝트는 인스턴스 변수에 저장해둔다.
    // private ConnectionMaker connectionMaker;
    // UserDao 오브젝트는 이제 생성자를 통해 주입받은 DConnectionMaker 오브젝트를 언제든지 사용하면 된다.
    // 반드시 인터페이스 타입의 파라미터 이어야만 한다.

//    private DataSource dataSource;

    // JdbcTemplate 사용
    private JdbcTemplate jdbcTemplate;

    private RowMapper<User> userMapper =
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    return user;
                }
            };

    /*
    // 생성자를 이용한 의존관계 주입
    public UserDao(ConnectionMaker connectionMaker){
        this.connectionMaker = connectionMaker;
    }
    */

    // 수정자 메소드를 이용한 의존관계 주입
//    public void setConnectionMaker(ConnectionMaker connectionMaker){
//        this.connectionMaker = connectionMaker;
//    }

    public void setDataSource(DataSource dataSource){   // 수정자 메소드이면서 JdbcContext 에 대한 생성, DI 작업을 동시에 수행한다.
//        this.jdbcContext = new JdbcContext();   // JdbcContext 생성 (IoC)
//        this.jdbcContext.setDataSource(dataSource); // 의존 오브젝트 주입(DI)

        // JdbcTemplate 사용
        this.jdbcTemplate = new JdbcTemplate(dataSource);   //DataSource 오브젝트는 JdbcTemplate 을 만든 후에는 사용하지 않으니 저장해두지 않아도 된다.
//        this.dataSource = dataSource;   // 아직 JdbcContext 를 적용하지 않은 메소드를 위해 저장해 둔다.
    }

    /*
    // DaoFactory 를 이용하는 생성자
    public UserDao(){
        DaoFactory daoFactory = new DaoFactory();
        this.connectionMaker = daoFactory.connectionMaker();
    }
    */

    /*
    // 의존관계 검색을 이용하는 UserDao 생성자
    public UserDao(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        this.connectionMaker = context.getBean("connectionMaker", ConnectionMaker.class);
    }
    */
//    private JdbcContext jdbcContext;

//    public void add(User user) throws SQLException {
//        // Connection c = connectionMaker.makeConnection();
//        Connection c = dataSource.getConnection();
//
//        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?,?,?)");
//        ps.setString(1, user.getId());
//        ps.setString(2, user.getName());
//        ps.setString(3, user.getPassword());
//
//        ps.executeUpdate();
//
//        ps.close();
//        c.close();
//    }

    // DI 적용을 위한 클라이언트 / 컨텍스트 분리 ----[S]
    // 컨텍스트
//    public void add(final User user) throws SQLException {
//        // AddStatement 클래스를 로컬 클래스로 이전
////        class AddStatement implements StatementStrategy {
////
////            @Override
////            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
////                PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?,?,?)");
////                ps.setString(1, user.getId());
////                ps.setString(2, user.getName());
////                ps.setString(3, user.getPassword());
////
////                return ps;
////            }
////        }
//
//        // AddStatement 클래스를 익명 내부 클래스로 이전
////        StatementStrategy st = new StatementStrategy() {
////            @Override
////            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
////                PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?,?,?)");
////                ps.setString(1, user.getId());
////                ps.setString(2, user.getName());
////                ps.setString(3, user.getPassword());
////
////                return ps;
////            }
////        };
//////        StatementStrategy st = new AddStatement();
////        jdbcContextWithStatementStrategy(st);
//
//
//        // AddStatement 클래스를 익명 내부 클래스로 이전 (jdbcContextWithStatementStrategy()메소드의 파라미터에서 바로 생성)
////        jdbcContextWithStatementStrategy(
////                new StatementStrategy() {
////                    @Override
////                    public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
////                        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?,?,?)");
////                        ps.setString(1, user.getId());
////                        ps.setString(2, user.getName());
////                        ps.setString(3, user.getPassword());
////
////                        return ps;
////                    }
////                }
////        );
//
//        this.jdbcContext.workWithStatementStrategy(
//                new StatementStrategy() {
//                    @Override
//                    public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
//                        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?,?,?)");
//                        ps.setString(1, user.getId());
//                        ps.setString(2, user.getName());
//                        ps.setString(3, user.getPassword());
//
//                        return ps;
//                    }
//                }
//        );
//    }
    // DI 적용을 위한 클라이언트 / 컨텍스트 분리 ----[E]

    // JdbcTemplate 이용한 add() 메소드
    public void add(final User user) throws DuplicateUserIdException {
        this.jdbcTemplate.update("insert into users(id, name, password) values (?,?,?)", user.getId(), user.getName(), user.getPassword());
//        try {
//
//        } catch (SQLException e) {
//            if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
//                throw new DuplicateUserIdException(e);  //예외 전환
//            else
//                throw new RuntimeException(e);  //예외 포장
//        }
    }

//    public User get(String id) throws SQLException {
//        // Connection c = connectionMaker.makeConnection();
//        Connection c = dataSource.getConnection();
//
//        PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
//        ps.setString(1, id);
//
//        ResultSet rs = ps.executeQuery();
//
//        User user = null;
//        if (rs.next()) {
//            user = new User();
//            user.setId(rs.getString("id"));
//            user.setName(rs.getString("name"));
//            user.setPassword(rs.getString("password"));
//        }
//
//        rs.close();
//        ps.close();
//        c.close();
//
//        if(user == null) throw new EmptyResultDataAccessException(1);
//
//        return user;
//    }

    // JdbcTemplate 이용
    public User get(String id) {
//        return this.jdbcTemplate.queryForObject("select * from users where id = ?",
//                new Object[]{id},   //SQL 에 바인딩할 파라미터 값 가변인자 대신 배열을 사용한다.
//                //ResultSet 한 로우의 결과를 오브젝트에 매핑해주는 RowMapper 콜백
//                new RowMapper<User>() {
//                    @Override
//                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
//                        User user = new User();
//                        user.setId(rs.getString("id"));
//                        user.setName(rs.getString("name"));
//                        user.setPassword(rs.getString("password"));
//                        return user;
//                    }
//                });
        return this.jdbcTemplate.queryForObject("select * from users where id = ?",
                new Object[] {id}, this.userMapper);
    }

//    public void deleteAll() throws SQLException {
//        Connection c = null;
//        PreparedStatement ps = null;
////        Connection c = dataSource.getConnection();
////        PreparedStatement ps = c.prepareStatement("delete from users");
//        try {
//            c = dataSource.getConnection();
////            ps = c.prepareStatement("delete from users"); -> 메소드 추출 방식 적용
////            ps = makeStatement(c); -> 템플릿 메소드 패턴의 적용
//
//            // 전략 패턴의 적용
//            StatementStrategy statementStrategy = new DeleteAllStatement();
//            ps = statementStrategy.makePrepareStatement(c);
//
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if(ps != null) {
//                try {
//                    ps.close();
//                } catch (SQLException ignored) {
//                }
//            }
//            if(c != null) {
//                try {
//                    c.close();
//                } catch (SQLException e) {
//                }
//            }
//        }
////        ps.close();
////        c.close();
//    }

// 메소드 추출 방식 적용
//    private PreparedStatement makeStatement(Connection c) throws SQLException {
//        PreparedStatement ps;
//        ps = c.prepareStatement("delete from users");
//        return ps;
//    }

    // 템플릿 메소드 패턴의 적용 ----[S]
    // UserDao 추상메소드로 변경 (슈퍼 클래스)
//    abstract protected PreparedStatement makeStatement(Connection c) throws SQLException;

// 서브 클래스
//public class UserDaoDeleteAll extends UserDao {
//
//    @Override
//    protected PreparedStatement makeStatement(Connection c) throws SQLException {
//        PreparedStatement ps = c.prepareStatement("delete from users");
//        return ps;
//    }
//}
    // 템플릿 메소드 패턴의 적용 ----[E]


    // DI 적용을 위한 클라이언트 / 컨텍스트 분리 ----[S]
    // 컨텍스트
//    public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException { //StatementStrategy stmt -> 클라이언트가 컨텍스트를 호출할 때 넘겨줄 전략 파라미터
//        Connection c = null;
//        PreparedStatement ps = null;
//
//        try {
//            c = dataSource.getConnection();
//            ps = stmt.makePrepareStatement(c);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (ps != null) {
//                try {
//                    ps.close();
//                } catch (SQLException e) {
//                }
//            }
//            if (c != null) {
//                try {
//                    c.close();
//                } catch (SQLException e) {
//                }
//            }
//        }
//    }

    // 클라이언트
//    public void deleteAll() throws SQLException {
//        StatementStrategy st = new DeleteAllStatement();    //선정한 전략 클래스의 오브젝트 생성
//        jdbcContextWithStatementStrategy(st);   //컨텍스트 호출, 전략 오브젝트 전달
//    }
    // DI 적용을 위한 클라이언트 / 컨텍스트 분리 ----[E]

    // DeleteAllStatement 클래스를 익명 내부 클래스로 이전 (jdbcContextWithStatementStrategy()메소드의 파라미터에서 바로 생성)
//    public void deleteAll() throws SQLException {
////        jdbcContextWithStatementStrategy(
////                new StatementStrategy() {
////                    @Override
////                    public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
////                        return c.prepareStatement("delete from users");
////                    }
////                }
////        );
////        this.jdbcContext.workWithStatementStrategy(
////                new StatementStrategy() {
////                    @Override
////                    public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
////                        return c.prepareStatement("delete from users");
////                    }
////                }
////        );
////        executeSql("delete from users");    // 변하지 않는 부분을 분리시킨 메소드를 이용하여 구현
////        this.jdbcContext.executeSql("delete from users");   // 분리시킨 메소드를 템플릿으로 이동 후 구현
//    }
    // JdbcTemplate 을 적용한 deleteAll() 메소드
    public void deleteAll() {
//        this.jdbcTemplate.update(new PreparedStatementCreator() {
//            @Override
//            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
//                return con.prepareStatement("delete from users");
//            }
//        });
        this.jdbcTemplate.update("delete from users");  // 내장콜백을 사용하는 update()로 변경한 deleteAll() 메소드
    }


    // 변하지 않는 부분을 분리시킨 메소드
//    private void executeSql(final String query) throws SQLException {
////        this.jdbcContext.workWithStatementStrategy(
////                new StatementStrategy() {
////                    @Override
////                    public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
////                        return c.prepareStatement(query);
////                    }
////                }
////        );
////    }



//    public int getCount() throws SQLException {
////        Connection c = dataSource.getConnection();
////        PreparedStatement ps = c.prepareStatement("select count(*) from users");
////        ResultSet rs = ps.executeQuery();
//        Connection c = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            c = dataSource.getConnection();
//
//            ps = c.prepareStatement("select count(*) from users");
//
//            rs = ps.executeQuery();
//            rs.next();
//            return rs.getInt(1);
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException e) {
//                }
//            }
//            if (ps != null) {
//                try {
//                    ps.close();
//                } catch (SQLException e) {
//                }
//            }
//            if (c != null) {
//                try {
//                    c.close();
//                } catch (SQLException e) {
//                }
//            }
//        }
////        int count = rs.getInt(1);
////        rs.close();
////        ps.close();
////        c.close();
////        return count;
//    }

    // 이중 콜백
//    public int getCount() {
//        return this.jdbcTemplate.query(new PreparedStatementCreator() { // 첫번째 콜백, Statement
//            @Override
//            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
//                return con.prepareStatement("select count(*) from users");
//            }
//        }, new ResultSetExtractor<Integer>() {  // 두번째 콜백. ResultSet 값 추출
//            @Override
//            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
//                rs.next();
//                return rs.getInt(1);
//            }
//        });
//    }

    // JdbcTemplate 이용
    public int getCount() {
        return this.jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
    }

    // query() 템플릿을 이용하는 getAll() 구현
    public List<User> getAll() {
//        return this.jdbcTemplate.query("select * from users order by id",
//                new RowMapper<User>() {
//                    @Override
//                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
//                        User user = new User();
//                        user.setId(rs.getString("id"));
//                        user.setName(rs.getString("name"));
//                        user.setPassword(rs.getString("password"));
//                        return user;
//                    }
//                });
        return this.jdbcTemplate.query("select * from users order by id", this.userMapper);
    }
}

