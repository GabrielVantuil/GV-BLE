package com.example.gvble

import java.util.UUID

const val UUID_BASE = "4756-616e-7475-696c5f424c45"//"4756-616e-7475-696c5f424c45" = {0x47, 0x56, 0x61, 0x6e, 0x74, 0x75, 0x69, 0x6c, 0x5f, 0x42, 0x4c, 0x45}= "GVantuil_BLE"
const val FLASHLIGHT_UUID_BASE = "0000"
const val POV_DISPLAY_UUID_BASE = "0001"

val TORCH_SERVICE_UUID: UUID =                UUID.fromString(FLASHLIGHT_UUID_BASE + "0000-" + UUID_BASE)
val TORCH_LOCK_CHAR_UUID: UUID =              UUID.fromString(FLASHLIGHT_UUID_BASE + "0001-" + UUID_BASE)
val TORCH_LED_POWER_CHAR_UUID: UUID =         UUID.fromString(FLASHLIGHT_UUID_BASE + "0002-" + UUID_BASE)
val TORCH_LED_PWM_CHAR_UUID: UUID =           UUID.fromString(FLASHLIGHT_UUID_BASE + "0003-" + UUID_BASE)
val TORCH_READ_LDR_CHAR_UUID: UUID =          UUID.fromString(FLASHLIGHT_UUID_BASE + "0006-" + UUID_BASE)

val POV_DISPLAY_SERVICE_UUID: UUID =                UUID.fromString(POV_DISPLAY_UUID_BASE + "0000-" + UUID_BASE)
val POV_DISPLAY_MODE_CHAR_UUID: UUID =              UUID.fromString(POV_DISPLAY_UUID_BASE + "0001-" + UUID_BASE)
val POV_DISPLAY_GET_LED_AMOUNT_CHAR_UUID: UUID =    UUID.fromString(POV_DISPLAY_UUID_BASE + "0002-" + UUID_BASE)
val POV_DISPLAY_SET_TEXT_CHAR_UUID: UUID =          UUID.fromString(POV_DISPLAY_UUID_BASE + "0003-" + UUID_BASE)
val POV_DISPLAY_SET_LEDS_CHAR_UUID: UUID =          UUID.fromString(POV_DISPLAY_UUID_BASE + "0004-" + UUID_BASE)
