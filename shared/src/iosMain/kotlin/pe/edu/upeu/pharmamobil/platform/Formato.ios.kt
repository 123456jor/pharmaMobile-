package pe.edu.upeu.pharmamobil.platform

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual fun formatearSoles(valor: Double): String {
    val formateador = NSNumberFormatter()
    formateador.numberStyle = NSNumberFormatterCurrencyStyle
    formateador.locale = NSLocale(localeIdentifier = "es_PE")
    return formateador.stringFromNumber(NSNumber(double = valor))
        ?: "S/ $valor"
}
