package learningtest.jdk.proxy;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DynamicProxyTest {

    // 클라이언트 역할의 테스트
    @Test
    public void simpleProxy() {
        Hello hello = new HelloTarget();    // 타깃은 인터페이스를 통해 접근하는 습관을 들이자.
        assertThat(hello.sayHello("KJ"), is("Hello KJ"));
        assertThat(hello.sayHi("KJ"), is("Hi KJ"));
        assertThat(hello.sayThankYou("KJ"), is("Thank You KJ"));

        // 프록시 생성
        Hello proxiedHello = (Hello) Proxy.newProxyInstance(    // 생성된 다이내믹 프록시 오브젝트는 Hello 인터페이스를 구현하고 있으므로 Hello 타입으로 캐스팅해도 안전하다.
                getClass().getClassLoader(),    // 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로더
                new Class[] { Hello.class },    // 구현할 인터페이스
                new UppercaseHandler(new HelloTarget()) // 부가기능과 위임 코드를 담은 InvocationHandler
        );

        assertThat(proxiedHello.sayHello("SungHee"), is("HELLO SUNGHEE"));
        assertThat(proxiedHello.sayHi("SungHee"), is("HI SUNGHEE"));
        assertThat(proxiedHello.sayThankYou("SungHee"), is("THANK YOU SUNGHEE"));

//        Hello proxyHello = new HelloUppercase(new HelloTarget());   // 프록시를 통해 타깃 오브젝트에 접근하도록 구성한다.
//        assertThat(proxyHello.sayHello("SungHee"), is("HELLO SUNGHEE"));
//        assertThat(proxyHello.sayHi("SungHee"), is("HI SUNGHEE"));
//        assertThat(proxyHello.sayThankYou("SungHee"), is("THANK YOU SUNGHEE"));
    }

    // 확장된 UppercaseHandler
    static class UppercaseHandler implements InvocationHandler {
        // 어떤 종류의 인터페이스를 구현한 타깃에도 적용 가능하도록 Object 타입으로 수정
        Object target;
        private UppercaseHandler(Object target) {
            this.target = target;
        }

        // 메소드를 선별해서 부가기능을 적용하는 invoke()
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object ret = method.invoke(target, args);
            if (ret instanceof String && method.getName().startsWith("say")) {  // 리턴타입과 메소드의 이름이 일치하는 경우에만 부가기능을 적용한다.
                return ((String)ret).toUpperCase();
            } else {
                return ret; // 조건이 일치하지 않으면 타깃 오브젝트의 호출 결과를 그대로 리턴한다.
            }
        }

//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            // 호출된 메소드의 리턴타입이 String 인 경우만 대문자 변경 기능을 적용하도록 수정
//            Object ret = method.invoke(target, args);
//            if (ret instanceof String) {
//                return ((String)ret).toUpperCase();
//            } else {
//                return ret;
//            }
//        }
    }

    // InvocationHandler 구현 클래스
//    static class UppercaseHandler implements InvocationHandler {
//        // 다이내믹 프록시로부터 전달받은 요청을 다시 타깃 오브젝트에 위임해야 하기 때문에 타깃 오브젝트를 주입 받아 둔다.
//        Hello target;
//
//        public UppercaseHandler(Hello target) {
//            this.target = target;
//        }
//
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            String ret = (String)method.invoke(target, args);   // 타깃으로 위임. 인터페이스의 메소드 호출에 모두 적용된다.
//            return ret.toUpperCase();   // 부가기능 제공
//        }
//    }

    // 프록시 클래스
    static class HelloUppercase implements Hello {
        Hello hello;    // 위임할 타깃 오브젝트. 여기서는 타깃 클래스의 오브젝트인 것은 알지만 다른 프록시를 추가할 수도 있으므로 인터페이스로 접근한다.

        public HelloUppercase(Hello hello) {
            this.hello = hello;
        }

        @Override
        public String sayHello(String name) {
            return hello.sayHello(name).toUpperCase();  // 위임과 부가기능 적용
        }

        @Override
        public String sayHi(String name) {
            return hello.sayHi(name).toUpperCase();
        }

        @Override
        public String sayThankYou(String name) {
            return hello.sayThankYou(name).toUpperCase();
        }
    }

    // Hello 인터페이스
    static interface Hello {
        String sayHello(String name);
        String sayHi(String name);
        String sayThankYou(String name);
    }

    // 타깃 클래스
    static class HelloTarget implements Hello {

        @Override
        public String sayHello(String name) {
            return "Hello " + name;
        }

        @Override
        public String sayHi(String name) {
            return "Hi " + name;
        }

        @Override
        public String sayThankYou(String name) {
            return "Thank You " + name;
        }
    }


}
