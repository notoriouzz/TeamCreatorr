package nl.tartaruzz.teamcreatorr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.tartaruzz.teamcreatorr.ui.TeamCreatorrRoot
import nl.tartaruzz.teamcreatorr.ui.theme.TeamCreatorrTheme

class MainActivity : ComponentActivity() {
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
