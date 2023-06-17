package it.fabiosassu.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.fabiosassu.demo.databinding.FragmentSingleItemBinding

class SingleItemFragment : Fragment() {

    private var binding: FragmentSingleItemBinding? = null
    var counter = STARTING_ITEM_NUMBER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSingleItemBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            // set data
            horizontalStack.setWidgets(getLayouts(requireContext(), false))
            verticalStack.setWidgets(getLayouts(requireContext()))
            // add items on button click
            addGroupBtn.setOnClickListener {
                counter++
                verticalStack.addWidget(getLayout(requireContext(), counter))
                horizontalStack.addWidget(getLayout(requireContext(), counter, false))
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}