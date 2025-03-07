import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.adapters.PostAdapter
import com.group147.appartmentblog.model.PostViewModel
import com.group147.appartmentblog.R

class PostFragment : Fragment() {
    private lateinit var postViewModel: PostViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PostAdapter() // Initialize with empty list
        recyclerView.adapter = adapter

        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        // Observe LiveData and update RecyclerView when data changes
        postViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }
    }
}
