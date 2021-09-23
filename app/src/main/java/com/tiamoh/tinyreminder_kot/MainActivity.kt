package com.tiamoh.tinyreminder_kot

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    var acc_time = 0
    var set_min: Int = 15
    var set_hour: Int = 0
    var min = 0
    var hour: Int = 0
    var sec: Int = 0
    var timeTick = 0
    val startTime: Long = SystemClock.elapsedRealtime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startBtn = findViewById<CompoundButton>(R.id.startButton)
        val settingBtn = findViewById<ImageButton>(R.id.settingIcon)

        //val myTimePicker = findViewById<View>(R.id.timepicker) as TimePicker
        val setTerm = findViewById<TextView>(R.id.setNumber)
        val accTime = findViewById<TextView>(R.id.todayNumber)
        settingBtn.setBackgroundResource(R.drawable.settingicon)
        settingBtn.setOnClickListener {
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
        }
        startBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //chronometer.setBase(SystemClock.elapsedRealtime())
                //chronometer.start()
            } else {
                //chronometer.stop()
                acc_time += timeTick
                hour = acc_time / 3600
                min = (acc_time - hour * 3600) / 60
                sec = acc_time % 60
                showToast(
                    String.format("%02d", hour) + " : " + String.format(
                        "%02d",
                        min
                    ) + " : " + String.format("%02d", sec)
                )
                accTime!!.text = String.format("%02d", hour) + " : " + String.format(
                    "%02d",
                    min
                ) + " : " + String.format("%02d", sec)
            }

        }

    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }
}