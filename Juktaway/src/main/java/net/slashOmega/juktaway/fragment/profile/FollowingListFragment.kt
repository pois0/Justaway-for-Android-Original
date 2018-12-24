package net.slashOmega.juktaway.fragment.profile

import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.slashOmega.juktaway.R
import net.slashOmega.juktaway.adapter.UserAdapter
import net.slashOmega.juktaway.model.TwitterManager
import net.slashOmega.juktaway.util.tryAndTraceGet

internal class FollowingListFragment: ProfileListFragmentBase() {
    override val mAdapter by lazy { UserAdapter(activity, R.layout.row_user) }
    override val layout = R.layout.list_guruguru
    override fun showList() {
        GlobalScope.launch(Dispatchers.Main) {
            val job = async(Dispatchers.Default) {
                tryAndTraceGet {
                    TwitterManager.twitter.getFriendsList(user.id, cursor).apply {
                        cursor = nextCursor
                    }
                }
            }
            mFooter.visibility = View.GONE
            job.await()?.run {
                forEach { mAdapter.add(it) }
                if (hasNext()) mAutoLoader = true
                mListView.visibility = View.VISIBLE
            }

            finishLoading()
        }
    }
}