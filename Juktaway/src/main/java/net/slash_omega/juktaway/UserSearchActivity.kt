package net.slash_omega.juktaway

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import jp.nephy.penicillin.endpoints.users
import jp.nephy.penicillin.endpoints.users.search
import jp.nephy.penicillin.extensions.await
import kotlinx.android.synthetic.main.activity_user_search.*
import kotlinx.coroutines.launch
import net.slash_omega.juktaway.adapter.UserAdapter
import net.slash_omega.juktaway.twitter.currentClient
import net.slash_omega.juktaway.util.KeyboardUtil
import net.slash_omega.juktaway.util.ThemeUtil
import org.jetbrains.anko.toast

/**
 * Created on 2018/08/29.
 */
class UserSearchActivity: ScopedFragmentActivity() {
    private lateinit var mSearchWord: String
    private var mPage = 1
    private lateinit var mAdapter: UserAdapter
    private var mAutoLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
        setContentView(R.layout.activity_user_search)

        actionBar?.run {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        list_view.visibility = View.GONE
        mAdapter = UserAdapter(this, R.layout.row_user)
        list_view.adapter = mAdapter

        search.setOnClickListener { search() }
        search_text.setOnKeyListener { _, code, e ->
            if (e.action == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_ENTER) {
                search()
                true
            } else false
        }

        intent.getStringExtra("query")?.let {
            search_text.setText(it)
            search.performClick()
        } ?: KeyboardUtil.showKeyboard(search_text)

        list_view.setOnScrollListener(object: AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun search() {
        KeyboardUtil.hideKeyboard(search_text)
        if (search_text.text == null) return
        mAdapter.clear()
        mPage = 1
        list_view.visibility = View.GONE
        guruguru.visibility = View.VISIBLE
        mSearchWord = search_text.text.toString()
        userSearch(mSearchWord)
    }

    private fun additionalReading() {
        if (!mAutoLoading) return
        guruguru.visibility = View.VISIBLE
        mAutoLoading = false
        userSearch(mSearchWord)
    }

    private fun userSearch(word: String) = launch {
        runCatching {
            currentClient.users.search(word, mPage, 20).await()
        }.onSuccess { res ->
            mAdapter.addAll(res)
            if (res.size == 20) {
                mAutoLoading = true
                mPage++
            }
            list_view.visibility = View.VISIBLE
        }.onFailure {
            toast(R.string.toast_load_data_failure)
        }

        guruguru.visibility = View.GONE
    }
}