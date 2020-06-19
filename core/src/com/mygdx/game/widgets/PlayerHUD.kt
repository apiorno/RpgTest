package com.mygdx.game.widgets

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.game.*
import com.mygdx.game.widgets.InventoryItem.Companion.doesRestoreHP
import com.mygdx.game.widgets.InventoryItem.Companion.doesRestoreMP
import com.mygdx.game.widgets.InventoryItem.ItemTypeID
import com.mygdx.game.widgets.InventoryObserver.InventoryEvent
import com.mygdx.game.widgets.StatusObserver.StatusEvent
import com.mygdx.game.widgets.StoreInventoryObserver.StoreInventoryEvent
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.audio.AudioSubject
import com.mygdx.game.battle.BattleObserver
import com.mygdx.game.battle.BattleObserver.BattleEvent
import com.mygdx.game.dialog.ConversationGraph
import com.mygdx.game.dialog.ConversationGraphObserver
import com.mygdx.game.dialog.ConversationGraphObserver.ConversationCommandEvent
import com.mygdx.game.ecs.Component
import com.mygdx.game.ecs.ComponentObserver
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.EntityConfig
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.profile.ProfileObserver
import com.mygdx.game.profile.ProfileObserver.ProfileEvent
import com.mygdx.game.quest.QuestGraph
import com.mygdx.game.screens.MainGameScreen
import com.mygdx.game.screens.MainGameScreen.Companion.setGameState
import com.mygdx.game.sfx.ClockActor
import com.mygdx.game.sfx.ScreenTransitionAction
import com.mygdx.game.sfx.ScreenTransitionAction.Companion.transition
import com.mygdx.game.sfx.ScreenTransitionActor
import com.mygdx.game.sfx.ShakeCamera

class PlayerHUD(private val _camera: Camera, private val _player: Entity, private val _mapMgr: MapManager) : Screen, AudioSubject, ProfileObserver, ComponentObserver, ConversationGraphObserver, StoreInventoryObserver, BattleObserver, InventoryObserver, StatusObserver {
    val stage: Stage
    private val _viewport: Viewport
    private val _statusUI: StatusUI
    private val _inventoryUI: InventoryUI
    private val _conversationUI: ConversationUI
    private val _storeInventoryUI: StoreInventoryUI
    private val _questUI: QuestUI
    private val _battleUI: BattleUI
    private val _messageBoxUI: Dialog
    private val _json: Json
    private val _observers: Array<AudioObserver?>
    private val _transitionActor: ScreenTransitionActor
    private val _shakeCam: ShakeCamera
    private val _clock: ClockActor

    val currentTimeOfDay: ClockActor.TimeOfDay
        get() = _clock.currentTimeOfDay

    fun updateEntityObservers() {
        _mapMgr.unregisterCurrentMapEntityObservers()
        _questUI.initQuests(_mapMgr)
        _mapMgr.registerCurrentMapEntityObservers(this)
    }

