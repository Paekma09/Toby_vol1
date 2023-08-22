package user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
@Transactional
//@TransactionConfiguration(defaultRollback=false) Deprecated 됨. 대신
@Rollback(false)    // 롤백 여부에 대한 기본 설정과 트랜잭션 매니저 빈을 지정하는데 사용할 수 있다. 디폴트 트랜잭션 매니저 아이디는 괜례를 따라서 transactionManager 로 되어 있다.
public class UserServiceTest {

    @Autowired
    UserService userService;

    // 같은 타입의 빈이 두 개 존재하기 때문에 필드 이름을 기준으로 주입될 빈이 결정된다. 자동 프록시 생성기에 의해 트랜잭션 부가기능이 testUserService 빈에 적용됐는지를 확인하는 것이 목적이다.
    @Autowired
    UserService testUserService;

//    @Autowired
//    UserServiceImpl userServiceImpl;
    @Autowired
    UserDao userDao;
    @Autowired
    MailSender mailSender;

    List<User> users;   // 테스트 픽스처

    /*
     * testUserService 빈을 사용하도록 수정된 테스트
     * */
    // 스프링 컨텍스트의 빈 설정을 변경하지 않으므로 @DirtiesContext 애노테이션은 제거됐다. 모든 테스트를 위한 DI 작업은 설정파일을 통해 서버에서 진행되므로 테스트 코드 자체는 단순해진다.
    @Test
    public void upgradeAllOrNothing() {
        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try {
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(1), false);
    }

    @Test
    public void advisorAutoProxyCreator() {
        assertThat(testUserService, is(java.lang.reflect.Proxy.class)); // 프록시호 변경된 오브젝트인지 확인한다.
    }

    // 트랜잭션 매니져를 이용해 트랜잭션을 미리 시작하게 만드는 테스트
//    @Test
//    public void transactionSync() {
//        // 트랜잭션 정의는 기본값을 사용한다.
//        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
//        txDefinition.setReadOnly(true); // 읽기 전용 트랜잭션으로 정의한다.
//        // 트랜잭션 매니져에게 트랜잭션을 요청한다. 기존에 시작된 트랜잭션이 없으니 새로운 트랜잭션을 시작시키고 트랜잭션 정보를 돌려준다. 동시에 만즐어진 트랜잭션을 다른 곳에서도 사용할 수 있도록 동기화 한다.
//        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
//
//        // 앞에서 만들어진 트랜잭션에 모두 참여한다.
//        userService.deleteAll();    // 테스트 코드에서 시작한 트랜잭션에 참여한다면 읽기전용 속성을 위반했으니 예외가 발생해야 한다.
//
//        userService.add(users.get(0));
//        userService.add(users.get(1));
//
//        // 앞에서 시작한 트랜잭션을 커밋한다.
//        transactionManager.commit(txStatus);
//    }

    // DAO 를 사용하는 트랜잭션 동기화 테스트
//    @Test
//    public void transactionSync() {
//        // 트랜잭션 정의는 기본값을 사용한다.
//        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
//        txDefinition.setReadOnly(true); // 읽기 전용 트랜잭션으로 정의한다.
//        // 트랜잭션 매니져에게 트랜잭션을 요청한다. 기존에 시작된 트랜잭션이 없으니 새로운 트랜잭션을 시작시키고 트랜잭션 정보를 돌려준다. 동시에 만즐어진 트랜잭션을 다른 곳에서도 사용할 수 있도록 동기화 한다.
//        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
//
//        // JdbcTemplate 을 통해 이미 시작된 트랜잭션이 있다면 자동으로 참여한다. 따라서 예외가 발생한다.
//        userDao.deleteAll();
//
//        // 앞에서 시작한 트랜잭션을 커밋한다.
//        transactionManager.commit(txStatus);
//    }

    // 트랜잭션의 롤백 테스트
//    @Test
//    public void transactionSync() {
//        // 트랜잭션을 롤백했을 때 돌아갈 초기 상태를 만들기 위해 트랜잭션 시작전에 초기화를 해둔다.
//        userDao.deleteAll();
//        assertThat(userDao.getCount(), is(0));
//
//        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
//        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
//
//        userService.add(users.get(0));
//        userService.add(users.get(1));
//        assertThat(userDao.getCount(), is(2));  // userDao 의 getCount() 메소드도 같은 트랜잭션에서 동작한다. add() 에 의해 두 개가 등록되었는지 확인해 둔다.
//
//        transactionManager.rollback(txStatus);  // 강제로 롤백한다. 트랜잭션 시작 전 상태로 돌아가야 한다.
//
//        assertThat(userDao.getCount(), is(0));  // add() 의 작업이 취소되고 트랜잭션 시작 이전의 상태임을 확인 할 수 있다.
//    }

