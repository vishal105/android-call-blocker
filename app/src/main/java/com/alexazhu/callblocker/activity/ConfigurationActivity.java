package com.alexazhu.callblocker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alexazhu.callblocker.R;
import com.alexazhu.callblocker.blockednumber.BlockedNumber;
import com.alexazhu.callblocker.blockednumber.BlockedNumberDao;
import com.alexazhu.callblocker.blockednumber.BlockedNumberDatabase;
import com.alexazhu.callblocker.blockednumber.BlockedNumberType;
import com.alexazhu.callblocker.layout.AddNumberDialogFragment;
import com.alexazhu.callblocker.layout.BlockedNumberListAdapter;
import com.alexazhu.callblocker.util.AsyncExecutorUtil;
import com.alexazhu.callblocker.util.PermissionsUtil;

import java.util.List;

public class ConfigurationActivity extends AppCompatActivity {
    private final String LOG_TAG = ConfigurationActivity.class.getSimpleName();

    private Context context;
    private BlockedNumberDao blockedNumberDao;

    private boolean actionButtonsVisible = false;
    private BlockedNumberListAdapter listAdapter;

    private RecyclerView listView;
    private FloatingActionButton exactFab;
    private TextView exactLabel;
    private FloatingActionButton regexFab;
    private TextView regexLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getApplicationContext();

        setContentView(R.layout.activity_configuration);

        setupButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();

        PermissionsUtil permissionsUtil = new PermissionsUtil(this);
        blockedNumberDao = BlockedNumberDatabase.getInstance(this).blockedNumberDao();

        fetchAndPopulateBlockedNumbers();

        if (!permissionsUtil.checkPermissions()) {
            startActivity(new Intent(this, RequestPermissionsActivity.class));
        }
    }

    private void fetchAndPopulateBlockedNumbers() {
        listView = findViewById(R.id.blocked_number_list);
        listAdapter = new BlockedNumberListAdapter(this);
        listView.setAdapter(listAdapter);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.addItemDecoration(new DividerItemDecoration(listView.getContext(), DividerItemDecoration.VERTICAL));
        listView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            // Also hide action buttons if listView is touched
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                if (actionButtonsVisible) {
                    hideActionButtons();
                }
                return false;           // false so touch event is passed down to list item
            }
        });

        AsyncExecutorUtil.getInstance().getExecutor().execute(() -> {
            List<BlockedNumber> blockedNumberList = blockedNumberDao.getAll();
            runOnUiThread(() -> listAdapter.addAll(blockedNumberList));
        });
    }

    public void addNumber(final BlockedNumber number) {
        if (!listAdapter.contains(number)) {
            Log.i(LOG_TAG, "Adding number " + number);
            listAdapter.add(number);
            AsyncExecutorUtil.getInstance().getExecutor().execute(() -> blockedNumberDao.insert(number));
        } else {
            Log.i(LOG_TAG, String.format("Number already added to list %s", number));
            Toast.makeText(context, "Number already added to list", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeNumber(final BlockedNumber number) {
        if (listAdapter.contains(number)) {
            Log.i(LOG_TAG, "Removing number " + number);
            listAdapter.remove(number);
            AsyncExecutorUtil.getInstance().getExecutor().execute(() -> blockedNumberDao.delete(number));
            Toast.makeText(context, "Number deleted", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(LOG_TAG, String.format("Number not in list and cannot be removed %s", number));
        }
    }

    private void setupButtons() {
        FloatingActionButton mainFab = findViewById(R.id.main_fab);

        exactFab = findViewById(R.id.exact_fab);
        exactLabel = findViewById(R.id.exact_label);
        regexFab = findViewById(R.id.regex_fab);
        regexLabel = findViewById(R.id.regex_label);

        // Toggle action button visibility from main view
        mainFab.setOnClickListener((view) -> {
            // Ugly if-then because FloatingActionButton.setVisibility() can't be called here
            if (actionButtonsVisible) {
                hideActionButtons();
            } else {
                showActionButtons();
            }
            collapseNumberItems();
        });

        View.OnClickListener openExactDialog = (view) -> {
            AddNumberDialogFragment dialog = new AddNumberDialogFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(AddNumberDialogFragment.LAYOUT_ID_KEY, R.layout.dialog_add_exact_match);
            arguments.putSerializable(AddNumberDialogFragment.DIALOG_TYPE, BlockedNumberType.EXACT_MATCH);
            arguments.putString(AddNumberDialogFragment.TITLE, "Add Exact Number");
            dialog.setArguments(arguments);
            dialog.show(getSupportFragmentManager(), "AddNumberDialogFragment");
            collapseNumberItems();
        };
        exactFab.setOnClickListener(openExactDialog);
        exactLabel.setOnClickListener(openExactDialog);

        View.OnClickListener openRegexDialog = (view) -> {
            AddNumberDialogFragment dialog = new AddNumberDialogFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(AddNumberDialogFragment.LAYOUT_ID_KEY, R.layout.dialog_add_prefix_match);
            arguments.putSerializable(AddNumberDialogFragment.DIALOG_TYPE, BlockedNumberType.REGEX_MATCH);
            arguments.putString(AddNumberDialogFragment.TITLE, "Add Prefix");
            dialog.setArguments(arguments);
            dialog.show(getSupportFragmentManager(), "AddNumberDialogFragment");
            collapseNumberItems();
        };
        regexFab.setOnClickListener(openRegexDialog);
        regexLabel.setOnClickListener(openRegexDialog);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideActionButtons();
        collapseNumberItems();
        return true;
    }

    private void collapseNumberItems() {
        collapseNumberItems(null);
    }

    public void collapseNumberItems(@Nullable RecyclerView.ViewHolder ignoredViewHolder) {
        for (int i = 0; i < listView.getChildCount(); i++) {
            BlockedNumberListAdapter.BlockedNumberViewHolder viewHolder = (BlockedNumberListAdapter.BlockedNumberViewHolder) listView.getChildViewHolder(listView.getChildAt(i));
            if (viewHolder != ignoredViewHolder && viewHolder.isExpanded()) {
                viewHolder.collapse();
            }
        }
    }

    private void showActionButtons() {
        actionButtonsVisible = true;
        exactFab.show();
        exactLabel.setVisibility(View.VISIBLE);
        regexFab.show();
        regexLabel.setVisibility(View.VISIBLE);
    }

    private void hideActionButtons() {
        actionButtonsVisible = false;
        exactFab.hide();
        exactLabel.setVisibility(View.GONE);
        regexFab.hide();
        regexLabel.setVisibility(View.GONE);
    }
}
