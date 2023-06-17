package it.fabiosassu.demo

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.fabiosassu.demo.databinding.StackExpandableRecyclerViewItemBinding

class StackExpandableAdapter(
    private val dataSet: MutableList<StackExpandableAdapterModel> = mutableListOf()
) : RecyclerView.Adapter<StackExpandableAdapter.ViewHolder>() {

    private val expandedStacks = SparseBooleanArray()

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        return ViewHolder(
            StackExpandableRecyclerViewItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val model = dataSet[position]
        viewHolder.binding.verticalStack.apply {
            setWidgets(model.widgets)
            if (expandedStacks[model.id]) {
                expand(false)
            } else {
                collapse(false)
            }
            onExpansionChangedListener = { _, expanded ->
                expandedStacks.put(model.id, expanded)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun addItem(model: StackExpandableAdapterModel) {
        dataSet.add(model)
        notifyItemInserted(dataSet.lastIndex)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(
        val binding: StackExpandableRecyclerViewItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

}

data class StackExpandableAdapterModel(
    val id: Int,
    val widgets: List<View>
)
