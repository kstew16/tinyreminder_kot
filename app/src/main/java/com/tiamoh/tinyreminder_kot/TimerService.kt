package com.tiamoh.tinyreminder_kot

//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.IntentFilter
import androidx.annotation.RequiresApi
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.concurrent.timer

class TimerService : Service() {
    //타이머 조작
    private val OFF = 9999
    //val GOING = 9998
    private val STOP = 0

    var isActivityRun: Boolean = false
    //var timerSwitch: Boolean = false

    var set_min: Int = 15
    var set_hour: Int = 0
    var min: Int = 0
    var hour: Int = 0
    var sec: Int = 0
    var time_tick = 0
    var set_term: Int = 0
    var startTime: Long = 0
    var notificationManager: NotificationManager? = null
    var notificationBuilder: NotificationCompat.Builder? = null
    var timerTask: Timer?=null

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
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //showToast("Start Command receive")
        //이건 알람 간격 바뀌었을때
        //이거 아래로 가면 인텐트 디폴트가 15라서 밸류 리셋 됨.
        val data = intent?.getIntExtra("SETTING", 15)
        if (data != null) {
            set_term = data
            set_hour = set_term / 60
            set_min = set_term - set_hour * 60
        }

        //메인 액티비티에서 매번 서비스를 스타트하는 식으로 데이터 통신해도 괜찮은가 모르겠네
        //로컬 브로드캐스트가 지양되는것같아서 이래함

        isActivityRun = intent?.getBooleanExtra("ACTIVITY_RUNNING", false)?:false


        val modeVal = intent?.getIntExtra("MODE", OFF)
        if (modeVal != null) {
            if (modeVal != OFF) {//intent가 null일시에 값 없을 수도 있음.
                try {
                    set_term = modeVal
                    set_hour = set_term / 60
                    set_min = set_term - set_hour * 60
                    startTime = SystemClock.elapsedRealtime()
                    //timerSwitch = true // 명시적으로 타이머 켬
                    //타이머 시작 버튼을 누르면 타이머 시작
                    //startForegroundService()
                    //TimerThread().run()
                    timerTask = timer(period = 1000){
                        //더하기 말고 시작시간
                        time_tick = ((SystemClock.elapsedRealtime()-startTime)/1000).toInt()
                        //time_tick++
                        if (time_tick > 0 && time_tick % (set_min * 60 + set_hour * 3600) == 0) {
                            showToast("설정한 시간이 되어 알려드렸어요!")
                            //val toast = Toast.makeText(this@TimerService, "설정한 시간이 되어 알려드렸어요!", Toast.LENGTH_SHORT)
                            //toast.show()
                            //그냥 기본 알림음이나 음성 재생하는 걸로... 미디어 받아와서 재생하는게 좋을 듯
                            val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            //vib.vibrate(longArrayOf(100, 150, 400, 150, 400, 150), -1)
                            val timing = longArrayOf(100, 150, 400, 150, 400, 150)
                            val amplitude =  intArrayOf(0,100,0,50,0,100)
                            val vibrationEffect = VibrationEffect.createWaveform(timing,amplitude,-1)
                            vib.vibrate(vibrationEffect)
                            val notification =
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            val ringtone =
                                RingtoneManager.getRingtone(applicationContext, notification)
                            ringtone.play()
                        }
                        hour = time_tick/3600
                        min = (time_tick - hour*3600)/60
                        sec = time_tick - hour*3600 - min*60
                        startForegroundService()
                        if(isActivityRun) {
                            val toMainIntent = Intent(applicationContext, MainActivity::class.java)
                            toMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            toMainIntent.putExtra("TIME", time_tick)
                            startActivity(toMainIntent)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (modeVal == STOP) {
                timerTask?.cancel()
                stopSelf()
                //타이머 종료 버튼을 누르면 타이머 종료
                //timerSwitch = false // 명시적으로 타이머 끔
                //acctime더하는 것도 저기서 함
                //acc_time += time_tick
                //변환 저기서 함.
                //hour = acc_time / 3600
                //min = (acc_time - hour * 3600) / 60
                //sec = acc_time % 60
            }
            //modeVal은 1회성 명령 intent이므로 1회 사용후 초기화한다.
            //이거 말고 인텐트 받을때만 실행시킬 수 있는데 그거 nullable이라 죠큼 귀찮아
            //알고보면 초기화되고있는듯
            //modeVal = GOING
        }

        //액티비티가 켜져 있을 때만 액티비티에 쓰레드가 정보 전달하게 하기 위해 받음
        //intent가 null이면 false때려버림


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        timerTask?.cancel()
        super.onDestroy()
    }


    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toast.show()
        }
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
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = channelDescription
            notificationManager!!.createNotificationChannel(notificationChannel)
            notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
        } else {
            notificationBuilder = NotificationCompat.Builder(applicationContext)
        }
        //notification 내용 작성
        notificationBuilder!!.setSmallIcon(R.drawable.star_off)
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
        notificationBuilder!!.setOnlyAlertOnce(true)
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

    /*
    inner class TimerThread: Runnable {
        override fun run(){
            var i = 0
            while(true){
                //어짜피 타이머 안 쓸때는 서비스가 꺼짐
                startForegroundService()

                i++
                val toMainIntent = Intent(applicationContext,MainActivity::class.java)
                toMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                toMainIntent.putExtra("TIME",i)
                startActivity(toMainIntent)
                showToast("횟수 "+i.toString())
                Thread.sleep(1000)//시험적으로 1초에 10번
            }

        }
    }*/
}