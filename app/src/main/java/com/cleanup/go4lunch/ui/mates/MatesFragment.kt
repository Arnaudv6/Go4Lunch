package com.cleanup.go4lunch.ui.mates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cleanup.go4lunch.R
import dagger.hilt.android.AndroidEntryPoint

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

        val textView: TextView = view.findViewById(R.id.text_notifications)

        viewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        return view
    }
}