package tr.main.rearth.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tr.main.rearth.Services.BlackListAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class BlackListViewModel :ViewModel(){

    private val blackListAPIService = BlackListAPIService()
    private val disposable = CompositeDisposable()

    val blackList = MutableLiveData<List<String>>()


    fun getDataFromAPI(){

        disposable.add(
            blackListAPIService.getData()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<String>>(){
                    override fun onSuccess(t: List<String>) {
                        blackList.value = t
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                })
        )
    }
}