    // 롤백 테스트
//    @Test
//    public void transactionSync() {
//        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
//        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);   // 테스트 안의 모든 작업을 하나의 트랜잭션으로 통합한다.
//
//        try {
//            // 테스트 안의 모든 작업을 하나의 트랜잭션으로 통합한다.
//            userService.deleteAll();
//            userService.add(users.get(0));
//            userService.add(users.get(1));
//        } finally {
//            // 테스트 결과가 어떻든 상관없이 테스트가 끝나면 무조건 롤백한다. 테스트 중에 발생했던 DB의 변경 사항은 모두 이전 상태로 복구된다.
//            transactionManager.rollback(txStatus);
//        }
//    }

    // 테스트에 적용된 @Transactional
    // 테스트 트랜잭션을 커밋시키도록 설정한 테스트
    @Test
    @Transactional
    @Rollback(value = false)
    public void transactionSync() {
            userService.deleteAll();
            userService.add(users.get(0));
            userService.add(users.get(1));
    }

    // 트랜잭션 적용 확인
//    @Test
//    @Transactional(readOnly = true)
//    public void transactionSync() {
//        userService.deleteAll();    // @Transactional 에 의해 시작된 트랜잭션에 참여하므로 읽기 전용 속성 위반으로 예외가 발생한다.
//    }


