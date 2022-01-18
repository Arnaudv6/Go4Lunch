package com.freemyip.go4lunch.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.freemyip.go4lunch.R
import com.freemyip.go4lunch.ui.main.DetailsActivityLauncher
import dagger.hilt.android.AndroidEntryPoint

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

        val adapter = PlacesListAdapter(activity as DetailsActivityLauncher)
        recyclerView.adapter = adapter

        viewModel.viewStateListLiveData.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return view
    }
}