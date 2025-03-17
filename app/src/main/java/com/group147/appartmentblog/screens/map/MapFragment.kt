import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.MainActivity

class MapFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).hideAddApartmentButton()
        return inflater.inflate(R.layout.fragment_map, container, false)
    }
}