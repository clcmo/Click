package clcmo.com.br.click.model

data class Coordinates(val latitude: Double, val longitude: Double)

data class Checkin(val coordinates: Coordinates, val timestamp: Long)
