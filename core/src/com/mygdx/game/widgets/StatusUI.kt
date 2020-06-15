package com.mygdx.game.widgets

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mygdx.game.Utility
import com.mygdx.game.temporal.StatusObserver
import com.mygdx.game.temporal.StatusObserver.*
import com.mygdx.game.temporal.StatusSubject
import com.mygdx.game.widgets.LevelTable.Companion.getLevelTables

class StatusUI : Window("stats", Utility.STATUSUI_SKIN), StatusSubject {
    private val hpBar: Image
    private val mpBar: Image
    private val xpBar: Image
    val inventoryButton: ImageButton
    val questButton: ImageButton
    private val observers: Array<StatusObserver>
    private val levelTables: Array<LevelTable>

    //Attributes
    private var levelVal = -1
    private var goldVal = -1
    var hpValue = -1
    var mpValue = -1
    var xpValue = 0
    var xpValueMax = -1
    var hpValueMax = -1
    var mpValueMax = -1
    private val hpValLabel: Label
    private val mpValLabel: Label
    private val xpValLabel: Label
    private val levelValLabel: Label
    private val goldValLabel: Label
    private var barWidth = 0f
    private var barHeight = 0f

    var levelValue: Int
        get() = levelVal
        set(levelValue) {
            levelVal = levelValue
            levelValLabel.setText(levelVal.toString())
            notify(levelVal, StatusEvent.UPDATED_LEVEL)
        }

    var goldValue: Int
        get() = goldVal
        set(goldValue) {
            goldVal = goldValue
            goldValLabel.setText(goldVal.toString())
            notify(goldVal, StatusEvent.UPDATED_GP)
        }

    fun addGoldValue(goldValue: Int) {
        goldVal += goldValue
        goldValLabel.setText(goldVal.toString())
        notify(goldVal, StatusEvent.UPDATED_GP)
    }

    var xPValue: Int
        get() = xpValue
        set(xpValue) {
            this.xpValue = xpValue
            if (this.xpValue > xpValueMax) {
                updateToNewLevel()
            }
            xpValLabel.setText(this.xpValue.toString())
            updateBar(xpBar, this.xpValue, xpValueMax)
            notify(this.xpValue, StatusEvent.UPDATED_XP)
        }

    fun addXPValue(xpValue: Int) {
        this.xpValue += xpValue
        if (this.xpValue > xpValueMax) {
            updateToNewLevel()
        }
        xpValLabel.setText(this.xpValue.toString())
        updateBar(xpBar, this.xpValue, xpValueMax)
        notify(this.xpValue, StatusEvent.UPDATED_XP)
    }

