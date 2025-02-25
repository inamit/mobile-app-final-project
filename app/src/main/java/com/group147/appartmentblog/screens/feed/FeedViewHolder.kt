import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.R

class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var title: TextView? = null
    var content: TextView? = null
    var image: ImageView? = null

    init {
        title = itemView.findViewById<TextView>(R.id.itemTitle);
        content = itemView.findViewById<TextView>(R.id.itemContent);
        image = itemView.findViewById<ImageView>(R.id.imageView);
    }
}