package com.tiamoh.tinyreminder_kot

import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var isToggled: Boolean = false
    var isServiceRun: Boolean = false
    var acc_time = 0
    var set_min: Int = 15
    var set_hour: Int = 0
    var min = 0
    var hour: Int = 0
    var sec: Int = 0
    var timeTick = 0
    //val startTime: Long = SystemClock.elapsedRealtime()

    //데베관련
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    lateinit var readableDatabase: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showToast("20211108 오후 8시 5분 초회실행")

        setContentView(R.layout.activity_main)
        val startBtn = findViewById<CompoundButton>(R.id.startButton)
        val settingBtn = findViewById<ImageButton>(R.id.settingIcon)
        val graphBtn = findViewById<ImageButton>(R.id.graphIcon)
        //val myTimePicker = findViewById<View>(R.id.timepicker) as TimePicker
        val setTerm = findViewById<TextView>(R.id.setNumber)
        val accTime = findViewById<TextView>(R.id.todayNumber)
        //val timeTickView = findViewById<TextView>(R.id.clock)

        settingBtn.setBackgroundResource(R.drawable.settingicon)


        //저 살아났어요
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("ACTIVITY_RUNNING", true)
        startService(serviceIntent)

        //저장된 정보들
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        var savedTerm = sharedPreferences.getInt("setTerm",15)
        val isSaved = sharedPreferences.getBoolean("isSaved",false)
        val isToggled = sharedPreferences.getBoolean("isToggled",false)

        if(isToggled){
            //토글된 상태에서 꺼진거면 서비스 켜진 상태로 꺼진거
            startBtn.toggle()
            isServiceRun=true
        }

        //sharedPrefernce에서 알람 간격 저장해둔거 불러옴
        set_hour = savedTerm/60
        set_min = savedTerm - (60*set_hour)
        setTerm.text = String.format("%02d", set_hour) + " 시간 " + String.format(
            "%02d",
            set_min
        ) + " 분"

        dbHelper = DBHelper(this, "Accumulate.db", null, 4)
        database = dbHelper.writableDatabase
        readableDatabase = dbHelper.readableDatabase
        var nowMills = System.currentTimeMillis()
        var date = Date(nowMills)
        var simpleDateFormat : SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
        var saveTime : String = simpleDateFormat.format(date)
        var c : Cursor? = readableDatabase.rawQuery("SELECT * FROM accTimeTable" ,null)
        if ((c!= null) && isSaved) {
            c.moveToLast()
            //오늘 이름으로 저장된 누적시간이 있으면 onCreate시에 불러옴
            if(c.getString(0)==saveTime){
                acc_time = c.getInt(1)
                hour = acc_time / 3600
                min = (acc_time - hour * 3600) / 60
                sec = acc_time % 60
                accTime!!.text = String.format("%02d", hour) + " : " + String.format(
                    "%02d",
                    min
                ) + " : " + String.format("%02d", sec)
            }
        }




       //알람 설정 시간 변경 버튼이 눌릴 시
        settingBtn.setOnClickListener {
            //타임피커 다이얼로그에서 시간을 받아와서 액티비티에 띄움
            val timeDialog =
                TimePickerDialog(
                    this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    { _, hourOfDay, minute ->
                        set_hour = hourOfDay
                        set_min = minute
                        if (set_hour == 0 && set_min == 0) {
                            showToast("시간 간격은 0분이 될 수 없습니다.")
                        } else {
                            showToast(
                                String.format(
                                    "%02d",
                                    set_hour
                                ) + " 시간 " + String.format("%02d", set_min) + " 분 마다 알림"
                            )
                            setTerm.text = String.format("%02d", set_hour) + " 시간 " + String.format(
                                "%02d",
                                set_min
                            ) + " 분"
                        }
                    }, set_hour, set_min, true
                )
            timeDialog.setTitle("알람 간격을 설정하세요:")
            timeDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            timeDialog.show()
            timeDialog.setOnDismissListener {
                //액티비티 실행중이라고 서비스에 알려줌 (실행중일때 타이머 데이터 보내줌)
                //근데 이거 왜 여기서 보냄 생각해보니까? 20211008의 의문
                val serviceIntent = Intent(this, TimerService::class.java)
                serviceIntent.putExtra("SETTING", set_hour * 60 + set_min)
                serviceIntent.putExtra("ACTIVITY_RUNNING", true)
                startService(serviceIntent)
                //데이터베이스에 설정시간 저장 SharedPref 이용
                val editor = sharedPreferences.edit()
                var setTerm = set_hour * 60 + set_min
                editor.putInt("setTerm",setTerm)
                //초회 실행시 데베가 비어서 튕기는 것 방지
                editor.putBoolean("isSaved",true)
                editor.commit()

            }


        }

        //토글버튼인 스타트 버튼이 눌릴 시
        startBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //토글된 상태에서 종료될 시에는 서비스가 켜져있음을 저장하기 위함
                val editor = sharedPreferences.edit()
                editor.putBoolean("isToggled",true)
                editor.commit()
                //토글 눌렸을 때 실행중이라고 알려줌
                val serviceIntent = Intent(this, TimerService::class.java)
                serviceIntent.putExtra("MODE", set_hour * 60 + set_min)
                serviceIntent.putExtra("ACTIVITY_RUNNING", true)
                startService(serviceIntent)
            } else {
                //액티비티는 뚠뚠 오늘도 뚠뚠
                val serviceIntent = Intent(this, TimerService::class.java)
                serviceIntent.putExtra("MODE", 0)
                serviceIntent.putExtra("ACTIVITY_RUNNING", true)
                stopService(serviceIntent)
                //Service로부터 onNewIntent로 받아온 timeTick을 계산해서 액티비티에 출력
                acc_time += timeTick
                hour = timeTick / 3600
                min = (timeTick - hour * 3600) / 60
                sec = timeTick % 60
                showToast(
                    String.format("%02d", hour) + " : " + String.format(
                        "%02d",
                        min
                    ) + " : " + String.format("%02d", sec) + " 기록"
                )
                hour = acc_time / 3600
                min = (acc_time - hour * 3600) / 60
                sec = acc_time % 60
                accTime!!.text = String.format("%02d", hour) + " : " + String.format(
                    "%02d",
                    min
                ) + " : " + String.format("%02d", sec)
                timeTick = 0
                //데이터베이스에 오늘 이름으로 누적시간을 저장
                var contentValue = ContentValues()
                var nowMills = System.currentTimeMillis()
                var date = Date(nowMills)
                var simpleDateFormat : SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
                var saveTime : String = simpleDateFormat.format(date)
                contentValue.put("accTime",acc_time)
                contentValue.put("saveTime",saveTime)
                var c : Cursor = database.query("accTimeTable",null,null,null,null,null,null)
                //이미 오늘날짜 있으면 덮어쓰기, 없으면 만들기 : 충돌처리 덮어쓰기
                database.delete("accTimeTable","saveTime=?", arrayOf(saveTime))
                database.insertWithOnConflict("accTimeTable",null,contentValue,SQLiteDatabase.CONFLICT_REPLACE)
                //토글된 상태에서 종료될 시에는 서비스가 켜져있음을 저장하기 위함
                val editor = sharedPreferences.edit()
                editor.putBoolean("isToggled",false)
                editor.commit()
            }

        }

        graphBtn.setOnClickListener {
            var graphIntent = Intent(this, GraphActivity::class.java)
            startActivity(graphIntent)
        }

    }
