package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import java.util.ArrayList

class LevelTable {
    var levelID: String? = null
    var xpMax = 0
    var hpMax = 0
    var mpMax = 0

    companion object {
        @JvmStatic
        fun getLevelTables(configFilePath: String?): Array<LevelTable> {
            val json = Json()
            val levelTable = Array<LevelTable>()
            val list: ArrayList<JsonValue> = json.fromJson(ArrayList::class.java, Gdx.files.internal(configFilePath)) as ArrayList<JsonValue>
            for (jsonVal in list) {
                val table = json.readValue(LevelTable::class.java, jsonVal)
                levelTable.add(table)
            }
            return levelTable
        }
    }
}