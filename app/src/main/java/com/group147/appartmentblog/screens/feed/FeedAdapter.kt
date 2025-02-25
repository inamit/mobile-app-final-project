import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.R

class FeedAdapter(private val data: List<Post>) : RecyclerView.Adapter<FeedViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_list_row, parent, false)
        return FeedViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = data[position]
        holder.title?.text = post.title
        holder.content?.text = post.content
    }

    override fun getItemCount(): Int {
        return data.size
    }
}