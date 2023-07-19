package user.domain;

public enum Level {
    BASIC(1), SILVER(2), GOLD(3);   //세 개의 이넘 오브젝트 정의

    private final int value;

    Level(int value) {  // DB에 저장할 값을 넣어줄 생성자를 만들어둔다.
        this.value = value;
    }

    public int intValue() { // 값을 가져오는 메소드
        return value;
    }

    public static Level valueOf(int value) {
        switch (value) {
            case 1: return BASIC;
            case 2: return SILVER;
            case 3: return GOLD;
            default: throw new AssertionError("Unknown value: " + value);
        }
    }
}
