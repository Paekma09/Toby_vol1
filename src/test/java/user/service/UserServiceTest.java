package user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    UserServiceImpl userServiceImpl;
    @Autowired
    UserDao userDao;
    @Autowired
    MailSender mailSender;

    List<User> users;   // 테스트 픽스처

    /*
    * 트랜잭션 매니저를 수동 DI 하도록 수정한 테스트
    * */
    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    public void upgradeAllOrNothing() throws Exception {
        TestUserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(userDao);
        testUserService.setMailSender(mailSender);

        // 다이내믹 프록시를 이용한 트랜잭션 테스트
        TransactionHandler txHandler = new TransactionHandler();
        // 트랜잭션 핸들러가 필요한 정보와 오브젝트를 DI 해준다.
        txHandler.setTarget(testUserService);
        txHandler.setTransactionManager(transactionManager);
        txHandler.setPattern("upgradeLevels");
        // UserService 인터페이스 타입의 다이내믹 프록시 생성
        UserService txUserService = (UserService) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[] { UserService.class }, txHandler
        );

        // 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로 수정할 필요가 없으니 그대로 사용한다.
//        UserServiceTx txUserService = new UserServiceTx();
//        txUserService.setTransactionManager(transactionManager);
//        txUserService.setUserService(testUserService);

//        testUserServiceImpl.setTransactionManager(transactionManager);  // userService 빈의 프로퍼티 설정과 동일한 수동 DI
//        testUserServiceImpl.setMailSender(mailSender);

        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try {
            // TestUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
//            testUserServiceImpl.upgradeLevels();
            txUserService.upgradeLevels();  // 트랜잭션 기능을 분리한 오브젝트를 통해 예외 발생용 TestUserService 가 호출되게 해야 한다.
            fail("TestUserServiceException expected");
        } catch (TestUserServiceException e) {  // TestUserService 가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패
        }
        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인
        checkLevelUpgraded(users.get(1), false);
    }
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


    // UserService 의 테스트용 대역 클래스
    static class TestUserService extends UserServiceImpl {
        private String id;

        private TestUserService(String id) {    //예외를 발생시킬 User 오브젝트의 id 를 지정할 수 있게 만든다.
            this.id = id;
        }

        // UserService 의 메소드를 오버라이드 한다.
        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) { // 지정된 id의 User 오브젝트가 발견되면 예외를 던져서 작업을 강제로 중단시킨다.
                throw new TestUserServiceException();
            }
            super.upgradeLevel(user);
        }
    }

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
