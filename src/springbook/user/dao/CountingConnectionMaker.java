package springbook.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class CountingConnectionMaker implements ConnectionMaker{
    int counter = 0;
    private ConnectionMaker realConnectionMaker;

    public CountingConnectionMaker(ConnectionMaker realConnectionMaker){
        this.realConnectionMaker = realConnectionMaker;
    }
    // Dao 가 DB 커넥션을 가져올 때마다 호출하는 makeConnection() 에서 DB 연결 횟수 카운터를 증가시킨다.
    // CountingConnectionMaker 는 자신의 관심사인 DB 연결 횟수 카운팅 작업을 마치면
    // 실제 DB 커넥션을 만들어주는 realConnectionMaker 에 저장된 ConnectionMaker 타입 오브젝트의
    // makeConnection() 을 호출해서 그 결과를 Dao 에 돌려준다.
    @Override
    public Connection makeConnection() throws ClassNotFoundException, SQLException {
        this.counter++;
        return realConnectionMaker.makeConnection();
    }

    public int getCounter() {
        return this.counter;
    }
}
