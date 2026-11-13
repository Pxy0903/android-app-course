package com.pengxiangyuan.halftext

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pengxiangyuan.halftext.databinding.ItemHistoryBinding

/** RecyclerView 适配器：每条历史记录对应一个 [ItemHistoryBinding]。 */
class HistoryAdapter :
    ListAdapter<HistoryRecord, HistoryAdapter.HistoryViewHolder>(RecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: HistoryRecord) {
            val context = binding.root.context
            binding.tvExpression.text = context.getString(
                R.string.history_expression_with_equal,
                record.expression
            )
            binding.tvResult.text = context.getString(
                R.string.history_result_format,
                record.result
            )
            binding.tvTime.text = DateUtils.formatDateTime(
                context,
                record.timeMillis,
                DateUtils.FORMAT_SHOW_DATE or
                    DateUtils.FORMAT_SHOW_TIME or
                    DateUtils.FORMAT_ABBREV_MONTH
            )
        }
    }

    private class RecordDiffCallback : DiffUtil.ItemCallback<HistoryRecord>() {
        override fun areItemsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean =
            oldItem.timeMillis == newItem.timeMillis && oldItem.expression == newItem.expression

        override fun areContentsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean =
            oldItem == newItem
    }
}
