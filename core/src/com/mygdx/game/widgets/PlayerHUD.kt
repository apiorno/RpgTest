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

class PlayerHUD(private val camera: Camera, private val player: Entity, private val mapMgr: MapManager) : Screen, AudioSubject, ProfileObserver, ComponentObserver, ConversationGraphObserver, StoreInventoryObserver, BattleObserver, InventoryObserver, StatusObserver {
    val stage: Stage
    private val viewport: Viewport
    private val statusUI: StatusUI
    private val inventoryUI: InventoryUI
    private val conversationUI: ConversationUI
    private val storeInventoryUI: StoreInventoryUI
    private val questUI: QuestUI
    private val battleUI: BattleUI
    private val messageBoxUI: Dialog
    private val json: Json
    private val observers: Array<AudioObserver?>
    private val transitionActor: ScreenTransitionActor
    private val shakeCam: ShakeCamera
    private val clock: ClockActor

    val currentTimeOfDay: ClockActor.TimeOfDay
        get() = clock.currentTimeOfDay

    fun updateEntityObservers() {
        mapMgr.unregisterCurrentMapEntityObservers()
        questUI.initQuests(mapMgr)
        mapMgr.registerCurrentMapEntityObservers(this)
    }

    fun addTransitionToScreen() {
        transitionActor.isVisible = true
        stage.addAction(
                Actions.sequence(
                        Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1f), transitionActor)))
    }

    @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST")
    override fun onNotify(profileManager: ProfileManager, event: ProfileEvent) {
        when (event) {
            ProfileEvent.PROFILE_LOADED -> {
                val firstTime: Boolean = profileManager.isNewProfile
                if (firstTime) {
                    InventoryUI.clearInventoryItems(inventoryUI.inventorySlotTable)
                    InventoryUI.clearInventoryItems(inventoryUI.equipSlotTable)
                    inventoryUI.resetEquipSlots()
                    questUI.quests = Array()

                    //add default items if first time
                    val items = player.entityConfig!!.inventory
                    val itemLocations = Array<InventoryItemLocation>()
                    var i = 0
                    while (i < items.size) {
                        itemLocations.add(InventoryItemLocation(i, items[i].toString(), 1, InventoryUI.PLAYER_INVENTORY))
                        i++
                    }
                    InventoryUI.populateInventory(inventoryUI.inventorySlotTable, itemLocations, inventoryUI.dragAndDrop, InventoryUI.PLAYER_INVENTORY, false)
                    profileManager.setProperty("playerInventory", InventoryUI.getInventory(inventoryUI.inventorySlotTable))

                    //start the player with some money
                    statusUI.goldValue = 20
                    statusUI.setStatusForLevel(1)
                    clock.totalTime = (60 * 60 * 12).toFloat() //start at noon
                    profileManager.setProperty("currentTime", clock.totalTime)
                } else {
                    val goldVal = profileManager.getProperty("currentPlayerGP", Int::class.java)!!
                    val inventory: Array<InventoryItemLocation> = profileManager.getProperty("playerInventory", Array::class.java) as Array<InventoryItemLocation>
                    InventoryUI.populateInventory(inventoryUI.inventorySlotTable, inventory, inventoryUI.dragAndDrop, InventoryUI.PLAYER_INVENTORY, false)
                    val equipInventory: Array<InventoryItemLocation>? = profileManager.getProperty("playerEquipInventory", Array::class.java) as Array<InventoryItemLocation>?
                    if (equipInventory != null && equipInventory.size > 0) {
                        inventoryUI.resetEquipSlots()
                        InventoryUI.populateInventory(inventoryUI.equipSlotTable, equipInventory, inventoryUI.dragAndDrop, InventoryUI.PLAYER_INVENTORY, false)
                    }
                    val quests: Array<QuestGraph> = profileManager.getProperty("playerQuests", Array::class.java) as Array<QuestGraph>
                    questUI.quests = quests
                    val xpMaxVal = profileManager.getProperty("currentPlayerXPMax", Int::class.java)!!
                    val xpVal = profileManager.getProperty("currentPlayerXP", Int::class.java)!!
                    val hpMaxVal = profileManager.getProperty("currentPlayerHPMax", Int::class.java)!!
                    val hpVal = profileManager.getProperty("currentPlayerHP", Int::class.java)!!
                    val mpMaxVal = profileManager.getProperty("currentPlayerMPMax", Int::class.java)!!
                    val mpVal = profileManager.getProperty("currentPlayerMP", Int::class.java)!!
                    val levelVal = profileManager.getProperty("currentPlayerLevel", Int::class.java)!!

                    //set the current max values first
                    statusUI.xpValueMax = xpMaxVal
                    statusUI.hpValueMax = hpMaxVal
                    statusUI.mpValueMax = mpMaxVal
                    statusUI.xpValue = xpVal
                    statusUI.hpValue = hpVal
                    statusUI.mpValue = mpVal

                    //then add in current values
                    statusUI.goldValue = goldVal
                    statusUI.levelValue = levelVal
                    val totalTime = profileManager.getProperty("currentTime", Float::class.java)!!
                    clock.totalTime = totalTime
                }
            }
            ProfileEvent.SAVING_PROFILE -> {
                profileManager.setProperty("playerQuests", questUI.quests)
                profileManager.setProperty("playerInventory", InventoryUI.getInventory(inventoryUI.inventorySlotTable))
                profileManager.setProperty("playerEquipInventory", InventoryUI.getInventory(inventoryUI.equipSlotTable))
                profileManager.setProperty("currentPlayerGP", statusUI.goldValue)
                profileManager.setProperty("currentPlayerLevel", statusUI.levelValue)
                profileManager.setProperty("currentPlayerXP", statusUI.xpValue)
                profileManager.setProperty("currentPlayerXPMax", statusUI.xpValueMax)
                profileManager.setProperty("currentPlayerHP", statusUI.hpValue)
                profileManager.setProperty("currentPlayerHPMax", statusUI.hpValueMax)
                profileManager.setProperty("currentPlayerMP", statusUI.mpValue)
                profileManager.setProperty("currentPlayerMPMax", statusUI.mpValueMax)
                profileManager.setProperty("currentTime", clock.totalTime)
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
                var config = json.fromJson(EntityConfig::class.java, value)

                //Check to see if there is a version loading into properties
                if (config.itemTypeID.equals(ItemTypeID.NONE.toString(), ignoreCase = true)) {
                    val configReturnProperty = ProfileManager.getProperty(config.entityID!!, EntityConfig::class.java)
                    if (configReturnProperty != null) {
                        config = configReturnProperty
                    }
                }
                conversationUI.loadConversation(config)
                conversationUI.currentConversationGraph?.addObserver(this)
            }
            ComponentObserver.ComponentEvent.SHOW_CONVERSATION -> {
                val configShow = json.fromJson(EntityConfig::class.java, value)
                if (configShow.entityID.equals(conversationUI.currentEntityID, ignoreCase = true)) {
                    conversationUI.isVisible = true
                }
            }
            ComponentObserver.ComponentEvent.HIDE_CONVERSATION -> {
                val configHide = json.fromJson(EntityConfig::class.java, value)
                if (configHide.entityID.equals(conversationUI.currentEntityID, ignoreCase = true)) {
                    conversationUI.isVisible = false
                }
            }
            ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED -> {
                val string = value.split(Component.MESSAGE_TOKEN).toTypedArray()
                val questID = string[0]
                val questTaskID = string[1]
                questUI.questTaskComplete(questID, questTaskID)
                updateEntityObservers()
            }
            ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED -> {
                battleUI.battleZoneTriggered(value.toInt())
            }
            ComponentObserver.ComponentEvent.PLAYER_HAS_MOVED -> if (battleUI.isBattleReady) {
                addTransitionToScreen()
                setGameState(MainGameScreen.GameState.SAVING)
                mapMgr.disableCurrentMapMusic()
                notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_BATTLE)
                battleUI.toBack()
                battleUI.isVisible = true
            }
        }
    }

    override fun onNotify(graph: ConversationGraph, event: ConversationCommandEvent) {
        when (event) {
            ConversationCommandEvent.LOAD_STORE_INVENTORY -> run{
                val selectedEntity = mapMgr.currentSelectedMapEntity ?: return@run
                val inventory: Array<InventoryItemLocation> = InventoryUI.getInventory(inventoryUI.inventorySlotTable)
                storeInventoryUI.loadPlayerInventory(inventory)
                val items = selectedEntity.entityConfig!!.inventory
                val itemLocations = Array<InventoryItemLocation>()
                var i = 0
                while (i < items.size) {
                    itemLocations.add(InventoryItemLocation(i, items[i].toString(), 1, InventoryUI.STORE_INVENTORY))
                    i++
                }
                storeInventoryUI.loadStoreInventory(itemLocations)
                conversationUI.isVisible = false
                storeInventoryUI.toFront()
                storeInventoryUI.isVisible = true
            }
            ConversationCommandEvent.EXIT_CONVERSATION -> {
                conversationUI.isVisible = false
                mapMgr.clearCurrentSelectedMapEntity()
            }
            ConversationCommandEvent.ACCEPT_QUEST -> run{
                val currentlySelectedEntity = mapMgr.currentSelectedMapEntity ?: return@run
                val config = currentlySelectedEntity.entityConfig
                val questGraph = questUI.loadQuest(config!!.questConfigPath)
                if (questGraph != null) {
                    //Update conversation dialog
                    config.conversationConfigPath = QuestUI.RETURN_QUEST
                    config.currentQuestID = questGraph.questID
                    ProfileManager.setProperty(config.entityID!!, config)
                    updateEntityObservers()
                }
                conversationUI.isVisible = false
                mapMgr.clearCurrentSelectedMapEntity()
            }
            ConversationCommandEvent.RETURN_QUEST -> run{
                val returnEntity = mapMgr.currentSelectedMapEntity ?: return@run
                val configReturn = returnEntity.entityConfig
                val configReturnProperty = ProfileManager.getProperty(configReturn!!.entityID!!, EntityConfig::class.java)
                        ?: return
                val questID = configReturnProperty.currentQuestID
                if (questUI.isQuestReadyForReturn(questID)) {
                    notify(AudioCommand.MUSIC_PLAY_ONCE, AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE)
                    val quest = questUI.getQuestByID(questID)
                    statusUI.addXPValue(quest!!.xpReward)
                    statusUI.addGoldValue(quest.goldReward)
                    notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_COIN_RUSTLE)
                    inventoryUI.removeQuestItemFromInventory(questID)
                    configReturnProperty.conversationConfigPath = QuestUI.FINISHED_QUEST
                    ProfileManager.setProperty(configReturnProperty.entityID!!, configReturnProperty)
                }
                conversationUI.isVisible = false
                mapMgr.clearCurrentSelectedMapEntity()
            }
            ConversationCommandEvent.ADD_ENTITY_TO_INVENTORY -> run{
                val entity = mapMgr.currentSelectedMapEntity ?: return@run
                if (inventoryUI.doesInventoryHaveSpace()) {
                    inventoryUI.addEntityToInventory(entity, entity.entityConfig!!.currentQuestID)
                    mapMgr.clearCurrentSelectedMapEntity()
                    conversationUI.isVisible = false
                    entity.unregisterObservers()
                    mapMgr.removeMapQuestEntity(entity)
                    questUI.updateQuests(mapMgr)
                } else {
                    mapMgr.clearCurrentSelectedMapEntity()
                    conversationUI.isVisible = false
                    messageBoxUI.isVisible = true
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
                statusUI.goldValue = `val`
                notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_COIN_RUSTLE)
            }
            StoreInventoryEvent.PLAYER_INVENTORY_UPDATED -> {
                val items: Array<InventoryItemLocation> = json.fromJson(Array::class.java, value) as Array<InventoryItemLocation>
                InventoryUI.populateInventory(inventoryUI.inventorySlotTable, items, inventoryUI.dragAndDrop, InventoryUI.PLAYER_INVENTORY, false)
            }
        }
    }

    override fun onNotify(value: Int, event: StatusEvent) {
        when (event) {
            StatusEvent.UPDATED_GP -> {
                storeInventoryUI.setPlayerGP(value)
                ProfileManager.setProperty("currentPlayerGP", statusUI.goldValue)
            }
            StatusEvent.UPDATED_HP -> ProfileManager.setProperty("currentPlayerHP", statusUI.hpValue)
            StatusEvent.UPDATED_LEVEL -> ProfileManager.setProperty("currentPlayerLevel", statusUI.levelValue)
            StatusEvent.UPDATED_MP -> ProfileManager.setProperty("currentPlayerMP", statusUI.mpValue)
            StatusEvent.UPDATED_XP -> ProfileManager.setProperty("currentPlayerXP", statusUI.xpValue)
            StatusEvent.LEVELED_UP -> notify(AudioCommand.MUSIC_PLAY_ONCE, AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE)
        }
    }

    override fun show() {
        shakeCam.reset()
    }

    override fun render(delta: Float) {
        if (shakeCam.isCameraShaking) {
            val shakeCoords = shakeCam.newShakePosition
            camera.position.x = shakeCoords.x + stage.width / 2
            camera.position.y = shakeCoords.y + stage.height / 2
        }
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        battleUI.validate()
        battleUI.resize()
    }

    override fun pause() {
        battleUI.resetDefaults()
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
                statusUI.addGoldValue(goldReward)
                val xpReward = enemyEntity.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_XP_REWARD.toString()).toInt()
                statusUI.addXPValue(xpReward)
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_BATTLE)
                mapMgr.enableCurrentMapMusic()
                addTransitionToScreen()
                battleUI.isVisible = false
            }
            BattleEvent.PLAYER_RUNNING -> {
                setGameState(MainGameScreen.GameState.RUNNING)
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_BATTLE)
                mapMgr.enableCurrentMapMusic()
                addTransitionToScreen()
                battleUI.isVisible = false
            }
            BattleEvent.PLAYER_HIT_DAMAGE -> {
                notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_PLAYER_PAIN)
                val hpVal = ProfileManager.getProperty("currentPlayerHP", Int::class.java)!!
                statusUI.hpValue = hpVal
                shakeCam.startShaking()
                if (hpVal <= 0) {
                    shakeCam.reset()
                    notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_BATTLE)
                    addTransitionToScreen()
                    battleUI.isVisible = false
                    setGameState(MainGameScreen.GameState.GAME_OVER)
                }
            }
            BattleEvent.PLAYER_USED_MAGIC -> {
                notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK)
                val mpVal = ProfileManager.getProperty("currentPlayerMP", Int::class.java)!!
                statusUI.mpValue = mpVal
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
                    statusUI.addHPValue(typeValue)
                } else if (doesRestoreMP(type)) {
                    notify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SOUND_DRINKING)
                    statusUI.addMPValue(typeValue)
                }
            }
            else -> {
            }
        }
    }

    override fun addObserver(audioObserver: AudioObserver) {
        observers.add(audioObserver)
    }

    override fun removeObserver(audioObserver: AudioObserver) {
        observers.removeValue(audioObserver, true)
    }

    override fun removeAllObservers() {
        observers.removeAll(observers, true)
    }

    override fun notify(command: AudioCommand, event: AudioTypeEvent) {
        observers.forEach { it?.onNotify(command,event) }
    }

    companion object {
        private val TAG = PlayerHUD::class.java.simpleName
        private const val INVENTORY_FULL = "Your inventory is full!"
    }

    init {
        viewport = ScreenViewport(camera)
        stage = Stage(viewport)
        //_stage.setDebugAll(true);
        observers = Array()
        transitionActor = ScreenTransitionActor()
        shakeCam = ShakeCamera(0F, 0F, 30.0f)
        json = Json()
        messageBoxUI = object : Dialog("Message", Utility.STATUSUI_SKIN, "solidbackground") {
            override fun result(`object`: Any) {
                cancel()
                isVisible = false
            }

            init {
                button("OK")
                text(INVENTORY_FULL)
            }
        }
        clock = ClockActor("0", Utility.STATUSUI_SKIN)
        clock.setPosition(stage.width - clock.width, 0f)
        clock.rateOfTime = 60F
        clock.isVisible = true
        messageBoxUI.isVisible = false
        messageBoxUI.pack()
        messageBoxUI.setPosition(stage.width / 2 - messageBoxUI.width / 2, stage.height / 2 - messageBoxUI.height / 2)
        statusUI = StatusUI()
        statusUI.isVisible = true
        statusUI.setPosition(0f, 0f)
        statusUI.setKeepWithinStage(false)
        statusUI.isMovable = false
        inventoryUI = InventoryUI()
        inventoryUI.setKeepWithinStage(false)
        inventoryUI.isMovable = false
        inventoryUI.isVisible = false
        inventoryUI.setPosition(statusUI.width, 0f)
        conversationUI = ConversationUI()
        conversationUI.isMovable = true
        conversationUI.isVisible = false
        conversationUI.setPosition(stage.width / 2, 0f)
        conversationUI.width = stage.width / 2
        conversationUI.height = stage.height / 2
        storeInventoryUI = StoreInventoryUI()
        storeInventoryUI.isMovable = false
        storeInventoryUI.isVisible = false
        storeInventoryUI.setPosition(0f, 0f)
        questUI = QuestUI()
        questUI.isMovable = false
        questUI.isVisible = false
        questUI.setKeepWithinStage(false)
        questUI.setPosition(0f, stage.height / 2)
        questUI.width = stage.width
        questUI.height = stage.height / 2
        battleUI = BattleUI()
        battleUI.isMovable = false
        //removes all listeners including ones that handle focus
        battleUI.clearListeners()
        battleUI.isVisible = false
        stage.addActor(battleUI)
        stage.addActor(questUI)
        stage.addActor(storeInventoryUI)
        stage.addActor(conversationUI)
        stage.addActor(messageBoxUI)
        stage.addActor(statusUI)
        stage.addActor(inventoryUI)
        stage.addActor(clock)
        battleUI.validate()
        questUI.validate()
        storeInventoryUI.validate()
        conversationUI.validate()
        messageBoxUI.validate()
        statusUI.validate()
        inventoryUI.validate()
        clock.validate()

        //add tooltips to the stage
        val actors = inventoryUI.inventoryActors
        actors.forEach { stage.addActor(it) }

        val storeActors = storeInventoryUI.inventoryActors
        storeActors.forEach { stage.addActor(it) }

        stage.addActor(transitionActor)
        transitionActor.isVisible = false

        //Observers
        player.registerObserver(this)
        statusUI.addObserver(this)
        storeInventoryUI.addObserver(this)
        inventoryUI.addObserver(battleUI.currentState!!)
        inventoryUI.addObserver(this)
        battleUI.currentState!!.addObserver(this)
        addObserver(AudioManager)

        //Listeners
        val inventoryButton = statusUI.inventoryButton
        inventoryButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                inventoryUI.isVisible = !inventoryUI.isVisible
            }
        })
        val questButton = statusUI.questButton
        questButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                questUI.isVisible = !questUI.isVisible
            }
        })
        conversationUI.closeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                conversationUI.isVisible = false
                mapMgr.clearCurrentSelectedMapEntity()
            }
        }
        )
        storeInventoryUI.closeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                storeInventoryUI.savePlayerInventory()
                storeInventoryUI.cleanupStoreInventory()
                storeInventoryUI.isVisible = false
                mapMgr.clearCurrentSelectedMapEntity()
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