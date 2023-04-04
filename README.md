# 🕛 작은 알리미

## 실행 화면

![tiny_reminder](/images/tiny_reminder.png)

**짠! 척추 요정이에요!**

학생들도, 직장인들도, 저희 개발자들도!
책상 앞에 하루 종일 앉아 스트레칭은 잊고 삽니다.
타이머를 켜시면, 화면을 꺼도, 앱을 종료해도!
설정하신 시간마다 척추요정이 뾰로롱 소리를 내면서 스트레칭 하라고 알려줄 거에요!
노력한 시간을 기록해서 통계로 만들어주는 건 덤이랍니다 ><

## 주요 특징

실시간 포어그라운드 서비스를 활용하여 타이머가 작동하는 동안 특정 간격으로 스트레칭 알림을 제공합니다. 또 타이머가 작동된 시간을 SQLite를 통해 로컬 데이터베이스에 저장하고, 그래프로 시각화하는 애플리케이션입니다.

## 🖥️ 빌드 환경

이 프로젝트는 `Gradle` 빌드 시스템을 사용합니다.
이 프로젝트를 빌드하려면 `gradlew build` 커맨드를 사용하거나, Android Studio 에서 프로젝트를 열어주세요.

이 프로젝트는 `Gradle 4.2.2` 와 `JDK 11`를 사용한 환경에서 작업되었습니다.

- `minSdkVersion` : 26
- `targetSdkVersion` : 30
  
### 🏛️ 주요 라이브러리

- [SQLite](https://developer.android.com/training/data-storage/sqlite)
- [MPAndroidChart 3](https://github.com/PhilJay/MPAndroidChart)
