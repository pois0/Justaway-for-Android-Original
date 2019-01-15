package net.slashOmega.juktaway.adapter.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row_switch_account.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.slashOmega.juktaway.adapter.ArrayAdapterBase
import net.slashOmega.juktaway.model.UserIconManager.displayUserIcon
import net.slashOmega.juktaway.twitter.Core
import net.slashOmega.juktaway.twitter.Identifier
import net.slashOmega.juktaway.twitter.currentIdentifier
import net.slashOmega.juktaway.twitter.identifierList

/**
 * Created on 2018/10/20.
 */
class IdentifierAdapter(context: Context?, resourceId: Int, private val highlightColor: Int, private val defaultColor: Int) : ArrayAdapterBase<Identifier>(context, resourceId) {
    init { identifierList.forEach { add(it) } }

    override val View.mView: (Int, ViewGroup?) -> Unit
        @SuppressLint("SetTextI18n")
        get() = { pos, _ ->
            getItem(pos)?.let { identifier ->
                GlobalScope.launch(Dispatchers.Main) {
                    icon.displayUserIcon(identifier.userId)
                    screen_name.text = identifier.screenName
                    consumer_name.text = Core.getConsumer(identifier.consumerId)?.name
                    (if (currentIdentifier == identifier) highlightColor else defaultColor).let {
                        screen_name.setTextColor(it)
                        consumer_name.setTextColor(it)
                    }
                }
            }
        }
}