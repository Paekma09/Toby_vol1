package springbook.user.dao;

public class DaoFactory {
    public UserDao userDao(){
        return new UserDao(connectionMaker());
    }

    public AccountDao accountDao(){
        return new AccountDoa(connectionMaker());
    }

    public MessageDao messageDao(){
        return new MessageDao(connectionMaker());
    }

    public ConnectionMaker connectionMaker(){
        return new DConnectionMaker();
    }
}
