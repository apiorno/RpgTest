package com.mygdx.game.maps

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Json
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.ecs.EntityFactory.EntityName
import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.EntityConfig
import com.mygdx.game.ecs.EntityFactory
import com.mygdx.game.ecs.PlayerPhysicsComponent
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.sfx.ParticleEffectFactory
import com.mygdx.game.sfx.ParticleEffectFactory.ParticleEffectType

class TownMap internal constructor() : Map(MapType.TOWN, mapPath) {
     override var json: Json = Json()
    override fun unloadMusic() {
        notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TOWN)
    }

    override fun loadMusic() {
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_TOWN)
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_TOWN)
    }

    private fun initSpecialEntityPosition(entity: Entity) {
        var position = Vector2(0F, 0F)
        if (specialNPCStartPositions.containsKey(entity.entityConfig!!.entityID)) {
            position = specialNPCStartPositions[entity.entityConfig!!.entityID]!!
        }
        entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(position))

        //Overwrite default if special config is found
        val entityConfig = ProfileManager.getProperty(entity.entityConfig!!.entityID!!, EntityConfig::class.java)
        if (entityConfig != null) {
            entity.entityConfig = entityConfig
        }
    }

    companion object {
        private val TAG = PlayerPhysicsComponent::class.java.simpleName
        private const val mapPath = "maps/town.tmx"
    }

    init {
        npcStartPositions.forEach {
            val entity: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_GUARD_WALKING)!!
            entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(it))
            mapEntities.add(entity)
        }

        //Special cases
        val blackSmith: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_BLACKSMITH)!!
        initSpecialEntityPosition(blackSmith)
        mapEntities.add(blackSmith)
        val mage: Entity = EntityFactory.Companion.instance!!.getEntityByName(EntityName.TOWN_MAGE)!!
        initSpecialEntityPosition(mage)
        mapEntities.add(mage)
        val innKeeper: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_INNKEEPER)!!
        initSpecialEntityPosition(innKeeper)
        mapEntities.add(innKeeper)
        val townfolk1: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK1)!!
        initSpecialEntityPosition(townfolk1)
        mapEntities.add(townfolk1)
        val townfolk2: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK2)!!
        initSpecialEntityPosition(townfolk2)
        mapEntities.add(townfolk2)
        val townfolk3: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK3)!!
        initSpecialEntityPosition(townfolk3)
        mapEntities.add(townfolk3)
        val townfolk4: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK4)!!
        initSpecialEntityPosition(townfolk4)
        mapEntities.add(townfolk4)
        val townfolk5: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK5)!!
        initSpecialEntityPosition(townfolk5)
        mapEntities.add(townfolk5)
        val townfolk6: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK6)!!
        initSpecialEntityPosition(townfolk6)
        mapEntities.add(townfolk6)
        val townfolk7: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK7)!!
        initSpecialEntityPosition(townfolk7)
        mapEntities.add(townfolk7)
        val townfolk8: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK8)!!
        initSpecialEntityPosition(townfolk8)
        mapEntities.add(townfolk8)
        val townfolk9: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK9)!!
        initSpecialEntityPosition(townfolk9)
        mapEntities.add(townfolk9)
        val townfolk10: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK10)!!
        initSpecialEntityPosition(townfolk10)
        mapEntities.add(townfolk10)
        val townfolk11: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK11)!!
        initSpecialEntityPosition(townfolk11)
        mapEntities.add(townfolk11)
        val townfolk12: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK12)!!
        initSpecialEntityPosition(townfolk12)
        mapEntities.add(townfolk12)
        val townfolk13: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK13)!!
        initSpecialEntityPosition(townfolk13)
        mapEntities.add(townfolk13)
        val townfolk14: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK14)!!
        initSpecialEntityPosition(townfolk14)
        mapEntities.add(townfolk14)
        val townfolk15: Entity = EntityFactory.instance!!.getEntityByName(EntityName.TOWN_FOLK15)!!
        initSpecialEntityPosition(townfolk15)
        mapEntities.add(townfolk15)
        val candleEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.CANDLE_FIRE)
        candleEffectPositions.forEach { mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.CANDLE_FIRE, it)) }
        val lanternEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.LANTERN_FIRE)
        lanternEffectPositions.forEach { mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.LANTERN_FIRE, it)) }
    }
}