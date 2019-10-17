package com.vishal.callblocker.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.vishal.callblocker.R
import com.vishal.callblocker.blockednumber.BlockedNumber
import com.vishal.callblocker.blockednumber.BlockedNumberDao
import com.vishal.callblocker.blockednumber.BlockedNumberDatabase
import com.vishal.callblocker.blockednumber.BlockedNumberType
import com.vishal.callblocker.layout.AddNumberDialogFragment
import com.vishal.callblocker.layout.BlockedNumberListAdapter
import com.vishal.callblocker.util.AsyncExecutorUtil
import com.vishal.callblocker.util.PermissionsUtil

class ConfigurationActivity : AppCompatActivity() {
    private val LOG_TAG = ConfigurationActivity::class.java.simpleName

    private var context: Context? = null
    private var blockedNumberDao: BlockedNumberDao? = null

    private var actionButtonsVisible = false
    private var listAdapter: BlockedNumberListAdapter? = null

    private var listView: RecyclerView? = null
    private var exactFab: FloatingActionButton? = null
    private var exactLabel: TextView? = null
    private var regexFab: FloatingActionButton? = null
    private var regexLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.context = applicationContext

        setContentView(R.layout.activity_configuration)

        setupButtons()
    }

    override fun onStart() {
        super.onStart()

        val permissionsUtil = PermissionsUtil(this)
        blockedNumberDao = BlockedNumberDatabase.getInstance(this)?.blockedNumberDao()

        fetchAndPopulateBlockedNumbers()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsUtil.checkPermissions()) {
                startActivity(Intent(this, RequestPermissionsActivity::class.java))
            }
        }
    }

    private fun fetchAndPopulateBlockedNumbers() {
        listView = findViewById(R.id.blocked_number_list)
        listAdapter = BlockedNumberListAdapter(this)
        listView?.adapter = listAdapter
        listView?.layoutManager = LinearLayoutManager(context)
        listView?.addItemDecoration(DividerItemDecoration(listView?.context, DividerItemDecoration.VERTICAL))
        listView?.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            // Also hide action buttons if listView is touched
            override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent): Boolean {
                if (actionButtonsVisible) {
                    hideActionButtons()
                }
                return false           // false so touch event is passed down to list item
            }
        })

        AsyncExecutorUtil.instance.executor.execute(Runnable {
            val blockedNumberList = blockedNumberDao?.all
            this@ConfigurationActivity.runOnUiThread { listAdapter?.addAll(blockedNumberList) }
        })
    }

    fun addNumber(number: BlockedNumber) {
        if (!(listAdapter?.contains(number) == true)) {
            Log.i(LOG_TAG, "Adding number $number")
            listAdapter?.add(number)
            AsyncExecutorUtil.instance.executor.execute(Runnable { blockedNumberDao?.insert(number) })
        } else {
            Log.i(LOG_TAG, String.format("Number already added to list %s", number))
            Toast.makeText(context, "Number already added to list", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeNumber(number: BlockedNumber) {
        if (listAdapter?.contains(number) == true) {
            Log.i(LOG_TAG, "Removing number $number")
            listAdapter?.remove(number)
            AsyncExecutorUtil.instance.executor.execute(Runnable { blockedNumberDao?.delete(number) })
            Toast.makeText(context, "Number deleted", Toast.LENGTH_SHORT).show()
        } else {
            Log.i(LOG_TAG, String.format("Number not in list and cannot be removed %s", number))
        }
    }

    private fun setupButtons() {
        val mainFab = findViewById<FloatingActionButton>(R.id.main_fab)

        exactFab = findViewById(R.id.exact_fab)
        exactLabel = findViewById(R.id.exact_label)
        regexFab = findViewById(R.id.regex_fab)
        regexLabel = findViewById(R.id.regex_label)

        // Toggle action button visibility from main view
        mainFab.setOnClickListener {
            // Ugly if-then because FloatingActionButton.setVisibility() can't be called here
            if (actionButtonsVisible) {
                this@ConfigurationActivity.hideActionButtons()
            } else {
                this@ConfigurationActivity.showActionButtons()
            }
            this@ConfigurationActivity.collapseNumberItems()
        }

        val openExactDialog = View.OnClickListener {
            val dialog = AddNumberDialogFragment()
            val arguments = Bundle()
            arguments.putInt(AddNumberDialogFragment.LAYOUT_ID_KEY, R.layout.dialog_add_exact_match)
            arguments.putSerializable(AddNumberDialogFragment.DIALOG_TYPE, BlockedNumberType.EXACT_MATCH)
            arguments.putString(AddNumberDialogFragment.TITLE, "Add Exact Number")
            dialog.arguments = arguments
            dialog.show(this@ConfigurationActivity.supportFragmentManager, "AddNumberDialogFragment")
            this@ConfigurationActivity.collapseNumberItems()
        }
        exactFab?.setOnClickListener(openExactDialog)
        exactLabel?.setOnClickListener(openExactDialog)

        val openRegexDialog = View.OnClickListener {
            val dialog = AddNumberDialogFragment()
            val arguments = Bundle()
            arguments.putInt(AddNumberDialogFragment.LAYOUT_ID_KEY, R.layout.dialog_add_prefix_match)
            arguments.putSerializable(AddNumberDialogFragment.DIALOG_TYPE, BlockedNumberType.REGEX_MATCH)
            arguments.putString(AddNumberDialogFragment.TITLE, "Add Prefix")
            dialog.arguments = arguments
            dialog.show(this@ConfigurationActivity.supportFragmentManager, "AddNumberDialogFragment")
            this@ConfigurationActivity.collapseNumberItems()
        }
        regexFab?.setOnClickListener(openRegexDialog)
        regexLabel?.setOnClickListener(openRegexDialog)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        hideActionButtons()
        collapseNumberItems()
        return true
    }

    private fun collapseNumberItems() {
        collapseNumberItems(null)
    }

    fun collapseNumberItems(ignoredViewHolder: RecyclerView.ViewHolder?) {
        for (i in 0 until listView?.childCount!!) {
            val viewHolder = listView?.getChildAt(i)?.let { listView?.getChildViewHolder(it) } as BlockedNumberListAdapter.BlockedNumberViewHolder
            if (viewHolder != ignoredViewHolder && viewHolder.isExpanded) {
                viewHolder.collapse()
            }
        }
    }

    private fun showActionButtons() {
        actionButtonsVisible = true
        exactFab?.show()
        exactLabel?.visibility = View.VISIBLE
        regexFab?.show()
        regexLabel?.visibility = View.VISIBLE
    }

    private fun hideActionButtons() {
        actionButtonsVisible = false
        exactFab?.hide()
        exactLabel?.visibility = View.GONE
        regexFab?.hide()
        regexLabel?.visibility = View.GONE
    }
}
