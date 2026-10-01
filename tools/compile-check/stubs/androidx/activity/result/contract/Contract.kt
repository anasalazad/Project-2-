package androidx.activity.result.contract
import android.content.Context
import android.content.Intent
abstract class ActivityResultContract<I, O> {
    abstract fun createIntent(context: Context, input: I): Intent
    abstract fun parseResult(resultCode: Int, intent: Intent?): O
}
