package com.cleanup.go4lunch.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.collectWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

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
        val adapter = PlacesListAdapter()
        recyclerView.adapter = adapter
        viewModel.viewStateListFlow.collectWithLifecycle(viewLifecycleOwner) {
            adapter.submitList(it)
            lifecycleScope.launchWhenStarted {
                delay(200)
                (recyclerView.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
            }
        }

        return view
    }
}