    /*
     * ProxyFactoryBean 을 이용한 트랜잭션 테스트
     * */
//    @Test
//    @DirtiesContext // 컨텍스트 설정을 변경하기 때문에 여전히 필요하다.
//    public void upgradeAllOrNothing() {
//        TestUserService testUserService = new TestUserService(users.get(3).getId());
//        testUserService.setUserDao(userDao);
//        testUserService.setMailSender(mailSender);
//
//        //userService 빈은 이제 스프링의 ProxyFactoryBean 이다.
//        ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
//        txProxyFactoryBean.setTarget(testUserService);
//        // FactoryBean 타입이므로 동일하게 getObject() 로 프록시를 가져온다.
//        UserService txUserService = (UserService) txProxyFactoryBean.getObject();
//
//        userDao.deleteAll();
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        try {
//            txUserService.upgradeLevels();
//            fail("TestUserServiceException expected");
//        } catch (TestUserServiceException e) {
//        }
//
//        checkLevelUpgraded(users.get(1), false);
//    }


    /*
     * 트랜잭션 프록시 팩토리 빈을 적용한 테스트
     * */
    @Autowired
    ApplicationContext context; // 팩토리 빈을 가져오려면 애플리케이션 컨텍스트가 필요하다.

//    @Test
//    @DirtiesContext // 다이내믹 프록시 팩토리 빈을 직접 만들어 사용할 때는 없앴다가 다시 등장한 컨텍스트 무효화 애노테이션
//    public void upgradeAllOrNothing() throws Exception {
//        TestUserService testUserService = new TestUserService(users.get(3).getId());
//        testUserService.setUserDao(userDao);
//        testUserService.setMailSender(mailSender);
//
//        // 팩토리 빈 자체를 가져와야 하므로 빈 이름에 & 를 반드시 넣는다.
//        TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", TxProxyFactoryBean.class);  // 테스트용 타깃 주입
//        txProxyFactoryBean.setTarget(testUserService);
//        UserService txUserService = (UserService) txProxyFactoryBean.getObject();   // 변경된 타깃 설정을 이용해서 트랜잭션 다이내믹 프록시 오브젝트를 다시 생성한다.
//
//        userDao.deleteAll();
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        try {
//            txUserService.upgradeLevels();
//            fail("TestUserServiceException expected");
//        } catch (TestUserServiceException e) {
//        }
//
//        checkLevelUpgraded(users.get(1), false);
//    }

    /*
    * 트랜잭션 매니저를 수동 DI 하도록 수정한 테스트
    * */
    @Autowired
    PlatformTransactionManager transactionManager;

//    @Test
//    public void upgradeAllOrNothing() throws Exception {
//        TestUserService testUserService = new TestUserService(users.get(3).getId());
//        testUserService.setUserDao(userDao);
//        testUserService.setMailSender(mailSender);
//
//        // 다이내믹 프록시를 이용한 트랜잭션 테스트
//        TransactionHandler txHandler = new TransactionHandler();
//        // 트랜잭션 핸들러가 필요한 정보와 오브젝트를 DI 해준다.
//        txHandler.setTarget(testUserService);
//        txHandler.setTransactionManager(transactionManager);
//        txHandler.setPattern("upgradeLevels");
//        // UserService 인터페이스 타입의 다이내믹 프록시 생성
//        UserService txUserService = (UserService) Proxy.newProxyInstance(
//                getClass().getClassLoader(), new Class[] { UserService.class }, txHandler
//        );
//
//        // 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로 수정할 필요가 없으니 그대로 사용한다.
////        UserServiceTx txUserService = new UserServiceTx();
////        txUserService.setTransactionManager(transactionManager);
////        txUserService.setUserService(testUserService);
//
////        testUserServiceImpl.setTransactionManager(transactionManager);  // userService 빈의 프로퍼티 설정과 동일한 수동 DI
////        testUserServiceImpl.setMailSender(mailSender);
//
//        userDao.deleteAll();
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        try {
//            // TestUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
////            testUserServiceImpl.upgradeLevels();
//            txUserService.upgradeLevels();  // 트랜잭션 기능을 분리한 오브젝트를 통해 예외 발생용 TestUserService 가 호출되게 해야 한다.
//            fail("TestUserServiceException expected");
//        } catch (TestUserServiceException e) {  // TestUserService 가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패
//        }
//        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인
//        checkLevelUpgraded(users.get(1), false);
//    }
    /*
    *
    * */

    /*
    * 동기화가 적용된 UserService 에 따라 수정된 테스트
    * */
//    @Autowired
//    DataSource dataSource;
//
//    @Test
//    public void upgradeAllOrNothing() throws Exception {
//        UserService testUserService = new TestUserService(users.get(3).getId());
//        testUserService.setUserDao(this.userDao);
//        testUserService.setDataSource(this.dataSource);
//
//        userDao.deleteAll();
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        try {
//            // TestUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
//            testUserService.upgradeLevels();
//            fail("TestUserServiceException expected");
//        } catch (TestUserServiceException e) {  // TestUserService 가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패
//        }
//        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인
//        checkLevelUpgraded(users.get(1), false);
//    }
    /*
    *
    * */

    // userService 빈의 주입을 확인하는 테스트
    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
    }

    // 상수 사용
    @Before
    public void setUp() {
        users = Arrays.asList(  // 배열을 리스트로 만들어주는 편리한 메소드, 배열을 가변인자로 넣어주면 더욱 편리하다.
                // 테스트에서는 가능한 한 경계값을 사용하는 것이 좋다.
                new User("kym57", "고영미", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "paekma09@gmail.com"),
                new User("paekma09", "남기준", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "paekma09@gmail.com"),
                new User("nambo81", "남보영", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "paekma09@gmail.com"),
                new User("chubss", "강성희", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "paekma09@gmail.com"),
                new User("roy", "로이", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "paekma09@gmail.com")
        );
    }

    /*
     * Mockito 를 적용한 테스트 코드
     * */
    @Test
    public void mockUpgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        // 다이내믹한 목 오브젝트 생성과 메소드의 리턴 값 설정, 그리고 DI 까지 세 줄이면 충분하다.
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        // 리턴 값이 없는 메소드를 가진 목 오브젝트는 더욱 간단하게 만들 수 있다.
        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();
        // 목 오브젝트가 제공하는 검증 기능을 통해서 어떤 메소드가 몇 번 호출 됐는지, 파라미터는 무엇인지 확인할 수 있다.
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(), is(Level.GOLD));

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        // 파라미터를 정밀하게 검사하기 위해 캡처할 수도 있다.
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
    }


    /*
     * MockUserDao 를 사용해서 만든 고립된 테스트
     * */
    @Test
    public void upgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();    // 고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성하면 된다.

        // 목 오브젝트로 만든 UserDao 를 직접 DI 해준다.
        MockUserDao mockUserDao = new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();  // MockUserDao 로부터 업데이트 결과를 가져온다.

        // 업데이트 횟수와 정보를 확인한다.
        assertThat(updated.size(), is(2));
        checkUserAndLevel(updated.get(0), "paekma09", Level.SILVER);
        checkUserAndLevel(updated.get(1), "chubss", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
        assertThat(request.get(0), is(users.get(1).getEmail()));
        assertThat(request.get(1), is(users.get(3).getEmail()));
    }

    // id와 level 을 확인하는 간단한 헬퍼 메소드
    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId(), is(expectedId));
        assertThat(updated.getLevel(), is(expectedLevel));
    }



    // 메일 발송 대상을 확인하는 upgradeLevels 테스트
