package com.vishal.callblocker.layout

import android.animation.AnimatorInflater
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.vishal.callblocker.R
import com.vishal.callblocker.activity.ConfigurationActivity
import com.vishal.callblocker.blockednumber.BlockedNumber

class BlockedNumberListAdapter(private val parentActivity: ConfigurationActivity) : RecyclerView.Adapter<BlockedNumberListAdapter.BlockedNumberViewHolder>() {
    private val blockedNumbers: SortedList<BlockedNumber>

    class BlockedNumberViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val matchTypeView: TextView
        val phoneNumberView: TextView
        val deleteButtonView: ImageView

        var isExpanded = false
            set

        init {

            matchTypeView = itemView.findViewById(R.id.match_type)
            phoneNumberView = itemView.findViewById(R.id.phone_number)
            deleteButtonView = itemView.findViewById(R.id.delete_button)
        }

        fun expand() {
            val animator = AnimatorInflater.loadAnimator(itemView.context, R.animator.expand_list_item)
            animator.setTarget(itemView.findViewById(R.id.list_item))
            animator.start()
            isExpanded = true
        }

        fun collapse() {
            val animator = AnimatorInflater.loadAnimator(itemView.context, R.animator.collapse_list_item)
            animator.setTarget(itemView.findViewById(R.id.list_item))
            animator.start()
            isExpanded = false
        }
    }

    init {
        this.blockedNumbers = SortedList(BlockedNumber::class.java, object : SortedListAdapterCallback<BlockedNumber>(this) {
            override fun compare(item1: BlockedNumber, item2: BlockedNumber): Int {
                return item1.toFormattedString().compareTo(item2.toFormattedString())
            }

            override fun areContentsTheSame(item1: BlockedNumber, item2: BlockedNumber): Boolean {
                return item1 == item2
            }

            override fun areItemsTheSame(item1: BlockedNumber, item2: BlockedNumber): Boolean {
                return item1 == item2
            }
        })
    }

    fun add(number: BlockedNumber) {
        blockedNumbers.add(number)
    }

    fun addAll(numbers: Collection<BlockedNumber>?) {
        numbers?.let { blockedNumbers.addAll(it) }
    }

    operator fun contains(number: BlockedNumber): Boolean {
        return blockedNumbers.indexOf(number) != SortedList.INVALID_POSITION
    }

    fun remove(number: BlockedNumber): Boolean {
        return blockedNumbers.remove(number)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): BlockedNumberViewHolder {
        val context = viewGroup.context
        val inflater = LayoutInflater.from(context)
        val phoneNumberView = inflater.inflate(R.layout.item_phone_number, viewGroup, false)
        return BlockedNumberViewHolder(phoneNumberView)
    }

    override fun onBindViewHolder(viewHolder: BlockedNumberViewHolder, index: Int) {
        val number = blockedNumbers.get(index)
        viewHolder.matchTypeView.text = number.type?.displayText
        viewHolder.phoneNumberView.text = number.toFormattedString()
        viewHolder.deleteButtonView.setOnClickListener { parentActivity.removeNumber(number) }
    }

    override fun onViewAttachedToWindow(viewHolder: BlockedNumberViewHolder) {
        viewHolder.itemView.setOnClickListener {
            if (viewHolder.isExpanded) {
                viewHolder.collapse()
            } else {
                viewHolder.expand()
            }

            parentActivity.collapseNumberItems(viewHolder)

            parentActivity.window.decorView.performClick()
        }
    }

    override fun onViewDetachedFromWindow(viewHolder: BlockedNumberViewHolder) {
        if (viewHolder.isExpanded) {
            val animator = AnimatorInflater.loadAnimator(parentActivity, R.animator.collapse_list_item)
            animator.setTarget(viewHolder.itemView.findViewById(R.id.list_item))
            animator.start()
            viewHolder.isExpanded = !viewHolder.isExpanded
        }
    }

    override fun getItemCount(): Int {
        return blockedNumbers.size()
    }
}
