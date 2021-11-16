package com.tiamoh.tinyreminder_kot

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GraphActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    lateinit var readableDatabase: SQLiteDatabase
    private val THEME_PURPLE = "#A593E0"
    private val THEME_BLUE = "#91ADED"
    private val THEME_GRAY = "#566270"
    private val dbVersion=5

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_graph)
        super.onCreate(savedInstanceState)


        var intent = intent
        val dbSavedVer = intent.getIntExtra("dbSavedVer",0)
        //val dbVersion=5

        val barChart = findViewById<BarChart>(R.id.barchart)
        val graphSelectRadioGroup = findViewById<RadioGroup>(R.id.graphSelect)
        val graphDay = findViewById<RadioButton>(R.id.graphDay)
        val graphWeek = findViewById<RadioButton>(R.id.graphWeek)
        val graphMonth = findViewById<RadioButton>(R.id.graphMonth)
        dbHelper = DBHelper(this, "Accumulate.db", null, dbVersion)
        database = dbHelper.writableDatabase
        readableDatabase = dbHelper.readableDatabase
        // setChart(barChart,0)
        if(dbSavedVer==dbVersion) {
            setBarChart(barChart, 0)

            graphSelectRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    graphDay.id -> {
                        setBarChart(barChart, 0)
                    }
                    graphWeek.id -> {
                        setBarChart(barChart, 1)
                    }
                    graphMonth.id -> {
                        setBarChart(barChart, 2)
                    }
                }
            }
        }
    }

    private fun initBarDataSet(barDataSet: BarDataSet){
        barDataSet.color = Color.parseColor(THEME_PURPLE)
        //Setting the size of the form in the legend
        barDataSet.formSize = 15f
        //showing the value of the bar, default true if not set
        barDataSet.setDrawValues(true)
        //setting the text size of the value of the bar
        barDataSet.valueTextSize = 12f
    }

    private fun initBarChart(barChart: BarChart){
        // 회색 배경 삭제
        barChart.setDrawGridBackground(false)
        // 막대그래프 그림자 삭제
        barChart.setDrawBarShadow(false)
        // 그래프 경계선 삭제
        barChart.setDrawBorders(false)

        // 우측 하단 description 삭제
        val description = Description()
        description.text = "단위 : 분"
        description.textSize = 10f
        description.isEnabled = true
        barChart.description = description

        //X, Y 바의 애니메이션 효과
        barChart.animateY(1000)
        barChart.animateX(1000)


        //바텀 좌표 값
        val xAxis: XAxis = barChart.getXAxis()
        //change the position of x-axis to the bottom
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //set the horizontal distance of the grid line
        xAxis.granularity = 1f
        xAxis.textColor = Color.parseColor(THEME_GRAY)
        xAxis.textSize = 12f
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false)
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false)


        //좌측 값 hiding the left y-axis line, default true if not set
        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.textColor = Color.parseColor(THEME_GRAY)
        leftAxis.textSize = 12f


        //우측 값 hiding the right y-axis line, default true if not set
        val rightAxis: YAxis = barChart.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.textColor = Color.parseColor(THEME_GRAY)
        rightAxis.textSize = 12f


        //바차트의 타이틀
        val legend: Legend = barChart.legend
        //setting the shape of the legend form to line, default square shape
        legend.form = Legend.LegendForm.LINE
        //setting the text size of the legend
        legend.textSize = 15f
        legend.textColor = Color.parseColor(THEME_GRAY)
        //setting the alignment of legend toward the chart
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        //setting the stacking direction of legend
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        //setting the location of legend outside the chart, default false if not set
        legend.setDrawInside(false)

    }


    private fun setBarChart(barChart: BarChart, graphIndex: Int){
        initBarChart(barChart)
        barChart.setScaleEnabled(false)
        val tableList = ArrayList<Int>()
        val timeList = ArrayList<Float>()
        val entries: ArrayList<BarEntry> = ArrayList()
        val title = "누적 시간"
        val calendar = Calendar.getInstance()
        var thisWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        var thisDay = calendar.get(Calendar.DAY_OF_YEAR)
        var thisMonth = calendar.get(Calendar.MONTH)

        // 데이터베이스로 로드하는 부분, 유효성 체크는 메소드 실행할 때 했음




        when (graphIndex) {
            0 -> {
                //Day
                //커서를 통해 DB에서 데이터 끌어옴
                var count = 0
                var c : Cursor? = readableDatabase.rawQuery(
                    "SELECT * FROM accTimeTable",
                    null
                )
                if(c!=null){

                    c.moveToLast()
                    var lastLable = c.getString(0).toInt()
                    var previousDay = lastLable%100 +1

                    while(c!=null&&count<5){
                        //데이터 5개 까지만 거꾸로 읽어 옴
                        var tableLable = c.getString(0).toInt()
                        var tableDay = tableLable%100
                        if (tableDay + 1 != previousDay){
                            //저장되지 않은 날 있으면 0 추가해줌
                            tableList.add(previousDay-1)
                            timeList.add(0F)
                            previousDay -= 1
                        }
                        else {
                            tableList.add(tableDay)
                            var savedSecond = (c.getInt(1).toFloat())/60
                            timeList.add(savedSecond)
                            previousDay = tableDay
                            if (!c.isFirst) {
                                c.moveToPrevious()
                            } else {
                                break
                            }
                        }
                        count += 1
                    }
                }
                // 끌어온 데이터를 추가함
                for (i in timeList.size-1 downTo 0) {
                    val barEntry = BarEntry(tableList[i].toFloat(), timeList[i].toFloat())
                    entries.add(barEntry)
                }
            }

            1 -> {
            //Week

            }
            2 -> {
            //Month


            }
        }



        """
        for (i in 0..5){
            timeList.add(i*100)
        }
        """


        val barDataSet = BarDataSet(entries, title)
        initBarDataSet(barDataSet)
        val data = BarData(barDataSet)
        barChart.data = data
        barChart.invalidate()


    }

}