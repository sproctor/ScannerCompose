package com.github.sproctor.scannercompose

data class DriverLicense(
    val familyName: String?,
    val firstName: String?,
    val middleNames: String?,
    val issueDate: String?,
    val expirationDate: String?,
    val birthDate: String?,
    val gender: String?,
    val eyeColor: String?,
    val height: String?,
    val addressStreet: String?,
    val addressCity: String?,
    val addressState: String?,
    val addressPostalCode: String?,
    val licenseNumber: String?,
    val country: String?,
)

fun parseAAMVA(barcodeData: String): DriverLicense? {
    // first character must be @
    if (barcodeData[0] != '@') return null
    var familyName: String? = null
    var firstName: String? = null
    var middleNames: String? = null
    var issueDate: String? = null
    var expirationDate: String? = null
    var birthDate: String? = null
    var gender: String? = null
    var eyeColor: String? = null
    var height: String? = null
    var addressStreet: String? = null
    var addressCity: String? = null
    var addressState: String? = null
    var addressPostalCode: String? = null
    var licenseNumber: String? = null
    var country: String? = null

    // read the subfile count
    val subfiles = barcodeData.substring(19, 21).toInt()
    // parse each subfile
    for (i in 0 until subfiles) {
        val offset = 21 + i * 10 // header is 21 bytes + 10 bytes per subfile designator
        // subfile designator is 2 bytes for the type, 4 bytes for the start, 4 bytes for the length
        val subfileCode = barcodeData.substring(offset, offset + 2)
        val startPos = barcodeData.substring(offset + 2, offset + 6).toInt()
        val length = barcodeData.substring(offset + 6, offset + 10).toInt()
        val subfilePrefix = barcodeData.substring(startPos, startPos + 2)
        assert(subfileCode == subfilePrefix)
        barcodeData.substring(startPos + 2, startPos + length).split("\n").forEach { line ->
            val code = line.subSequence(0, 3)
            val text = line.drop(3)
            when (code) {
                "DCS" -> familyName = text
                "DAC" -> firstName = text
                "DAD" -> middleNames = text
                "DBD" -> issueDate = text
                "DBA" -> expirationDate = text
                "DBB" -> birthDate = text
                "DBC" -> gender = text
                "DAY" -> eyeColor = text
                "DAU" -> height = text
                "DAG" -> addressStreet = text
                "DAI" -> addressCity = text
                "DAJ" -> addressState = text
                "DAK" -> addressPostalCode = text
                "DAQ" -> licenseNumber = text
                "DCG" -> country = text
            }
        }
    }

    return DriverLicense(
        familyName = familyName,
        firstName = firstName,
        middleNames = middleNames,
        issueDate = issueDate,
        expirationDate = expirationDate,
        birthDate = birthDate,
        gender = gender,
        eyeColor = eyeColor,
        height = height,
        addressStreet = addressStreet,
        addressCity = addressCity,
        addressState = addressState,
        addressPostalCode = addressPostalCode,
        licenseNumber = licenseNumber,
        country = country,
    )
}