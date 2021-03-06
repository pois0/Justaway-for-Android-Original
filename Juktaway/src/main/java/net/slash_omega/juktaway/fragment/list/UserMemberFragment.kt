package net.slash_omega.juktaway.fragment.list

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import android.widget.ProgressBar
import jp.nephy.penicillin.core.request.action.CursorJsonObjectApiAction
import jp.nephy.penicillin.endpoints.lists
import jp.nephy.penicillin.endpoints.lists.members
import jp.nephy.penicillin.extensions.await
import jp.nephy.penicillin.extensions.cursor.hasNext
import jp.nephy.penicillin.extensions.cursor.next
import jp.nephy.penicillin.models.cursor.CursorUsers
import kotlinx.android.synthetic.main.list_guruguru.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.slash_omega.juktaway.R
import net.slash_omega.juktaway.adapter.UserAdapter
import net.slash_omega.juktaway.twitter.currentClient
import net.slash_omega.juktaway.util.scope

class UserMemberFragment : Fragment() {
    private val mAdapter by lazy { UserAdapter(activity!!, R.layout.row_user) }
    private var mListId: Long = 0L
    private var mCursor: CursorJsonObjectApiAction<CursorUsers>? = null
    private lateinit var mListView: ListView
    private lateinit var mFooter: ProgressBar
    private var mAutoLoader = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.list_guruguru, container, false)?.apply {
        arguments?.getLong("listId")?.let { mListId = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mListView = list_view.apply {
            visibility = View.GONE
            adapter = mAdapter
            setOnScrollListener(object : AbsListView.OnScrollListener {

                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    // 最後までスクロールされたかどうかの判定
                    if (totalItemCount == firstVisibleItem + visibleItemCount) {
                        additionalReading()
                    }
                }
            })
        }

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(mListView)

        mFooter = guruguru
        applyListMembers(mListId)
    }

    private fun additionalReading() {
        if (!mAutoLoader) return
        mFooter.visibility = View.VISIBLE
        mAutoLoader = false
        applyListMembers(mListId)
    }

    private fun applyListMembers(listId: Long) {
        activity?.scope?.launch(Dispatchers.Main) {
            val resp = runCatching {
                (mCursor ?: currentClient.lists.members(listId)).await()
            }.getOrNull()
            mFooter.visibility = View.GONE
            if (resp == null) return@launch
            if (resp.hasNext) mCursor = resp.next
            mAdapter.addAll(resp.result.users)
            if (resp.result.nextCursor < 0) mAutoLoader = true
            mListView.visibility = View.VISIBLE
        }
    }
}
