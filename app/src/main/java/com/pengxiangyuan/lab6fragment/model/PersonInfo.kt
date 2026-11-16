package com.pengxiangyuan.lab6fragment.model

import java.io.Serializable

/**
 * Model 层：玩家个人信息。
 *
 * 实验六要求"个人信息也采用序列化的方法存储历史信息"，
 * 因此实现 [Serializable]：
 *  - 修改信息页保存时用 ObjectOutputStream 写入私有文件；
 *  - 个人信息 Fragment 每次进入时用 ObjectInputStream 读回，
 *    退出程序后再打开仍能恢复上一次填写的内容。
 */
class PersonInfo(
    var name: String = "",
    var gender: String = "",
    var phone: String = ""
) : Serializable {

    companion object {
        /** 序列化版本号 */
        private const val serialVersionUID = 1L
    }
}
