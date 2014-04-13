package info.justaway;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.justaway.adapter.UserAdapter;
import twitter4j.ResponseList;
import twitter4j.User;

public class UserSearchActivity extends FragmentActivity {

    private EditText mSearchWords;
    private int mPage = 1;
    private UserAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private boolean mAutoLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.activity_user_search);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button search = (Button) findViewById(R.id.search);
        Typeface fontello = JustawayApplication.getFontello();
        search.setTypeface(fontello);

        mSearchWords = (EditText) findViewById(R.id.searchWords);
        mProgressBar = (ProgressBar) findViewById(R.id.guruguru);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        // ユーザをViewに描写するアダプター
        mAdapter = new UserAdapter(this, R.layout.row_user);
        mListView.setAdapter(mAdapter);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        mSearchWords.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //EnterKeyが押されたかを判定
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {
                    search();
                    return true;
                }
                return false;
            }
        });

        Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        if (query != null) {
            mSearchWords.setText(query);
            search.performClick();
        } else {
            JustawayApplication.getApplication().showKeyboard(mSearchWords);
        }

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void additionalReading() {
        if (!mAutoLoading) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mAutoLoading = false;
        new UserSearchTask().execute(mSearchWords.getText().toString());
    }

    private void search() {
        JustawayApplication.getApplication().hideKeyboard(mSearchWords);
        if (mSearchWords.getText() == null) return;
        mAdapter.clear();
        mListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        new UserSearchTask().execute(mSearchWords.getText().toString());
    }

    private class UserSearchTask extends AsyncTask<String, Void, ResponseList<User>> {
        @Override
        protected ResponseList<User> doInBackground(String... params) {
            String query = params[0];
            try {
                Log.d("justaway", "task");
                return JustawayApplication.getApplication().getTwitter().searchUsers(query, mPage);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<User> users) {
            mProgressBar.setVisibility(View.GONE);
            if (users == null) {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
                return;
            }
            for (User user : users) {
                mAdapter.add(user);
            }
            if (users.size() == 20) {
                mAutoLoading = true;
                mPage++;
            }
            mListView.setVisibility(View.VISIBLE);
        }
    }
}
