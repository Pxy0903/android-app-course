package com.pengxiangyuan.lab4_cardgame

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI 测试（Espresso）：需要连接真机或模拟器运行。
 *
 * 验证三个页面都能正常启动，并且关键控件（按钮、得分栏）已经显示出来，
 * 以此确认 ViewBinding 绑定与布局没有问题。
 */
@RunWith(AndroidJUnit4::class)
class GameUiTest {

    /** 首页：信息区与两个按钮可见 */
    @Test
    fun mainScreen_showsPlayerInfoAndButtons() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btnEdit)).check(matches(isDisplayed()))
            onView(withId(R.id.btnPlay)).check(matches(isDisplayed()))
            onView(withId(R.id.tvScore)).check(matches(isDisplayed()))
        }
    }

    /** 游戏页：得分栏与底部按钮可见 */
    @Test
    fun gameScreen_showsScoreAndButtons() {
        ActivityScenario.launch(GameActivity::class.java).use {
            onView(withId(R.id.tvScore)).check(matches(isDisplayed()))
            onView(withId(R.id.btnReset)).check(matches(isDisplayed()))
            onView(withId(R.id.btnExit)).check(matches(isDisplayed()))
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
