package springbook.learningtest.template;

// 라인별 작업을 정의한 콜백 인터페이스
//public interface LineCallback {
//    Integer doSomethingWithLine(String line, Integer value);
//}

// 제네릭으로 변형
public interface LineCallback<T> {
    T doSomethingWithLine(String line, T value);
}
