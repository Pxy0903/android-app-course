package com.pengxiangyuan.lab6fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pengxiangyuan.lab6fragment.databinding.ActivityMainBinding
import com.pengxiangyuan.lab6fragment.model.CardMatchingGame

/**
 * Controller 层：主界面。
 *
 * 实验六用 Fragment 重构 APP：
 *  1. 界面只有一个 [MainActivity]，底部导航（BottomNavigationView）共 3 个标签，
 *     对应 3 个 Fragment：
 *      - 第 1 个：个人信息 [PersonalInfoFragment]；
 *      - 第 2 个：两张牌的翻牌游戏 [GameFragment]（newInstance 传 2）；
 *      - 第 3 个：三张牌的翻牌游戏 [GameFragment]（newInstance 传 3）。
 *  2. 切换标签用 supportFragmentManager 的事务把对应 Fragment
 *     replace 到 frameLayout 容器里；
 *  3. 无论是个人信息还是翻牌游戏，历史信息都用对象序列化保存
 *     （见 GameFragment / PersonalInfoFragment / EditActivity），退出程序后再打开可以恢复。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** 三个 Fragment 提前创建好，切换标签时直接复用 */
    private val personalInfoFragment: PersonalInfoFragment = PersonalInfoFragment.newInstance()
    private val game2Fragment: GameFragment =
        GameFragment.newInstance(CardMatchingGame.TWO_CARD_GROUP)
    private val game3Fragment: GameFragment =
        GameFragment.newInstance(CardMatchingGame.THREE_CARD_GROUP)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 提交要求：应用标题显示"姓名+学号"
        supportActionBar?.title = getString(R.string.app_name)

        // 第一次启动：默认显示第 1 个 Fragment（个人信息）。
        // 旋转屏幕等导致的重建由 FragmentManager 自动恢复，不重复 add，避免界面重叠。
        if (savedInstanceState == null) {
            replaceFragment(personalInfoFragment)
        }

        // 底部导航切换：把选中的标签对应的 Fragment 放进 frameLayout
        binding.bottomNV.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_personal_info -> replaceFragment(personalInfoFragment)
                R.id.nav_game_2 -> replaceFragment(game2Fragment)
                R.id.nav_game_3 -> replaceFragment(game3Fragment)
            }
            true
        }
    }

    /** 用 Fragment 事务把 target 替换到 frameLayout 容器中 */
    private fun replaceFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, target)
            .commit()
    }

    /** ActionBar 右侧的"关于"菜单 */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 弹出"关于"对话框：显示应用名称、姓名、学号与本次实验说明 */
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(getString(R.string.about_message, getString(R.string.app_name)))
            .setPositiveButton(R.string.about_ok, null)
            .show()
    }
}
