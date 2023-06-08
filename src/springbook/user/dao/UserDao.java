package springbook.user.dao;

import springbook.user.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    // 생성자 파라미터를 통해 전달 받은 런타임 의존관계를 갖는 오브젝트는 인스턴스 변수에 저장해둔다.
    private ConnectionMaker connectionMaker;
    // UserDao 오브젝트는 이제 생성자를 통해 주입받은 DConnectionMaker 오브젝트를 언제든지 사용하면 된다.
    // 반드시 인터페이스 타입의 파라미터 이어야만 한다.
    public UserDao(ConnectionMaker connectionMaker){
        this.connectionMaker = connectionMaker;
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

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.makeConnection();

        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?,?,?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.makeConnection();

        PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        c.close();

        return user;
    }
}

