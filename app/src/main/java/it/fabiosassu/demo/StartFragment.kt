package it.fabiosassu.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.fabiosassu.demo.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    private var binding: FragmentStartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            singleItemButton.setOnClickListener {
                findNavController().navigate(
                    StartFragmentDirections.actionFragmentStartToFragmentSingleItem()
                )
            }
            recyclerViewButton.setOnClickListener {
                findNavController().navigate(
                    StartFragmentDirections.actionFragmentStartToFragmentRecyclerView()
                )
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}