package com.viclin.ridernavigator.source.remote

import com.viclin.ridernavigator.model.DirectionRequestV2
import com.viclin.ridernavigator.model.DirectionsResponseV2
import com.viclin.ridernavigator.network.RequestState
import com.viclin.ridernavigator.network.RetrofitClient
import com.viclin.ridernavigator.source.DirectionsDataSourceV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RemoteDirectionsDataSourceV2 : DirectionsDataSourceV2 {
    override suspend fun getDirectionsV2(requestBody: DirectionRequestV2): RequestState<DirectionsResponseV2> =
        withContext(Dispatchers.IO) {
            RequestState.Success(RetrofitClient.directionsDataSourceV2.getDirectionV2(requestBody))
        }

}