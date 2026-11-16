package com.pengxiangyuan.lab6fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pengxiangyuan.lab6fragment.databinding.FragmentPersonalInfoBinding
import com.pengxiangyuan.lab6fragment.model.CardMatchingGame
import com.pengxiangyuan.lab6fragment.model.PersonInfo
import java.io.ObjectInputStream

/**
 * Controller 层：个人信息 Fragment（实验六的第 1 个 Fragment）。
 *
 * 职责：
 *  1. 展示玩家信息（姓名、性别、电话），每次进入界面（onResume）都从文件重新读取，
 *     这样在修改信息页保存后返回时能立即刷新；
 *  2. 点击"修改个人信息"打开 [EditActivity]；
 *  3. 历史信息采用序列化存储：[EditActivity] 保存时用 ObjectOutputStream 把
 *     [PersonInfo] 对象写入私有文件，这里用 ObjectInputStream 读回，
 *     退出程序后再打开仍能恢复上一次填写的内容；
 *  4. "修改个人信息"按钮下方展示两张牌 / 三张牌两种玩法的最高分纪录：
 *     最高分记录在 [CardMatchingGame.bestScore] 里，随牌局一起序列化保存，
 *     每次进入本界面都从对应的存档文件读回并刷新显示。
 */
class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    /** 当前展示的玩家信息，从序列化文件读出 */
    private var info: PersonInfo = PersonInfo()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(requireActivity(), EditActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        info = loadInfo()
        refreshViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** 把文件里的玩家信息与两局牌的最高分纪录刷新到界面上 */
    private fun refreshViews() {
        val notFilled = getString(R.string.not_filled)
        binding.tvName.text = getString(R.string.name_format, info.name.ifBlank { notFilled })
        binding.tvGender.text = getString(R.string.gender_format, info.gender.ifBlank { notFilled })
        binding.tvPhone.text = getString(R.string.phone_format, info.phone.ifBlank { notFilled })

        binding.tvBestScore2.text = getString(
            R.string.best_score_2card_format,
            loadBestScore(CardMatchingGame.TWO_CARD_GROUP)
        )
        binding.tvBestScore3.text = getString(
            R.string.best_score_3card_format,
            loadBestScore(CardMatchingGame.THREE_CARD_GROUP)
        )
    }

    /**
     * 序列化读取个人信息：openFileInput 打开私有文件，
     * ObjectInputStream 把 [PersonInfo] 对象还原出来；第一次运行没有存档时返回空信息。
     */
    private fun loadInfo(): PersonInfo {
        return try {
            val input = requireActivity().openFileInput(FILE_PERSON_INFO)
            val objectInputStream = ObjectInputStream(input)
            val saved = objectInputStream.readObject() as? PersonInfo ?: PersonInfo()
            objectInputStream.close()
            input.close()
            saved
        } catch (e: Exception) {
            PersonInfo()
        }
    }

    /**
     * 读取某种玩法的最高分纪录：打开该玩法的牌局存档（game2 / game3），
     * 用 ObjectInputStream 还原 [CardMatchingGame]，取其中的 bestScore。
     * 还没玩过（没有存档）时返回 0。
     */
    private fun loadBestScore(cardsPerSet: Int): Int {
        val fileName = if (cardsPerSet == CardMatchingGame.TWO_CARD_GROUP) {
            GameFragment.FILE_GAME_2
        } else {
            GameFragment.FILE_GAME_3
        }
        return try {
            val input = requireActivity().openFileInput(fileName)
            val objectInputStream = ObjectInputStream(input)
            val game = objectInputStream.readObject() as? CardMatchingGame
            objectInputStream.close()
            input.close()
            game?.bestScore ?: 0
        } catch (e: Exception) {
            0
        }
    }

    companion object {
        /** 个人信息存档文件名（与两个游戏Fragment的存档互相独立） */
        const val FILE_PERSON_INFO = "person_info"

        @JvmStatic
        fun newInstance(): PersonalInfoFragment = PersonalInfoFragment()
    }
}
