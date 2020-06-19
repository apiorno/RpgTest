package com.mygdx.game.widgets

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mygdx.game.widgets.StatusObserver.StatusEvent
import com.mygdx.game.Utility
import com.mygdx.game.battle.LevelTable
import com.mygdx.game.battle.LevelTable.Companion.getLevelTables

class StatusUI : Window("stats", Utility.STATUSUI_SKIN), StatusSubject {
    private val _hpBar: Image
    private val _mpBar: Image
    private val _xpBar: Image
    val inventoryButton: ImageButton
    val questButton: ImageButton
    private val _observers: Array<StatusObserver>
    private val _levelTables: Array<LevelTable>

    //Attributes
    private var _levelVal = -1
    private var _goldVal = -1
    var hpValue = -1
    var mpValue = -1
    var xpValue = 0
    var xpValueMax = -1
    var hpValueMax = -1
    var mpValueMax = -1
    private val _hpValLabel: Label
    private val _mpValLabel: Label
    private val _xpValLabel: Label
    private val _levelValLabel: Label
    private val _goldValLabel: Label
    private var _barWidth = 0f
    private var _barHeight = 0f

    var levelValue: Int
        get() = _levelVal
        set(levelValue) {
            _levelVal = levelValue
            _levelValLabel.setText(_levelVal.toString())
            notify(_levelVal, StatusEvent.UPDATED_LEVEL)
        }

    var goldValue: Int
        get() = _goldVal
        set(goldValue) {
            _goldVal = goldValue
            _goldValLabel.setText(_goldVal.toString())
            notify(_goldVal, StatusEvent.UPDATED_GP)
        }

    fun addGoldValue(goldValue: Int) {
        _goldVal += goldValue
        _goldValLabel.setText(_goldVal.toString())
        notify(_goldVal, StatusEvent.UPDATED_GP)
    }

    var xPValue: Int
        get() = xpValue
        set(xpValue) {
            this.xpValue = xpValue
            if (this.xpValue > xpValueMax) {
                updateToNewLevel()
            }
            _xpValLabel.setText(this.xpValue.toString())
            updateBar(_xpBar, this.xpValue, xpValueMax)
            notify(this.xpValue, StatusEvent.UPDATED_XP)
        }

    fun addXPValue(xpValue: Int) {
        this.xpValue += xpValue
        if (this.xpValue > xpValueMax) {
            updateToNewLevel()
        }
        _xpValLabel.setText(this.xpValue.toString())
        updateBar(_xpBar, this.xpValue, xpValueMax)
        notify(this.xpValue, StatusEvent.UPDATED_XP)
    }

    fun setStatusForLevel(level: Int) {
        for (table in _levelTables) {
            if (table.levelID!!.toInt() == level) {
                xpValueMax = table.xpMax
                xPValue = 0
                hpValueMax = table.hpMax
                hPValue = table.hpMax
                mpValueMax = table.mpMax
                mPValue = table.mpMax
                levelValue = table.levelID!!.toInt()
                return
            }
        }
    }

    fun updateToNewLevel() {
        for (table in _levelTables) {
            //System.out.println("XPVAL " + _xpVal + " table XPMAX " + table.getXpMax() );
            if (xpValue > table.xpMax) {
                continue
            } else {
                xpValueMax = table.xpMax
                hpValueMax = table.hpMax
                hPValue = table.hpMax
                mpValueMax = table.mpMax
                mPValue = table.mpMax
                levelValue = table.levelID!!.toInt()
                notify(_levelVal, StatusEvent.LEVELED_UP)
                return
            }
        }
    }

    //HP
    var hPValue: Int
        get() = hpValue
        set(hpValue) {
            this.hpValue = hpValue
            _hpValLabel.setText(this.hpValue.toString())
            updateBar(_hpBar, this.hpValue, hpValueMax)
            notify(this.hpValue, StatusEvent.UPDATED_HP)
        }

    fun removeHPValue(hpValue: Int) {
        this.hpValue = MathUtils.clamp(this.hpValue - hpValue, 0, hpValueMax)
        _hpValLabel.setText(this.hpValue.toString())
        updateBar(_hpBar, this.hpValue, hpValueMax)
        notify(this.hpValue, StatusEvent.UPDATED_HP)
    }

    fun addHPValue(hpValue: Int) {
        this.hpValue = MathUtils.clamp(this.hpValue + hpValue, 0, hpValueMax)
        _hpValLabel.setText(this.hpValue.toString())
        updateBar(_hpBar, this.hpValue, hpValueMax)
        notify(this.hpValue, StatusEvent.UPDATED_HP)
    }

    //MP
    var mPValue: Int
        get() = mpValue
        set(mpValue) {
            this.mpValue = mpValue
            _mpValLabel.setText(this.mpValue.toString())
            updateBar(_mpBar, this.mpValue, mpValueMax)
            notify(this.mpValue, StatusEvent.UPDATED_MP)
        }

