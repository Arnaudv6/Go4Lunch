package com.cleanup.go4lunch.ui.mates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.cleanup.go4lunch.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MatesFragment : Fragment() {

    private val viewModel: MatesViewModel by viewModels()

    companion object {
        fun newInstance(): MatesFragment {
            return MatesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mates, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.mates_recycler_view)
        val adapter = MatesAdapter()
        recycler.adapter = adapter

        viewModel.refreshMatesList()
        viewModel.matesListLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return view
    }
}