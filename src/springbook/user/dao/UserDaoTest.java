package springbook.user.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTest {

//    @Autowired
//    private ApplicationContext context;
    @Autowired
    UserDao dao;
    @Autowired
    DataSource dataSource;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() {
//        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
//        this.dao = this.context.getBean("userDao", UserDao.class);
        this.user1 = new User("paekma09", "남기준", "KHS0909");
        this.user2 = new User("ksh0909", "강성희", "kj1116");
        this.user3 = new User("kjsh09", "기준성희", "79840908");

//        System.out.println(this.context);
//        System.out.println(this);
    }

    @Test
    public void addAndGet() throws SQLException {
        // static 메소드인 main() 에서는 DI를 이용해 오브젝트를 주입받을 방법이 없기 때문에 의존관계 검색 방식을 사용
        // ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);

        // xml 이용하여 의존 관계 주입
//        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
//
//        UserDao dao = context.getBean("userDao", UserDao.class);

//        User user1 = new User("paekma09", "남기준", "KHS0909");
//        User user2 = new User("ksh0909", "강성희", "kj1116");

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userget1 = dao.get(user1.getId());
        assertThat(userget1.getName(), is(user1.getName()));
        assertThat(userget1.getPassword(), is(user1.getPassword()));

        User usetget2 = dao.get(user2.getId());
        assertThat(usetget2.getName(), is(user2.getName()));
        assertThat(usetget2.getPassword(), is(user2.getPassword()));

//        dao.add(user);
//        assertThat(dao.getCount(), is(1));

//        System.out.println(user.getId() + " 등록 성공");

//        User user2 = dao.get(user.getId());
//
//        assertThat(user2.getName(), is(user.getName()));
//        assertThat(user2.getPassword(), is(user.getPassword()));

//        if(!user.getName().equals(user2.getName())){
//            System.out.println("테스트 실패 (name)");
//        } else if (!user.getPassword().equals(user2.getPassword())) {
//            System.out.println("테스트 실패 (password)");
//        } else {
//            System.out.println("조회 테스트 성공");
//        }
//        System.out.println(user2.getName());
//        System.out.println(user2.getPassword());
//
//        System.out.println(user2.getId() + " 조회 성공");
    }

//    public static void main(String[] args) {
//        JUnitCore.main("springbook.user.dao.UserDaoTest");
//    }

    @Test
    public void count() throws SQLException {
//        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
//
//        UserDao dao = context.getBean("userDao", UserDao.class);
//        User user1 = new User("paekma09", "남기준", "KSH0909");
//        User user2 = new User("ksh0909", "강성희", "kj1116");
//        User user3 = new User("kjsh09", "기준성희", "79840908");

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }

    @Test(expected = EmptyResultDataAccessException.class) // 테스트 중에 발생할 것으로 기대하는 예외 클래스를 지정 해준다.
    public void getUserFailure() throws SQLException {
//        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
//
//        UserDao dao = context.getBean("userDao", UserDao.class);
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }

    @Test
    public void getAll() {
        dao.deleteAll();

        List<User> users0 = dao.getAll();
        assertThat(users0.size(), is(0));   //데이터가 없을 때 크기가 0인 리스트 오브젝트가 리턴되어야 한다.

        dao.add(user1); //ID : paekma09
        List<User> users1 = dao.getAll();
        assertThat(users1.size(), is(1));
        checkSameUser(user1, users1.get(0));

        dao.add(user2); //ID : ksh0909
        List<User> users2 = dao.getAll();
        assertThat(users2.size(), is(2));
        checkSameUser(user2, users2.get(0));
        checkSameUser(user1, users2.get(1));

        dao.add(user3); //ID : kjsh09
        List<User> users3 = dao.getAll();
        assertThat(users3.size(), is(3));
        checkSameUser(user3, users3.get(0));    //user3의 id 값이 알파벳순으로 가장 빠르므로 getAll()의 첫번째 엘리먼트여야 한다.
        checkSameUser(user2, users3.get(1));
        checkSameUser(user1, users3.get(2));
    }

    //User 오브젝트의 내용을 비교하는 검증 코드, 테스트에서 반복적으로 사용되므로 분리해 놓았다.
    public void checkSameUser(User user1, User user2) {
        assertThat(user1.getId(), is(user2.getId()));
        assertThat(user1.getName(), is(user2.getName()));
        assertThat(user1.getPassword(), is(user2.getPassword()));
    }

    //DataAccessException 에 대한 테스트
//    @Test(expected = DataAccessException.class)
    @Test(expected = DuplicateKeyException.class)
    public void duplicateKey() {
        dao.deleteAll();

        dao.add(user1);
        dao.add(user1); //강제로 같은 사용자를 두 번 등록한다. 여기서 예외가 발생해야 한다.
    }

    //SQLException 전환 기능의 학습 테스트
    @Test
    public void sqlExceptionTranslate() {
        dao.deleteAll();

        try {
            dao.add(user1);
            dao.add(user1);
        } catch (DuplicateKeyException ex) {
            SQLException sqlEx = (SQLException) ex.getRootCause();
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);   //코드를 이용한 SQLException 의 전환

            assertThat(set.translate(null,null, sqlEx), instanceOf(DuplicateKeyException.class));   //에러 메세지를 만들때 사용하는 정보이므로 null 로 넣어도 된다.
        }
    }
}
