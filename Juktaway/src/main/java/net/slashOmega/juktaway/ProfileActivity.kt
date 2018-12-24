package net.slashOmega.juktaway

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.slashOmega.juktaway.adapter.SimplePagerAdapter
import net.slashOmega.juktaway.event.AlertDialogEvent
import net.slashOmega.juktaway.fragment.profile.*
import net.slashOmega.juktaway.model.Profile
import net.slashOmega.juktaway.model.TwitterManager
import net.slashOmega.juktaway.task.ShowUserLoader
import net.slashOmega.juktaway.util.KusoripuUtil
import net.slashOmega.juktaway.util.MessageUtil
import net.slashOmega.juktaway.util.ThemeUtil
import net.slashOmega.juktaway.util.displayImage
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import twitter4j.Relationship
import twitter4j.User
import java.lang.ref.WeakReference

class ProfileActivity: FragmentActivity(), LoaderManager.LoaderCallbacks<Profile> {
    companion object {
        private const val OPTION_MENU_DESTROY_BLOCK = 4
        private const val OPTION_MENU_GROUP_RELATION = 1
        private const val OPTION_MENU_CREATE_BLOCK = 1
        private const val OPTION_MENU_CREATE_OFFICIAL_MUTE = 2
        private const val OPTION_MENU_CREATE_NO_RETWEET = 3
        private const val OPTION_MENU_DESTROY_OFFICIAL_MUTE = 5
        private const val OPTION_MENU_DESTROY_NO_RETWEET = 6

        private class DestroyBlockTask(context: ProfileActivity) : AsyncTask<Long, Void, Boolean>() {
            private val ref = WeakReference(context)

            override fun doInBackground(vararg params: Long?): Boolean? {
                return params[0]?.let {
                    try {
                        TwitterManager.twitter.destroyBlock(it)
                        net.slashOmega.juktaway.model.Relationship.removeBlock(it)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } ?: false
            }

            override fun onPostExecute(success: Boolean?) {
                MessageUtil.dismissProgressDialog()
                if (success!!) {
                    MessageUtil.showToast(R.string.toast_destroy_block_success)
                    ref.get()?.restart()
                } else {
                    MessageUtil.showToast(R.string.toast_destroy_block_failure)
                }

            }
        }

        private class CreateNoRetweetTask(context: ProfileActivity, val notification: Boolean): AsyncTask<Long, Void, Boolean>() {
            val ref = WeakReference(context)

            override fun doInBackground(vararg params: Long?): Boolean {
                return params[0]?.let {
                    try {
                        TwitterManager.twitter.updateFriendship(it, notification, false)
                        net.slashOmega.juktaway.model.Relationship.setNoRetweet(it)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } ?: false
            }

            override fun onPostExecute(result: Boolean?) {
                MessageUtil.dismissProgressDialog()
                if (result!!) {
                    MessageUtil.showToast(R.string.toast_create_no_retweet_success)
                    ref.get()?.restart()
                } else {
                    MessageUtil.showToast(R.string.toast_create_no_retweet_failure)
                }
            }
        }

        private class DestroyNoRetweetTask(context: ProfileActivity, val notification: Boolean): AsyncTask<Long, Void, Boolean>() {
            val ref = WeakReference(context)

            override fun doInBackground(vararg params: Long?): Boolean {
                return params[0]?.let {
                    try {
                        TwitterManager.twitter.updateFriendship(it, notification, true)
                        net.slashOmega.juktaway.model.Relationship.removeNoRetweet(it)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } ?: false
            }

            override fun onPostExecute(result: Boolean?) {
                MessageUtil.dismissProgressDialog()
                if (result!!) {
                    MessageUtil.showToast(R.string.toast_destroy_no_retweet_success)
                    ref.get()?.restart()
                } else {
                    MessageUtil.showToast(R.string.toast_destroy_no_retweet_failure)
                }
            }
        }

        private class CreateOfficialMuteTask(context: ProfileActivity): AsyncTask<Long, Void, Boolean>() {
            val ref = WeakReference(context)

            override fun doInBackground(vararg params: Long?): Boolean {
                return params[0]?.let {
                    try {
                        TwitterManager.twitter.destroyMute(it)
                        net.slashOmega.juktaway.model.Relationship.removeOfficialMute(it)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } ?: false
            }

            override fun onPostExecute(result: Boolean?) {
                MessageUtil.dismissProgressDialog()
                if (result!!) {
                    MessageUtil.showToast(R.string.toast_destroy_official_mute_success)
                    ref.get()?.restart()
                } else {
                    MessageUtil.showToast(R.string.toast_destroy_official_mute_failure)
                }
            }
        }

        private class DestroyOfficialMuteTask(context: ProfileActivity): AsyncTask<Long, Void, Boolean>() {
            val ref = WeakReference(context)

            override fun doInBackground(vararg params: Long?): Boolean {
                return params[0]?.let {
                    try {
                        TwitterManager.twitter.destroyMute(it)
                        net.slashOmega.juktaway.model.Relationship.removeOfficialMute(it)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } ?: false
            }

            override fun onPostExecute(result: Boolean?) {
                MessageUtil.dismissProgressDialog()
                if (result!!) {
                    MessageUtil.showToast(R.string.toast_destroy_official_mute_success)
                    ref.get()?.restart()
                } else {
                    MessageUtil.showToast(R.string.toast_destroy_official_mute_failure)
                }
            }
        }

        private class ReportSpamTask(context: ProfileActivity) : AsyncTask<Long, Void, Boolean>() {
            val ref = WeakReference(context)

            override fun doInBackground(vararg params: Long?): Boolean? {
                return params[0]?.let { try {
                        TwitterManager.twitter.reportSpam(it)
                        net.slashOmega.juktaway.model.Relationship.setBlock(it)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } ?: false
            }

            override fun onPostExecute(success: Boolean?) {
                MessageUtil.dismissProgressDialog()
                if (success!!) {
                    MessageUtil.showToast(R.string.toast_report_spam_success)
                    ref.get()?.restart()
                } else {
                    MessageUtil.showToast(R.string.toast_report_spam_failure)
                }

            }
        }
    }

    private lateinit var mUser: User
    private lateinit var mRelationship: Relationship
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
        setContentView(R.layout.activity_profile)

        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        val args = Bundle(1)
        with (intent) {
            if (Intent.ACTION_VIEW == action && data != null
                    && data?.lastPathSegment.isNullOrEmpty().not()) {
                args.putString("screenName", data!!.lastPathSegment)
            } else {
                val screenName = getStringExtra("screenName")
                if (screenName != null) {
                    args.putString("screenName", screenName)
                } else {
                    args.putLong("userId", getLongExtra("userId", 0))
                }
            }
        }
        MessageUtil.showProgressDialog(this, getString(R.string.progress_loading))
        supportLoaderManager.initLoader(0, args, this)

        collapse_label.setOnClickListener {
            with (frame) {
                if (visibility == View.VISIBLE) {
                    visibility = View.GONE
                    collapse_label.setText(R.string.fontello_down)
                } else {
                    visibility = View.VISIBLE
                    collapse_label.setText(R.string.fontello_up)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile, menu)
        menu?.let { this.menu = it }
        return true
    }

    fun onEventMainThread(event: AlertDialogEvent) {
        event.dialogFragment.show(supportFragmentManager, "dialog")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return item?.let {
            if (it.groupId == OPTION_MENU_GROUP_RELATION) {
                when (it.itemId) {
                    OPTION_MENU_CREATE_BLOCK ->
                        AlertDialog.Builder(this)
                                .setMessage(R.string.confirm_create_block)
                                .setPositiveButton(R.string.button_create_block) { _, _ ->
                                    MessageUtil.showProgressDialog(this, getString(R.string.progress_process))
                                    GlobalScope.launch {
                                        val res = withContext(Dispatchers.Default) { runCatching {
                                            TwitterManager.twitter.createBlock(mUser.id)
                                            net.slashOmega.juktaway.model.Relationship.setBlock(mUser.id)
                                        }.isSuccess }
                                        MessageUtil.dismissProgressDialog()
                                        if (res) {
                                            MessageUtil.showToast(R.string.toast_create_block_success)
                                            restart()
                                        } else {
                                            MessageUtil.showToast(R.string.toast_create_block_failure)
                                        }
                                    }
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                    OPTION_MENU_CREATE_OFFICIAL_MUTE ->
                        AlertDialog.Builder(this)
                                .setMessage(R.string.confirm_create_official_mute)
                                .setPositiveButton(R.string.button_create_official_mute) { _, _ ->
                                    MessageUtil.showProgressDialog(this, getString(R.string.progress_process))
                                    CreateOfficialMuteTask(this).execute(mUser.id)
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                    OPTION_MENU_CREATE_NO_RETWEET ->
                        AlertDialog.Builder(this)
                                .setMessage(R.string.confirm_create_no_retweet)
                                .setPositiveButton(R.string.button_create_no_retweet) { _, _ ->
                                    MessageUtil.showProgressDialog(this, getString(R.string.progress_process))
                                    CreateNoRetweetTask(this, mRelationship.isSourceNotificationsEnabled).execute(mUser.id)
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                    OPTION_MENU_DESTROY_BLOCK ->
                        AlertDialog.Builder(this)
                                .setMessage(R.string.confirm_create_block)
                                .setPositiveButton(R.string.button_destroy_block) { _, _ ->
                                    MessageUtil.showProgressDialog(this, getString(R.string.progress_process))
                                    DestroyBlockTask(this).execute(mUser.id)
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                    OPTION_MENU_DESTROY_OFFICIAL_MUTE ->
                        AlertDialog.Builder(this)
                                .setMessage(R.string.confirm_destroy_official_mute)
                                .setPositiveButton(R.string.button_destroy_official_mute) { _, _ ->
                                    MessageUtil.showProgressDialog(this, getString(R.string.progress_process))
                                    DestroyOfficialMuteTask(this).execute(mUser.id)
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                    OPTION_MENU_DESTROY_NO_RETWEET ->
                        AlertDialog.Builder(this@ProfileActivity)
                                .setMessage(R.string.confirm_destroy_no_retweet)
                                .setPositiveButton(R.string.button_destroy_no_retweet) { _, _ ->
                                    MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                                    DestroyNoRetweetTask(this, mRelationship.isSourceNotificationsEnabled).execute(mUser.id)
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                }
                true
            } else {
                when (it.itemId) {
                    android.R.id.home ->
                        finish()
                    R.id.send_reply -> {
                        val text = "@" + mUser.screenName
                        startActivity<PostActivity>("status" to text, "selection" to text.length)
                    }
                    R.id.send_direct_messages -> {
                        val text = "D " + mUser.screenName
                        startActivity<PostActivity>("status" to text, "selection" to text.length)
                    }
                    R.id.send_kusoripu -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            if (Build.VERSION.SDK_INT > 20) {
                                val content = withContext(Dispatchers.Default) { KusoripuUtil.getKusoripu(mUser.screenName) }
                                startActivity<PostActivity>("status" to content, "selection" to content.length)
                            } else {
                                toast(R.string.repooply_version)
                            }
                        }
                    }
                    R.id.add_to_list ->
                        startActivity<RegisterUserListActivity>("userId" to mUser.id)
                    R.id.open_twitter ->
                        startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://twitter.com/" + mUser.screenName)))
                    R.id.open_favstar ->
                        MessageUtil.showToast("This item is unavailable.")
                    R.id.open_aclog ->
                        startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://aclog.koba789.com/" + mUser.screenName + "/timeline")))
                    R.id.open_twilog ->
                        startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://twilog.org/" + mUser.screenName)))
                    R.id.report_spam ->
                        AlertDialog.Builder(this@ProfileActivity)
                                .setMessage(R.string.confirm_report_spam)
                                .setPositiveButton(R.string.button_report_spam) { _, _ ->
                                    MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                                    ReportSpamTask(this).execute(mUser.id)
                                }
                                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                                .show()
                }
                true
            }
        } ?: false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Profile> {
        return args!!.getString("screenName")?.let {
            ShowUserLoader(this, it)
        } ?: ShowUserLoader(this, args.getLong("userId"))
    }

    //TODO data binding
    override fun onLoadFinished(loader: Loader<Profile>, profile: Profile?) {
        MessageUtil.dismissProgressDialog()

        if (profile == null) {
            MessageUtil.showToast(R.string.toast_load_data_failure, "(null)")
            return
        }
        if (profile.error != null && profile.error!!.isNotEmpty()) {
            MessageUtil.showToast(R.string.toast_load_data_failure, profile.error!!)
            return
        }
        mUser = profile.user!!
        mRelationship = profile.relationship!!

        favourites_count.text = getString(R.string.label_favourites, String.format("%1$,3d", mUser.favouritesCount))
        statuses_count.text = getString(R.string.label_tweets, String.format("%1$,3d", mUser.statusesCount))
        friends_count.text = getString(R.string.label_following, String.format("%1$,3d", mUser.friendsCount))
        followers_count.text = getString(R.string.label_followers, String.format("%1$,3d", mUser.followersCount))
        listed_count.text = getString(R.string.label_listed, String.format("%1$,3d", mUser.listedCount))

        mUser.profileBannerMobileRetinaURL?.let {
            banner.displayImage(it)
        }

        with (menu) {
            if (mRelationship.isSourceBlockingTarget) {
                add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_BLOCK, 100, R.string.menu_destroy_block)
            } else {
                add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_BLOCK, 100, R.string.menu_create_block)
            }
            if (mRelationship.isSourceFollowingTarget) {
                if (mRelationship.isSourceMutingTarget) {
                    add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_OFFICIAL_MUTE, 100, R.string.menu_destory_official_mute)
                } else {
                    add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_OFFICIAL_MUTE, 100, R.string.menu_create_official_mute)
                }
                if (mRelationship.isSourceWantRetweets) {
                    add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_NO_RETWEET, 100, R.string.menu_create_no_retweet)
                } else {
                    add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_NO_RETWEET, 100, R.string.menu_destory_no_retweet)
                }
            }
        }

        val args = Bundle().apply {
            putSerializable("user", mUser)
            putSerializable("relationship", mRelationship)
        }
        SimplePagerAdapter(this, pager).apply {
            addTab(SummaryFragment::class, args)
            addTab(DescriptionFragment::class, args)
        }.notifyDataSetChanged()
        symbol.setViewPager(pager)

        /*
         * スワイプの度合いに応じて背景色を暗くする
         * これは透明度＆背景色黒で実現している、背景色黒だけだと背景画像が見えないが、
         * 透明度を指定することで背景画像の表示と白色のテキストの視認性を両立している
         */
        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                symbol.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                /*
                 * 背景色の透過度の範囲は00〜99とする（FFは真っ黒で背景画像が見えない）
                 * 99は10進数で153
                 * positionは0が1ページ目（スワイプ中含む）で1だと完全に2ページ目に遷移した状態
                 * positionOffsetには0.0〜1.0のスクロール率がかえってくる、真ん中だと0.5
                 * hexにはpositionOffsetに応じて00〜99（153）の値が入るように演算を行う
                 * 例えばpositionOffsetが0.5の場合はhexは4dになる
                 * positionが1の場合は最大値（99）を無条件で設定している
                 */

                val maxHex = 153
                val hex = if (position == 1) "99" else String.format("%02X", (maxHex * positionOffset).toInt())
                pager.setBackgroundColor(Color.parseColor("#" + hex + "000000"))
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                symbol.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                symbol.onPageSelected(position)
            }
        })

        val listArgs = Bundle().apply {
            putSerializable("user", mUser)
        }

        SimplePagerAdapter(this, list_pager).apply {
            addTab(UserTimelineFragment::class, listArgs)
            addTab(FollowingListFragment::class, listArgs)
            addTab(FollowersListFragment::class, listArgs)
            addTab(UserListMembershipsFragment::class, listArgs)
            addTab(FavoritesListFragment::class, listArgs)
        }.notifyDataSetChanged()
        list_pager.offscreenPageLimit = 5

        val tabs = listOf<TextView>(statuses_count, friends_count, followers_count, listed_count, favourites_count)

        val colorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue)
        val colorWhite = ThemeUtil.getThemeTextColor(R.attr.text_color)

        tabs.forEach { it.setTextColor(colorWhite) }
        tabs[0].setTextColor(colorBlue)

        list_pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                /**
                 * タブのindexと選択されたpositionを比較して色を設定
                 */
                for (i in tabs.indices) {
                    tabs[i].setTextColor(if (i == position) colorBlue else colorWhite)
                }
            }
        })

        for (i in tabs.indices) {
            tabs[i].setOnClickListener { list_pager.currentItem = i }
        }
    }

    override fun onLoaderReset(loader: Loader<Profile>) {}

    private fun restart() {
        startActivity(Intent().apply {
            setClass(this@ProfileActivity, ProfileActivity::class.java)
            putExtra("userId", mUser.id)
        })
        finish()
    }
}