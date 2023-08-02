package user.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import user.domain.User;

// 위임기능을 가진 UserServiceTx 클래스
public class UserServiceTx implements UserService{

    // UserService 를 구현한 다른 오브젝트를 DI 받는다.
    UserService userService;    // 타깃 오브젝트
    PlatformTransactionManager transactionManager;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    // DI 받은 UserService 오브젝트에 모든 기능을 위임한다. (메소드 구현과 위임)
    @Override
    public void add(User user) {
        userService.add(user);
    }

    @Override
    public void upgradeLevels() {   // 메소드 구현
        // 부가기능 수행
        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 위임
            userService.upgradeLevels();
            // 부가기능 수행
            this.transactionManager.commit(status);
        } catch (RuntimeException e) {
            this.transactionManager.rollback(status);
            throw e;
        }
    }
}
