package nl.tartaruzz.teamcreatorr

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import nl.tartaruzz.teamcreatorr.ui.TeamCreatorrRoot
import nl.tartaruzz.teamcreatorr.ui.theme.TeamCreatorrTheme
import nl.tartaruzz.teamcreatorr.util.AppPrefs
import nl.tartaruzz.teamcreatorr.util.LanguagePrefs

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePrefs.applyLocale(newBase, LanguagePrefs.get(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applySystemBarAppearance()
        val darkTheme = AppPrefs.darkModeEnabled(this)
        setContent {
            TeamCreatorrTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeamCreatorrRoot()
                }
            }
        }
    }

    private fun applySystemBarAppearance() {
        val darkTheme = AppPrefs.darkModeEnabled(this)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
