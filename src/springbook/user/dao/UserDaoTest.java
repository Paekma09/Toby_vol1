package springbook.user.dao;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import springbook.user.domain.User;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserDaoTest {

    @Test
    public void addAndGet() throws SQLException {
        // static 메소드인 main() 에서는 DI를 이용해 오브젝트를 주입받을 방법이 없기 때문에 의존관계 검색 방식을 사용
        // ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);

        // xml 이용하여 의존 관계 주입
         ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");

        UserDao dao = context.getBean("userDao", UserDao.class);

        User user1 = new User("paekma09", "남기준", "KHS0909");
        User user2 = new User("ksh0909", "강성희", "kj1116");

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
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");

        UserDao dao = context.getBean("userDao", UserDao.class);
        User user1 = new User("paekma09", "남기준", "KSH0909");
        User user2 = new User("ksh0909", "강성희", "kj1116");
        User user3 = new User("kjsh09", "기준성희", "79840908");

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
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");

        UserDao dao = context.getBean("userDao", UserDao.class);
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }
}