//    @Test
//    @DirtiesContext // 컨텍스트의 DI 설정을 변경하는 테스트라는 것을 알려준다.
//    public void upgradeLevels() throws Exception {
//        userDao.deleteAll();
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        // 메일 발송 결과를 테스트 할수 있도록 목 오브젝트를 만들어 userService 의 의존 오브젝트로 주입해준다.
//        MockMailSender mockMailSender = new MockMailSender();
//        userServiceImpl.setMailSender(mockMailSender);
//
//        // 업그레이드 테스트, 메일 발송이 일어나면 MockMailSender 오브젝트의 리스트에 그 결과가 저장된다.
//        userService.upgradeLevels();
//
//        checkLevelUpgraded(users.get(0), false);
//        checkLevelUpgraded(users.get(1), true);
//        checkLevelUpgraded(users.get(2), false);
//        checkLevelUpgraded(users.get(3), true);
//        checkLevelUpgraded(users.get(4), false);
//
//        // 목 오브젝트에 저장된 메일 수신자 목록을 가져와 업그레이드 대상과 일치하는지 확인 한다.
//        List<String> request = mockMailSender.getRequests();
//        assertThat(request.size(), is(2));
//        assertThat(request.get(0), is(users.get(1).getEmail()));
//        assertThat(request.get(1), is(users.get(3).getEmail()));
//    }

    // checkLevelUpgraded() 메소드 이용하여 사용자 레벨 업그레이드 테스트 (개선)
//    @Test
//    public void upgradeLevels() throws Exception {
//        userDao.deleteAll();
//
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        userService.upgradeLevels();
//
//        checkLevelUpgraded(users.get(0), false);
//        checkLevelUpgraded(users.get(1), true);
//        checkLevelUpgraded(users.get(2), false);
//        checkLevelUpgraded(users.get(3), true);
//        checkLevelUpgraded(users.get(4), false);
//    }

    // 사용자 레벨 업그레이드 테스트
//    @Test
//    public void upgradeLevels() {
//        userDao.deleteAll();
//
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        userService.upgradeLevels();
//
//        // 각 사용자별로 업그레이드 후의 예상 레벨을 검증한다.
//        checkLevel(users.get(0), Level.BASIC);
//        checkLevel(users.get(1), Level.SILVER);
//        checkLevel(users.get(2), Level.SILVER);
//        checkLevel(users.get(3), Level.GOLD);
//        checkLevel(users.get(4), Level.GOLD);
//    }

    // 어떤 레벨로 바뀔 것인가가 아니라, 다음 레벨로 업그레이드 될 것인가 아닌가를 지정한다.
    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            // 업그레이드가 일어났는지 확인
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel())); // 다음 레벨이 무엇인지는 Level 에게 물어보면 된다.
        } else {
            // 업그레이드가 일어나지 않았는지 확인
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

    // DB 에서 사용자 정보를 가져와 레벨을 확인하는 코드가 중복되므로 헬퍼 메소드로 분리했다.
    private void checkLevel(User user, Level expectedLevel) {
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel(), is(expectedLevel));
    }

    // add()메소드의 테스트
    @Test
    @Rollback   // 메소드에서 디폴트 설정과 그 밖의 롤백 방법으로 재설정할 수 있다.
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);  // GOLD 레벨 ---> GOLD 레벨이 이미 지정된 User 라면 레벨을 초기화하지 않아야 한다.
        // 레벨이 비어 있는 사용자, 로직에 따라 등록 중에 BASIC 레벨도 설정 되어야 한다.
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        // Db 에 저장된 결과를 가져와 확인 한다.
        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
    }

    // 예외 발생시 작업 취소 여부 테스트
