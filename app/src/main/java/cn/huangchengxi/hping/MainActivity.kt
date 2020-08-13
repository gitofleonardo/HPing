package cn.huangchengxi.hping

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import kotlin.concurrent.thread

class MainActivity : BaseActivity(),View.OnClickListener {
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val pingBtn by lazy { findViewById<FloatingActionButton>(R.id.pingBtn) }
    private val pingIp by lazy { findViewById<EditText>(R.id.ipAddress) }
    private val pingList by lazy { findViewById<ListView>(R.id.pingList) }
    private val pingResults=ArrayList<String>()
    private val adapter by lazy { ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,pingResults) }
    private var ipType=IPType.IPV4
    private var pingThread:Thread?=null
    private var pingProcess:Process?=null
    private val mHandler=MHandler()

    private val RESULT_ARRIVAL=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setStatusBarTransparent()
        fitToolbarToWindow(toolbar)
        init()
    }
    private fun init(){
        toolbar.title=resources.getString(R.string.ipv4)
        setSupportActionBar(toolbar)
        pingBtn.setOnClickListener(this)
        pingList.adapter=adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.ipv4->{
                toolbar.title=SpannableStringBuilder(resources.getString(R.string.ipv4))
                ipType=IPType.IPV4
                return true
            }
            R.id.ipv6->{
                toolbar.title=SpannableStringBuilder(resources.getString(R.string.ipv6))
                ipType=IPType.IPV6
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private enum class IPType{
        IPV4,
        IPV6
    }

    override fun onClick(p0: View?) {
        if (p0==null) return
        when (p0.id){
            R.id.pingBtn->{
                when (ipType){
                    IPType.IPV4->{
                        pingIpv4()
                    }
                    IPType.IPV6->{
                        pingIpv6()
                    }
                }
            }
        }
    }
    private fun pingIpv4(){
        pingResults.clear()
        adapter.notifyDataSetChanged()
        val address=pingIp.text.toString()
        if (address==""){
            pingIp.error="IP address not valid"
            return
        }
        val cmd="ping -c 5 -W 1 -t 64 $address"
        pingThread?.interrupt()
        pingThread= thread(start = true) {
            val rt=Runtime.getRuntime()
            try {
                Log.e("exec",cmd)
                pingProcess=rt.exec(cmd)
                val br=BufferedReader(InputStreamReader(pingProcess!!.inputStream))
                val errorBr=BufferedReader(InputStreamReader(pingProcess!!.errorStream))
                var line=br.readLine()
                while (line!=null){
                    val msg=mHandler.obtainMessage()
                    msg.what=RESULT_ARRIVAL
                    msg.obj=line
                    mHandler.sendMessage(msg)
                    line=br.readLine()
                }
                line=errorBr.readLine()
                while (line!=null){
                    val msg=mHandler.obtainMessage()
                    msg.what=RESULT_ARRIVAL
                    msg.obj=line
                    mHandler.sendMessage(msg)
                    line=errorBr.readLine()
                }
            }catch (e:Exception){
                pingProcess?.destroy()
            }
        }
    }
    private fun pingIpv6(){
        pingResults.clear()
        adapter.notifyDataSetChanged()
        val address=pingIp.text.toString()
        if (address==""){
            pingIp.error="IP address not valid"
            return
        }
        val cmd="ping6 -c 5 $address"
        pingThread?.interrupt()
        pingThread= thread(start = true) {
            val rt=Runtime.getRuntime()
            try {
                pingProcess=rt.exec(cmd)
                val br=BufferedReader(InputStreamReader(pingProcess!!.inputStream))
                var line=br.readLine()
                while (line!=null){
                    Log.e("ping6","$line")
                    val msg=mHandler.obtainMessage()
                    msg.what=RESULT_ARRIVAL
                    msg.obj=line
                    mHandler.sendMessage(msg)
                }
                val errorBr=BufferedReader(InputStreamReader(pingProcess!!.errorStream))
                line=errorBr.readLine()
                while (line!=null){
                    val msg=mHandler.obtainMessage()
                    msg.what=RESULT_ARRIVAL
                    msg.obj=line
                    mHandler.sendMessage(msg)
                    line=errorBr.readLine()
                }
            }catch (e:Exception){
                pingProcess?.destroy()
            }
        }
    }
    @SuppressLint("HandlerLeak","Deprecated")
    private inner class MHandler():Handler(){
        override fun handleMessage(msg: Message) {
            when (msg.what){
                RESULT_ARRIVAL->{
                    pingResults.add(msg.obj as String)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}