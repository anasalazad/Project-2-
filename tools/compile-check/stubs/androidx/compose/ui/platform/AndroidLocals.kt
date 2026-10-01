package androidx.compose.ui.platform
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
val LocalContext: ProvidableCompositionLocal<Context> = staticCompositionLocalOf { TODO() }
val LocalConfiguration: ProvidableCompositionLocal<Configuration> = staticCompositionLocalOf { TODO() }
