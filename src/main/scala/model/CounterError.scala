package model

sealed abstract class CounterError(val msg: String)

final case class GetCounterStatusError(error:String)
    extends CounterError(s"An error occurred while retrieving the current counter status: $error")