/*
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        //액티비티가 꺼지면 토글버튼이 리셋되는 점을 해결하기 위함
        outState.putBoolean("TOGGLE",isToggled)
        outState.putBoolean("SERVICE_RUNNING",isServiceRun)
        super.onSaveInstanceState(outState, outPersistentState)
    }
*/
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //앱 재실행시에 액티비티 데이터 튀는거 매니지용
        //여기부터 사실상 onTimeTick
        timeTick = intent.getIntExtra("TIME",0)
        isServiceRun = intent.getBooleanExtra("SERVICE_RUNNING",false)
        hour = timeTick/3600
        min = (timeTick - hour*3600)/60
        sec = timeTick - hour*3600 - min*60
        if(hour>0){
            val timeTickView = findViewById<TextView>(R.id.clock)
                timeTickView!!.text = String.format("%02d", hour) + " : " + String.format(
                "%02d",
                min
            ) + " : " + String.format("%02d", sec)
        }
        else{
            val timeTickView = findViewById<TextView>(R.id.clock)
            timeTickView!!.text = String.format(
                "%02d",
                min
            ) + " : " + String.format("%02d", sec)
        }
    }

    override fun onRestart() {
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("ACTIVITY_RUNNING", true)
        startService(serviceIntent)
        super.onRestart()

    }
    override fun onStop(){
        //저 죽어요
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("ACTIVITY_RUNNING", false)
        startService(serviceIntent)
        super.onStop()
    }

    override fun onPause() {
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("ACTIVITY_RUNNING", false)
        startService(serviceIntent)
        super.onPause()
    }

    override fun onResume() {
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("ACTIVITY_RUNNING", true)
        startService(serviceIntent)
        super.onResume()
    }

    override fun onDestroy() {
        //생각해보니까 앱 종료시에 서비스를 죽일 필요가 없음
        //val serviceIntent = Intent(this, TimerService::class.java)
        //stopService(serviceIntent)
        database.close()
        readableDatabase.close()
        super.onDestroy()

    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

}