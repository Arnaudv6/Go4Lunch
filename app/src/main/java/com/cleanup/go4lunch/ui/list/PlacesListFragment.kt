package com.cleanup.go4lunch.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlacesListFragment : Fragment() {

    private val viewModel: PlacesListViewModel by viewModels()

    companion object {
        fun newInstance(): PlacesListFragment {
            return PlacesListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.addItemDecoration(
            DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        val adapter = PlacesListAdapter()
        recyclerView.adapter = adapter

        viewModel.viewActionLiveData.observe(viewLifecycleOwner) {
            when (it) {
                PlacesListViewAction.ScrollToTop ->
                    (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPosition(0)
                else -> Unit
            }.exhaustive
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_IDLE) {
                    viewModel.onRecyclerViewIdle()
                }
            }
        })

        viewModel.viewStateListLiveData.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return view
    }
}