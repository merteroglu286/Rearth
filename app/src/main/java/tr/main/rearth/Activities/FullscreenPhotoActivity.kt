package tr.main.rearth.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import tr.main.rearth.databinding.ActivityFullscreenPhotoBinding

class FullscreenPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenPhotoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenPhotoBinding.inflate(layoutInflater)

        val img = intent.getStringExtra("img")
        Log.i("zxc",img.toString())
        Glide.with(this).load(img.toString()).into(binding.photoView)
        setContentView(binding.root)
    }
}