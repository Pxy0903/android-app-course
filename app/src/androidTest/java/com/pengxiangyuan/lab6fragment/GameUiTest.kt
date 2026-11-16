package com.pengxiangyuan.lab6fragment

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI 测试（Espresso）：需要连接真机或模拟器运行。
 *
 * 验证实验六的主界面（3 个 Fragment 切换）与修改信息页都能正常启动，
 * 关键控件（信息区、牌桌、底部导航）已经显示出来，
 * 以此确认 Fragment 事务、ViewBinding 绑定与布局没有问题。
 */
@RunWith(AndroidJUnit4::class)
class GameUiTest {

    /** 主界面默认显示个人信息 Fragment：头像信息区与"修改个人信息"按钮可见 */
    @Test
    fun mainScreen_showsPersonalInfoFragment() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.tvName)).check(matches(isDisplayed()))
            onView(withId(R.id.btnEdit)).check(matches(isDisplayed()))
            onView(withId(R.id.bottomNV)).check(matches(isDisplayed()))
        }
    }

    /** 切到第 2 个标签：两张牌游戏的牌桌与重置按钮可见 */
    @Test
    fun game2Tab_showsCardBoard() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_game_2)).perform(click())
            onView(withId(R.id.rvCards)).check(matches(isDisplayed()))
            onView(withId(R.id.btnReset)).check(matches(isDisplayed()))
        }
    }

    /** 切到第 3 个标签：三张牌游戏的牌桌与重置按钮可见 */
    @Test
    fun game3Tab_showsCardBoard() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_game_3)).perform(click())
            onView(withId(R.id.rvCards)).check(matches(isDisplayed()))
            onView(withId(R.id.btnReset)).check(matches(isDisplayed()))
        }
    }

    /** 修改信息页：输入框与保存/取消按钮可见 */
    @Test
    fun editScreen_showsForm() {
        ActivityScenario.launch(EditActivity::class.java).use {
            onView(withId(R.id.etName)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSave)).check(matches(isDisplayed()))
            onView(withId(R.id.btnCancel)).check(matches(isDisplayed()))
        }
    }
}
