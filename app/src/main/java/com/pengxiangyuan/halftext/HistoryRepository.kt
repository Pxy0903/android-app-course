package com.pengxiangyuan.halftext

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** 一条计算历史：表达式（如 "12 + 5"）、结果文本与计算时间。 */
data class HistoryRecord(
    val expression: String,
    val result: String,
    val timeMillis: Long
)

/**
 * 历史记录仓库。
 *
 * 用 SharedPreferences 持久化：横竖屏切换、跳转第二个 Activity、甚至重启应用后
 * 记录都不会丢失；只保留最近 [MAX_RECORDS] 条，最新的排在最前。
 */
object HistoryRepository {

    private const val PREF_NAME = "calculator_history"
    private const val KEY_RECORDS = "records"
    private const val MAX_RECORDS = 100

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** 按时间倒序读取全部记录。数据损坏时安全降级为空列表。 */
    fun getAll(context: Context): List<HistoryRecord> {
        val raw = preferences(context).getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                HistoryRecord(
                    expression = item.getString("expression"),
                    result = item.getString("result"),
                    timeMillis = item.getLong("time")
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    /** 追加一条记录到最前面。 */
    fun add(context: Context, record: HistoryRecord) {
        val updated = buildList {
            add(record)
            addAll(getAll(context))
        }.take(MAX_RECORDS)

        val array = JSONArray()
        updated.forEach { array.put(it.toJson()) }
        preferences(context).edit { putString(KEY_RECORDS, array.toString()) }
    }

    fun clear(context: Context) {
        preferences(context).edit { remove(KEY_RECORDS) }
    }

    private fun HistoryRecord.toJson(): JSONObject = JSONObject()
        .put("expression", expression)
        .put("result", result)
        .put("time", timeMillis)
}
