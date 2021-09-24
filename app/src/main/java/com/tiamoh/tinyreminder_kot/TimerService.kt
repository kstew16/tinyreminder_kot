package com.tiamoh.tinyreminder_kot

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
//import android.content.BroadcastReceiver
//import android.content.Context
import android.content.Intent
//import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    //타이머 조작
    val OFF = 9999
    val STOP = 0

    var isActivityRun: Boolean = false


    var acc_time: Int = 0
    var set_min: Int = 15
    var set_hour: Int = 0
    var min: Int = 0
    var hour: Int = 0
    var sec: Int = 0
    var time_tick: Int = 0
    var set_term: Int = 0
    val startTime: Long = SystemClock.elapsedRealtime()
    var notificationManager: NotificationManager? = null
    var notificationBuilder: NotificationCompat.Builder? = null

    /*
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val modeVal = intent.getIntExtra("MODE",15)
            if (modeVal != null) {
                if (modeVal != 0) {
                    try {
                        set_term=modeVal
                        //타이머 시작 버튼을 누르면 타이머 시작
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else{
                    //타이머 종료 버튼을 누르면 타이머 종료
                    //
                    acc_time += time_tick
                    hour = acc_time / 3600
                    min = (acc_time - hour * 3600) / 60
                    sec = acc_time % 60
                }
            }
            val data = intent.getIntExtra("SETTING",15)
            if (data != null) {
                set_term = data
            }
        }
    }
    */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showToast("Start Command receive")
        //메인 액티비티에서 매번 서비스를 스타트하는 식으로 데이터 통신해도 괜찮은가 모르겠네
        //로컬 브로드캐스트가 지양되는것같아서 이래함
        val modeVal = intent?.getIntExtra("MODE", OFF)
        if (modeVal != null) {
            if (modeVal != OFF) {//intent가 null일시에 값 없을 수도 있음.
                try {
                    set_term = modeVal
                    //타이머 시작 버튼을 누르면 타이머 시작
                    startForegroundService()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (modeVal == STOP) {
                //타이머 종료 버튼을 누르면 타이머 종료
                acc_time += time_tick
                hour = acc_time / 3600
                min = (acc_time - hour * 3600) / 60
                sec = acc_time % 60
            }
        }
        //이건 알람 간격 바뀌었을때
        val data = intent?.getIntExtra("SETTING", 15)
        if (data != null) {
            set_term = data
            set_hour = set_term / 60
            set_min = set_term - set_hour * 60
        }
        //액티비티가 켜져 있을 때만 액티비티에 쓰레드가 정보 전달하게 하기 위해 받음
        //intent가 null이면 false때려버림
        isActivityRun = intent?.getBooleanExtra("ACTIVITY_RUNNING", false) ?: false

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        showToast("Service built")
        super.onCreate()
    }


    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun startForegroundService() {
        //이걸 매초 반복하는걸 원래는 chronometer tick 썼는데 방법구현이 중요
        //쓰레드로 주기적으로 타이머를 갱신해주고 <- 갱신으로 하셈 필수임
        //액티비티가 켜져 있으면 인텐트로 플래그 몇 개 줘서 데이터 전달 ㄱㄱ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "Tiny Reminder"
            val channelName = "실시간 알림"
            val channelDescription = "실시간으로 알림 설정 시간과 기록 시간을 알려드려요"
            val notificationChannel =
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = channelDescription
            notificationManager!!.createNotificationChannel(notificationChannel)
            notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
        } else {
            notificationBuilder = NotificationCompat.Builder(applicationContext)
        }
        //notification 내용 작성
        notificationBuilder!!.setSmallIcon(R.drawable.star_on)
        notificationBuilder!!.setWhen(System.currentTimeMillis())
        notificationBuilder!!.setContentTitle(
            String.format(
                "%02d",
                set_hour
            ) + " 시간 " + String.format("%02d", set_min) + " 분 마다 알림"
        )
        notificationBuilder!!.setContentText(
            String.format(
                "%02d",
                hour
            ) + " : " + String.format("%02d", min) + " : " + String.format("%02d", sec)
        )
        notificationBuilder!!.setOngoing(true)
        val notifyIntent = Intent(applicationContext, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            applicationContext,
            10,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder!!.setContentIntent(pIntent)
        //notificationManager.notify(222,notificationBuilder.build());
        startForeground(222, notificationBuilder!!.build())
        //notify 띄웠으면 메인 액티비티에 누적 초를 전달해야 하는데 이거는 메인액티비티 활성화시에만...
    }
}