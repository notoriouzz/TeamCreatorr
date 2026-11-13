package nl.tartaruzz.teamcreatorr

import android.app.Application
import nl.tartaruzz.teamcreatorr.data.Repository
import nl.tartaruzz.teamcreatorr.data.TeamCreatorrDatabase

class TeamCreatorrApp : Application() {

    val repository: Repository by lazy {
        Repository(TeamCreatorrDatabase.get(this))
    }
}
