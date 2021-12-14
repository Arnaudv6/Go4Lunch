package com.cleanup.go4lunch.ui.mates

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.ui.main.DetailsActivityLauncher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatesFragment : Fragment() {

    private val viewModel: MatesViewModel by viewModels()

    companion object {
        fun newInstance() = MatesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mates, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.mates_recycler_view)
        val adapter = MatesAdapter(viewModel)
        recycler.adapter = adapter

        viewModel.mMatesListLiveData.observe(viewLifecycleOwner) {
            Log.e("TAG", "displayed even when recycler view stays blank")
            adapter.submitList(it)
        }

        viewModel.mateClickSingleLiveEvent.observe(viewLifecycleOwner){
            (activity as DetailsActivityLauncher).onClicked(it)
        }

        view.findViewById<SwipeRefreshLayout>(R.id.mates_swipe_refresh_layout).let {
            it.setOnRefreshListener {
                lifecycleScope.launchWhenStarted {
                    viewModel.swipeRefresh()
                    it.isRefreshing = false
                }
            }
        }

        return view
    }
}

