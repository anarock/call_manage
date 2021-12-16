package com.chooloo.www.koler.ui.dialer

import com.chooloo.www.koler.data.account.ContactAccount
import com.chooloo.www.koler.ui.dialpad.DialpadContract

class DialerContract : DialpadContract {
    interface View : DialpadContract.View {
        val suggestionsCount: Int
        var isSuggestionsVisible: Boolean
        var isAddContactButtonVisible: Boolean

        fun setSuggestionsFilter(filter: String)
    }

    interface Controller<V : View> : DialpadContract.Controller<V> {
        fun onCallClick()
        fun onAddContactClick()
        fun onSuggestionsChanged(contacts: List<ContactAccount>)
    }
}