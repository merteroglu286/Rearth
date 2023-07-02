package tr.main.rearth

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import java.lang.Exception

class InfoWindowRefresher(private val markerToRefresh: Marker, val imageUrl: String,
                          val userImage: ImageView,val context:Context
): Callback {


    override fun onSuccess() {
        if (markerToRefresh.isInfoWindowShown) {
            markerToRefresh.hideInfoWindow();

            Glide.with(context).load(imageUrl).into(userImage)

            markerToRefresh.showInfoWindow();
        }
    }

    override fun onError(e: Exception?) {
        TODO("Not yet implemented")
    }
}