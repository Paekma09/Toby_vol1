package user.service;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

import java.util.List;

@Transactional
public class UserServiceImpl implements UserService{
    // 상수의 도입
    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_FOR_GOLD = 30;


    /*
     * 트랜잭션 코드를 제거한 UserService 구현 클래스
     * */
    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for (User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }
    }

    /*
    * 트랜잭션 매니저를 빈으로 분리시킨 UserService
    * */
//    private PlatformTransactionManager transactionManager;
//
//    public void setTransactionManager(PlatformTransactionManager transactionManager) {
//        this.transactionManager = transactionManager;   // 프로퍼티 이름은 관례를 따라 transactionManager 라고 만드는 것이 편리하다.
//    }

//    public void upgradeLevels() {
//        // DI 받은 트랜잭션 매니저를 공유해서 사용한다. 멀티스레드 환경에서도 안전함.
//        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
//
//        try {
//            List<User> users = userDao.getAll();
//            for (User user : users) {
//                if (canUpgradeLevel(user)) {
//                    upgradeLevel(user);
//                }
//            }
//            this.transactionManager.commit(status);
//        } catch (RuntimeException e) {
//            this.transactionManager.rollback(status);
//            throw e;
//        }
//    }

    /*
     * 비지니스 로직과 트랜잭션 경계설정의 분리
     * */
//    public void upgradeLevels() throws Exception {
//        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
//
//        try {
//            upgradeLevelsInternal();
//            this.transactionManager.commit(status);
//        } catch (Exception e) {
//            this.transactionManager.rollback(status);
//            throw e;
//        }
//    }

    // 분리된 비지니스 로직 코드, 트랜잭션을 적용하기 전과 동일하다.
//    private void upgradeLevelsInternal() {
//        List<User> users = userDao.getAll();
//        for (User user : users) {
//            if (canUpgradeLevel(user)) {
//                upgradeLevel(user);
//            }
//        }
//    }


    /*
    * 트랜잭션 동기화 방식을 적용한 UserService
    * */
    // Connection 을 생성할때 사용 할 DataSource 를 DI 받도록 한다.
//    private DataSource dataSource;
//
//    public void setDataSource(DataSource dataSource) {
//        this.dataSource = dataSource;
//    }

//    public void upgradeLevels() throws Exception {
//        TransactionSynchronizationManager.initSynchronization();    //트랜잭션 동기화 관리자를 이용해 동기화 작업을 초기화 한다.
//        // DB 커넥션을 생성하고 트랜잭션을 시작한다. 이후의 DAO 작업은 모두 여기서 시자간 트랜잭션 안에서 진행된다.
//        Connection c = DataSourceUtils.getConnection(dataSource);   // DB 커넥션 생성과 동기화를 함께 해주는 유틸리티 메소드
//        c.setAutoCommit(false);
//
//        try {
//            List<User> users = userDao.getAll();
//            for (User user : users) {
//                if (canUpgradeLevel(user)) {
//                    upgradeLevel(user);
//                }
//            }
//            c.commit(); // 정상적으로 작업을 마치면 트랜잭션 커밋
//        } catch (Exception e) {
//            c.rollback();   // 예외가 발생하면 롤백한다.
//            throw e;
//        } finally {
//            DataSourceUtils.releaseConnection(c, dataSource);   // 스프링 유틸리티 메소드를 이용해 DB 커넥션을 안전하게 닫는다.
//            // 동기화 작업 종료 및 정리
//            TransactionSynchronizationManager.unbindResource(this.dataSource);
//            TransactionSynchronizationManager.clearSynchronization();
//        }
//    }
    /*
    *
    * */

    /*
     * 스프링의 트랜잭션 추상화 API 를 적용한 upgradeLevels()
     * */
//    public void upgradeLevels() {
//        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);   // JDBC 트랜잭션 추상 오브젝트 생성
//        // 트랜잭션 시작
//        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
//
//        try {
//            // 트랜잭션 안에서 진행되는 작업
//            List<User> users = userDao.getAll();
//            for (User user : users) {
//                if (canUpgradeLevel(user)) {
//                    upgradeLevel(user);
//                }
//            }
//            transactionManager.commit(status);  // 트랜잭션 커밋
//        } catch (RuntimeException e) {
//            transactionManager.rollback(status);    // 트랜잭션 롤백
//            throw e;
//        }
//    }

    UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    // 기본 작업 흐름만 남겨둔 upgradeLevels()
