@file:Suppress("unused", "UNUSED_PARAMETER")
package android.app
import android.content.Context
import android.content.Intent
import android.os.Bundle
abstract class Application : Context() { open fun onCreate() {} }
abstract class Activity : Context() {
    val intent: Intent get() = TODO()
    val application: Application get() = TODO()
    protected open fun onCreate(savedInstanceState: Bundle?) {}
    fun setResult(resultCode: Int, data: Intent?) {}
    open fun finish() {}
    companion object {
        const val RESULT_OK = -1
        const val RESULT_CANCELED = 0
    }
}