    fun addTransitionToScreen() {
        _transitionActor.isVisible = true
        stage.addAction(
                Actions.sequence(
                        Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1f), _transitionActor)))
    }

    @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST")
    override fun onNotify(profileManager: ProfileManager, event: ProfileEvent) {
        when (event) {
            ProfileEvent.PROFILE_LOADED -> {
                val firstTime: Boolean = profileManager.isNewProfile
                if (firstTime) {
                    InventoryUI.Companion.clearInventoryItems(_inventoryUI.inventorySlotTable)
                    InventoryUI.Companion.clearInventoryItems(_inventoryUI.equipSlotTable)
                    _inventoryUI.resetEquipSlots()
                    _questUI.quests = Array()

                    //add default items if first time
                    val items = _player.entityConfig!!.inventory
                    val itemLocations = Array<InventoryItemLocation>()
                    var i = 0
                    while (i < items.size) {
                        itemLocations.add(InventoryItemLocation(i, items[i].toString(), 1, InventoryUI.Companion.PLAYER_INVENTORY))
                        i++
                    }
                    InventoryUI.Companion.populateInventory(_inventoryUI.inventorySlotTable, itemLocations, _inventoryUI.dragAndDrop, InventoryUI.Companion.PLAYER_INVENTORY, false)
                    profileManager.setProperty("playerInventory", InventoryUI.Companion.getInventory(_inventoryUI.inventorySlotTable))

                    //start the player with some money
                    _statusUI.goldValue = 20
                    _statusUI.setStatusForLevel(1)
                    _clock.totalTime = (60 * 60 * 12).toFloat() //start at noon
                    profileManager.setProperty("currentTime", _clock.totalTime)
                } else {
                    val goldVal = profileManager.getProperty("currentPlayerGP", Int::class.java)!!
                    val inventory: Array<InventoryItemLocation> = profileManager.getProperty("playerInventory", Array::class.java) as Array<InventoryItemLocation>
                    InventoryUI.Companion.populateInventory(_inventoryUI.inventorySlotTable, inventory, _inventoryUI.dragAndDrop, InventoryUI.Companion.PLAYER_INVENTORY, false)
                    val equipInventory: Array<InventoryItemLocation>? = profileManager.getProperty("playerEquipInventory", Array::class.java) as Array<InventoryItemLocation>?
                    if (equipInventory != null && equipInventory.size > 0) {
                        _inventoryUI.resetEquipSlots()
                        InventoryUI.Companion.populateInventory(_inventoryUI.equipSlotTable, equipInventory, _inventoryUI.dragAndDrop, InventoryUI.Companion.PLAYER_INVENTORY, false)
                    }
                    val quests: Array<QuestGraph> = profileManager.getProperty("playerQuests", Array::class.java) as Array<QuestGraph>
                    _questUI.quests = quests
                    val xpMaxVal = profileManager.getProperty("currentPlayerXPMax", Int::class.java)!!
                    val xpVal = profileManager.getProperty("currentPlayerXP", Int::class.java)!!
                    val hpMaxVal = profileManager.getProperty("currentPlayerHPMax", Int::class.java)!!
                    val hpVal = profileManager.getProperty("currentPlayerHP", Int::class.java)!!
                    val mpMaxVal = profileManager.getProperty("currentPlayerMPMax", Int::class.java)!!
                    val mpVal = profileManager.getProperty("currentPlayerMP", Int::class.java)!!
                    val levelVal = profileManager.getProperty("currentPlayerLevel", Int::class.java)!!

                    //set the current max values first
                    _statusUI.xpValueMax = xpMaxVal
                    _statusUI.hpValueMax = hpMaxVal
                    _statusUI.mpValueMax = mpMaxVal
                    _statusUI.xpValue = xpVal
                    _statusUI.hpValue = hpVal
                    _statusUI.mpValue = mpVal

                    //then add in current values
                    _statusUI.goldValue = goldVal
                    _statusUI.levelValue = levelVal
                    val totalTime = profileManager.getProperty("currentTime", Float::class.java)!!
                    _clock.totalTime = totalTime
                }
            }
            ProfileEvent.SAVING_PROFILE -> {
                profileManager.setProperty("playerQuests", _questUI.quests)
                profileManager.setProperty("playerInventory", InventoryUI.Companion.getInventory(_inventoryUI.inventorySlotTable))
                profileManager.setProperty("playerEquipInventory", InventoryUI.Companion.getInventory(_inventoryUI.equipSlotTable))
                profileManager.setProperty("currentPlayerGP", _statusUI.goldValue)
                profileManager.setProperty("currentPlayerLevel", _statusUI.levelValue)
                profileManager.setProperty("currentPlayerXP", _statusUI.xpValue)
                profileManager.setProperty("currentPlayerXPMax", _statusUI.xpValueMax)
                profileManager.setProperty("currentPlayerHP", _statusUI.hpValue)
                profileManager.setProperty("currentPlayerHPMax", _statusUI.hpValueMax)
                profileManager.setProperty("currentPlayerMP", _statusUI.mpValue)
                profileManager.setProperty("currentPlayerMPMax", _statusUI.mpValueMax)
                profileManager.setProperty("currentTime", _clock.totalTime)
            }
            ProfileEvent.CLEAR_CURRENT_PROFILE -> {
                profileManager.setProperty("playerQuests", Array<QuestGraph>())
                profileManager.setProperty("playerInventory", Array<InventoryItemLocation>())
                profileManager.setProperty("playerEquipInventory", Array<InventoryItemLocation>())
                profileManager.setProperty("currentPlayerGP", 0)
                profileManager.setProperty("currentPlayerLevel", 0)
                profileManager.setProperty("currentPlayerXP", 0)
                profileManager.setProperty("currentPlayerXPMax", 0)
                profileManager.setProperty("currentPlayerHP", 0)
                profileManager.setProperty("currentPlayerHPMax", 0)
                profileManager.setProperty("currentPlayerMP", 0)
                profileManager.setProperty("currentPlayerMPMax", 0)
                profileManager.setProperty("currentTime", 0)
            }
        }
    }

    override fun onNotify(value: String, event: ComponentObserver.ComponentEvent) {
        when (event) {
            ComponentObserver.ComponentEvent.LOAD_CONVERSATION -> {
                var config = _json.fromJson(EntityConfig::class.java, value)

                //Check to see if there is a version loading into properties
                if (config.itemTypeID.equals(ItemTypeID.NONE.toString(), ignoreCase = true)) {
                    val configReturnProperty = ProfileManager.instance.getProperty(config.entityID!!, EntityConfig::class.java)
                    if (configReturnProperty != null) {
                        config = configReturnProperty
                    }
                }
                _conversationUI.loadConversation(config)
                _conversationUI.currentConversationGraph?.addObserver(this)
            }
            ComponentObserver.ComponentEvent.SHOW_CONVERSATION -> {
                val configShow = _json.fromJson(EntityConfig::class.java, value)
                if (configShow.entityID.equals(_conversationUI.currentEntityID, ignoreCase = true)) {
                    _conversationUI.isVisible = true
                }
            }
            ComponentObserver.ComponentEvent.HIDE_CONVERSATION -> {
                val configHide = _json.fromJson(EntityConfig::class.java, value)
                if (configHide.entityID.equals(_conversationUI.currentEntityID, ignoreCase = true)) {
                    _conversationUI.isVisible = false
                }
            }
            ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED -> {
                val string = value.split(Component.MESSAGE_TOKEN).toTypedArray()
                val questID = string[0]
                val questTaskID = string[1]
                _questUI.questTaskComplete(questID, questTaskID)
                updateEntityObservers()
            }
            ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED -> {
                _battleUI.battleZoneTriggered(value.toInt())
            }
            ComponentObserver.ComponentEvent.PLAYER_HAS_MOVED -> if (_battleUI.isBattleReady) {
                addTransitionToScreen()
                setGameState(MainGameScreen.GameState.SAVING)
                _mapMgr.disableCurrentmapMusic()
                notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_BATTLE)
                _battleUI.toBack()
                _battleUI.isVisible = true
            }
        }
    }

    override fun onNotify(graph: ConversationGraph, event: ConversationCommandEvent) {
        when (event) {
            ConversationCommandEvent.LOAD_STORE_INVENTORY -> run{
                val selectedEntity = _mapMgr.currentSelectedMapEntity ?: return@run
                val inventory: Array<InventoryItemLocation> = InventoryUI.Companion.getInventory(_inventoryUI.inventorySlotTable)
                _storeInventoryUI.loadPlayerInventory(inventory)
                val items = selectedEntity.entityConfig!!.inventory
                val itemLocations = Array<InventoryItemLocation>()
                var i = 0
                while (i < items.size) {
                    itemLocations.add(InventoryItemLocation(i, items[i].toString(), 1, InventoryUI.Companion.STORE_INVENTORY))
                    i++
                }
                _storeInventoryUI.loadStoreInventory(itemLocations)
                _conversationUI.isVisible = false
                _storeInventoryUI.toFront()
                _storeInventoryUI.isVisible = true
            }
            ConversationCommandEvent.EXIT_CONVERSATION -> {
                _conversationUI.isVisible = false
                _mapMgr.clearCurrentSelectedMapEntity()
            }
            ConversationCommandEvent.ACCEPT_QUEST -> run{
                val currentlySelectedEntity = _mapMgr.currentSelectedMapEntity ?: return@run
                val config = currentlySelectedEntity.entityConfig
                val questGraph = _questUI.loadQuest(config!!.questConfigPath)
                if (questGraph != null) {
                    //Update conversation dialog
                    config.conversationConfigPath = QuestUI.Companion.RETURN_QUEST
                    config.currentQuestID = questGraph.questID
                    ProfileManager.instance.setProperty(config.entityID!!, config)
                    updateEntityObservers()
                }
                _conversationUI.isVisible = false
                _mapMgr.clearCurrentSelectedMapEntity()
            }
            ConversationCommandEvent.RETURN_QUEST -> run{
                val returnEntity = _mapMgr.currentSelectedMapEntity ?: return@run
                val configReturn = returnEntity.entityConfig
                val configReturnProperty = ProfileManager.instance.getProperty(configReturn!!.entityID!!, EntityConfig::class.java)
                        ?: return
                val questID = configReturnProperty.currentQuestID
                if (_questUI.isQuestReadyForReturn(questID)) {
                    notify(AudioCommand.MUSIC_PLAY_ONCE, AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE)
                    val quest = _questUI.getQuestByID(questID)
                    _statusUI.addXPValue(quest!!.xpReward)
                    _statusUI.addGoldValue(quest.goldReward)
                    notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_COIN_RUSTLE)
                    _inventoryUI.removeQuestItemFromInventory(questID)
                    configReturnProperty.conversationConfigPath = QuestUI.Companion.FINISHED_QUEST
                    ProfileManager.instance.setProperty(configReturnProperty.entityID!!, configReturnProperty)
                }
                _conversationUI.isVisible = false
                _mapMgr.clearCurrentSelectedMapEntity()
            }
            ConversationCommandEvent.ADD_ENTITY_TO_INVENTORY -> run{
                val entity = _mapMgr.currentSelectedMapEntity ?: return@run
                if (_inventoryUI.doesInventoryHaveSpace()) {
                    _inventoryUI.addEntityToInventory(entity, entity.entityConfig!!.currentQuestID)
                    _mapMgr.clearCurrentSelectedMapEntity()
                    _conversationUI.isVisible = false
                    entity.unregisterObservers()
                    _mapMgr.removeMapQuestEntity(entity)
                    _questUI.updateQuests(_mapMgr)
                } else {
                    _mapMgr.clearCurrentSelectedMapEntity()
                    _conversationUI.isVisible = false
                    _messageBoxUI.isVisible = true
                }
            }
            ConversationCommandEvent.NONE -> {
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onNotify(value: String, event: StoreInventoryEvent) {
        when (event) {
            StoreInventoryEvent.PLAYER_GP_TOTAL_UPDATED -> {
                val `val` = Integer.valueOf(value)
                _statusUI.goldValue = `val`
                notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_COIN_RUSTLE)
            }
            StoreInventoryEvent.PLAYER_INVENTORY_UPDATED -> {
                val items: Array<InventoryItemLocation> = _json.fromJson(Array::class.java, value) as Array<InventoryItemLocation>
                InventoryUI.Companion.populateInventory(_inventoryUI.inventorySlotTable, items, _inventoryUI.dragAndDrop, InventoryUI.Companion.PLAYER_INVENTORY, false)
            }
        }
    }

    override fun onNotify(value: Int, event: StatusEvent) {
        when (event) {
            StatusEvent.UPDATED_GP -> {
                _storeInventoryUI.setPlayerGP(value)
                ProfileManager.instance.setProperty("currentPlayerGP", _statusUI.goldValue)
            }
            StatusEvent.UPDATED_HP -> ProfileManager.instance.setProperty("currentPlayerHP", _statusUI.hpValue)
            StatusEvent.UPDATED_LEVEL -> ProfileManager.instance.setProperty("currentPlayerLevel", _statusUI.levelValue)
            StatusEvent.UPDATED_MP -> ProfileManager.instance.setProperty("currentPlayerMP", _statusUI.mpValue)
            StatusEvent.UPDATED_XP -> ProfileManager.instance.setProperty("currentPlayerXP", _statusUI.xpValue)
            StatusEvent.LEVELED_UP -> notify(AudioCommand.MUSIC_PLAY_ONCE, AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE)
        }
    }

    override fun show() {
        _shakeCam.reset()
    }

    override fun render(delta: Float) {
        if (_shakeCam.isCameraShaking) {
            val shakeCoords = _shakeCam.newShakePosition
            _camera.position.x = shakeCoords.x + stage.width / 2
            _camera.position.y = shakeCoords.y + stage.height / 2
        }
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        _battleUI.validate()
        _battleUI.resize()
    }

    override fun pause() {
        _battleUI.resetDefaults()
    }

    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        stage.dispose()
    }

    override fun onNotify(enemyEntity: Entity, event: BattleEvent) {
        when (event) {
            BattleEvent.OPPONENT_HIT_DAMAGE -> notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_CREATURE_PAIN)
            BattleEvent.OPPONENT_DEFEATED -> {
                setGameState(MainGameScreen.GameState.RUNNING)
                val goldReward = enemyEntity.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_GP_REWARD.toString()).toInt()
                _statusUI.addGoldValue(goldReward)
                val xpReward = enemyEntity.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_XP_REWARD.toString()).toInt()
                _statusUI.addXPValue(xpReward)
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_BATTLE)
                _mapMgr.enableCurrentmapMusic()
                addTransitionToScreen()
                _battleUI.isVisible = false
            }
            BattleEvent.PLAYER_RUNNING -> {
                setGameState(MainGameScreen.GameState.RUNNING)
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_BATTLE)
                _mapMgr.enableCurrentmapMusic()
                addTransitionToScreen()
                _battleUI.isVisible = false
            }
            BattleEvent.PLAYER_HIT_DAMAGE -> {
                notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_PLAYER_PAIN)
                val hpVal = ProfileManager.instance.getProperty("currentPlayerHP", Int::class.java)!!
                _statusUI.hpValue = hpVal
                _shakeCam.startShaking()
                if (hpVal <= 0) {
                    _shakeCam.reset()
                    notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_BATTLE)
                    addTransitionToScreen()
                    _battleUI.isVisible = false
                    setGameState(MainGameScreen.GameState.GAME_OVER)
                }
            }
            BattleEvent.PLAYER_USED_MAGIC -> {
                notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK)
                val mpVal = ProfileManager.instance.getProperty("currentPlayerMP", Int::class.java)!!
                _statusUI.mpValue = mpVal
            }
            else -> {
            }
        }
    }

    override fun onNotify(value: String, event: InventoryEvent) {
        when (event) {
            InventoryEvent.ITEM_CONSUMED -> {
                val strings = value.split(Component.MESSAGE_TOKEN).toTypedArray()
                if (strings.size != 2) return
                val type = strings[0].toInt()
                val typeValue = strings[1].toInt()
                if (doesRestoreHP(type)) {
                    notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_EATING)
                    _statusUI.addHPValue(typeValue)
                } else if (doesRestoreMP(type)) {
                    notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_DRINKING)
                    _statusUI.addMPValue(typeValue)
                }
            }
            else -> {
            }
        }
    }

    override fun addObserver(audioObserver: AudioObserver) {
        _observers.add(audioObserver)
    }

    override fun removeObserver(audioObserver: AudioObserver) {
        _observers.removeValue(audioObserver, true)
    }

    override fun removeAllObservers() {
        _observers.removeAll(_observers, true)
    }

    override fun notify(command: AudioCommand, event: AudioTypeEvent) {
        for (observer in _observers) {
            observer!!.onNotify(command, event)
        }
    }

    companion object {
        private val TAG = PlayerHUD::class.java.simpleName
        private const val INVENTORY_FULL = "Your inventory is full!"
    }

    init {
        _viewport = ScreenViewport(_camera)
        stage = Stage(_viewport)
        //_stage.setDebugAll(true);
        _observers = Array()
        _transitionActor = ScreenTransitionActor()
        _shakeCam = ShakeCamera(0F, 0F, 30.0f)
        _json = Json()
        _messageBoxUI = object : Dialog("Message", Utility.STATUSUI_SKIN, "solidbackground") {
            override fun result(`object`: Any) {
                cancel()
                isVisible = false
            }

            init {
                button("OK")
                text(INVENTORY_FULL)
            }
        }
        _clock = ClockActor("0", Utility.STATUSUI_SKIN)
        _clock.setPosition(stage.width - _clock.width, 0f)
        _clock.rateOfTime = 60F
        _clock.isVisible = true
        _messageBoxUI.isVisible = false
        _messageBoxUI.pack()
        _messageBoxUI.setPosition(stage.width / 2 - _messageBoxUI.width / 2, stage.height / 2 - _messageBoxUI.height / 2)
        _statusUI = StatusUI()
        _statusUI.isVisible = true
        _statusUI.setPosition(0f, 0f)
        _statusUI.setKeepWithinStage(false)
        _statusUI.isMovable = false
        _inventoryUI = InventoryUI()
        _inventoryUI.setKeepWithinStage(false)
        _inventoryUI.isMovable = false
        _inventoryUI.isVisible = false
        _inventoryUI.setPosition(_statusUI.width, 0f)
        _conversationUI = ConversationUI()
        _conversationUI.isMovable = true
        _conversationUI.isVisible = false
        _conversationUI.setPosition(stage.width / 2, 0f)
        _conversationUI.width = stage.width / 2
        _conversationUI.height = stage.height / 2
        _storeInventoryUI = StoreInventoryUI()
        _storeInventoryUI.isMovable = false
        _storeInventoryUI.isVisible = false
        _storeInventoryUI.setPosition(0f, 0f)
        _questUI = QuestUI()
        _questUI.isMovable = false
        _questUI.isVisible = false
        _questUI.setKeepWithinStage(false)
        _questUI.setPosition(0f, stage.height / 2)
        _questUI.width = stage.width
        _questUI.height = stage.height / 2
        _battleUI = BattleUI()
        _battleUI.isMovable = false
        //removes all listeners including ones that handle focus
        _battleUI.clearListeners()
        _battleUI.isVisible = false
        stage.addActor(_battleUI)
        stage.addActor(_questUI)
        stage.addActor(_storeInventoryUI)
        stage.addActor(_conversationUI)
        stage.addActor(_messageBoxUI)
        stage.addActor(_statusUI)
        stage.addActor(_inventoryUI)
        stage.addActor(_clock)
        _battleUI.validate()
        _questUI.validate()
        _storeInventoryUI.validate()
        _conversationUI.validate()
        _messageBoxUI.validate()
        _statusUI.validate()
        _inventoryUI.validate()
        _clock.validate()

        //add tooltips to the stage
        val actors = _inventoryUI.inventoryActors
        for (actor in actors) {
            stage.addActor(actor)
        }
        val storeActors = _storeInventoryUI.inventoryActors
        for (actor in storeActors) {
            stage.addActor(actor)
        }
        stage.addActor(_transitionActor)
        _transitionActor.isVisible = false

        //Observers
        _player.registerObserver(this)
        _statusUI.addObserver(this)
        _storeInventoryUI.addObserver(this)
        _inventoryUI.addObserver(_battleUI.currentState!!)
        _inventoryUI.addObserver(this)
        _battleUI.currentState!!.addObserver(this)
        addObserver(AudioManager.instance)

        //Listeners
        val inventoryButton = _statusUI.inventoryButton
        inventoryButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                _inventoryUI.isVisible = if (_inventoryUI.isVisible) false else true
            }
        })
        val questButton = _statusUI.questButton
        questButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                _questUI.isVisible = if (_questUI.isVisible) false else true
            }
        })
        _conversationUI.closeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                _conversationUI.isVisible = false
                _mapMgr.clearCurrentSelectedMapEntity()
            }
        }
        )
        _storeInventoryUI.closeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                _storeInventoryUI.savePlayerInventory()
                _storeInventoryUI.cleanupStoreInventory()
                _storeInventoryUI.isVisible = false
                _mapMgr.clearCurrentSelectedMapEntity()
            }
        }
        )

        //Music/Sound loading
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_BATTLE)
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE)
        notify(AudioCommand.SOUND_LOAD, AudioTypeEvent.SOUND_COIN_RUSTLE)
        notify(AudioCommand.SOUND_LOAD, AudioTypeEvent.SOUND_CREATURE_PAIN)
        notify(AudioCommand.SOUND_LOAD, AudioTypeEvent.SOUND_PLAYER_PAIN)
        notify(AudioCommand.SOUND_LOAD, AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK)
        notify(AudioCommand.SOUND_LOAD, AudioTypeEvent.SOUND_EATING)
        notify(AudioCommand.SOUND_LOAD, AudioTypeEvent.SOUND_DRINKING)
    }
}