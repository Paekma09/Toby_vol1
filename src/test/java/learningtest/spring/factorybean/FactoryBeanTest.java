package learningtest.spring.factorybean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// 팩토리 빈 테스트
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/FactoryBeanTest-context.xml")   // 설정파일 이름을 지정하지 않으면 클래스의 이름 + '-context.xml' 이 디폴트로 사용된다.
public class FactoryBeanTest {
    @Autowired
    ApplicationContext context;

    @Test
    public void getMessageFromFactoryBean() {
        Object message = context.getBean("message");
        assertThat(message, is(Message.class)); // 타입 확인
        assertThat(((Message)message).getText(), is("Factory Bean")); // 설정과 기능 확인
    }

    @Test
    public void getFactoryBean() throws Exception {
        Object factory = context.getBean("&message");   // &가 붙고 안 붙고에 따라 getBean() 메소드가 돌려주는 오브젝트가 달라진다.
        assertThat(factory, is(MessageFactoryBean.class));
    }
}
