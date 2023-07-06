package springbook.learningtest.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {
//    public Integer calcSum(String filepath) throws IOException {
//        BufferedReader br = new BufferedReader(new FileReader(filepath));   // 한줄씩 읽기 편하게 BufferedReader 로 파일을 가져온다.
//        Integer sum = 0;
//        String line = null;
//        while ((line = br.readLine()) != null) {    // 마직막 라인까지 한줄씩 읽어가면서 숫자를 더한다.
//            sum += Integer.valueOf(line);
//        }
//
//        br.close(); // 한번 연 파일은 반드시 닫아준다.
//        return sum;
//    }

    // 예외처리 추가
//    public Integer calcSum(String filepath) throws IOException {
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(filepath));
//            Integer sum = 0;
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                sum += Integer.valueOf(line);
//            }
//            return sum;
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//            throw e;
//        }  finally {
//            if (br != null) {   // BufferedReader 오브젝트가 생성되기 전에 예외가 발생할 수도 있으므로 반드시 null 체크를 먼저 해야 한다.
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//        }
//    }

    // BufferedReaderCallback 템플릿 메소드를 이용한 분리
//    public Integer calcSum(String filepath) throws IOException {
//        BufferedReaderCallback sumCallback = new BufferedReaderCallback() {
//            @Override
//            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
//                Integer sum = 0;
//                String line = null;
//                while ((line = br.readLine()) != null) {
//                    sum += Integer.valueOf(line);
//                }
//                return sum;
//            }
//        };
//        return fileReadTemplate(filepath, sumCallback);
//    }


    // LineCallback 템플릿 메소드를 이용한 분리
    public Integer calcSum(String filepath) throws IOException {
//        LineCallback sumCallback = new LineCallback() {
        LineCallback<Integer> sumCallback = new LineCallback<Integer>() {   // 제네릭형으로 변형
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value + Integer.valueOf(line);
            }
        };
        return lineReadTemplate(filepath, sumCallback, 0);
    }

    // BufferedReaderCallback 템플릿 메소드를 이용한 분리
//    public Integer calcMultiply(String filepath) throws IOException {
//        BufferedReaderCallback multiplyCallback = new BufferedReaderCallback() {
//            @Override
//            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
//                Integer multiply = 1;
//                String line = null;
//                while ((line = br.readLine()) != null) {
//                    multiply *= Integer.valueOf(line);
//                }
//                return multiply;
//            }
//        };
//        return fileReadTemplate(filepath, multiplyCallback);
//    }

    // LineCallback 템플릿 메소드를 이용한 분리
    public Integer calcMultiply(String filepath) throws IOException {
//        LineCallback multiplyCallback = new LineCallback() {
        LineCallback<Integer> multiplyCallback = new LineCallback<Integer>() {  // 제네릭형으로 변형
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value * Integer.valueOf(line);
            }
        };
        return lineReadTemplate(filepath, multiplyCallback, 1);
    }


    // BufferedReaderCallback 을 사용하는 템플릿 메소드
    public Integer fileReadTemplate(String filepath, BufferedReaderCallback callback) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filepath));
            int ret = callback.doSomethingWithReader(br);
            return ret;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    // LineCallback 을 사용하는 템플릿 메소드
//    public Integer lineReadTemplate(String filepath, LineCallback lineCallback, int initVal) throws IOException {   // initVal --> 계산 결과를 저장할 변수의 초기값
//        BufferedReader br = null;
//
//        try {
//            br = new BufferedReader(new FileReader(filepath));
//            Integer res = initVal;
//            String line = null;
//            while ((line = br.readLine()) != null) {    // 파일의 각 라인을 루프를 돌면서 가져 오는 것도 템플릿이 담당한다.
//                res = lineCallback.doSomethingWithLine(line, res);  // res --> 콜백이 계산한 값을 저장해뒀다가 다음 라인 계산에 다시 사용한다. // 각 라인의 내용을 가지고 계산하는 작업만 콜백에게 맡긴다.
//            }
//            return res;
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//            throw e;
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//        }
//    }

    // 제네릭으로 변형
    public <T> T lineReadTemplate(String filepath, LineCallback<T> lineCallback, T initVal) throws IOException {   // initVal --> 계산 결과를 저장할 변수의 초기값
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filepath));
            T res = initVal;
            String line = null;
            while ((line = br.readLine()) != null) {    // 파일의 각 라인을 루프를 돌면서 가져 오는 것도 템플릿이 담당한다.
                res = lineCallback.doSomethingWithLine(line, res);  // res --> 콜백이 계산한 값을 저장해뒀다가 다음 라인 계산에 다시 사용한다. // 각 라인의 내용을 가지고 계산하는 작업만 콜백에게 맡긴다.
            }
            return res;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    // 문자열 연결 기능 콜백을 이용해 만든 concatenate() 메소드
    public String concatenate(String filepath) throws IOException {
        LineCallback<String> concatenateCallback = new LineCallback<String>() {
            @Override
            public String doSomethingWithLine(String line, String value) {
                return value + line;
            }
        };
        return lineReadTemplate(filepath, concatenateCallback, "");
    }
}
