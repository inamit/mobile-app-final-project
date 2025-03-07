import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.R
import com.group147.appartmentblog.adapters.PostAdapter
import com.group147.appartmentblog.model.PostViewModel

class FeedFragment : Fragment() {
    private lateinit var postViewModel: PostViewModel
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PostAdapter() // Initialize with empty list
        recyclerView.adapter = adapter

        // Get ViewModel instance directly
        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        // Observe LiveData for automatic UI updates
        postViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                adapter.submitList(it)  // Update the adapter's data
            }
        }

        return view
    }
}