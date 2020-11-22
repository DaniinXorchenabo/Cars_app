package com.example.cars2

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import android.widget.SeekBar
import android.widget.RadioGroup
import kotlin.math.round
import android.widget.Toast
import android.widget.SeekBar.OnSeekBarChangeListener

//import org.jetbrains.anko.toast
//https://www.javatpoint.com/kotlin-android-seekbar - seekBar
//https://github.com/harjot-oberai/Croller,  - красивый seekBar
//https://github.com/neild001/SeekArc/blob/master - красивый seekBar
// https://uxplanet.org/top-15-seekbar-and-slider-github-ui-libraries-and-components-java-swift-kotlin-d0f6a9658be3   - красивый seekBar
//buid->build Apk - собрать приложение
class MainActivity : AppCompatActivity() { // расширяем MainActivity

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var m_pairedDevises: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1
    //------
    var speedCalculationSeekBar: SeekBar? = null
    var timeCalculationSeekBar: SeekBar? = null
    var distanceCalculationSeekBar: SeekBar? = null
    var valueInt:Int? = 1
    var valueInt2:Int? = 1
    var permissionToEdit: Boolean = true
    //-----------

    var tasksSeekBarSpeed: SeekBar? = null
    var tasksSeekBarTime: SeekBar? = null
    var tasksSeekBarDistance: SeekBar? = null
    var whereCheckBoxClickDown: String = "distance"
    var startValueForSeekBar: Int = 1
    companion object{ //статический объект
        val EXTRA_ADRESS: String = "Device_address"
        var userLogining: Boolean = false
        var userLoginFirst : Boolean = false
        var m_myUUID: UUID =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                //message.setText(R.string.title_home)
                relativeLayout2.visibility = View.VISIBLE
                relativeLayout3.visibility = View.INVISIBLE
                relativeLayout4.visibility = View.INVISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                relativeLayout2.visibility = View.INVISIBLE
                relativeLayout3.visibility = View.VISIBLE
                relativeLayout4.visibility = View.INVISIBLE
                //message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                relativeLayout2.visibility = View.INVISIBLE
                relativeLayout3.visibility = View.INVISIBLE
                relativeLayout4.visibility = View.VISIBLE
                //message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    private val passiveNavigator = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                //message.setText(R.string.title_home)
                relativeLayout2.visibility = View.VISIBLE
                relativeLayout3.visibility = View.INVISIBLE
                relativeLayout4.visibility = View.INVISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                relativeLayout2.visibility = View.VISIBLE
                relativeLayout3.visibility = View.INVISIBLE
                relativeLayout4.visibility = View.INVISIBLE
                //message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener false
            }
            R.id.navigation_notifications -> {
                relativeLayout2.visibility = View.VISIBLE
                relativeLayout3.visibility = View.INVISIBLE
                relativeLayout4.visibility = View.INVISIBLE
                //message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener false
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)//создание экрана и отрисовка главного активити
        relativeLayout4.visibility = View.INVISIBLE
        relativeLayout2.visibility = View.VISIBLE
        relativeLayout3.visibility = View.INVISIBLE
        navigation.setOnNavigationItemSelectedListener(passiveNavigator)
        val n:String = "60"
        Log.i("----80", n)


        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()//создание объекта блютуза
        if(m_bluetoothAdapter == null){
            toast("это устройство не поддерживает Bluetooth")
            return

        }
        if (!m_bluetoothAdapter!!.isEnabled){ //если блютуз не включен (прога сломается, если m_bluetoothAdapter будет null)
            //toast("включение Bluetooth, если он не включен")
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)// принять объект соединения с блютузом телефона
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)// запустить модкль

        }
        select_device_refresh.setOnClickListener{pairdDeviceList()} //ловим нажатие кнопки refresh, если нажата отправляемся дальше
        var myRadioGroupObject: RadioGroup = findViewById(R.id.change_parametr_for_tasks)// выбор параметра во второй вкладке

        //select_device_refresh.setOnClickListener{pairdDeviceList()} //ловим нажатие кнопки refresh, если нажата отправляемся дальше
       // val radioGroup = findViewById(R.id.change_parametr_for_tasks)
        // инициализация бегунков для третей вкладки
        speedCalculationSeekBar= findViewById(R.id.calculation_seekBar_speed)
        timeCalculationSeekBar= findViewById(R.id.calculation_seekBar_time)
        distanceCalculationSeekBar = findViewById(R.id.calculation_seekBar_distance)
        // инициализация бегунков для второй вкладки
        tasksSeekBarSpeed = findViewById(R.id.tasks_seekBar_speed)
        tasksSeekBarTime = findViewById(R.id.tasks_seekBar_time)
        tasksSeekBarDistance = findViewById(R.id.tasks_seekBar_distance)
        // обработка переключателей режимов на второй вкладке
        myRadioGroupObject?.setOnCheckedChangeListener { group, checkedId ->
            //toast("Был нажат,  $checkedId")
            if (R.id.red == checkedId){
                toast("Был нажат скорость")
                generateValueForTasks("speed")
            } else if (R.id.green == checkedId){
                toast("Был нажат время")
                generateValueForTasks("time")
            } else if (R.id.yellow== checkedId) {
                toast("Был нажат расстояние")
                generateValueForTasks("distance")
            }
        }

        seekBarsControlFun()
        seekBarControlTasksFun()
    }


    private fun pairdDeviceList(){
        m_pairedDevises = m_bluetoothAdapter!!.bondedDevices// получить список сопряжённых устройств
        val list: ArrayList<BluetoothDevice> = ArrayList()

        if(!m_pairedDevises.isEmpty()){ //если список не пустой
            for(devise:BluetoothDevice in m_pairedDevises){
                // переменная devise типа BluetoothDevice

                list.add(devise)

                Log.i("devise","" + devise)
            }
        } else{
            toast("не найдено сопряженное устройство bluetooth")
        }

        val adapter = ArrayAdapter(this, R.layout.simple_stile_for_list_view, list)//создаём адаптер на основе сопряжённых устройств
        select_device_list.adapter = adapter // передаём его в XML

        select_device_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address

            //val intent = Intent(this, ControlActivity::class.java)
            toast("EXTRA_ADRESS")
            m_address = address
            userLogining = true
            userLoginFirst = true
            connect()
            //переход на 2 активити
            //intent.putExtra(EXTRA_ADRESS, address)
            //startActivity(intent)

        }
    }
    fun connect(){
        if(userLogining){
            toast("userLogining...")
            if (userLoginFirst) {
                ConnectToDevise(this).execute()// подключение к блютузу, вызывая функцию ConnectToDevise,   execute предлагает простой и удобный механизм для перемещения трудоёмких операций в фоновый поток
                // установление событий клика
                //m_address = address
                userLoginFirst = false
                toast("connecting...")
            }
            toast("connecting...")
            //control_led_on.setOnClickListener{sendcommand("a")} //запускаем функцию отправки
            //control_led_off.setOnClickListener{sendcommand("b")}
            //control_led_disconnect.setOnClickListener{disconnect()}//вызываем отключение от блютуза при нажатии на кнопку
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            select_device_refresh.text = "disconnect"
            select_device_refresh.setOnClickListener{disconnect()}//вызываем отключение от блютуза при нажатии на кнопку
            calculation_button_start.setOnClickListener{outputDataForCar()} // ловим нажатие на кнопку отправки в третем окне
            tasks_button_start.setOnClickListener{cookingDataForOutput()} // ловим нажатие на кнопку отправки во втором окне
        }
    }
