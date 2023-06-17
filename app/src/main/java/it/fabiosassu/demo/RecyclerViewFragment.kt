package it.fabiosassu.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import it.fabiosassu.demo.databinding.FragmentRecyclerViewBinding

class RecyclerViewFragment : Fragment() {

    private var binding: FragmentRecyclerViewBinding? = null
    private val stackExpandableAdapter: StackExpandableAdapter by lazy { StackExpandableAdapter() }
    var counter = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
                adapter = stackExpandableAdapter
            }
            // add items on button click
            addGroupBtn.setOnClickListener {
                counter++
                addRecyclerItem()
            }
        }
        addRecyclerItem()
    }

    private fun addRecyclerItem() {
        stackExpandableAdapter.addItem(
            StackExpandableAdapterModel(
                id = counter,
                widgets = getLayouts(requireContext())
            )
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}