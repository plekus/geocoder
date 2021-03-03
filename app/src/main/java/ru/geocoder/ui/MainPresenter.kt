package ru.geocoder.ui

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.geocoder.R
import ru.geocoder.data.GeocodeAPI
import ru.geocoder.model.LocationInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainView : MvpView {
    fun showLocation(locationInfo: LocationInfo, isFirst: Boolean)
}

@InjectViewState
class MainPresenter(
    private val context: Context,
    private val geocodeAPI: GeocodeAPI
) : MvpPresenter<MainView>() {

    private val compositeDisposable = CompositeDisposable()
    private var locationInfo: LocationInfo? = null

    fun getAddress(lat: Double, lon: Double) {
        if (locationInfo?.lat == lat && locationInfo?.lon == lon) {
            viewState.showLocation(locationInfo!!, false)
            return
        }

        geocodeAPI.getAddress(lat, lon)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val isFirst = locationInfo == null
                locationInfo = LocationInfo(
                    address = it.suggestions.firstOrNull()?.value
                        ?: context.getString(R.string.no_address),
                    lat = lat,
                    lon = lon
                )
                viewState.showLocation(locationInfo!!, isFirst)
            }, {
                it.printStackTrace()
                if (locationInfo == null) {
                    viewState.showLocation(
                        LocationInfo(
                            address = context.getString(R.string.geocode_error),
                            lat = lat,
                            lon = lon
                        ),
                        false
                    )
                }
            }).also {
                compositeDisposable.add(it)
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}