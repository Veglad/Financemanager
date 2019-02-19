package com.example.vlad.financemanager.ui.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.SparseArray
import android.view.ViewGroup

import com.example.vlad.financemanager.ui.fragments.TabFragment
import com.example.vlad.financemanager.data.enums.PeriodsOfTime

import java.util.Calendar

class ViewPagerAdapter(fragmentManager: FragmentManager,
                       var tabTitles: MutableList<String>,
                       var endOfPeriodList: MutableList<Calendar>,
                       private var currentPeriod: PeriodsOfTime,
                       private var accountId: Int,
                       private var isIncome: Boolean) : FragmentStatePagerAdapter(fragmentManager) {
    private val registeredFragments = SparseArray<TabFragment>()

    override fun getItem(position: Int): Fragment {
        return TabFragment.newInstance(currentPeriod, endOfPeriodList[position], isIncome, accountId, tabTitles[position])
    }

    fun setAccountId(accountId: Int) {
        this.accountId = accountId
    }

    fun setCurrentPeriod(currentPeriod: PeriodsOfTime) {
        this.currentPeriod = currentPeriod
    }

    //This method is invoked after notifyDataSetChanged method invocation
    override fun getItemPosition(any: Any): Int {
        val fragment = any as TabFragment
        fragment.fullTabFragmentUpdate(currentPeriod, fragment.currentEndOfPeriod, isIncome, accountId, fragment.dateTitle)

        return super.getItemPosition(any)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as TabFragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, any)
    }

    fun getRegisteredFragment(position: Int): TabFragment {
        return registeredFragments.get(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return endOfPeriodList.size
    }

    fun setIsIncome(isIncome: Boolean) {
        this.isIncome = isIncome
    }
}