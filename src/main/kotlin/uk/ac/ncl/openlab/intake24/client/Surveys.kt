package uk.ac.ncl.openlab.intake24.client

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import java.time.OffsetDateTime
import java.util.*


data class RecallSubmission(val id: UUID,
                            val userId: Int,
                            val userAlias: List<String>,
                            val userCustomData: Map<String, String>,
                            val surveyCustomData: Map<String, String>,
                            val startTime: OffsetDateTime,
                            val endTime: OffsetDateTime,
                            val meals: List<RecallMeal>)


data class RecallMealTime(val hours: Int, val minutes: Int)

data class RecallMissingFood(val name: String, val brand: String, val description: String, val portionSize: String, val leftovers: String)

data class RecallMeal(val name: String,
                      val time: RecallMealTime,
                      val customData: Map<String, String>,
                      val foods: List<RecallFood>,
                      val missingFoods: List<RecallMissingFood>)

data class RecallFood(
        val code: String,
        val englishDescription: String,
        val localDescription: List<String>,
        val searchTerm: String,
        val nutrientTableId: String,
        val nutrientTableCode: String,
        val isReadyMeal: Boolean,
        val portionSize: RecallPortionSize,
        val reasonableAmount: Boolean,
        val foodGroupId: Int,
        val brand: String,
        val customData: Map<String, String>,
        val fields: Map<String, String>,
        val nutrients: Map<Int, Double>)

data class RecallPortionSize(
        val servingWeight: Double,
        val leftoversWeight: Double,
        val portionWeight: Double,
        val method: String,
        val data: Map<String, String>)


class Surveys(val baseUrl: String, val httpHandler: HttpHandler, val codec: JacksonCodec) {
    // userName: Option[String], dateFrom: Option[String], dateTo: Option[String], offset: Int, limit: Int)
    fun getSubmissions(surveyId: String, userName: String?, dateFrom: String?, dateTo: String?, offset: Int, limit: Int): List<RecallSubmission> {

        var request = Request(Method.GET, "$baseUrl/data-export/$surveyId/submissions")
                .query("offset", offset.toString())
                .query("limit", limit.toString())

        request = if (userName != null) request.query("userName", userName) else request
        request = if (dateFrom != null) request.query("dateFrom", dateFrom) else request
        request = if (dateTo != null) request.query("dateTo", dateTo) else request

        val response = httpHandler.invoke(request)

        return codec.decode(response.bodyString())
    }
}
