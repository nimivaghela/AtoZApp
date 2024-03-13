package com.app.atoz.common.helper

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import timber.log.Timber
import java.util.*

const val AUTOCOMPLETE_REQUEST_CODE = 404
//const val GOOGLE_PLACE_API_KEY = "AIzaSyChRd82osRF6w6w3nkLVhyWzfPmRHMqqW0"
//const val GOOGLE_PLACE_API_KEY = "AIzaSyDP7GmYr0Om-EuTqFUkqhMRQmxlbCG6-3I"

class PlacePickerHelper(val activity: Activity, private val mListener: PlacePikerListener) {


//    init {
//        //init place piker
//        Places.initialize(activity, GOOGLE_PLACE_API_KEY)
//    }

    private val mFields = Arrays.asList(
        Place.Field.ID,
        Place.Field.ADDRESS,
        Place.Field.ADDRESS_COMPONENTS,
        Place.Field.NAME,
        Place.Field.LAT_LNG
    )

    fun openLocationPiker(fields: MutableList<Place.Field> = mFields) {
        if (Places.isInitialized()) {
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            ).setCountry("IN")
                .build(activity)
            activity.startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } else {
            mListener.onFail(Status.RESULT_INTERRUPTED)
        }

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    mListener.onResult(Autocomplete.getPlaceFromIntent(data!!))

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Timber.i(status.statusMessage)
                    mListener.onFail(status)

                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }


    interface PlacePikerListener {
        fun onResult(place: Place)
        fun onFail(status: Status)
    }
}