//-----------------------------------------------------------------------------------------------------
    private fun sendcommand(input:String){// функция отправки
        if (m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())// видимо байтовая отправка
            } catch (e: IOException){
                e.printStackTrace()// вроде выброс исключения
            }
        }

    }
    private fun disconnect(){
        userLogining = false
        userLoginFirst = false
        select_device_refresh.text = "refresh"
        select_device_refresh.setOnClickListener{pairdDeviceList()} //ловим нажатие кнопки refresh, если нажата отправляемся дальше
        navigation.setOnNavigationItemSelectedListener(passiveNavigator)
        if(m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.close() // закрываем соединение
                m_bluetoothSocket = null // удаляем сокет
                m_isConnected = false
            } catch (e: IOException){
                e.printStackTrace()// если сокет уже удален выбрасываем исключение
            }
        }
       // finish()// не понятная вещь
    }
    private class ConnectToDevise(c: Context): AsyncTask<Void, Void, String>(){
        private  var connectSuccess: Boolean = true
        private  var context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() { // метод, выполняемый перед запуском в фоновом процессе подключения к блютузу (в исходниках стоит заглушка на этом методе)
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")// переменная прогресса подключения к модулю блютуз (видимо это и отображается при подключении)
        }

        override fun doInBackground(vararg params: Void?): String? { //Этот метод выполняется в фоновом потоке, поэтому в нем не должно быть никакого взаимодействия с элементами пользовательского интерфейса. Размещайте здесь трудоёмкий код, используя метод publishProgress(), который позволит обработчику onProgressUpdate() передавать изменения в пользовательский интерфейс. Когда фоновая задача завершена, данный метод возвращает конечный результат для обработчика onPostExecute(), который сообщит о нём в поток пользовательского интерфейса.
            try{

                if (m_bluetoothSocket == null || !m_isConnected){//если модуль блютуз отключен, или разорвал соединение (процесс подключения блютуза)
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val devise: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = devise.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }

            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) { // метод выполняется после выполнения фонового процесса подключения
            super.onPostExecute(result)
            if(!connectSuccess){ // если подключится не удалось
                Log.i("data", "couldn t connect")
            } else{
                m_isConnected = true
            }
            m_progress.dismiss()//скорее всего, конец отображения окна подключения к блютузу.
        }
    }

    fun seekBarsControlFun(){ //для изменения состояния в калькуляторе
        // twoSeekBar?.getProgress()
        var u:Int
        var t:Int
        var simpleFlag:Boolean = true

        // устанавливаем обработку для ползунка скорости в третей вкладке
        speedCalculationSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                valueInt = timeCalculationSeekBar?.getProgress()
                if ((valueInt != null)&&(permissionToEdit)){
                    permissionToEdit = false
                    distanceCalculationSeekBar?.setProgress(progress*valueInt!!)
                    permissionToEdit = true
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        // устанавливаем обработку для ползунка времени в третей вкладке
        timeCalculationSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                valueInt = speedCalculationSeekBar?.getProgress()
                if ((valueInt != null)&&(permissionToEdit)){
                    permissionToEdit = false
                    distanceCalculationSeekBar?.setProgress(progress*valueInt!!)
                    permissionToEdit = true

                }
                toast("edit time $valueInt $permissionToEdit")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        // устанавливаем обработку для ползунка расстояния в третей вкладке
        distanceCalculationSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                valueInt = speedCalculationSeekBar?.getProgress()
                valueInt2 = timeCalculationSeekBar?.getProgress()
                if ((valueInt != null) && (valueInt2 != null) &&(permissionToEdit)){
                    permissionToEdit = false
                    simpleFlag = true
                    for (i in 1..51){
                        if((progress%i == 0)&&(progress/i <= 10)){
                            valueInt2 = (progress/i)
                            valueInt = i
                            simpleFlag = false
                        } else if((simpleFlag)&&(progress/i <= 10)){
                            valueInt2 = (progress/i)
                            valueInt = i
                        }
                    }
                    speedCalculationSeekBar?.setProgress(progress*valueInt!!)
                    timeCalculationSeekBar?.setProgress(progress*valueInt!!)
                    permissionToEdit = true
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext, "seekbar touch stopped!", Toast.LENGTH_SHORT).show()
                if ((valueInt != null) && (valueInt2 != null) &&(permissionToEdit)) {
                    permissionToEdit = false
                    speedCalculationSeekBar?.setProgress(valueInt!!)
                    timeCalculationSeekBar?.setProgress(valueInt2!!)
                    permissionToEdit = true
                }
            }
        })


    }

    fun seekBarControlTasksFun(){
        var u:Int
        var t:Int
        var simpleFlag:Boolean = true

        tasksSeekBarSpeed?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if ((whereCheckBoxClickDown != "speed")&&(!permissionToEdit)) {
                    tasksSeekBarSpeed?.setProgress(startValueForSeekBar)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                startValueForSeekBar = tasksSeekBarSpeed?.getProgress()?: 1
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        tasksSeekBarTime?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //valueInt = speedCalculationSeekBar?.getProgress()
                if ((whereCheckBoxClickDown != "time")&&(!permissionToEdit)){
                    tasksSeekBarTime?.setProgress(startValueForSeekBar)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                startValueForSeekBar = tasksSeekBarTime?.getProgress()?: 1
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        tasksSeekBarDistance?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if ((whereCheckBoxClickDown != "distance")&&(!permissionToEdit)) {
                    tasksSeekBarDistance?.setProgress(startValueForSeekBar)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){
                startValueForSeekBar = tasksSeekBarDistance?.getProgress()?: 1
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
    fun cookingDataForOutput(){
        if (whereCheckBoxClickDown == "distance"){
            valueInt = tasksSeekBarSpeed?.getProgress()
            valueInt2 = tasksSeekBarTime?.getProgress()
            if(valueInt == null){valueInt = 0}
            if(valueInt2 == null){valueInt2 = 0}
            sendcommand("/"+valueInt.toString() + "/" + valueInt2.toString()+"/")
        }
        if (whereCheckBoxClickDown == "time"){
            valueInt = tasksSeekBarSpeed?.getProgress()
            valueInt2 = tasksSeekBarDistance?.getProgress() // полуаем расстояние с бегунка 2 окна
            if(valueInt == null){valueInt = 0}
            if(valueInt2 == null){valueInt2 = 0}
            valueInt2 = (valueInt2!! / valueInt!!)!!.toInt() // считаем время, исходя из расстояния
            sendcommand("/"+valueInt.toString() + "/" + valueInt2.toString()+"/")
        }
        if (whereCheckBoxClickDown == "speed"){
            valueInt = tasksSeekBarDistance?.getProgress() // получаем расстояние с бегунка 2 окна
            valueInt2 = tasksSeekBarTime?.getProgress()
            if(valueInt == null){valueInt = 0}
            if(valueInt2 == null){valueInt2 = 0}
            valueInt = (valueInt!! / valueInt2!!)!!.toInt() // ищем скорость
            sendcommand("/"+valueInt.toString() + "/" + valueInt2.toString()+"/")
        }

    }
    fun outputDataForCar(){
        valueInt = speedCalculationSeekBar?.getProgress()
        valueInt2 = timeCalculationSeekBar?.getProgress()
        if(valueInt == null){valueInt = 0}
        if(valueInt2 == null){valueInt2 = 0}
        sendcommand("/"+valueInt.toString() + "/" + valueInt2.toString()+"/")
        //sendcommand()
    }

    fun generateValueForTasks(whereParametrValue:String){
        permissionToEdit = true
        val random = java.util.Random()

        //randomInt = random.nextInt(51)
        //toast("передано")

        if (whereParametrValue == "speed"){
            whereCheckBoxClickDown = "speed"
            var randomInt1 = 1
            var randomInt2 = 1
            randomInt1= random.nextInt(11)
            randomInt2= random.nextInt(501)
            tasksSeekBarTime?.setProgress(randomInt1)
            tasksSeekBarDistance?.setProgress(randomInt2)
            tasksSeekBarSpeed?.setProgress(0)
            //toast("speed")
        }
        else if(whereParametrValue == "time"){
            whereCheckBoxClickDown = "time"
            var randomInt1 = 1
            var randomInt2 = 1
            randomInt1= random.nextInt(51)
            randomInt2= random.nextInt(501)
            tasksSeekBarSpeed?.setProgress(randomInt1)
            tasksSeekBarDistance?.setProgress(randomInt2)
            tasksSeekBarTime?.setProgress(0)

        }else if(whereParametrValue == "distance"){
            whereCheckBoxClickDown = "distance"
            var randomInt1 = 1
            var randomInt2 = 1
            randomInt1= random.nextInt(51)
            randomInt2= random.nextInt(11)
            tasksSeekBarSpeed?.setProgress(randomInt1)
            tasksSeekBarTime?.setProgress(randomInt2)
            tasksSeekBarDistance?.setProgress(0)
        }
        permissionToEdit = false
    }

}