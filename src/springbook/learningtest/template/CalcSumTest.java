package springbook.learningtest.template;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CalcSumTest {

    Calculator calculator;
    String numFilepath;

    @Before
    public void setUp() {
        this.calculator = new Calculator();
        this.numFilepath = getClass().getResource("numbers.txt").getPath();
    }

//    @Test
//    public void sumOfNumbers() throws IOException {
//        Calculator calculator = new Calculator();
//        int sum = calculator.calcSum(getClass().getResource("numbers.txt").getPath());
//        assertThat(sum, is(10));
//    }

    // 템플릿을 이용한 테스트 메소드 수정
    @Test
    public void sumOfNumbers() throws IOException {
        assertThat(calculator.calcSum(this.numFilepath), is(10));
    }

    @Test
    public void multiplyOfNumbers() throws IOException {
        assertThat(calculator.calcMultiply(this.numFilepath), is(10));
    }

    @Test
    public void concatenateStrings() throws  IOException {
        assertThat(calculator.concatenate(this.numFilepath), is("1234"));
    }
}
