package com.mygdx.game.temporal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.mygdx.game.temporal.MonsterFactory.*
import java.util.*

class MonsterZone {
    var zoneID: String? = null
    var monsters: Array<MonsterEntityType>? = null

    companion object {
        fun getMonsterZones(configFilePath: String?): Hashtable<String?, Array<MonsterEntityType>?> {
            val json = Json()
            val monsterZones = Hashtable<String?, Array<MonsterEntityType>?>()
            val list: ArrayList<JsonValue> = json.fromJson(ArrayList::class.java, Gdx.files.internal(configFilePath)) as ArrayList<JsonValue>
            for (jsonVal in list) {
                val zone = json.readValue(MonsterZone::class.java, jsonVal)
                monsterZones[zone.zoneID] = zone.monsters
            }
            return monsterZones
        }
    }
}