//    @Test
//    public void upgradeAllOrNothing() {
//        UserService testUserService = new TestUserService(users.get(3).getId());    // 예외를 발생시킬 네 번째 사용자의 id를 넣어서 테스트용 UserService 대역 오브젝트를 생성한다.
//        testUserService.setUserDao(this.userDao);   // userDao 를 수동 DI 해준다.
//
//        userDao.deleteAll();
//        for (User user : users) {
//            userDao.add(user);
//        }
//
//        try {
//            // TestUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
//            testUserService.upgradeLevels();
//            fail("TestUserServiceException expected");
//        } catch (TestUserServiceException e) {  // TestUserService 가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패
//        }
//        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인
//        checkLevelUpgraded(users.get(1), false);
//    }

    // 수정한 테스트용 UserService 구현 클래스
//    static class TestUserServiceImpl extends UserServiceImpl {  // 포인트컷의 클래스 필터에 선정되도록 이름 변경.
//        private String id = "chubss";   // 테스트 픽스처의 users(3)의 id 값을 고정시켜버렸다.
//
//        protected void upgradeLevel(User user) {
//            if (user.getId().equals(this.id)) throw new TestUserServiceException();
//            super.upgradeLevel(user);
//        }
//    }

    // 읽기 전용 속성 테스트
//    @Test   // 일단은 언떤 예외가 던저질지 모르기 때문에 expected 없이 테스트를 작성한다.
    @Test(expected = TransientDataAccessException.class)    // 예외 확인 테스트로 수정
    public void readOnlyTransactionAttribute() {
        testUserService.getAll();   // 트랜잭션 속성이 제대로 적용 됐다면 여기서 읽기 전용 속성을 위반했기 때문에 예외가 발생해야 한다.
    }

    // 읽기 전용 메소드에 쓰기 작업을 추가한 테스트용 클래스
    static class TestUserService extends UserServiceImpl {
        private String id = "chubss";

        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }

        // 읽기 전용 트랜잭션의 대상인 get 으로 시작하는 메소드를 오버라이드한다.
        public List<User> getAll() {
            for (User user : super.getAll()) {
                super.update(user); // 강제로 쓰기 시도를 한다. 여기서 읽기 전용 속성으로 인한 예외가 발생해야 한다.
            }
            return null;    // 메소드가 끝나기 전에 예외가 발생해야 하니 리턴 값은 별 의미가 없다. 적당한 값을 넣어서 컴파일만 되게 한다.
        }
    }


    // UserService 의 테스트용 대역 클래스
//    static class TestUserService extends UserServiceImpl {
//        private String id;
//
//        private TestUserService(String id) {    //예외를 발생시킬 User 오브젝트의 id 를 지정할 수 있게 만든다.
//            this.id = id;
//        }
//
//        // UserService 의 메소드를 오버라이드 한다.
//        protected void upgradeLevel(User user) {
//            if (user.getId().equals(this.id)) { // 지정된 id의 User 오브젝트가 발견되면 예외를 던져서 작업을 강제로 중단시킨다.
//                throw new TestUserServiceException();
//            }
//            super.upgradeLevel(user);
//        }
//    }

    // 테스트용 예외
    static class TestUserServiceException extends RuntimeException {
    }

    /*
     * 목 오브젝트로 만든 메일 전송 확인용 클래스
     * */
    static class MockMailSender implements MailSender {
        // UserService 로부터 전송 요청을 받은 메일 주소를 저장해두고 이를 읽을 수 있게 한다.
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]);   // 전송 요청을 받은 이메일 주소를 저장해둔다. 간단하게 첫번째 수신자 메일 주소만 저장 했다.
        }

        @Override
        public void send(SimpleMailMessage... mailMessages) throws MailException {

        }
    }

    /*
     * UserDao 오브젝트
     * */
    static class MockUserDao implements UserDao {
        private List<User> users;   // 레벨 업그레이드 후보 User 오브젝트 목록
        private List<User> updated = new ArrayList<>(); // 업그레이드 대상 오브젝트를 저장해둘 목록

        public MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated() {
            return updated;
        }

        // 스텁기능 제공
        @Override
        public List<User> getAll() {
            return this.users;
        }

        // 목 오브젝트 기능 제공
        @Override
        public void update(User user) {
            updated.add(user);
        }


        // 테스트에 사용되지 않는 메소드
        @Override
        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            throw new UnsupportedOperationException();
        }

    }
}