//    public void upgradeLevels() {
//        List<User> users = userDao.getAll();
//        for (User user : users) {
//            if (canUpgradeLevel(user)) {
//                upgradeLevel(user);
//            }
//        }
//    }

    // 업그레이드 가능 확인 메소드 - 상수 도입
    private boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();
        switch (currentLevel) {
            case BASIC: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
            case SILVER: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
            case GOLD: return false;
            default: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
        }
    }

    // 업그레이드 가능 확인 메소드
//    private boolean canUpgradeLevel(User user) {
//        Level currentLevel = user.getLevel();
//        // 레벨별로 구분해서 조건을 판단한다.
//        switch (currentLevel) {
//            case BASIC: return (user.getLogin() >= 50);
//            case SILVER: return (user.getRecommend() >= 30);
//            case GOLD: return false;
//            // 현재 로직에서 다룰 수 없는 레벨이 주어지면 예외를 발생시킨다. 새로운 레벨이 추가되고 로직을 수정하지 않으면 에러가 나서 확인할 수 있다.
//            default: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
//        }
//    }

    // 간결해진 upgradeLevel() ---> 테스트용 UserService 서브클래스에서 오버라이딩으로 인한 접근권한 변경 (private -> protected)
    protected void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEMail(user);
    }

    // 사용자 신규 등록 로직을 담은 add() 메소드
    public void add(User user) {
        if (user.getLevel() == null) {
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }

    /*
    * DAO 로 위임하도록 만든다. 필요한 부가 로직을 넣어도 좋다.
    * */
    @Override
    public User get(String id) {
        return userDao.get(id);
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll();
    }

    @Override
    public void deleteAll() {
        userDao.deleteAll();
    }

    @Override
    public void update(User user) {
        userDao.update(user);
    }

    // 레벨 업그레이드 작업 메소드
//    private void upgradeLevel(User user) {
//        if (user.getLevel() == Level.BASIC) {
//            user.setLevel(Level.SILVER);
//        } else if (user.getLevel() == Level.SILVER) {
//            user.setLevel(Level.GOLD);
//        }
//        userDao.update(user);
//    }

    // 사용자 레벨 업그레이드 메소드
//    public void upgradeLevels() {
//        List<User> users = userDao.getAll();
//        for (User user : users) {
//            Boolean changed = null; // 레벨의 변화가 있는지를 확인하는 플래그
//            // BASIC 레벨 업그레이드 작업
//            if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) {
//                user.setLevel(Level.SILVER);
//                changed = true; // 레벨 변경 플래그 설정
//            // SILVER 레벨 업그레이드 작업
//            } else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
//                user.setLevel(Level.GOLD);
//                changed = true; // 레벨 변경 플래그 설정
//            } else if (user.getLevel() == Level.GOLD) {
//                changed = false;    // GOLD 레벨은 변경이 일어나지 않는다.
//            } else {
//                changed = false;    // 일치하는 조건이 없으면 변경 없음
//            }
//
//            // 레벨의 변경이 있는 경우에만 update() 호출
//            if (changed) {
//                userDao.update(user);
//            }
//        }
//    }

    // JavaMail 을 이용한 메일 발송 메소드
//    private void sendUpgradeEMail(User user) {
//        Properties props = new Properties();
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        Session s = Session.getInstance(props, null);
//
//        MimeMessage message = new MimeMessage(s);
//
//        try {
//            message.setFrom(new InternetAddress("paekma09@gmail.com"));
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
//            message.setSubject("Upgrade 안내");
//            message.setText("사용자님의 등급이  " + user.getLevel().name() + "로 업그레이드 되었습니다.");
//            Transport.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // 스프링의 MailSender 를 이용한 메일 발송 메소드
//    private void sendUpgradeEMail(User user) {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();   // MailSender 구현 클래스의 오브젝트를 생성한다.
//        mailSender.setHost("mail.server.com");
//
//        // MailMessage 인터페이스의 구현 클래스 오브젝트를 만들어 메일 내용을 작성한다.
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailMessage.setTo(user.getEmail());
//        mailMessage.setFrom("paekma09@gmail.com");
//        mailMessage.setSubject("Upgrade 안내");
//        mailMessage.setText("사용자님의 등급이 " + user.getLevel().name());
//
//        mailSender.send(mailMessage);
//    }

    /*
     * 메일 전송 기능을 가진 오브젝트를 DI 받도록 수정한 UserService
     * */
    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendUpgradeEMail(User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("paekma09@gmail.com");
        mailMessage.setSubject("Upgrade 안내");
        mailMessage.setText("사용자님의 등급이 " + user.getLevel().name());

        this.mailSender.send(mailMessage);
    }

}
