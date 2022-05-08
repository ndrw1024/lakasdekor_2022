package com.example.lakasdekoraciowebshop;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity {

    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private int queryLimit = 10;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;
    private JobScheduler mJobScheduler;

    private int gridNumber = 1;
    private boolean viewRow = true;

    private FrameLayout redCircle;
    private TextView contentTextView;

    private int cartItems = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);
        mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            Log.d(LOG_TAG, "Authenticated user");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user");
            finish();
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemList);
        
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        //setAlarmManager();
        setJobScheduler();
    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action == null){
                return;
            }
            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }
            queryData();
        }
    };

    private void queryData() {
        //kitörlöm a listából a dolgokat
        mItemList.clear();

        //mItems.whereEqualTo();
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemList.add(item);
            }

            if (mItemList.size() == 0){
                initializeData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();
        });
    }

    public void deleteItem(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(success -> {
            Log.d(LOG_TAG, "Item is succesfully deleted: " + item._getId());
        })
                .addOnFailureListener(failure -> {
                    Toast.makeText(this, "Item " + item._getId() + " cannot be deleted.", Toast.LENGTH_LONG).show();
                });
        queryData();
        mNotificationHandler.cancel();
    }

    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for (int i=0; i<itemsList.length; i++){
            mItems.add(new ShoppingItem(itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemsRate.getFloat(i,0),
                    itemsImageResource.getResourceId(i,0),
                    0));
        }

        itemsImageResource.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_button:
                Log.d(LOG_TAG, "Logout clicked!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings_button:
                Log.d(LOG_TAG, "Settings clicked!");
                return true;
            case R.id.add_to_cart:
                Log.d(LOG_TAG, "Add to cart clicked!");
                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG, "View selector clicked!");
                if(viewRow){
                    changeSpanCount(item, R.drawable.ic_view_selector_grid, 1);
                }else{
                    changeSpanCount(item, R.drawable.ic_view_selector_row, 2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int drawableID, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableID);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.add_to_cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(ShoppingItem item){
        cartItems = (cartItems+1);
        if(0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        }else{
            contentTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? View.VISIBLE : View.GONE);

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount() + 1).addOnFailureListener(failure -> {
            Toast.makeText(this, "Item " + item._getId() + " cannot be changed.", Toast.LENGTH_LONG).show();
        });

        mNotificationHandler.send(item.getName());

        queryData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager(){
        long repeatInterval =  AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime()+ repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime,repeatInterval,pendingIntent);

        //mAlarmManager.cancel(pendingIntent); megállítás
    }

    private void setJobScheduler() {
        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadLine = 5000;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadLine);

        mJobScheduler.schedule(builder.build());
        //mJobScheduler.cancel(0);
    }
}