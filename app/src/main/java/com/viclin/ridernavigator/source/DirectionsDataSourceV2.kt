package com.viclin.ridernavigator.source

import com.viclin.ridernavigator.model.DirectionRequestV2
import com.viclin.ridernavigator.model.DirectionsResponseV2
import com.viclin.ridernavigator.network.RequestState

//暂未使用
interface DirectionsDataSourceV2 {
    suspend fun getDirectionsV2(requestBody: DirectionRequestV2): RequestState<DirectionsResponseV2>
}