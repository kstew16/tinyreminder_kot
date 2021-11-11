package com.tiamoh.tinyreminder_kot

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    lateinit var readableDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_graph)
        super.onCreate(savedInstanceState)
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val dbSavedVer = sharedPreferences.getInt("isSaved",0)
        val dbVersion=5

        dbHelper = DBHelper(this, "Accumulate.db", null, dbVersion)
        database = dbHelper.writableDatabase
        readableDatabase = dbHelper.readableDatabase
        var nowMills = System.currentTimeMillis()
        var date = Date(nowMills)
        var simpleDateFormat : SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
        var saveTime : String = simpleDateFormat.format(date)
        var c : Cursor? = readableDatabase.rawQuery("SELECT * FROM accTimeTable" ,null)
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        val graphSelectRadioGroup = findViewById<RadioGroup>(R.id.graphSelect)
        val graphDay = findViewById<RadioButton>(R.id.graphDay)
        val graphWeek = findViewById<RadioButton>(R.id.graphWeek)
        val graphMonth = findViewById<RadioButton>(R.id.graphMonth)
        setChart(lineChart,0)

        graphSelectRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId==graphDay.id){
                setChart(lineChart,0)
            }
            else if(checkedId==graphWeek.id){
                setChart(lineChart,1)
            }
            else if(checkedId==graphMonth.id){
                setChart(lineChart,2)
            }
        }


    }

    private fun setChart(lineChart:LineChart,checkedRadioButtonIndex: Int){
        val xAxis = lineChart.xAxis

        xAxis.apply{
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 10f
            setDrawGridLines(false)
            granularity=1f
            axisMinimum=2f
            isGranularityEnabled=true
        }
        val apply = lineChart.apply {
            axisRight.isEnabled = false
            axisLeft.axisMaximum = 24f
            legend.apply {
                textSize = 15f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
        val lineData = LineData()
        lineChart.data=lineData
        addData(checkedRadioButtonIndex)
    }

    private fun addData(checkedRadioButtonIndex: Int) {
        //not yet implemented
        //0은 day, 1은 week, 2는 month 별 그래프를 그리기기 위한 데이터엔티티를 추가하는 내용 구현
    }

}