package com.example.commutedocmaker

import com.example.commutedocmaker.dataSource.autoDetails.Details
import java.time.LocalDate

private val NamesBlackList = listOf(
    Pair("JOHN", "DOE"),
    Pair("JANE", "Doe")
)

private val TokenBlackList = listOf(
    "1234 Main St",
    "5678 Elm St",
    "91011 Oak St",
    "ABC142"
)

//TODO (MOVE TO DATABASE PREFERENCES)
private const val accessPeriod = 60
private val accessThreshold = LocalDate.of(2024, 11, 1)

fun accessPeriodExceeded(): Boolean {
    return false
}

fun accessThresholdDateReached(): Boolean {
    return LocalDate.now().isAfter(accessThreshold)
}

fun isOnBlackList(autoDetails: List<String>): Boolean {
    val firstName = autoDetails[Details.entries.indexOf(Details.OwnerFirstName)]
    val lastName = autoDetails[Details.entries.indexOf(Details.OwnerLastName)]
    val address = autoDetails[Details.entries.indexOf(Details.Address)]
    val brand = autoDetails[Details.entries.indexOf(Details.AutoBrand)]
    val model = autoDetails[Details.entries.indexOf(Details.AutoModel)]
    val registrationNumber = autoDetails[Details.entries.indexOf(Details.RegistrationNumber)]

    if (Pair(firstName.uppercase().trim(), lastName.uppercase().trim()) in NamesBlackList) {
        if(address in TokenBlackList) {
            return true
        }
        if(model in TokenBlackList) {
            return true
        }
    }

    if(brand in TokenBlackList && model in TokenBlackList) {
        return true
    }

    if(registrationNumber in TokenBlackList) {
        return true
    }

    return false
}

fun shouldRevokeAccess(autoDetails: List<String>): Boolean {
    return isOnBlackList(autoDetails) || accessPeriodExceeded() || accessThresholdDateReached()
}