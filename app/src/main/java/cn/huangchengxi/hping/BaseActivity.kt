package cn.huangchengxi.hping

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

open class BaseActivity:AppCompatActivity() {
    protected fun setStatusBarTransparent(){
        if (Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT){
            return
        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor= Color.TRANSPARENT
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
    protected fun fitToolbarToWindow(toolbar: Toolbar){
        val lp=toolbar.layoutParams
        lp.height+=getStatusBarHeight(this)
        toolbar.setPadding(toolbar.paddingLeft,getStatusBarHeight(this),toolbar.paddingRight,toolbar.paddingBottom)
        toolbar.layoutParams=lp
    }
    open fun getStatusBarHeight(context: Context): Int {
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }
}