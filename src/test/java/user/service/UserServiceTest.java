package user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserService.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {

    @Autowired
    UserService userService;
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
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(userDao);
        testUserService.setTransactionManager(transactionManager);  // userService 빈의 프로퍼티 설정과 동일한 수동 DI
        testUserService.setMailSender(mailSender);

        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try {
            // TestUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
            testUserService.upgradeLevels();
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

    // 메일 발송 대상을 확인하는 upgradeLevels 테스트
    @Test
    @DirtiesContext // 컨텍스트의 DI 설정을 변경하는 테스트라는 것을 알려준다.
    public void upgradeLevels() throws Exception {
        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        // 메일 발송 결과를 테스트 할수 있도록 목 오브젝트를 만들어 userService 의 의존 오브젝트로 주입해준다.
        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        // 업그레이드 테스트, 메일 발송이 일어나면 MockMailSender 오브젝트의 리스트에 그 결과가 저장된다.
        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        // 목 오브젝트에 저장된 메일 수신자 목록을 가져와 업그레이드 대상과 일치하는지 확인 한다.
        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
        assertThat(request.get(0), is(users.get(1).getEmail()));
        assertThat(request.get(1), is(users.get(3).getEmail()));
    }

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
    static class TestUserService extends UserService {
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
}
