package com.mygdx.game.temporal

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.ashley.core.Entity
import com.mygdx.game.EntityConfig
import getEntityConfigs
import java.util.*

 class MonsterFactory private constructor() {
    enum class MonsterEntityType {
        MONSTER001, MONSTER002, MONSTER003, MONSTER004, MONSTER005, MONSTER006, MONSTER007, MONSTER008, MONSTER009, MONSTER010, MONSTER011, MONSTER012, MONSTER013, MONSTER014, MONSTER015, MONSTER016, MONSTER017, MONSTER018, MONSTER019, MONSTER020, MONSTER021, MONSTER022, MONSTER023, MONSTER024, MONSTER025, MONSTER026, MONSTER027, MONSTER028, MONSTER029, MONSTER030, MONSTER031, MONSTER032, MONSTER033, MONSTER034, MONSTER035, MONSTER036, MONSTER037, MONSTER038, MONSTER039, MONSTER040, MONSTER041, MONSTER042, NONE
    }

    private val entities: Hashtable<String?, EntityConfig> = Hashtable<String?, EntityConfig>()
    private val monsterZones: Hashtable<String?, Array<MonsterEntityType>?>

    fun getMonster(monsterEntityType: MonsterEntityType): Entity? {
        val config = getMonsterConfigByType(monsterEntityType)!!
        return EntityFactory.instance!!.getEntity(EntityFactory.EntityType.NPC, config)
    }

      fun getMonsterConfigByType(monsterEntityType: MonsterFactory.MonsterEntityType): EntityConfig? {
            return entities[monsterEntityType.toString()]
     }

     fun getRandomMonster(monsterZoneID: Int): Entity? {
        val monsters = monsterZones[monsterZoneID.toString()]
        val size = monsters!!.size
        if (size == 0) {
            return null
        }
        val randomIndex = MathUtils.random(size - 1)
        return getMonster(monsters[randomIndex])
    }

    companion object {
        private var uniqueInstance: MonsterFactory? = null
        val instance: MonsterFactory?
            get() {
                if (uniqueInstance == null) {
                    uniqueInstance = MonsterFactory()
                }
                return uniqueInstance
            }
    }

    init {
        val configs: Array<EntityConfig> = getEntityConfigs("scripts/monsters.json")
        initEntities(configs)
        monsterZones = MonsterZone.getMonsterZones("scripts/monster_zones.json")
    }

     private fun initEntities(configs: Array<EntityConfig>) {
         //val entities = Hashtable<String?, Entity?>()
         for (config in configs) {
             //val entity =EntityFactory.instance!!.getEntity(EntityFactory.EntityType.NPC, config)
             entities[config.entityID] = config
         }
         //return entities
     }
 }