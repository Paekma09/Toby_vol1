package springbook.user.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class DaoFactory {
    // ConnectionMaker 타입의 오브젝트를 결정하고 이를 생성한 후에 UserDao의 생성자 파라미터로 주입

    /*
    // 생성자를 이용한 의존관계 주입
    @Bean
    public UserDao userDao(){
        return new UserDao(connectionMaker());
    }
    */

    // 수정자를 이용한 의존관계 주입
    @Bean
    public UserDao userDao(){
        UserDao userDao = new UserDao();
        userDao.setConnectionMaker(connectionMaker());
        return userDao;
    }

    @Bean
    public AccountDao accountDao(){
        return new AccountDao(connectionMaker());
    }

    @Bean
    public MessageDao messageDao(){
        return new MessageDao(connectionMaker());
    }

    // ConnectionMaker 타입의 오브젝트를 결정하고 이를 생성한 후에 UserDao의 생성자 파라미터로 주입
    @Bean
    public ConnectionMaker connectionMaker(){
        return new DConnectionMaker();
    }

    /*
    // 개발용 ConnectionMaker 생성 코드
    @Bean
    public ConnectionMaker connectionMaker(){
        return new LocalDBConnectionMaker();
    }
    */

    /*
    // 운영용 ConnectionMaker 생성 코드
    @Bean
    public ConnectionMaker connectionMaker(){
        return new ProductionDBConnectionMaker();
    }
    */

}
