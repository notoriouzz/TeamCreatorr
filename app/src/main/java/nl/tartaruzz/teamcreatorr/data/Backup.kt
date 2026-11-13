package nl.tartaruzz.teamcreatorr.data

import org.json.JSONArray
import org.json.JSONObject

/** JSON backup serialization (photos are intentionally excluded). */
object Backup {
    const val FORMAT_VERSION = 1

    data class Payload(
        val settings: SettingsEntity,
        val players: List<PlayerEntity>,
        val matches: List<MatchEntity>,
        val matchPlayers: List<MatchPlayerEntity>,
        val duos: List<DuoEntity>,
        val matchDuos: List<MatchDuoEntity>,
        val adjustments: List<PointAdjustmentEntity>
    )

    fun toJson(p: Payload): String {
        val root = JSONObject()
        root.put("version", FORMAT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("settings", JSONObject().apply {
            put("attendancePoints", p.settings.attendancePoints)
            put("winPoints", p.settings.winPoints)
            put("drawPoints", p.settings.drawPoints)
        })
        root.put("players", JSONArray().apply {
            p.players.forEach { x ->
                put(JSONObject().apply {
                    put("id", x.id); put("name", x.name)
                    put("nickname", x.nickname ?: JSONObject.NULL)
                    put("active", x.active); put("createdAt", x.createdAt)
                })
            }
        })
        root.put("matches", JSONArray().apply {
            p.matches.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id); put("date", m.date); put("teamSize", m.teamSize)
                    put("scoreA", m.scoreA ?: JSONObject.NULL)
                    put("scoreB", m.scoreB ?: JSONObject.NULL)
                    put("status", m.status); put("createdAt", m.createdAt)
                })
            }
        })
        root.put("matchPlayers", JSONArray().apply {
            p.matchPlayers.forEach { mp ->
                put(JSONObject().apply {
                    put("matchId", mp.matchId); put("playerId", mp.playerId); put("team", mp.team)
                })
            }
        })
        root.put("duos", JSONArray().apply {
            p.duos.forEach { d ->
                put(JSONObject().apply {
                    put("id", d.id); put("playerAId", d.playerAId); put("playerBId", d.playerBId)
                })
            }
        })
        root.put("matchDuos", JSONArray().apply {
            p.matchDuos.forEach { d ->
                put(JSONObject().apply {
                    put("matchId", d.matchId); put("playerAId", d.playerAId); put("playerBId", d.playerBId)
                })
            }
        })
        root.put("pointAdjustments", JSONArray().apply {
            p.adjustments.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id); put("playerId", a.playerId); put("points", a.points)
                    put("description", a.description); put("createdAt", a.createdAt)
                })
            }
        })
        return root.toString()
    }

    /** Returns null when the file is not a valid/compatible TeamCreatorr backup. */
    fun parse(json: String): Payload? = runCatching {
        val root = JSONObject(json)
        if (root.optInt("version", -1) != FORMAT_VERSION) return null
        val s = root.getJSONObject("settings")
        val settings = SettingsEntity(
            id = 1,
            attendancePoints = s.optInt("attendancePoints", 1),
            winPoints = s.optInt("winPoints", 2),
            drawPoints = s.optInt("drawPoints", 1)
        )
        val players = root.getJSONArray("players").mapArr { o ->
            PlayerEntity(
                id = o.getLong("id"),
                name = o.getString("name"),
                nickname = if (o.isNull("nickname")) null else o.getString("nickname"),
                active = o.optBoolean("active", true),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val matches = root.getJSONArray("matches").mapArr { o ->
            MatchEntity(
                id = o.getLong("id"),
                date = o.getLong("date"),
                teamSize = o.getInt("teamSize"),
                scoreA = if (o.isNull("scoreA")) null else o.getInt("scoreA"),
                scoreB = if (o.isNull("scoreB")) null else o.getInt("scoreB"),
                status = o.optInt("status", 0),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val matchPlayers = root.getJSONArray("matchPlayers").mapArr { o ->
            MatchPlayerEntity(o.getLong("matchId"), o.getLong("playerId"), o.getString("team"))
        }
        val duos = root.getJSONArray("duos").mapArr { o ->
            DuoEntity(o.getLong("id"), o.getLong("playerAId"), o.getLong("playerBId"))
        }
        val matchDuos = root.getJSONArray("matchDuos").mapArr { o ->
            MatchDuoEntity(o.getLong("matchId"), o.getLong("playerAId"), o.getLong("playerBId"))
        }
        val adjustments = root.getJSONArray("pointAdjustments").mapArr { o ->
            PointAdjustmentEntity(
                id = o.getLong("id"),
                playerId = o.getLong("playerId"),
                points = o.getInt("points"),
                description = o.getString("description"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        Payload(settings, players, matches, matchPlayers, duos, matchDuos, adjustments)
    }.getOrNull()

    private inline fun <T> JSONArray.mapArr(block: (JSONObject) -> T): List<T> {
        val out = ArrayList<T>(length())
        for (i in 0 until length()) out.add(block(getJSONObject(i)))
        return out
    }
}
