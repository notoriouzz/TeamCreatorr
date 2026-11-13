package nl.tartaruzz.teamcreatorr

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.tartaruzz.teamcreatorr.ui.TeamCreatorrRoot
import nl.tartaruzz.teamcreatorr.ui.theme.TeamCreatorrTheme
import nl.tartaruzz.teamcreatorr.util.LanguagePrefs

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePrefs.applyLocale(newBase, LanguagePrefs.get(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeamCreatorrTheme {
                TeamCreatorrRoot()
            }
        }
    }
}