    fun setStatusForLevel(level: Int) {
        for (table in levelTables) {
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
        for (table in levelTables) {
            //System.out.println("XPVAL " + xpVal + " table XPMAX " + table.getXpMax() );
            if (xpValue > table.xpMax) {
                continue
            } else {
                xpValueMax = table.xpMax
                hpValueMax = table.hpMax
                hPValue = table.hpMax
                mpValueMax = table.mpMax
                mPValue = table.mpMax
                levelValue = table.levelID!!.toInt()
                notify(levelVal, StatusEvent.LEVELED_UP)
                return
            }
        }
    }

    //HP
    var hPValue: Int
        get() = hpValue
        set(hpValue) {
            this.hpValue = hpValue
            hpValLabel.setText(this.hpValue.toString())
            updateBar(hpBar, this.hpValue, hpValueMax)
            notify(this.hpValue, StatusEvent.UPDATED_HP)
        }

    fun removeHPValue(hpValue: Int) {
        this.hpValue = MathUtils.clamp(this.hpValue - hpValue, 0, hpValueMax)
        hpValLabel.setText(this.hpValue.toString())
        updateBar(hpBar, this.hpValue, hpValueMax)
        notify(this.hpValue, StatusEvent.UPDATED_HP)
    }

    fun addHPValue(hpValue: Int) {
        this.hpValue = MathUtils.clamp(this.hpValue + hpValue, 0, hpValueMax)
        hpValLabel.setText(this.hpValue.toString())
        updateBar(hpBar, this.hpValue, hpValueMax)
        notify(this.hpValue, StatusEvent.UPDATED_HP)
    }

    //MP
    var mPValue: Int
        get() = mpValue
        set(mpValue) {
            this.mpValue = mpValue
            mpValLabel.setText(this.mpValue.toString())
            updateBar(mpBar, this.mpValue, mpValueMax)
            notify(this.mpValue, StatusEvent.UPDATED_MP)
        }

    fun removeMPValue(mpValue: Int) {
        this.mpValue = MathUtils.clamp(this.mpValue - mpValue, 0, mpValueMax)
        mpValLabel.setText(this.mpValue.toString())
        updateBar(mpBar, this.mpValue, mpValueMax)
        notify(this.mpValue, StatusEvent.UPDATED_MP)
    }

    fun addMPValue(mpValue: Int) {
        this.mpValue = MathUtils.clamp(this.mpValue + mpValue, 0, mpValueMax)
        mpValLabel.setText(this.mpValue.toString())
        updateBar(mpBar, this.mpValue, mpValueMax)
        notify(this.mpValue, StatusEvent.UPDATED_MP)
    }

    fun updateBar(bar: Image, currentVal: Int, maxVal: Int) {
        val `val` = MathUtils.clamp(currentVal, 0, maxVal)
        val tempPercent = `val`.toFloat() / maxVal.toFloat()
        val percentage = MathUtils.clamp(tempPercent, 0f, 100f)
        bar.setSize(barWidth * percentage, barHeight)
    }

    override fun addObserver(statusObserver: StatusObserver) {
        observers.add(statusObserver)
    }

    override fun removeObserver(statusObserver: StatusObserver) {
        observers.removeValue(statusObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in observers) {
            observers.removeValue(observer, true)
        }
    }

    override fun notify(value: Int, event: StatusEvent?) {
        for (observer in observers) {
            observer.onNotify(value, event)
        }
    }

    companion object {
        private const val LEVEL_TABLE_CONFIG = "scripts/level_tables.json"
    }

    init {
        levelTables = getLevelTables(LEVEL_TABLE_CONFIG)
        observers = Array()

        //groups
        val group = WidgetGroup()
        val group2 = WidgetGroup()
        val group3 = WidgetGroup()

        //images
        hpBar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("HP_Bar"))
        val bar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"))
        mpBar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("MP_Bar"))
        val bar2 = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"))
        xpBar = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("XP_Bar"))
        val bar3 = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"))
        barWidth = hpBar.width
        barHeight = hpBar.height


        //labels
        val hpLabel = Label(" hp: ", Utility.STATUSUI_SKIN)
        hpValLabel = Label(hpValue.toString(), Utility.STATUSUI_SKIN)
        val mpLabel = Label(" mp: ", Utility.STATUSUI_SKIN)
        mpValLabel = Label(mpValue.toString(), Utility.STATUSUI_SKIN)
        val xpLabel = Label(" xp: ", Utility.STATUSUI_SKIN)
        xpValLabel = Label(xpValue.toString(), Utility.STATUSUI_SKIN)
        val levelLabel = Label(" lv: ", Utility.STATUSUI_SKIN)
        levelValLabel = Label(levelVal.toString(), Utility.STATUSUI_SKIN)
        val goldLabel = Label(" gp: ", Utility.STATUSUI_SKIN)
        goldValLabel = Label(goldVal.toString(), Utility.STATUSUI_SKIN)

        //buttons
        inventoryButton = ImageButton(Utility.STATUSUI_SKIN, "inventory-button")
        inventoryButton.imageCell.size(32f, 32f)
        questButton = ImageButton(Utility.STATUSUI_SKIN, "quest-button")
        questButton.imageCell.size(32f, 32f)

        //Align images
        hpBar.setPosition(3f, 6f)
        mpBar.setPosition(3f, 6f)
        xpBar.setPosition(3f, 6f)

        //add to widget groups
        group.addActor(bar)
        group.addActor(hpBar)
        group2.addActor(bar2)
        group2.addActor(mpBar)
        group3.addActor(bar3)
        group3.addActor(xpBar)

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
        this.add(hpValLabel).align(Align.left)
        row()
        this.add(group2).size(bar2.width, bar2.height).padRight(10f)
        this.add(mpLabel)
        this.add(mpValLabel).align(Align.left)
        row()
        this.add(group3).size(bar3.width, bar3.height).padRight(10f)
        this.add(xpLabel)
        this.add(xpValLabel).align(Align.left).padRight(20f)
        row()
        this.add(levelLabel).align(Align.left)
        this.add(levelValLabel).align(Align.left)
        row()
        this.add(goldLabel)
        this.add(goldValLabel).align(Align.left)

        //this.debug();
        pack()
    }
}