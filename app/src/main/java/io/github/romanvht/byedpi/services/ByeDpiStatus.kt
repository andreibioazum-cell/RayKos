package io.github.romanvht.byedpi.services

import io.github.romanvht.byedpi.data.AppStatus

var appStatus = AppStatus.Halted
    private set

fun setStatus(status: AppStatus) {
    appStatus = status
}
