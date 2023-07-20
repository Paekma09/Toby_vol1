package user.service;

import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

import java.util.List;

public class UserService {
    // 상수의 도입
    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_FOR_GOLD = 30;

    UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    // 기본 작업 흐름만 남겨둔 upgradeLevels()
    public void upgradeLevels() {
        List<User> users =userDao.getAll();
        for (User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }
    }

    // 업그레이드 가능 확인 메소드 - 상수 도입
    private boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();
        switch (currentLevel) {
            case BASIC: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
            case SILVER: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
            case GOLD: return false;
            default: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
        }
    }

    // 업그레이드 가능 확인 메소드
//    private boolean canUpgradeLevel(User user) {
//        Level currentLevel = user.getLevel();
//        // 레벨별로 구분해서 조건을 판단한다.
//        switch (currentLevel) {
//            case BASIC: return (user.getLogin() >= 50);
//            case SILVER: return (user.getRecommend() >= 30);
//            case GOLD: return false;
//            // 현재 로직에서 다룰 수 없는 레벨이 주어지면 예외를 발생시킨다. 새로운 레벨이 추가되고 로직을 수정하지 않으면 에러가 나서 확인할 수 있다.
//            default: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
//        }
//    }

    // 간결해진 upgradeLevel()
    private void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
    }

    // 사용자 신규 등록 로직을 담은 add() 메소드
    public void add(User user) {
        if (user.getLevel() == null) {
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }

    // 레벨 업그레이드 작업 메소드
//    private void upgradeLevel(User user) {
//        if (user.getLevel() == Level.BASIC) {
//            user.setLevel(Level.SILVER);
//        } else if (user.getLevel() == Level.SILVER) {
//            user.setLevel(Level.GOLD);
//        }
//        userDao.update(user);
//    }

    // 사용자 레벨 업그레이드 메소드
//    public void upgradeLevels() {
//        List<User> users = userDao.getAll();
//        for (User user : users) {
//            Boolean changed = null; // 레벨의 변화가 있는지를 확인하는 플래그
//            // BASIC 레벨 업그레이드 작업
//            if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) {
//                user.setLevel(Level.SILVER);
//                changed = true; // 레벨 변경 플래그 설정
//            // SILVER 레벨 업그레이드 작업
//            } else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
//                user.setLevel(Level.GOLD);
//                changed = true; // 레벨 변경 플래그 설정
//            } else if (user.getLevel() == Level.GOLD) {
//                changed = false;    // GOLD 레벨은 변경이 일어나지 않는다.
//            } else {
//                changed = false;    // 일치하는 조건이 없으면 변경 없음
//            }
//
//            // 레벨의 변경이 있는 경우에만 update() 호출
//            if (changed) {
//                userDao.update(user);
//            }
//        }
//    }
}