    fun removeMPValue(mpValue: Int) {
        this.mpValue = MathUtils.clamp(this.mpValue - mpValue, 0, mpValueMax)
        _mpValLabel.setText(this.mpValue.toString())
        updateBar(_mpBar, this.mpValue, mpValueMax)
        notify(this.mpValue, StatusEvent.UPDATED_MP)
    }

    fun addMPValue(mpValue: Int) {
        this.mpValue = MathUtils.clamp(this.mpValue + mpValue, 0, mpValueMax)
        _mpValLabel.setText(this.mpValue.toString())
        updateBar(_mpBar, this.mpValue, mpValueMax)
        notify(this.mpValue, StatusEvent.UPDATED_MP)
    }

    fun updateBar(bar: Image, currentVal: Int, maxVal: Int) {
        val `val` = MathUtils.clamp(currentVal, 0, maxVal)
        val tempPercent = `val`.toFloat() / maxVal.toFloat()
        val percentage = MathUtils.clamp(tempPercent, 0f, 100f)
        bar.setSize(_barWidth * percentage, _barHeight)
    }

    override fun addObserver(statusObserver: StatusObserver) {
        _observers.add(statusObserver)
    }

    override fun removeObserver(statusObserver: StatusObserver) {
        _observers.removeValue(statusObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in _observers) {
            _observers.removeValue(observer, true)
        }
    }

    override fun notify(value: Int, event: StatusEvent) {
        for (observer in _observers) {
            observer.onNotify(value, event)
        }
    }

    companion object {
        private const val LEVEL_TABLE_CONFIG = "scripts/level_tables.json"
    }

    init {
        _levelTables = getLevelTables(LEVEL_TABLE_CONFIG)
        _observers = Array()

        //groups
        val group = WidgetGroup()
        val group2 = WidgetGroup()
        val group3 = WidgetGroup()

        //images
        _hpBar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("HP_Bar"))
        val bar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"))
        _mpBar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("MP_Bar"))
        val bar2 = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"))
        _xpBar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("XP_Bar"))
        val bar3 = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"))
        _barWidth = _hpBar.width
        _barHeight = _hpBar.height


        //labels
        val hpLabel = Label(" hp: ", Utility.STATUSUI_SKIN)
        _hpValLabel = Label(hpValue.toString(), Utility.STATUSUI_SKIN)
        val mpLabel = Label(" mp: ", Utility.STATUSUI_SKIN)
        _mpValLabel = Label(mpValue.toString(), Utility.STATUSUI_SKIN)
        val xpLabel = Label(" xp: ", Utility.STATUSUI_SKIN)
        _xpValLabel = Label(xpValue.toString(), Utility.STATUSUI_SKIN)
        val levelLabel = Label(" lv: ", Utility.STATUSUI_SKIN)
        _levelValLabel = Label(_levelVal.toString(), Utility.STATUSUI_SKIN)
        val goldLabel = Label(" gp: ", Utility.STATUSUI_SKIN)
        _goldValLabel = Label(_goldVal.toString(), Utility.STATUSUI_SKIN)

        //buttons
        inventoryButton = ImageButton(Utility.STATUSUI_SKIN, "inventory-button")
        inventoryButton.imageCell.size(32f, 32f)
        questButton = ImageButton(Utility.STATUSUI_SKIN, "quest-button")
        questButton.imageCell.size(32f, 32f)

        //Align images
        _hpBar.setPosition(3f, 6f)
        _mpBar.setPosition(3f, 6f)
        _xpBar.setPosition(3f, 6f)

        //add to widget groups
        group.addActor(bar)
        group.addActor(_hpBar)
        group2.addActor(bar2)
        group2.addActor(_mpBar)
        group3.addActor(bar3)
        group3.addActor(_xpBar)

        //Add to layout
        defaults().expand().fill()

        //account for the title padding
        this.pad(padTop + 10, 10f, 10f, 10f)
        this.add()
        this.add(questButton).align(Align.center)
        this.add(inventoryButton).align(Align.right)
        row()
        this.add(group).size(bar.width, bar.height).padRight(10f)
        this.add(hpLabel)
        this.add(_hpValLabel).align(Align.left)
        row()
        this.add(group2).size(bar2.width, bar2.height).padRight(10f)
        this.add(mpLabel)
        this.add(_mpValLabel).align(Align.left)
        row()
        this.add(group3).size(bar3.width, bar3.height).padRight(10f)
        this.add(xpLabel)
        this.add(_xpValLabel).align(Align.left).padRight(20f)
        row()
        this.add(levelLabel).align(Align.left)
        this.add(_levelValLabel).align(Align.left)
        row()
        this.add(goldLabel)
        this.add(_goldValLabel).align(Align.left)

        //this.debug();
        pack()
    }
}