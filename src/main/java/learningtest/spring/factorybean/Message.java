package learningtest.spring.factorybean;

// 생성자를 제공하지 않는 클래스
public class Message {
    String text;

    // 생성자가 private 으로 선언되어 있어서 외부에서 생성자를 통해 오브젝트를 만들 수 없다.
    private Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    // 생성자 대신 사용할 수 있는 스태틱 팩토리 메소드를 제공한다.
    public static Message newMessage(String text) {
        return new Message(text);
    }
}
